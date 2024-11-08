package cn.jaychang.ecp.uid.extend.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @类名称 UidModelEnum.java
 * @类描述 <pre>id生产模式</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月3日 上午9:54:09
 * @版本 1.00
 * @修改记录 <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年9月3日
 *     ----------------------------------------------
 * </pre>
 */
@Getter
@AllArgsConstructor
public enum UidModelEnum {
    /**
     * 步长自增(空实现,依赖数据库步长设置)
     */
    SPRING_STEP("spring-step"),
    /**
     * 分段批量(基于美团leaf)
     */
    MEITUAN_LEAF("meituan-leaf"),
    /**
     * Snowflake算法(源自twitter)
     */
    TWITTER_SNOWFLAKE("twitter-snowflake"),
    /**
     * 百度UidGenerator
     */
    BAIDU_UID("baidu-uid");

    private final String name;
}
