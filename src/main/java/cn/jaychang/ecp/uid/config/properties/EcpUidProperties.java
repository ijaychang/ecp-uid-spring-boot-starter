package cn.jaychang.ecp.uid.config.properties;

import cn.jaychang.ecp.uid.extend.annotation.UidModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "ecp.uid")
public class EcpUidProperties implements Serializable {
    private static final long serialVersionUID = 1626096582500766485L;
    /**
     * uid 生成策略 默认 SNOWFLAKE
     * <p>
     * STEP 步长自增(空实现,依赖数据库步长设置)
     * </p>
     * <p>
     * SEGMENT 分段批量(基于leaf)
     * </p>
     * <p>
     * SNOWFLAKE Snowflake算法(源自twitter)
     * </p>
     * <p>
     * BAIDU 百度UidGenerator
     * </p>
     */
    private String strategy = UidModel.SNOWFLAKE.getName();


    /**
     * baidu uid配置
     */
    private BaiduUidProperties baiduUid;

    /**
     * 美团 leaf配置
     */
    private MeituanLeafProperties meituanLeaf;

    /**
     * Twitter snowflake配置
     */
    private TwitterSnowflakeProperties twitterSnowflake;

    /**
     * Spring 数据库字段自增方式配置
     */
    private SpringStepProperties springStep;

}
