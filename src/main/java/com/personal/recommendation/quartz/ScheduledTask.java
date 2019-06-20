package com.personal.recommendation.quartz;

import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.service.recommendation.CalculatorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 定时器
 */
@Component
@Configuration
@EnableScheduling
public class ScheduledTask {

    private static final Logger logger = Logger.getLogger(ScheduledTask.class);

    private final NewsLogsManager newsLogsManager;
    private final NewsManager newsManager;

    @Autowired
    public ScheduledTask(NewsLogsManager newsLogsManager, NewsManager newsManager) {
        this.newsLogsManager = newsLogsManager;
        this.newsManager = newsManager;
    }

    /**
     * 定时更新数据库表时间记录, 确保测试效果
     * 仅作为测试用
     */
    @Scheduled(cron = "0 0 12 * * ?")
    private void refreshDBTime() {
        logger.info("Refresh db time and topHotNewsList");
        newsManager.updateNewsTime(new Date());
        newsLogsManager.updateViewTime(new Date());
        CalculatorService.topHotNewsList.clear();
    }

}
