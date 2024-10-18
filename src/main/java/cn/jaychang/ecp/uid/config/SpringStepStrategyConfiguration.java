package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.SpringStepProperties;
import cn.jaychang.ecp.uid.extend.strategy.SpringStepStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring step 生成策略配置
 *
 * @author jaychang
 */
@Configuration
@EnableConfigurationProperties(SpringStepProperties.class)
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('spring-step')}")
public class SpringStepStrategyConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringStepStrategy springStepStrategy(SpringStepProperties springStepProperties) {
        SpringStepStrategy springStepStrategy = new SpringStepStrategy();
        springStepStrategy.setAsynLoadingSegment(springStepProperties.getAsynLoadingSegment());
        return springStepStrategy;
    }


}
