package com.personal.recommendation;

import com.personal.recommendation.config.Initialize;
import com.personal.recommendation.utils.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

@SpringBootApplication
@MapperScan("com.personal.recommendation.dao")
public class RecommendationApplication implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * 工程启动类
     * @param args String[]
     */
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecommendationApplication.class);
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
    }

    /**
     * 启动前初始化
     * @param contextRefreshedEvent ContextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext ctx = contextRefreshedEvent.getApplicationContext();
        SpringContextUtil.setApplicationContext(contextRefreshedEvent.getApplicationContext());
        Initialize.initialize(ctx);
    }

}


