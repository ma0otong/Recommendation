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

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecommendationApplication.class);
        application.addListeners(new ApplicationPidFileWriter());
        application.run(args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext ctx = contextRefreshedEvent.getApplicationContext();
        SpringContextUtil.setApplicationContext(contextRefreshedEvent.getApplicationContext());
        Initialize.initialize(ctx);
    }

}


