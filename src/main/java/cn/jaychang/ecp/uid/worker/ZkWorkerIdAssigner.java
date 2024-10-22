package cn.jaychang.ecp.uid.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * @类名称 ZkWorkerIdAssigner.java
 * @类描述 <pre>zk编号分配器{可设置interval-心跳间隔、pidHome-workerId文件存储目录、zkAddress-zk地址、pidPort-使用端口(默认不开,同机多uid应用时区分端口)}</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年4月27日 下午8:14:21
 * @版本 2.1.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 	庄梦蝶殇 	2018年04月27日             利用zk的版本实现workid
 *     2.0.0    庄梦蝶殇      2018年09月04日             引入leaf的理念基于zkclient进行开发
 *     2.1.0    庄梦蝶殇      2019年03月27日             使用原生Zookeeper。去除zkclient依赖
 *     2.1.1    jaychang    2024年10月22日              使用Curator
 *     ----------------------------------------------
 * </pre>
 */
@Slf4j
public class ZkWorkerIdAssigner extends AbstractIntervalWorkId {
    public static final String ZK_SPLIT = "/";

    public static final String UNDERLINE = "_";

    /**
     * ZK上uid根目录
     */
    public static final String UID_ROOT = "ecp-uid";
    
    /**
     * 持久顺序节点根目录(用于保存节点的顺序编号)
     */
    public static final String UID_FOREVER = "forever";
    
    /**
     * 临时节点根目录(用于保存活跃节点及活跃心跳)
     */
    public static final String UID_TEMPORARY = "temporary";

    /**
     * session失效时间
     */
    public static final int SESSION_TIMEOUT = Integer.getInteger("curator-default-session-timeout", 60 * 1000);;
    
    /**
     * connection连接时间
     */
    public static final int CONNECTION_TIMEOUT = Integer.getInteger("curator-default-connection-timeout", 15 * 1000);
    
    /**
     * zk客户端
     */
    private CuratorFramework zkClient;
    
    /**
     * zk注册地址
     */
    private String zkAddress = "localhost:2181";
    
    /**
     * 临时节点名，用于上报时间使用
     */
    private String temporaryNode;
    
    @Override
    public long action() {
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

            zkClient = CuratorFrameworkFactory.builder().connectString(this.zkAddress)
                    .sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
                    .namespace(UID_ROOT).build();
            zkClient.start();

            // 是否存在 /ecp-uid/forever 目录，没有则创建
            String uidForeverPath = ZK_SPLIT + UID_FOREVER;
            if (null == zkClient.checkExists().forPath(uidForeverPath)) {
                zkClient.create().withMode(CreateMode.PERSISTENT).forPath(uidForeverPath);
            }
            // 格式举例： /forever/192.168.56.1_53001_
            String workNode = uidForeverPath + ZK_SPLIT + pidName + UNDERLINE;
            
            /**
             * 2、文件不存在，检查zk上是否存在ip:port的节点
             */
            if (null == workerId || null != zkClient.checkExists().forPath(workNode)) {
                List<String> seqNodePaths = zkClient.getChildren().forPath(uidForeverPath);
                for (String nodePath : seqNodePaths) {
                    if (nodePath.startsWith(pidName)) {
                        workerId = Long.valueOf(nodePath.substring(nodePath.length() - 10));
                        break;
                    }
                }
                // b、 不存在，创建ip:port节点
                if (null == workerId) {
                    String nodePath = zkClient.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(workNode);
                    workerId = Long.valueOf(nodePath.substring(nodePath.length() - 10));
                }
            }
            
            /**
             * 3、创建临时节点
             */
            String temporaryPath = ZK_SPLIT + UID_TEMPORARY;
            String uidTemporaryPath = temporaryPath + ZK_SPLIT + workerId + UNDERLINE;
            temporaryNode = zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(uidTemporaryPath, longToBytes(System.currentTimeMillis()));
            if (StringUtils.hasText(temporaryNode)) {
                log.debug("用于给workerId[{}]定时上报时间戳的临时节点[{}]创建成功", workerId, temporaryNode);
            }
            active.set(true);
            
            /**
             * 4、获取本地时间，跟zk临时节点列表的时间平均值做比较(zk临时节点用于存储活跃节点的上报时间，每隔一段时间上报一次临时节点时间)
             */
            List<String> activeNodes = zkClient.getChildren().forPath(temporaryPath);
            Long sumTime = 0L;
            if (CollectionUtils.isEmpty(activeNodes)) {
                return sumTime;
            }
            for (String itemNode : activeNodes) {
                Long nodeTime = bytesToLong(zkClient.getData().forPath(temporaryPath + ZK_SPLIT + itemNode));
                sumTime += nodeTime;
            }
            return sumTime / activeNodes.size();
        } catch (KeeperException | InterruptedException | IOException e) {
            log.error("zk初始化节点或上报时间戳失败", e);
        } catch (Exception e) {
            log.error("zk初始化节点或上报时间戳失败", e);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (Objects.nonNull(zkClient)) {
                    zkClient.close();
                    log.debug("已关闭zkClient");
                }
            }));
        }
        return System.currentTimeMillis();
    }
    
    @Override
    public boolean where() {
        return null != workerId && null != zkClient;
    }
    
    @Override
    public void report() {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            zkClient.setData().forPath(temporaryNode, longToBytes(currentTimeMillis)).setVersion(-1);
            log.debug("workerId[{}]完成定时上报时间戳[{}]至[{}]节点", workerId, currentTimeMillis, temporaryNode);
        } catch (KeeperException | InterruptedException e) {
            log.error(String.format("workerId[{}]定时上报时间戳[{}]至[{}]节点失败", workerId, currentTimeMillis, temporaryNode), e);
        } catch (Exception e) {
            log.error(String.format("workerId[{}]定时上报时间戳[{}]至[{}]节点失败", workerId, currentTimeMillis, temporaryNode), e);
        }
    }
    
    public String getZkAddress() {
        return zkAddress;
    }
    
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    /** 分配8个字节(刚好可以存储Long类型的时间戳)的缓冲区 */
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
    
    public static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();// need flip
        return buffer.getLong();
    }
}
