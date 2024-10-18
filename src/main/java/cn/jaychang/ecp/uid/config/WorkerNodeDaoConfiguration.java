package cn.jaychang.ecp.uid.config;

import cn.jaychang.ecp.uid.worker.dao.WorkerNodeDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WorkerNodeDao 配置
 *
 * @author jaychang
 */
@Configuration
@ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('db') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('db')}")
public class WorkerNodeDaoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WorkerNodeDao workNodeDao() {
        return new WorkerNodeDao();
    }
}
