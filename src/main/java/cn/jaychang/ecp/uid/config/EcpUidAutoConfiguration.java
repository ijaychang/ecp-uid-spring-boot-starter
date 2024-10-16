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
import cn.jaychang.ecp.uid.worker.dao.WorkerNodeDAO;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

@Slf4j
@Configuration
@EnableConfigurationProperties({EcpUidProperties.class})
@AllArgsConstructor
public class EcpUidAutoConfiguration {
    @Autowired
    private EcpUidProperties ecpUidProperties;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

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
    public SpringStepStrategy springStepStrategy() {
        SpringStepStrategy springStepStrategy = new SpringStepStrategy();
        springStepStrategy.setJdbcTemplate(jdbcTemplate);
        springStepStrategy.setAsynLoadingSegment(ecpUidProperties.getSpringStep().getAsynLoadingSegment());
        return springStepStrategy;
    }

    @Bean
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
    public MeituanLeafSegmentStrategy meituanLeafSegmentStrategy() {
        MeituanLeafSegmentStrategy meituanLeafSegmentStrategy = new MeituanLeafSegmentStrategy();
        meituanLeafSegmentStrategy.setAsynLoadingSegment(ecpUidProperties.getMeituanLeaf().getAsynLoadingSegment());
        return meituanLeafSegmentStrategy;
    }

    @Bean
    public BaiduUidStrategy baiduUidStrategy() {
        BaiduUidStrategy baiduUidStrategy = new BaiduUidStrategy();
        BaiduUidProperties baiduUidProperties = ecpUidProperties.getBaiduUid();

        UidGenerator uidGenerator = null;
        if (UidGeneratorTypeEnum.CACHE.equals(baiduUidProperties.getType())) {
            uidGenerator = new CachedUidGenerator();
        } else if (UidGeneratorTypeEnum.DEFAULT.equals(baiduUidProperties.getType())) {
            uidGenerator = new DefaultUidGenerator();
        } else {
            throw new IllegalArgumentException(String.format("UidGeneratorType:[%s] is illegal", baiduUidProperties.getType()));
        }
        DefaultUidGenerator defaultUidGenerator = (DefaultUidGenerator) uidGenerator;
        defaultUidGenerator.setEpochStr(baiduUidProperties.getEpochStr());
        defaultUidGenerator.setTimeBits(baiduUidProperties.getTimeBits());
        defaultUidGenerator.setSeqBits(baiduUidProperties.getSeqBits());

        WorkerIdAssigner workerIdAssigner = createWorkerIdAssigner(baiduUidProperties);
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);

        try {
            defaultUidGenerator.afterPropertiesSet();
        } catch (Exception e) {
            log.error("初始化UidGenerator失败", e);
            throw new RuntimeException(e);
        }
        baiduUidStrategy.setUidGenerator(uidGenerator);
        return baiduUidStrategy;
    }

    @Bean
    public WorkerIdAssigner createWorkerIdAssigner(WorkerIdAssignerProperties workerIdAssignerProperties) {
        // workId 分配方式
        WorkerIdAssigner workerIdAssigner = null;
        if (WorkerIdAssignerEnum.ZK.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
            zkWorkerIdAssigner.setZkAddress(workerIdAssignerProperties.getZookeeperConnection());
            workerIdAssigner = zkWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.DB.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            WorkerNodeDAO workerNodeDAO = new WorkerNodeDAO(jdbcTemplate);
            DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner(workerNodeDAO);
            workerIdAssigner = disposableWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.REDIS.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            RedisWorkIdAssigner redisWorkIdAssigner = new RedisWorkIdAssigner(redisTemplate);
            workerIdAssigner = redisWorkIdAssigner;
        } else if (WorkerIdAssignerEnum.SIMPLE.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            SimpleWorkerIdAssigner simpleWorkerIdAssigner = new SimpleWorkerIdAssigner();
            workerIdAssigner = simpleWorkerIdAssigner;
        } else {
            throw new IllegalArgumentException(String.format("WorkerIdAssigner:[%s] is illegal", workerIdAssignerProperties.getWorkerIdAssigner()));
        }
        return workerIdAssigner;
    }

}
