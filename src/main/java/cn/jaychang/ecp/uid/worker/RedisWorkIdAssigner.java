package cn.jaychang.ecp.uid.worker;

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

    /**
     * 上报时间戳
     */
    public static final String UID_REPORT_PREFIX = UID_ROOT.concat("report:");

    @Autowired
    @Qualifier("redisTemplateForWorkIdAssigner")
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public long action() {
        /**
         * 检查redis上是否存在ip_port的机器节点
         */
        // 按 score 从小到大顺序排列(以保证workerId是不变的)
        Set<Object> uidWorkSet = Optional.ofNullable(redisTemplate.opsForZSet().range(UID_FOREVER, 0, -1)).orElse(Collections.emptySet());
        // 检查redis上是否存在ip_port的节点,存在，获取节点的顺序编号
        Long i = 0L;
        for (Object item : uidWorkSet) {
            if (Objects.nonNull(item) && item.toString().equals(ipPort)) {
                workerId = i;
                break;
            }
            i++;
        }
        // workerId 不存在，创建ip_port节点
        if (null == workerId) {
            workerId = (long)uidWorkSet.size();
            long currentTimeMillis = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(UID_FOREVER, ipPort, currentTimeMillis);
            redisTemplate.opsForValue().set(UID_REPORT_PREFIX.concat(ipPort), String.valueOf(currentTimeMillis));
        }

        active.set(true);
        
        /**
         * 3、获取本地时间，跟uid
         */
        Object lastTimestampStr = redisTemplate.opsForValue().get(UID_REPORT_PREFIX.concat(ipPort));
        if (Objects.isNull(lastTimestampStr)) {
            throw new RuntimeException("未获取到最新上报的时间戳");
        }
        lastTimestamp = Long.valueOf(lastTimestampStr.toString());
        return lastTimestamp;
    }
    
    @Override
    public boolean where() {
        return null != workerId;
    }
    
    @Override
    public void report() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            log.warn("由于当前时间戳[{}]小于上一次上报的时间戳[{}]workerNode[{}]忽略上报至[{}]节点", currentTimestamp, lastTimestamp, UID_FOREVER+ ":" + ipPort);
            return;
        }
        String key = UID_REPORT_PREFIX.concat(ipPort);
        redisTemplate.opsForValue().set(key, String.valueOf(currentTimestamp));
        lastTimestamp = currentTimestamp;
        log.debug("workerNode[{}]上报时间戳[{}]至[{}]", workerId, currentTimestamp, key);
    }
}
