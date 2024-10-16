package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.baidu.enums.UidGeneratorTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BaiduUidProperties extends WorkerIdAssignerProperties implements Serializable {

    private static final long serialVersionUID = 5040536433413671208L;
    /**
     * Bits allocate
     */
    private int timeBits = 34;
    private int workerBits = 16;
    private int seqBits = 13;

    /**
     * UID Generator Type
     */
    private UidGeneratorTypeEnum type = UidGeneratorTypeEnum.CACHE;

    /**
     * Customer epoch, unit as second. For example 2024-10-15 (ms: 1728921600000)
     */
    private String epochStr = "2024-10-15";

}
