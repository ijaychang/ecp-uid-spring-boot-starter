package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * meituan leaf 配置属性类
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = MeituanLeafProperties.PREFIX)
public class MeituanLeafProperties implements Serializable {

    private static final long serialVersionUID = 7920440459872322191L;

    /**
     * Prefix for the MeituanLeafProperties.
     */
    public static final String PREFIX = "ecp.uid.meituan-leaf";

    /**
     * 异步标识(true-异步，false-同步)
     */
    private Boolean asynLoadingSegment = true;
}
