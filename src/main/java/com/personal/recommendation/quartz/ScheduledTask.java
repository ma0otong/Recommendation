package com.personal.recommendation.quartz;

import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.service.CalculatorService;
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
//
//    private HashMap<Integer, int[]> randomModuleMap = new HashMap<>();
//
//    private int MAX_USER_ID = 7;
//    private int MIN_USER_ID = 1;

    private final NewsLogsManager newsLogsManager;
    private final NewsManager newsManager;

    @Autowired
    public ScheduledTask(NewsLogsManager newsLogsManager, NewsManager newsManager) {
        this.newsLogsManager = newsLogsManager;
        this.newsManager = newsManager;
    }
//
//    /**
//     * 定时新增随机用户浏览记录
//     */
//    @Scheduled(cron = "*/1 * * * * ?")
//    private void addNewsLogs() {
//        NewsLogs newsLog = new NewsLogs();
//        // 随机生成user
//        int userId = (new Random().nextInt(MAX_USER_ID) % (MAX_USER_ID - MIN_USER_ID + 1) + MIN_USER_ID);
//
//        // 随机生成news
//        int[] randNums = randomModuleMap.get(userId);
//        if(randNums == null){
//            initializeMap();
//            randNums = randomModuleMap.get(userId);
//        }
//        Long moduleId = (long) randNums[new Random().nextInt(randNums.length)];
//
//        Long newsId = newsManager.getRandomNewsByModule(moduleId);
//
//        // 写入newsLogs
//        newsLog.setUserId((long) userId);
//        newsLog.setNewsId(newsId);
//        newsLogsManager.insertNewsLogs(newsLog);
//
//        logger.info(String.format("Auto add new newsLog record ... userId : %s, newsId : %s, moduleId : %s",
//                userId, newsId, moduleId));
//    }

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

//    public void initializeMap(){
//        randomModuleMap.put(1, new int[]{1, 5, 8, 13});
//        randomModuleMap.put(2, new int[]{1, 2, 7, 9});
//        randomModuleMap.put(3, new int[]{2, 3, 5, 4});
//        randomModuleMap.put(4, new int[]{11, 4, 6, 14});
//        randomModuleMap.put(5, new int[]{17, 16, 1, 8});
//        randomModuleMap.put(6, new int[]{15, 14, 3, 2});
//        randomModuleMap.put(7, new int[]{4, 7, 12, 6});
//    }
}
