package com.personal.recommendation.component.aop;

import com.personal.recommendation.utils.IpUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 切面类
 */
@Component
@Aspect
public class Aop {

    private static final Logger logger = Logger.getLogger(Aop.class);

    // 为http请求增加耗时统计
    @Pointcut("execution(* com.personal.recommendation.controller.*.*(..))")
    public void aopControllerMethod(){}

    @Around("aopControllerMethod()")
    public Object controllerAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long time = System.currentTimeMillis();
        String ipAddress = null;
        String url = null;
        Object proceed = joinPoint.proceed();
        if(joinPoint.getArgs() != null && joinPoint.getArgs().length > 0){
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                if(joinPoint.getArgs()[i] instanceof HttpServletRequest){
                    HttpServletRequest req = ((HttpServletRequest)joinPoint.getArgs()[i]);
                    ipAddress = IpUtil.getIpAddr(req);
                    url = req.getServletPath();
                }
            }
        }
        if(StringUtils.isNotBlank(ipAddress) && StringUtils.isNotBlank(url))
            logger.info(String.format("execute Controller, url <%s>, ip <%s>, cost <%s ms>",
                    url, ipAddress, (System.currentTimeMillis() - time)));
        else
            logger.info(String.format("execute Controller, method name <%s> cost <%s ms>",
                    joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));

        return proceed;
    }

//     // 为数据库查询增加耗时统计
//    @Pointcut("execution(* com.personal.recommendation.dao.*.*(..))")
//    public void aopDAOMethod(){}
//
//    @Around("aopDAOMethod()")
//    public Object daoAround(ProceedingJoinPoint joinPoint) throws Throwable{
//        long time = System.currentTimeMillis();
//        Object proceed = joinPoint.proceed();
//        logger.info(String.format("execute DAO, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
//        return proceed;
//    }
//
//    // 为service方法增加耗时统计
//    @Pointcut("execution(* com.personal.recommendation.service.*.*(..))")
//    public void aopServiceMethod(){}
//
//    @Around("aopServiceMethod()")
//    public Object serviceAround(ProceedingJoinPoint joinPoint) throws Throwable{
//        long time = System.currentTimeMillis();
//        Object proceed = joinPoint.proceed();
//        logger.info(String.format("execute Service, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
//        return proceed;
//    }

}
