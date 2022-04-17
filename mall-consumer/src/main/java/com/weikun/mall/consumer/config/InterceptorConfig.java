package com.weikun.mall.consumer.config;


import com.weikun.mall.consumer.aop.SystemLogAspect;
import com.weikun.mall.consumer.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：配置拦截器
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private AuthenticationInterceptor authorizationInterceptor;//必须用注入形式 否则找不到service

    @Autowired
    private SystemLogAspect systemLogAspect;//必须用注入形式 否则找不到service

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有请求，通过判断是否有@LoginRequired注解， 决定是否需要登录
        registry.addInterceptor(authorizationInterceptor).addPathPatterns("/**");
        registry.addInterceptor(systemLogAspect).addPathPatterns("/**");

    }


}
