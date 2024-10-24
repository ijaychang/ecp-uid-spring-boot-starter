package cn.jaychang.ecp.uid.worker;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

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
     * uid 活跃节点心跳列表(用于保存活跃节点及活跃心跳)
     */
    public static final String UID_TEMPORARY = UID_ROOT.concat("temporary:");

    @Autowired
    @Qualifier("redisTemplateForWorkIdAssigner")
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public long action() {
        /**
         * 1、文件不存在，检查redis上是否存在ip:port的机器节点
         */
        // 按 score 从小到大顺序排列
        Set<Object> uidWorkSet = redisTemplate.opsForZSet().range(UID_FOREVER, 0, -1);
        if (null == workerId) {
            // a、 检查redis上是否存在ip:port的节点,存在，获取节点的顺序编号
            Long i = 0L;
            for (Object item : uidWorkSet) {
                if (item.toString().equals(pidName)) {
                    workerId = i;
                    break;
                }
                i++;
            }
            // b、 不存在，创建ip:port节点
            if (null == workerId) {
                workerId = (long)uidWorkSet.size();
                // 使用zset 时间排序，保证有序性
                redisTemplate.opsForZSet().add(UID_FOREVER, pidName, System.currentTimeMillis());
                uidWorkSet.add(pidName);
            }
        }
        /**
         * 2、创建临时机器节点的时间
         */
        redisTemplate.opsForValue().set(UID_TEMPORARY + pidName, String.valueOf(System.currentTimeMillis()), interval * 3000, TimeUnit.MILLISECONDS);
        active.set(true);
        
        /**
         * 3、获取本地时间，跟uid 活跃节点心跳列表的时间平均值做比较(uid 活跃节点心跳列表 用于存储活跃节点的上报时间，每隔一段时间上报一次临时节点时间)
         */
        Double lastTimeMillisDouble = redisTemplate.opsForZSet().score(UID_FOREVER, pidName);
        if (Objects.isNull(lastTimeMillisDouble)) {
            lastTimeMillisDouble = new Double(System.currentTimeMillis());
        }
        long lastTimeMillis = lastTimeMillisDouble.longValue();
        if (CollectionUtils.isEmpty(uidWorkSet)) {
            return lastTimeMillis;
        }
        long sumTime = 0;
        int itemCount = 0;
        for (Object itemName : uidWorkSet) {
            Object itemValue = redisTemplate.opsForValue().get(UID_TEMPORARY + itemName);
            if (Objects.nonNull(itemValue)) {
                itemCount ++;
                sumTime += Long.valueOf((String) itemValue);
            }
        }
        if (itemCount == 0) {
            return lastTimeMillis;
        }
        long averageTimeMillis = sumTime / itemCount;
        return averageTimeMillis > lastTimeMillis ? averageTimeMillis : lastTimeMillis;
    }
    
    @Override
    public boolean where() {
        return null != workerId;
    }
    
    @Override
    public void report() {
        long currentTimeMillis = System.currentTimeMillis();
        redisTemplate.opsForValue().set(UID_TEMPORARY + pidName, String.valueOf(currentTimeMillis), interval * 3, TimeUnit.MILLISECONDS);
        Double lastTimestamp = redisTemplate.opsForZSet().score(UID_FOREVER, pidName);
        Double timestamp = new BigDecimal(currentTimeMillis).subtract(new BigDecimal(lastTimestamp)).doubleValue();
        redisTemplate.opsForZSet().incrementScore(UID_FOREVER, pidName, timestamp);
        log.debug("workerId[{}]上报时间戳[{}]至[{}]和[{}]", workerId, currentTimeMillis, UID_TEMPORARY + pidName, UID_FOREVER+ ":" + pidName);
    }
}
