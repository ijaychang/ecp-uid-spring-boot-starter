package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SnowflakeProperties implements Serializable {

    private static final long serialVersionUID = -552051843461413001L;
}
