package com.personal.recommendation.component.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DAOAop {

    private static final Logger logger = Logger.getLogger(DAOAop.class);

    // 为数据库查询增加耗时统计
    @Pointcut("execution(* com.personal.recommendation.dao.*.*(..))")
    public void aopMethod(){}

    @Around("aopMethod()")
    public Object  around(ProceedingJoinPoint joinPoint) throws Throwable{
        long time = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        logger.info(String.format("execute DAO, method name <%s> cost <%s ms>", joinPoint.getSignature().getName(), (System.currentTimeMillis() - time)));
        return proceed;
    }

}
