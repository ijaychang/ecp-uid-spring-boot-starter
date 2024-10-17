package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.baidu.buffer.RingBuffer;
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


    // 以下配置仅当type=cache时有效
    /**
     * RingBuffer size扩容参数, 可提高UID生成的吞吐量 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
     */
    private Integer boostPower = 3;

    /**
     * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
     * 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
     * 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
     */
    private Integer paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;

    /**
     * 另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充
     * 默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
     */
    private Integer scheduleInterval;

}
