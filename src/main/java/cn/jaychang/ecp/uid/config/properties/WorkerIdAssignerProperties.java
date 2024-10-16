package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class WorkerIdAssignerProperties implements Serializable {
    private static final long serialVersionUID = -5969365082580407201L;
    /**
     * work id 分配方案(默认zk)
     * 可选值：zk,db,redis,simple
     */
    private WorkerIdAssignerEnum workerIdAssigner;


    /**
     * zookeeper address
     */
    private String zookeeperConnection = "localhost:2181";
}
