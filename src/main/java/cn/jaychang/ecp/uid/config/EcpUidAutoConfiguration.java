package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.UidContext;
import cn.jaychang.ecp.uid.baidu.UidGenerator;
import cn.jaychang.ecp.uid.baidu.enums.UidGeneratorTypeEnum;
import cn.jaychang.ecp.uid.baidu.impl.CachedUidGenerator;
import cn.jaychang.ecp.uid.baidu.impl.DefaultUidGenerator;
import cn.jaychang.ecp.uid.config.properties.BaiduUidProperties;
import cn.jaychang.ecp.uid.config.properties.EcpUidProperties;
import cn.jaychang.ecp.uid.config.properties.TwitterSnowflakeProperties;
import cn.jaychang.ecp.uid.config.properties.WorkerIdAssignerProperties;
import cn.jaychang.ecp.uid.extend.annotation.UidModel;
import cn.jaychang.ecp.uid.extend.strategy.*;
import cn.jaychang.ecp.uid.worker.*;
import cn.jaychang.ecp.uid.worker.dao.WorkerNodeDao;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Objects;

@Slf4j
@Configuration
@EnableConfigurationProperties({EcpUidProperties.class})
@AllArgsConstructor
public class EcpUidAutoConfiguration {
    private final EcpUidProperties ecpUidProperties;

    @Bean
    public UidContext uidContext() {
        IUidStrategy uidStrategy;
        if (UidModel.STEP.getName().equals(ecpUidProperties.getStrategy())) {
            uidStrategy = springStepStrategy();
        } else if (UidModel.SNOWFLAKE.getName().equals(ecpUidProperties.getStrategy())) {
            uidStrategy = twitterSnowflakeStrategy();
        } else if (UidModel.SEGMENT.getName().equals(ecpUidProperties.getStrategy())) {
            uidStrategy = meituanLeafSegmentStrategy();
        } else if (UidModel.BAIDU.getName().equals(ecpUidProperties.getStrategy())) {
            uidStrategy = baiduUidStrategy();
        } else {
            throw new IllegalArgumentException(String.format("Strategy:[%s] is illegal.", ecpUidProperties.getStrategy()));
        }
        UidContext uidContext = new UidContext(uidStrategy);
        return uidContext;
    }

    @Bean
    @ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('step')}")
    public SpringStepStrategy springStepStrategy() {
        SpringStepStrategy springStepStrategy = new SpringStepStrategy();
        springStepStrategy.setAsynLoadingSegment(ecpUidProperties.getSpringStep().getAsynLoadingSegment());
        return springStepStrategy;
    }

    @Bean
    @ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('snowflake')}")
    public TwitterSnowflakeStrategy twitterSnowflakeStrategy() {
        TwitterSnowflakeStrategy twitterSnowflakeStrategy = new TwitterSnowflakeStrategy();
        TwitterSnowflakeProperties twitterSnowflakeProperties = ecpUidProperties.getTwitterSnowflake();
        twitterSnowflakeStrategy.setDatacenterId(twitterSnowflakeProperties.getDatacenterId());
        twitterSnowflakeStrategy.setWorkerId(twitterSnowflakeProperties.getWorkId());
        if (Objects.nonNull(twitterSnowflakeProperties.getWorkerIdAssigner())) {
            twitterSnowflakeStrategy.setAssigner(createWorkerIdAssigner(twitterSnowflakeProperties));
        }
        return twitterSnowflakeStrategy;
    }

    @Bean
    @ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('segment')}")
    public MeituanLeafSegmentStrategy meituanLeafSegmentStrategy() {
        MeituanLeafSegmentStrategy meituanLeafSegmentStrategy = new MeituanLeafSegmentStrategy();
        meituanLeafSegmentStrategy.setAsynLoadingSegment(ecpUidProperties.getMeituanLeaf().getAsynLoadingSegment());
        return meituanLeafSegmentStrategy;
    }

    @Bean
    @ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('baidu')}")
    public BaiduUidStrategy baiduUidStrategy() {
        BaiduUidStrategy baiduUidStrategy = new BaiduUidStrategy();
        BaiduUidProperties baiduUidProperties = ecpUidProperties.getBaiduUid();
        baiduUidStrategy.setUidGenerator(uidGenerator(baiduUidProperties));
        return baiduUidStrategy;
    }

    @Bean
    @ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('baidu')}")
    public UidGenerator uidGenerator(BaiduUidProperties baiduUidProperties) {
        DefaultUidGenerator defaultUidGenerator = null;
        if (UidGeneratorTypeEnum.CACHE.equals(baiduUidProperties.getType())) {
            defaultUidGenerator = new CachedUidGenerator();
        } else if (UidGeneratorTypeEnum.DEFAULT.equals(baiduUidProperties.getType())) {
            defaultUidGenerator = new DefaultUidGenerator();
        } else {
            throw new IllegalArgumentException(String.format("UidGeneratorType:[%s] is illegal", baiduUidProperties.getType()));
        }
        defaultUidGenerator.setEpochStr(baiduUidProperties.getEpochStr());
        defaultUidGenerator.setTimeBits(baiduUidProperties.getTimeBits());
        defaultUidGenerator.setWorkerBits(baiduUidProperties.getWorkerBits());
        defaultUidGenerator.setSeqBits(baiduUidProperties.getSeqBits());

        WorkerIdAssigner workerIdAssigner = createWorkerIdAssigner(baiduUidProperties);
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        return defaultUidGenerator;
    }

    @Bean
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
    @ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('redis') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('redis')}")
    public RedisWorkIdAssigner redisWorkIdAssigner() {
        return new RedisWorkIdAssigner();
    }

}
