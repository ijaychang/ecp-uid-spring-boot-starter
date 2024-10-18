package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.MeituanLeafProperties;
import cn.jaychang.ecp.uid.extend.strategy.MeituanLeafSegmentStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 美团 Leaf 生成策略配置
 *
 * @author jaychang
 */
@Configuration
@EnableConfigurationProperties(MeituanLeafProperties.class)
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('meituan-leaf')}")
public class MeituanLeafStrategyConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MeituanLeafSegmentStrategy meituanLeafSegmentStrategy(MeituanLeafProperties meituanLeafProperties) {
        MeituanLeafSegmentStrategy meituanLeafSegmentStrategy = new MeituanLeafSegmentStrategy();
        meituanLeafSegmentStrategy.setAsynLoadingSegment(meituanLeafProperties.getAsynLoadingSegment());
        return meituanLeafSegmentStrategy;
    }
}
