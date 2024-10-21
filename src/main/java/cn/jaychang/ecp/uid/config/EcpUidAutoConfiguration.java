package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.UidContext;
import cn.jaychang.ecp.uid.config.properties.EcpUidProperties;
import cn.jaychang.ecp.uid.config.properties.InetUtilsProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Ecp Uid 自动配置类
 *
 * @author jaychang
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({EcpUidProperties.class, InetUtilsProperties.class})
@Import({SpringStepStrategyConfiguration.class, TwitterSnowflakeStrategyConfiguration.class, BaiduUidStrategyConfiguration.class, MeituanLeafStrategyConfiguration.class})
@AllArgsConstructor
public class EcpUidAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public UidContext uidContext() {
        return new UidContext();
    }
}
