package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.TwitterSnowflakeProperties;
import cn.jaychang.ecp.uid.extend.strategy.TwitterSnowflakeStrategy;
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
            twitterSnowflakeStrategy.setAssigner(workerIdAssigner(twitterSnowflakeProperties));
        }
        return twitterSnowflakeStrategy;
    }
}
