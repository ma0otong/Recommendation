package com.personal.recommendation;

import com.personal.recommendation.utils.DBConnectionUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.personal.recommendation.dao")
public class RecommendationApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationApplication.class, args);
		// 为协同过滤配置提供数据库连接
		DBConnectionUtil.URL = ctx.getEnvironment().getProperty("spring.datasource.url");
		DBConnectionUtil.USERNAME = ctx.getEnvironment().getProperty("spring.datasource.username");
		DBConnectionUtil.PASSWORD = ctx.getEnvironment().getProperty("spring.datasource.password");
	}

}
