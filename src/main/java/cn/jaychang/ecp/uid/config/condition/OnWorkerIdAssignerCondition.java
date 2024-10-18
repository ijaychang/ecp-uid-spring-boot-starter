package cn.jaychang.ecp.uid.config.condition;

import cn.jaychang.ecp.uid.config.properties.BaiduUidProperties;
import cn.jaychang.ecp.uid.config.properties.TwitterSnowflakeProperties;
import cn.jaychang.ecp.uid.worker.enums.WorkerIdAssignerEnum;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

/**
 * 判断条件是否存在 WorkerIdAssigner
 * @author jaychang
 */
public class OnWorkerIdAssignerCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("WorkerIdAssigner Type Configured Condition");
        Environment environment = context.getEnvironment();
        try {
            BindResult<TwitterSnowflakeProperties> twitterSnowflakeBindResult = Binder.get(environment).bind("ecp.uid.twitter-snowflake", TwitterSnowflakeProperties.class);
            TwitterSnowflakeProperties twitterSnowflakeProperties = twitterSnowflakeBindResult.orElse(new TwitterSnowflakeProperties());
            if (Objects.nonNull(twitterSnowflakeProperties)) {
                WorkerIdAssignerEnum twitterSnowflakeWorkerIdAssigner = twitterSnowflakeProperties.getWorkerIdAssigner();
                if (Objects.nonNull(twitterSnowflakeWorkerIdAssigner)) {
                    return ConditionOutcome.match("WorkerIdAssigner type matched");
                }
            }
            BaiduUidProperties baiduUidProperties = Binder.get(environment).bind("ecp.uid.baidu-uid", BaiduUidProperties.class).orElse(new BaiduUidProperties());
            if (Objects.nonNull(baiduUidProperties)) {
                WorkerIdAssignerEnum baiduUidWorkerIdAssigner = baiduUidProperties.getWorkerIdAssigner();
                if (Objects.nonNull(baiduUidWorkerIdAssigner)) {
                    return ConditionOutcome.match("WorkerIdAssigner type matched");
                }
            }
        } catch (BindException e) {
            return ConditionOutcome.noMatch(message.because(String.format("match WorkerIdAssigner type error:%s", e.getMessage())));
        }
        return ConditionOutcome.noMatch(message.because("unknown WorkerIdAssigner type"));
    }
}
