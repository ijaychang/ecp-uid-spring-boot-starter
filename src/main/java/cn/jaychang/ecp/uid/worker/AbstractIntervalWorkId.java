package cn.jaychang.ecp.uid.worker;

import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.jaychang.ecp.uid.baidu.utils.NamingThreadFactory;
import cn.jaychang.ecp.uid.config.properties.InetUtilsProperties;
import cn.jaychang.ecp.uid.util.InetUtils;
import cn.jaychang.ecp.uid.util.ServerSocketHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import cn.jaychang.ecp.uid.util.WorkerIdUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @类名称 AbstractIntervalWorkId.java
 * @类描述 <pre>WorkId生成基类</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年1月17日 下午4:21:17
 * @版本 1.0.0
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 		庄梦蝶殇 	2019年1月17日             
 *     ----------------------------------------------
 * </pre>
 */
@Slf4j
public abstract class AbstractIntervalWorkId implements WorkerIdAssigner, InitializingBean, DisposableBean, ApplicationContextAware {

    /**
     * 线程名-心跳
     */
    public static final String THREAD_HEARTBEAT_NAME = "ecp_uid_heartbeat";


    /**
     * 下划线常量
     */
    public static final String UNDERLINE = "_";
    
    /**
     * 心跳是否活跃原子标识
     */
    protected AtomicBoolean active = new AtomicBoolean(false);
    
    /**
     * 心跳间隔
     */
    protected Long interval = 3000L;

    /**
     * 工作节点ID (可理解为一个运行的Java应用，需要给这个应用分配一个ID)
     */
    protected Long workerId;
    
    /**
     * 占用的端口号(同机多uid应用时区分端口)
     */
    private Integer port;

    /**
     * ip地址_端口号
     */
    protected String ipPort;

    /**
     * 需要占用的端口
     */
    protected ServerSocket socket;

    protected ApplicationContext applicationContext;


    /**
     * 上次工作节点上报的时间戳，初始为0
     */
    protected long lastTimestamp;
    
    @Override
    public void afterPropertiesSet()
        throws Exception {
        try {
            /**
             * 检查workId文件是否存在。文件名为ip_port_顺序编号
             */
            ServerSocketHolder socketHolder = new ServerSocketHolder();
            // pidName 格式： ip地址_端口号
            InetUtilsProperties inetUtilsProperties = applicationContext.getBean(InetUtilsProperties.class);
            // 如果是 springboot 应用，且未指定设置端口号则使用 springboot 应用的端口
            String serverPort = applicationContext.getEnvironment().getProperty("server.port");
            if (StringUtils.hasText(serverPort) && Objects.isNull(port)) {
                port = Integer.valueOf(serverPort);
                InetUtils inetUtils = new InetUtils(inetUtilsProperties);
                String ipAddr = inetUtils.findFirstNonLoopbackAddress().getHostAddress();
                ipPort = ipAddr + UNDERLINE + port;
            } else {
                if (Objects.nonNull(inetUtilsProperties)
                        && (!CollectionUtils.isEmpty(inetUtilsProperties.getIgnoredInterfaces())
                        || !CollectionUtils.isEmpty(inetUtilsProperties.getPreferredNetworks()))) {
                    ipPort = WorkerIdUtils.getIpPort(port, socketHolder, inetUtilsProperties);
                } else {
                    ipPort = WorkerIdUtils.getIpPort(port, socketHolder);
                }
                socket = socketHolder.getServerSocket();
                port = socket.getLocalPort();
            }

            /**
             * 获取本地时间，跟uid 机器节点上报的时间戳比较做差值
             */
            long diff = System.currentTimeMillis() - action();
            if (diff < 0) {
                // 当前时间小于活跃节点的平均心跳时间，证明出现时间回拨，进入等待。
                WorkerIdUtils.sleepMs(interval * 2, diff);
            }
            if (null != workerId) {
                startHeartBeatThread();
            }
        } catch (Exception e) {
            active.set(false);
            if (null != socket) {
                socket.close();
            }
            throw e;
        }
    }

    @Override
    public void destroy() throws Exception {
        // bean销毁时，关闭占用的端口
         if (null != socket) {
             if (!socket.isClosed()) {
                 socket.close();
                 log.debug("已关闭socket端口[{}]", port);
             }
         }
    }

    /**
     * @方法名称 action
     * @功能描述 <pre>workId文件不存在时的操作</pre>
     * @return 机器节点上报的时间戳
     */
    public abstract long action();
    
    /**
     * @方法名称 startHeartBeatThread
     * @功能描述 <pre>心跳线程，用于每隔一段时间上报一次临时节点时间</pre>
     */
    protected void startHeartBeatThread() {
        ScheduledExecutorService scheduledpool = new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(THREAD_HEARTBEAT_NAME, true));
        scheduledpool.scheduleAtFixedRate(() -> {
            if (active.get() == false) {
                scheduledpool.shutdownNow();
            } else if (where()) {
                report();
            }
        }, 0L, interval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * @方法名称 where
     * @功能描述 <pre>心跳条件</pre>
     * @return true:执行心跳上报，false:空动作
     */
    public abstract boolean where();
    
    /**
     * @方法名称 report
     * @功能描述 <pre>心跳上报</pre>
     */
    public abstract void report();
    
    @Override
    public long assignWorkerId() {
        return workerId;
    }
    
    public Long getInterval() {
        return interval;
    }
    
    public void setInterval(Long interval) {
        this.interval = interval;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** 分配8个字节(刚好可以存储Long类型的时间戳)的缓冲区 */
    protected static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

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
