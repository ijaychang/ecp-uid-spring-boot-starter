package cn.jaychang.ecp.uid.config;

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
}
