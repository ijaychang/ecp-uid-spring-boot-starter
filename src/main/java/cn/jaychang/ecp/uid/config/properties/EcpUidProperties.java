package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.extend.annotation.UidModelEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * ecp uid 配置属性类
 *
 * @author jaychang
 */
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "ecp.uid")
public class EcpUidProperties implements Serializable {
    private static final long serialVersionUID = 1626096582500766485L;
    /**
     * UID 生成策略 默认 TWITTER_SNOWFLAKE
     * <p>
     * SPRING_STEP 步长自增(空实现,依赖数据库步长设置)
     * </p>
     * <p>
     * MEITUAN_LEAF 分段批量(基于leaf)
     * </p>
     * <p>
     * TWITTER_SNOWFLAKE Snowflake算法(源自twitter)
     * </p>
     * <p>
     * BAIDU_UID 百度UidGenerator
     * </p>
     */
    private String strategy = UidModelEnum.TWITTER_SNOWFLAKE.getName();


    /**
     * 可选：基因因子，如设置则启用混淆
     */
    private Long factor;

    /**
     * 可选：除余基数，建议使用固定值，不可更改 控制位移
     */
    private Integer fixed;

    /**
     * Spring Step 数据库字段自增方式配置
     */
    private SpringStepProperties springStep;

    /**
     * Twitter Snowflake 配置
     */
    private TwitterSnowflakeProperties twitterSnowflake;

    /**
     * Baidu Uid Generator 配置
     */
    private BaiduUidProperties baiduUid;

    /**
     * 美团 Leaf 配置
     */
    private MeituanLeafProperties meituanLeaf;
}
