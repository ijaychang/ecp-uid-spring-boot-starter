package cn.jaychang.ecp.uid.config.properties;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BaiduProperties implements Serializable {

    private static final long serialVersionUID = 5040536433413671208L;
    /**
     * Bits allocate
     */
    private int timeBits = 23;
    private int workerBits = 31;
    private int seqBits = 9;

    /**
     * work id 分配方案(默认zk)
     * 可选值：zk,db,redis
     */
    private String workerIdAssigner = "zk";

    /**
     * zookeeper address
     */
    private String zookeeperConnection = "localhost:2181";

    /**
     * UID Generator Type
     */
    private String type = "default";

    /**
     * Customer epoch, unit as second. For example 2018-10-19 (ms: 1539878400000)
     */
    private String epochStr = "2018-10-19";

}
