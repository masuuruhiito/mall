package com.weikun.mall.consumer.config;

import com.weikun.mall.consumer.aop.SystemLogAspect;
import com.weikun.mall.consumer.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 创建人：SHI
 * 创建时间：2021/12/2
 * 描述你的类：
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将所有/static/upload/images/** 访问都映射到classpath:/static/upload/images/ 目录下
        //addResourceLocations的每一个值必须以'/'结尾,否则虽然映射了,但是依然无法访问该目录下的的文件(支持: classpath:/xxx/xx/, file:/xxx/xx/, http://xxx/xx/)
        registry.addResourceHandler("/static/upload/images/**").addResourceLocations("classpath:/static/upload/images/");
        //以下服务于Swagger 否则http://localhost:8080/swagger-ui.html 访问不到
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");


    }


}
