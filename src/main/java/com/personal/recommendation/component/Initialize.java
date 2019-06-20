package com.personal.recommendation.component;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.component.thread.InitialModelThread;
import com.personal.recommendation.component.thread.RecommendServiceThread;
import com.personal.recommendation.utils.DBConnectionUtil;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Initialize {

    public static void initialize(ApplicationContext ctx){
        // 为协同过滤配置提供数据库连接
        DBConnectionUtil.URL = ctx.getEnvironment().getProperty("spring.datasource.url");
        DBConnectionUtil.USERNAME = ctx.getEnvironment().getProperty("spring.datasource.username");
        DBConnectionUtil.PASSWORD = ctx.getEnvironment().getProperty("spring.datasource.password");

        // 创建分类model初始化线程
        ExecutorService modelService = Executors.newSingleThreadExecutor();
        modelService.execute(new InitialModelThread());

        // 创建推荐请求消费线程
        ExecutorService recommendService = Executors.newFixedThreadPool(RecommendationConstants.THREAD_NUM);
        for(int i=0;i<RecommendationConstants.THREAD_NUM;i++) {
            recommendService.execute(new RecommendServiceThread());
        }
    }

}
