package com.autoparts.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 标注在 Controller 或 Service 方法上，由 AOP 切面自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {

    /** 模块：AUTH/PRODUCT/STOCK_IN/ORDER/INVENTORY */
    String module();

    /** 操作类型：LOGIN/CREATE/UPDATE/DELETE/STOCK_IN/REFUND 等 */
    String action();

    /** 操作描述（可选，默认从方法名推断） */
    String description() default "";
}
