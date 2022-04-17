package com.weikun.mall.consumer.aop;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.weikun.api.annotation.SystemLog;
import com.weikun.api.common.IpInfoUtil;
import com.weikun.api.common.ThreadPoolUtil;
import com.weikun.api.model.UMSLog;
import com.weikun.api.model.UmsAdmin;
import com.weikun.api.service.IBrandService;
import com.weikun.api.service.ITokenService;
import com.weikun.api.service.IUMSLogService;
import com.weikun.api.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.helpers.ThreadLocalMap;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Spring AOP实现日志管理
 * 创建人：SHI
 * 创建时间：2021/12/2
 */
@Aspect
@Component
@Slf4j
public class SystemLogAspect implements HandlerInterceptor {
    //多个连续的操作数据库，最好用线程池技术，提高用户体验
    private static final ThreadLocal<Date> beginTimeThreadLocal = new NamedThreadLocal<Date>("ThreadLocal beginTime");



    @Reference(
            version = "1.0.0",interfaceClass = IUMSLogService.class,
            interfaceName = "com.weikun.api.service.IUMSLogService",
            timeout =120000
    )

    private IUMSLogService logService;

    @Reference(
            version = "1.0.0",interfaceClass = IUserService.class,
            interfaceName = "com.weikun.api.service.IUserService",
            timeout =120000
    )
    private IUserService userService;

    @Reference(
            version = "1.0.0",interfaceClass = ITokenService.class,
            interfaceName = "com.weikun.api.user.service.ITokenService",
            timeout =120000
    )
    private ITokenService tokenService;

//    @Autowired(required = false)
//    private HttpServletRequest request;

    /**
     * Controller层切点,注解方式
     */

    @Pointcut("@annotation(com.weikun.api.annotation.SystemLog)")//等效
  // @Pointcut("execution(* com.weikun.mall.consumer.controller.*.*(..))")
    public void controllerAspect() {
        System.out.println("ok");
    }





    /**
     * 前置通知 (在方法执行之前返回)用于拦截Controller层记录用户的操作的开始时间
     *
     * @param joinPoint 切点
     * @throws InterruptedException
     */

    @Before("controllerAspect()")//controllerAspect方法之前运行 取出开始时间
    public void before(JoinPoint joinPoint) throws InterruptedException {

        //线程绑定变量（该数据只有当前请求的线程可见）
        Date beginTime = new Date();
        beginTimeThreadLocal.set(beginTime);
    }


    /**
     * 后置通知(在方法执行之后并返回数据) 用于拦截Controller层无异常的操作
     *
     * @param joinPoint 切点
     */
    @AfterReturning("controllerAspect()")//controllerAspect方法之后运行 存储
    public void after(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String description = getControllerMethodInfo(joinPoint).get("description").toString();//取方法信息
            Map<String, String[]> requestParams = request.getParameterMap();

            UMSLog log = new UMSLog();

            String token=request.getHeader("Authorization");//得到token 和前端呼应
            if(token==null){
                throw new RuntimeException("无token，请重新登录！");
            }
            token=token.split("@")[1];

            Long userid= Long.parseLong(tokenService.getUserId(token));//取出在登录后，得到token的时候存进去的id

            UmsAdmin user=userService.findUserById(userid);
            if(user==null){
                throw new RuntimeException("用户不存在，请重新登录！");
            }

            if(tokenService.checkSign(token,user.getPassword())){

            }else{
                throw new RuntimeException("Token 验证失败！");
            }

            //log.setUserid(userid.intValue());正式场景
            //以下模拟用户id，同一个用户id看不出大数据场景
            final Random random = new Random();
            int userId1 = random.nextInt(100);
            log.setUserid(userId1);


            //日志标题
            log.setName(description);
            //日志类型
            log.setLogType((int) getControllerMethodInfo(joinPoint).get("type"));
            //日志请求url
            log.setRequestUrl(request.getRequestURI());
            //请求方式
            log.setRequestType(request.getMethod());
            //请求参数
           // log.setRequestParam(ObjectUtil.mapToString(requestParams));
            String objectStr2 = (JSON.toJSONString(requestParams)).replace(",","&&");//必须这样处理，否则 flink无法区做字段值的区分，正常字段值用,分割
            System.out.println(objectStr2);
            log.setRequestParam(objectStr2.replace("\"","\'"));//防止json数据分不清楚

            //其他属性
            log.setIp(IpInfoUtil.getIpAddr(request));

            log.setCreateTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            log.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));


//            beginTimeThreadLocal;
//            ThreadLocalMap map = new ThreadLocalMap();
//            ThreadLocal local = new ThreadLocal();



            //.......
            //请求开始时间
            long beginTime = beginTimeThreadLocal.get().getTime();
            long endTime = System.currentTimeMillis();
            //请求耗时JS
            Long logElapsedTime = endTime - beginTime;
            log.setCostTime(logElapsedTime.intValue());

            //持久化(存储到数据或者ES，可以考虑用线程池)
            //logService.insert(log);
            ThreadPoolUtil.getPool().execute(new SaveSystemLogThread(log, logService));

        } catch (Exception e) {
            log.error("AOP后置通知异常", e);
        }
    }



    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param joinPoint 切点
     * @return 方法描述
     * @throws Exception
     */
    public static Map<String, Object> getControllerMethodInfo(JoinPoint joinPoint) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>(16);
        //获取目标类名
        String targetName = joinPoint.getTarget().getClass().getName();
        //获取方法名
        String methodName = joinPoint.getSignature().getName();
        //获取相关参数
        Object[] arguments = joinPoint.getArgs();
        //生成类对象
        Class targetClass = Class.forName(targetName);
        //获取该类中的方法
        Method[] methods = targetClass.getMethods();

        String description = "";
        Integer type = null;

        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class[] clazzs = method.getParameterTypes();
            if (clazzs.length != arguments.length) {
                //比较方法中参数个数与从切点中获取的参数个数是否相同，原因是方法可以重载哦
                continue;
            }
            description = method.getAnnotation(SystemLog.class).description();
            type = method.getAnnotation(SystemLog.class).type().ordinal();//取出序号
            map.put("description", description);
            map.put("type", type);
        }
        return map;
    }

}
