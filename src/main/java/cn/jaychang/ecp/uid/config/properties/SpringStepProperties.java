package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * spring step 属性配置类
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
public class SpringStepProperties extends MeituanLeafProperties implements Serializable {

    private static final long serialVersionUID = 4414114365097978569L;
}
