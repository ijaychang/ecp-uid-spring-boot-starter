package cn.jaychang.ecp.uid.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * spring boot 配置条件注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnWorkerIdAssignerCondition.class)
public @interface ConditionalOnWorkIdAssigner {
}