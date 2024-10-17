package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * meituan leaf 配置属性类
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
public class MeituanLeafProperties implements Serializable {

    private static final long serialVersionUID = 7920440459872322191L;

    private Boolean asynLoadingSegment = true;

}
