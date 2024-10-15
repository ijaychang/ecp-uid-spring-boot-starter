package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.UidContext;
import cn.jaychang.ecp.uid.extend.annotation.UidModel;
import cn.jaychang.ecp.uid.extend.strategy.IUidStrategy;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class EcpUidAutoConfiguration {
    private final EcpUidProperties ecpUidProperties;

    @Bean
    public UidContext uidContext() {

        IUidStrategy uidStrategy = null;
        if (UidModel.STEP.getName().equals(ecpUidProperties.getStrategy())) {

        } else if (UidModel.SNOWFLAKE.getName().equals(ecpUidProperties.getStrategy())) {

        } else if (UidModel.SEGMENT.getName().equals(ecpUidProperties.getStrategy())) {

        } else if (UidModel.BAIDU.getName().equals(ecpUidProperties.getStrategy())) {

        }
        UidContext uidContext = new UidContext(uidStrategy);
        return uidContext;
    }
}
