package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.config.condition.ConditionalOnWorkIdAssigner;
import cn.jaychang.ecp.uid.config.properties.WorkerIdAssignerProperties;
import cn.jaychang.ecp.uid.worker.*;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * worker id 分配器配置
 *
 * @author jaychang
 */
@Configuration
@Import({RedisTemplateConfiguration.class, WorkerNodeDaoConfiguration.class})
public class WorkerIdConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWorkIdAssigner
    public WorkerIdAssigner workerIdAssigner(WorkerIdAssignerProperties workerIdAssignerProperties) {
        // workerId 分配方式
        WorkerIdAssigner workerIdAssigner;
        if (WorkerIdAssignerEnum.ZK.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            ZkWorkerIdAssigner zkWorkerIdAssigner = new ZkWorkerIdAssigner();
            zkWorkerIdAssigner.setZkAddress(workerIdAssignerProperties.getZkAddr());
            zkWorkerIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            zkWorkerIdAssigner.setPort(workerIdAssignerProperties.getPidPort());
            workerIdAssigner = zkWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.DB.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner();
            workerIdAssigner = disposableWorkerIdAssigner;
        } else if (WorkerIdAssignerEnum.REDIS.equals(workerIdAssignerProperties.getWorkerIdAssigner())) {
            RedisWorkIdAssigner redisWorkIdAssigner = new RedisWorkIdAssigner();
            redisWorkIdAssigner.setInterval(workerIdAssignerProperties.getHeartbeatInterval());
            redisWorkIdAssigner.setPort(workerIdAssignerProperties.getPidPort());
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
