package com.weikun.api.annotation;

import java.lang.annotation.*;

/**
 * 创建人：SHI
 * 创建时间：2021/11/4
 * 描述你的类：
 */
@Target({ElementType.METHOD,ElementType.PARAMETER})//针对方法和参数
@Retention(RetentionPolicy.RUNTIME)//在运行时起作用
@Documented//Documented注解表明这个注释是由 javadoc记录的，
public @interface SystemLog {
    //日志名称
    String description() default "";
    //日志类型
    LogType type() ;
}
