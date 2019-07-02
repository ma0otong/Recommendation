package com.personal.recommendation.component.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class Aop {

    private static final Logger logger = Logger.getLogger(Aop.class);

    // 为数据库查询增加耗时统计
//    @Pointcut("execution(* com.personal.recommendation.dao.*.*(..))")
//    public void aopDAOMethod(){}

    // 为http请求增加耗时统计
    @Pointcut("execution(* com.personal.recommendation.controller.*.*(..))")
    public void aopControllerMethod(){}

    // 为service方法增加耗时统计
    @Pointcut("execution(* com.personal.recommendation.service.*.*(..))")
    public void aopServiceMethod(){}

//    @Around("aopDAOMethod()")
//    public Object daoAround(ProceedingJoinPoint joinPoint) throws Throwable{
//        long time = System.currentTimeMillis();
//        Object proceed = joinPoint.proceed();
//        logger.info(String.format("execute DAO, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
//        return proceed;
//    }

    @Around("aopControllerMethod()")
    public Object controllerAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long time = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        logger.info(String.format("execute Controller, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
        return proceed;
    }

    @Around("aopServiceMethod()")
    public Object serviceAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long time = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        logger.info(String.format("execute Service, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
        return proceed;
    }

}
