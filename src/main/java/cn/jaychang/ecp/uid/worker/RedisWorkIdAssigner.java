package cn.jaychang.ecp.uid.worker;

import java.math.BigDecimal;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @类名称 RedisWorkIdAssigner.java
 * @类描述 <pre>Redis编号分配器</pre>
 * @作者  庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2019年1月16日 下午3:32:07
 * @版本 1.0.1
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.0.0 		庄梦蝶殇 	2019年1月16日             
 *     1.0.1        庄梦蝶殇    2019年1月18日      完善代码
 *     ----------------------------------------------
 * </pre>
 */
@Slf4j
public class RedisWorkIdAssigner extends AbstractIntervalWorkId {
    /**
     * redis上uid 机器节点的key前缀
     */
    public static final String UID_ROOT = "ecp:uid:";
    
    /**
     * uid 机器节点列表
     */
    public static final String UID_FOREVER = UID_ROOT.concat("forever");
    

    @Autowired
    @Qualifier("redisTemplateForWorkIdAssigner")
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public long action() {
        /**
         * 文件不存在，检查redis上是否存在ip:port的机器节点
         */
        // 按 score 从小到大顺序排列
        Set<Object> uidWorkSet = redisTemplate.opsForZSet().range(UID_FOREVER, 0, -1);
        if (null == workerId) {
            // 检查redis上是否存在ip_port的节点,存在，获取节点的顺序编号
            Long i = 0L;
            for (Object item : uidWorkSet) {
                if (item.toString().equals(pidName)) {
                    workerId = i;
                    break;
                }
                i++;
            }
            // workerId 不存在，创建ip_port节点
            if (null == workerId) {
                workerId = (long)uidWorkSet.size();
                redisTemplate.opsForZSet().add(UID_FOREVER, pidName, System.currentTimeMillis());
                uidWorkSet.add(pidName);
            }
        } else {
            // 本地文件已有记录workerId的文件，校验下与zk上记录的workerId是否一致(如果非人为修改，肯定是一致的)，校验下的话，保险一点
            Long remoteWorkerId = null;
            Long i = 0L;
            for (Object item : uidWorkSet) {
                if (item.toString().equals(pidName)) {
                    remoteWorkerId = i;
                    break;
                }
                i++;
            }

            if (Objects.nonNull(remoteWorkerId)) {
                if (!workerId.equals(remoteWorkerId)) {
                    // 订正本地workerId文件
                    fixedWorkerIdFile(remoteWorkerId);
                }
            } else {
                // redis上没有记录,说明是人为修改的或增加的workerId文件(如果非人为修改，不会发生这样的情况)，这里保险处理
                if (null == remoteWorkerId) {
                    // redis上创建新节点，并删除本地错误的workerId文件
                    remoteWorkerId = (long)uidWorkSet.size();
                    redisTemplate.opsForZSet().add(UID_FOREVER, pidName, System.currentTimeMillis());
                    fixedWorkerIdFile(remoteWorkerId);
                }
            }
        }

        active.set(true);
        
        /**
         * 3、获取本地时间，跟uid
         */
        Double lastTimeMillisDouble = redisTemplate.opsForZSet().score(UID_FOREVER, pidName);
        if (Objects.isNull(lastTimeMillisDouble)) {
            throw new RuntimeException("未获取到最新上报的时间戳");
        }
        long lastTimeMillis = lastTimeMillisDouble.longValue();
        return lastTimeMillis;
    }
    
    @Override
    public boolean where() {
        return null != workerId;
    }
    
    @Override
    public void report() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            log.warn("由于当前时间戳[{}]小于上次时间戳[{}]workerNode[{}]忽略上报至[{}]节点", currentTimestamp, lastTimestamp, UID_FOREVER+ ":" + pidName);
            return;
        }
        Double lastUploadTimestamp = redisTemplate.opsForZSet().score(UID_FOREVER, pidName);
        Double timestamp = new BigDecimal(currentTimestamp).subtract(new BigDecimal(lastUploadTimestamp)).doubleValue();
        redisTemplate.opsForZSet().incrementScore(UID_FOREVER, pidName, timestamp);
        log.debug("workerNode[{}]上报时间戳[{}]至[{}]", workerId, currentTimestamp, UID_FOREVER+ ":" + pidName);
    }
}
