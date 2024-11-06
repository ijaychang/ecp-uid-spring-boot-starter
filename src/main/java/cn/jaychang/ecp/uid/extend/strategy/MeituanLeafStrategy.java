package cn.jaychang.ecp.uid.extend.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.jaychang.ecp.uid.extend.annotation.UidModelEnum;
import cn.jaychang.ecp.uid.leaf.SegmentIDGenImpl;
import cn.jaychang.ecp.uid.leaf.dao.IDAllocDao;
import cn.jaychang.ecp.uid.leaf.service.IDAllocService;
import cn.jaychang.ecp.uid.leaf.service.impl.IDAllocServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @类名称 LeafSegmentStrategy.java
 * @类描述 <pre>Leaf分段批量Id策略(可配置asynLoadingSegment-异步标识)</pre>
 * @作者 庄梦蝶殇 linhuaichuan1989@126.com
 * @创建时间 2018年9月5日 上午11:35:53
 * @版本 1.00
 *
 * @修改记录
 * <pre>
 *     版本                       修改人 		修改日期 		 修改内容描述
 *     ----------------------------------------------
 *     1.00 	庄梦蝶殇 	2018年9月5日             
 *     ----------------------------------------------
 * </pre>
 */
public class MeituanLeafStrategy implements IUidStrategy {
    private final static String MSG_UID_PARSE = "{\"UID\":\"%s\"}";
    private final static Logger log = LoggerFactory.getLogger(MeituanLeafStrategy.class);
    
    /**
     * 生成器集合
     */
    protected static Map<String, SegmentIDGenImpl> generatorMap = new ConcurrentHashMap<>();
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * 同步/异步两种更新数据库方式。可选配置asynLoadingSegment(true-异步，false-同步)，默认使用异步
     */
    private boolean asynLoadingSegment = true;
    
    /**
     * 获取uid生成器
     * @方法名称 getUidGenerator
     * @功能描述 <pre>获取uid生成器</pre>
     * @param prefix 前缀
     * @return uid生成器
     */
    public SegmentIDGenImpl getSegmentService(String prefix) {
        SegmentIDGenImpl segmentService = generatorMap.get(prefix);
        if (null == segmentService) {
            synchronized (generatorMap) {
                if (null == segmentService) {
                    IDAllocDao idAllocDao = new IDAllocDao(jdbcTemplate);
                    IDAllocService idAllocService = new IDAllocServiceImpl(idAllocDao);
                    segmentService = new SegmentIDGenImpl();
                    segmentService.setIdAllocService(idAllocService);
                    if (segmentService.init()) {
                        log.info("Segment Service Init Successfully");
                    } else {
                        throw new RuntimeException("Segment Service Init Fail");
                    }
                }
                generatorMap.put(prefix, segmentService);
            }
        }
        return segmentService;
    }
    
    @Override
    public UidModelEnum getName() {
        return UidModelEnum.MEITUAN_LEAF;
    }
    
    @Override
    public long getUID(String group) {
        return getSegmentService(group).get();
    }
    
    @Override
    public String parseUID(long uid, String group) {
        return String.format(MSG_UID_PARSE, uid);
    }

    public boolean isAsynLoadingSegment() {
        return asynLoadingSegment;
    }

    public void setAsynLoadingSegment(boolean asynLoadingSegment) {
        this.asynLoadingSegment = asynLoadingSegment;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
