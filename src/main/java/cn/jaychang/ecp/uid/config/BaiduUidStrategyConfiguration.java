package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.baidu.UidGenerator;
import cn.jaychang.ecp.uid.baidu.enums.UidGeneratorTypeEnum;
import cn.jaychang.ecp.uid.baidu.impl.CachedUidGenerator;
import cn.jaychang.ecp.uid.baidu.impl.DefaultUidGenerator;
import cn.jaychang.ecp.uid.config.properties.BaiduUidProperties;
import cn.jaychang.ecp.uid.config.properties.EcpUidProperties;
import cn.jaychang.ecp.uid.extend.strategy.BaiduUidStrategy;
import cn.jaychang.ecp.uid.worker.WorkerIdAssigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("#{'${ecp.uid.strategy}'.equals('baidu-uid')}")
public class BaiduUidStrategyConfiguration extends WorkerIdConfiguration {
    @Bean
    public BaiduUidStrategy baiduUidStrategy(EcpUidProperties ecpUidProperties) {
        BaiduUidStrategy baiduUidStrategy = new BaiduUidStrategy();
        BaiduUidProperties baiduUidProperties = ecpUidProperties.getBaiduUid();
        baiduUidStrategy.setUidGenerator(uidGenerator(baiduUidProperties));
        return baiduUidStrategy;
    }

    @Bean
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


}
