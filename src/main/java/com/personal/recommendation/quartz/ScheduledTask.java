package com.personal.recommendation.quartz;

import com.personal.recommendation.config.Initialize;
import com.personal.recommendation.service.impl.HotDataRecommendation;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时器
 */
@Component
@Configuration
@EnableScheduling
public class ScheduledTask {

    /**
     * 定时更新新闻池
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    private void formNewsPoolList() {
        Initialize.formNewsPoolList();
    }

    /**
     * 定时更新热点新闻
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    private void formTopHotNewsList() {
        HotDataRecommendation.formTopHotNewsList();
    }

}
