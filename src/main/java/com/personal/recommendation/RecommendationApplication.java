package com.personal.recommendation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.personal.recommendation.dao")
public class RecommendationApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendationApplication.class, args);
	}

}
