package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.baidu.UidGenerator;
import cn.jaychang.ecp.uid.baidu.enums.UidGeneratorTypeEnum;
import cn.jaychang.ecp.uid.baidu.impl.CachedUidGenerator;
import cn.jaychang.ecp.uid.baidu.impl.DefaultUidGenerator;
import cn.jaychang.ecp.uid.config.properties.BaiduUidProperties;
import cn.jaychang.ecp.uid.config.properties.WorkerIdAssignerProperties;
import cn.jaychang.ecp.uid.extend.strategy.BaiduUidStrategy;
import cn.jaychang.ecp.uid.worker.*;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * baidu uid 生成策略配置类
 *
 * @author jaychang
 */
@Configuration
@EnableConfigurationProperties(BaiduUidProperties.class)
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('baidu-uid')}")
public class BaiduUidStrategyConfiguration extends WorkerIdConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BaiduUidStrategy baiduUidStrategy(BaiduUidProperties baiduUidProperties) {
        BaiduUidStrategy baiduUidStrategy = new BaiduUidStrategy();
        baiduUidStrategy.setUidGenerator(uidGenerator(baiduUidProperties));
        return baiduUidStrategy;
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator uidGenerator(BaiduUidProperties baiduUidProperties) {
        DefaultUidGenerator defaultUidGenerator = null;
        if (UidGeneratorTypeEnum.CACHE.equals(baiduUidProperties.getType())) {
            defaultUidGenerator = new CachedUidGenerator();
        } else if (UidGeneratorTypeEnum.DEFAULT.equals(baiduUidProperties.getType())) {
            defaultUidGenerator = new DefaultUidGenerator();
        } else {
            throw new IllegalArgumentException(String.format("UidGeneratorType:[%s] is illegal", baiduUidProperties.getType()));
        }
        defaultUidGenerator.setEpochStr(baiduUidProperties.getEpochStr());
        defaultUidGenerator.setTimeBits(baiduUidProperties.getTimeBits());
        defaultUidGenerator.setWorkerBits(baiduUidProperties.getWorkerBits());
        defaultUidGenerator.setSeqBits(baiduUidProperties.getSeqBits());

        WorkerIdAssigner workerIdAssigner = createWorkerIdAssigner(baiduUidProperties);
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        return defaultUidGenerator;
    }


    public WorkerIdAssigner createWorkerIdAssigner(WorkerIdAssignerProperties workerIdAssignerProperties) {
        // workerId 分配方式
        WorkerIdAssigner workerIdAssigner = null;
        if (WorkerIdAssignerEnum.ZK.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
            zkWorkerIdAssigner.setZkAddress(workerIdAssignerProperties.getZkAddress());
            zkWorkerIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            zkWorkerIdAssigner.setPidHome(workerIdAssignerProperties.getPidHome());
            zkWorkerIdAssigner.setPidPort(workerIdAssignerProperties.getPidPort());
            workerIdAssigner = zkWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.DB.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner();
            workerIdAssigner = disposableWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.REDIS.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            RedisWorkIdAssigner redisWorkIdAssigner = new RedisWorkIdAssigner();
            redisWorkIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            redisWorkIdAssigner.setPidHome(workerIdAssignerProperties.getPidHome());
            redisWorkIdAssigner.setPidPort(workerIdAssignerProperties.getPidPort());
            workerIdAssigner = redisWorkIdAssigner;
        } else if (WorkerIdAssignerEnum.SIMPLE.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            SimpleWorkerIdAssigner simpleWorkerIdAssigner = new SimpleWorkerIdAssigner();
            workerIdAssigner = simpleWorkerIdAssigner;
        } else {
            throw new IllegalArgumentException(String.format("WorkerIdAssigner:[%s] is illegal", workerIdAssignerProperties.getWorkerIdAssigner()));
        }
        return workerIdAssigner;
    }


}
