package com.weikun.mall.consumer.interceptor;

import com.alibaba.dubbo.config.annotation.Reference;


import com.weikun.api.annotation.UserLoginToken;
import com.weikun.api.model.UmsAdmin;
import com.weikun.api.service.ITokenService;
import com.weikun.api.service.IUserService;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 创建人：SHI
 * 创建时间：2021/12/5
 * 描述你的类：
 * 在适当的HandlerAdapter触发处理程序本身的执行之前调用HandlerInterceptor。
 * 该机制可用于很多领域的预处理工作，例如， 授权检查，通用处理流程（区域设置或主题更改）。
 * 拦截器的其主要目的是允许分解重复的处理代码。
 *

 */
@Component //是个注入的组件
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Reference(
            version = "1.0.0",interfaceClass = IUserService.class,
            interfaceName = "com.weikun.api.user.service.IUserService",
            timeout =120000
    )
    private IUserService service;


    @Reference(
            version = "1.0.0",interfaceClass = ITokenService.class,
            interfaceName = "com.weikun.api.user.service.ITokenService",
            timeout =120000
    )
    private ITokenService tokenService;
    /*
    预处理回调方法,实现处理器的预处理，第三个参数为响应的处理器,自定义Controller,
    返回值为true表示继续流程（如调用下一个拦截器或处理器）或者接着执行
postHandle()和afterCompletion()；false表示流程中断，不会继续调用其他的拦截器或处理器，中断执行。

     */
    @Override //最早执行 在执行核心业务之前 进行验证
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token=request.getHeader("Authorization");//从http请求头部取出token
        //如果不是映射到方法 就直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod) handler;//是方法
        Method method=handlerMethod.getMethod();

        //检查有没有需要用户权限的注解
        if(method.isAnnotationPresent(UserLoginToken.class)){
            UserLoginToken userLoginToken=method.getAnnotation(UserLoginToken.class);
            if(userLoginToken.required()){
                //执行认证
                if(token==null){
                    throw new RuntimeException("无token，请重新登录！");
                }
                //获取token中的user id
                token=token.split("@")[1];
                Long userid;
                userid= Long.parseLong(tokenService.getUserId(token));//取出在登录后，得到token的时候存进去的id

                UmsAdmin user=service.findUserById(userid);
                if(user==null){
                    throw new RuntimeException("用户不存在，请重新登录！");
                }
                // 验证 token
                //验证token
                if(tokenService.checkSign(token,user.getPassword())){
                    return true;
                }else{
                    throw new RuntimeException("Token 验证失败！");
                }
            }
        }
        return true;
    }

    @Override //执行阶段
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override//执行完毕
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("After");
    }
}
