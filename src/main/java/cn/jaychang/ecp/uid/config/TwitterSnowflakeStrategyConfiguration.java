package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.TwitterSnowflakeProperties;
import cn.jaychang.ecp.uid.config.properties.WorkerIdAssignerProperties;
import cn.jaychang.ecp.uid.extend.strategy.TwitterSnowflakeStrategy;
import cn.jaychang.ecp.uid.worker.*;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Twitter雪花策略配置
 *
 * @author jaychang
 */
@Configuration
@EnableConfigurationProperties(TwitterSnowflakeProperties.class)
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('twitter-snowflake')}")
public class TwitterSnowflakeStrategyConfiguration extends WorkerIdConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TwitterSnowflakeStrategy twitterSnowflakeStrategy(TwitterSnowflakeProperties twitterSnowflakeProperties) {
        TwitterSnowflakeStrategy twitterSnowflakeStrategy = new TwitterSnowflakeStrategy();
        twitterSnowflakeStrategy.setDatacenterId(twitterSnowflakeProperties.getDatacenterId());
        twitterSnowflakeStrategy.setWorkerId(twitterSnowflakeProperties.getWorkId());
        if (Objects.nonNull(twitterSnowflakeProperties.getWorkerIdAssigner())) {
            twitterSnowflakeStrategy.setAssigner(createWorkerIdAssigner(twitterSnowflakeProperties));
        }
        return twitterSnowflakeStrategy;
    }

    public WorkerIdAssigner createWorkerIdAssigner(WorkerIdAssignerProperties workerIdAssignerProperties) {
        // workerId 分配方式
        WorkerIdAssigner workerIdAssigner = null;
        if (WorkerIdAssignerEnum.ZK.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
            zkWorkerIdAssigner.setZkAddress(workerIdAssignerProperties.getZkAddress());
            zkWorkerIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            zkWorkerIdAssigner.setPidHome(workerIdAssignerProperties.getPidHome());
            zkWorkerIdAssigner.setPidPort(workerIdAssignerProperties.getPidPort());
            workerIdAssigner = zkWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.DB.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner();
            workerIdAssigner = disposableWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.REDIS.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            RedisWorkIdAssigner redisWorkIdAssigner = new RedisWorkIdAssigner();
            redisWorkIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            redisWorkIdAssigner.setPidHome(workerIdAssignerProperties.getPidHome());
            redisWorkIdAssigner.setPidPort(workerIdAssignerProperties.getPidPort());
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
