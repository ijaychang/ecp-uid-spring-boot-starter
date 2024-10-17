package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.EcpUidProperties;
import cn.jaychang.ecp.uid.extend.strategy.SpringStepStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('spring-step')}")
public class SpringStepStrategyConfiguration {

    @Bean
    public SpringStepStrategy springStepStrategy(EcpUidProperties ecpUidProperties) {
        SpringStepStrategy springStepStrategy = new SpringStepStrategy();
        springStepStrategy.setAsynLoadingSegment(ecpUidProperties.getSpringStep().getAsynLoadingSegment());
        return springStepStrategy;
    }


}
