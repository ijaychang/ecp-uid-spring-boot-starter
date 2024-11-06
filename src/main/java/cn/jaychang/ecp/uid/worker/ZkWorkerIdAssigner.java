package cn.jaychang.ecp.uid.worker;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
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
 *     2.1.1    jaychang    2024年10月22日              使用Curator,心跳同时上报应用时间戳给对应的持久顺序节点，保证应用重启后还能取到持久顺序节点上报上来最新的时间戳
 *     ----------------------------------------------
 * </pre>
 */
@Slf4j
public class ZkWorkerIdAssigner extends AbstractIntervalWorkId {
    public static final String ZK_SPLIT = "/";

    /**
     * ZK上uid根目录
     */
    public static final String UID_ROOT = "ecp-uid";
    
    /**
     * 持久顺序节点根目录(用于保存节点的顺序编号)
     */
    public static final String UID_FOREVER = "forever";

    /**
     * session失效时间
     */
    public static final int SESSION_TIMEOUT = Integer.getInteger("curator-default-session-timeout", 10 * 1000);;
    
    /**
     * connection连接时间
     */
    public static final int CONNECTION_TIMEOUT = Integer.getInteger("curator-default-connection-timeout", 6 * 1000);
    
    /**
     * zk客户端
     */
    private CuratorFramework zkClient;
    
    /**
     * zk注册地址
     */
    private String zkAddress = "localhost:2181";
    
    /**
     * 持久节点名
     */
    private String foreverNode;


    @Override
    public long action() {
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 16);

            zkClient = CuratorFrameworkFactory.builder().connectString(this.zkAddress)
                    .sessionTimeoutMs(SESSION_TIMEOUT).connectionTimeoutMs(CONNECTION_TIMEOUT).retryPolicy(retryPolicy)
                    .namespace(UID_ROOT).build();
            zkClient.start();

            // zk 是否存在 /ecp-uid/forever 目录，没有则创建
            String uidForeverPath = ZK_SPLIT + UID_FOREVER;
            if (null == zkClient.checkExists().forPath(uidForeverPath)) {
                zkClient.create().withMode(CreateMode.PERSISTENT).forPath(uidForeverPath);
            }
            // 格式举例： /forever/192.168.56.1_53001_
            String workNode = uidForeverPath + ZK_SPLIT + ipPort + UNDERLINE;
            
            /**
             * 检查zk上是否存在ip_port的节点
             */
            List<String> seqNodePaths = zkClient.getChildren().forPath(uidForeverPath);
            if (!CollectionUtils.isEmpty(seqNodePaths)) {
                for (String nodePath : seqNodePaths) {
                    if (nodePath.startsWith(ipPort)) {
                        // nodePath 是不完整的路径，所以需要拼接上 uidForeverPath
                        foreverNode = uidForeverPath + ZK_SPLIT + nodePath;
                        workerId = Long.valueOf(nodePath.substring(nodePath.lastIndexOf(UNDERLINE) + 1));
                        break;
                    }
                }
            }
            // zk上不存在对应ip_port的持久顺序节点，则创建ip_port持久顺序节点
            if (null == workerId) {
                // 这里创建创建完成返回的nodePath是完整路径
                String createdNodePath = zkClient.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(workNode, longToBytes(System.currentTimeMillis()));
                foreverNode = createdNodePath;
                workerId = Long.valueOf(createdNodePath.substring(createdNodePath.lastIndexOf(UNDERLINE) + 1));
            }

            active.set(true);

            // 从持久节点上获取上报的最新时间戳
            lastTimestamp = bytesToLong(zkClient.getData().forPath(foreverNode));
            return lastTimestamp;
        } catch (KeeperException | InterruptedException | IOException e) {
            log.error("zk初始化节点、或上报时间戳至zk节点失败", e);
        } catch (Exception e) {
            log.error("zk初始化节点、上报时间戳至zk节点失败", e);
        } finally {
            // 注册钩子函数，当应用shutdown时，关闭zkClient
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (Objects.nonNull(zkClient)) {
                    zkClient.close();
                    log.debug("已关闭zkClient");
                }
            }));
        }
        // 这里不能返回当前应用的时间戳，否则有可能会出现时间回拨，而是应该抛出异常
        throw new RuntimeException("获取持久顺序节点的时间戳失败");
    }
    
    @Override
    public boolean where() {
        return null != workerId && null != zkClient;
    }
    
    @Override
    public void report() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            log.warn("由于当前时间戳[{}]小于上次时间戳[{}]workerNode[{}]忽略上报至[{}]节点", currentTimestamp, lastTimestamp, foreverNode);
            return;
        }
        try {
            byte[] longToBytes = longToBytes(currentTimestamp);
            zkClient.setData().forPath(foreverNode, longToBytes);
            lastTimestamp = currentTimestamp;
            log.debug("workerNode[{}]完成定时上报时间戳[{}]至节点[{}]", workerId, currentTimestamp, foreverNode);
        } catch (KeeperException | InterruptedException e) {
            log.error(String.format("workerNode[%s]定时上报时间戳[%s]至[%s]节点失败", workerId, currentTimestamp, foreverNode), e);
        } catch (Exception e) {
            log.error(String.format("workerNode[%s]定时上报时间戳[%s]至[%s]节点失败", workerId, currentTimestamp, foreverNode), e);
        }
    }
    
    public String getZkAddress() {
        return zkAddress;
    }
    
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }


}
