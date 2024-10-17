package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.WorkerIdAssignerProperties;
import cn.jaychang.ecp.uid.worker.*;
import cn.jaychang.ecp.uid.worker.dao.WorkerNodeDao;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * worker id 配置
 * @author jaychang
 */
@Configuration
public class WorkerIdConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("#{'${ecp.uid.strategy.baidu-uid.worker-id-assigner}' != null or '${ecp.uid.strategy.twitter-snowflake.worker-id-assigner}' != null}")
    public WorkerIdAssigner createWorkerIdAssigner(WorkerIdAssignerProperties workerIdAssignerProperties) {
        // workId 分配方式
        WorkerIdAssigner workerIdAssigner = null;
        if (WorkerIdAssignerEnum.ZK.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
            zkWorkerIdAssigner.setZkAddress(workerIdAssignerProperties.getZookeeperConnection());
            workerIdAssigner = zkWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.DB.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner(workNodeDao());
            workerIdAssigner = disposableWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.REDIS.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            RedisWorkIdAssigner redisWorkIdAssigner = redisWorkIdAssigner();
            workerIdAssigner = redisWorkIdAssigner;
        } else if (WorkerIdAssignerEnum.SIMPLE.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            SimpleWorkerIdAssigner simpleWorkerIdAssigner = new SimpleWorkerIdAssigner();
            workerIdAssigner = simpleWorkerIdAssigner;
        } else {
            throw new IllegalArgumentException(String.format("WorkerIdAssigner:[%s] is illegal", workerIdAssignerProperties.getWorkerIdAssigner()));
        }
        return workerIdAssigner;
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('db') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('db')}")
    public WorkerNodeDao workNodeDao() {
        return new WorkerNodeDao();
    }


    /**
     * 自定义 RedisTemplate
     * <p>
     * 修改 Redis 序列化方式，默认 JdkSerializationRedisSerializer
     *
     * @param redisConnectionFactory {@link RedisConnectionFactory}
     * @return {@link RedisTemplate}
     */
    @Bean(name = "redisTemplateForWorkIdAssigner")
    @ConditionalOnMissingBean(name = "redisTemplateForWorkIdAssigner")
    @ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('redis') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('redis')}")
    public RedisTemplate<String, Object> redisWorkIdAssigner(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());

        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('redis') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('redis')}")
    public RedisWorkIdAssigner redisWorkIdAssigner() {
        return new RedisWorkIdAssigner();
    }
}
