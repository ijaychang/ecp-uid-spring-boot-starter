package cn.jaychang.ecp.uid.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis template 配置
 *
 * @author jaychang
 */
@Configuration
@ConditionalOnExpression("#{'${ecp.uid.baidu-uid.worker-id-assigner}'.equals('redis') or '${ecp.uid.twitter-snowflake.worker-id-assigner}'.equals('redis')}")
public class RedisTemplateConfiguration {

    @Bean(name = "redisTemplateForWorkIdAssigner")
    @ConditionalOnMissingBean(name = "redisTemplateForWorkIdAssigner")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());

        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
