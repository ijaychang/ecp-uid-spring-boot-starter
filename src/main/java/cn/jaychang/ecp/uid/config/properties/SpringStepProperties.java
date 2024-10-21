package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * spring step 属性配置类
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = SpringStepProperties.PREFIX)
public class SpringStepProperties extends MeituanLeafProperties implements Serializable {

    private static final long serialVersionUID = 4414114365097978569L;

    /**
     * Prefix for the BaiduUidProperties.
     */
    public static final String PREFIX = "ecp.uid.spring-step";
}
