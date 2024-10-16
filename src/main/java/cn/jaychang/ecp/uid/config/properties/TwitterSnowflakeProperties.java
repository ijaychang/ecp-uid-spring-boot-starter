package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class TwitterSnowflakeProperties extends WorkerIdAssignerProperties implements Serializable {

    private static final long serialVersionUID = -552051843461413001L;

    /**
     * 数据中心ID 可不配
     */
    private Long datacenterId;

    /**
     * 工作节点ID 可不配(如果配了workerIdAssigner，优先使用workerIdAssigner)
     */
    private Long workId;

}
