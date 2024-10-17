package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.properties.EcpUidProperties;
import cn.jaychang.ecp.uid.extend.strategy.MeituanLeafSegmentStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('meituan-leaf')}")
public class MeituanLeafStrategyConfiguration {

    @Bean
    public MeituanLeafSegmentStrategy meituanLeafSegmentStrategy(EcpUidProperties ecpUidProperties) {
        MeituanLeafSegmentStrategy meituanLeafSegmentStrategy = new MeituanLeafSegmentStrategy();
        meituanLeafSegmentStrategy.setAsynLoadingSegment(ecpUidProperties.getMeituanLeaf().getAsynLoadingSegment());
        return meituanLeafSegmentStrategy;
    }
}
