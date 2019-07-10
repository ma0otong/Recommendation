package com.personal.recommendation.component.thread;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Recommendations;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 异步DB操作
 */
public class RecommendationDbThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RecommendationDbThread.class);

    // NewsLogs请求队列
    private static LinkedBlockingDeque<NewsLogs> newsLogsQueue = new LinkedBlockingDeque<>();

    // Recommendations请求队列
    private static LinkedBlockingDeque<Recommendations> recommendationsQueue = new LinkedBlockingDeque<>();

    // User browsed map
    public static Map<Long, List<Long>> userBrowsedMap = new HashMap<>();

    private static NewsLogsManager newsLogsManager = (NewsLogsManager) SpringContextUtil.getBean("newsLogsManager");
    private static RecommendationsManager recommendationsManager = (RecommendationsManager) SpringContextUtil.getBean("recommendationsManager");

    /**
     * 执行NewsLogs单次请求
     * @param newsLogs NewsLogs
     */
    public static void addNewsLogsRequest(NewsLogs newsLogs) {
        newsLogsQueue.add(newsLogs);
    }

    public static void addRecommendationsRequest(Recommendations recommendation){
        recommendationsQueue.add(recommendation);
    }

    /**
     * 处理请求队列
     */
    @Override
    public void run() {
        try {
            logger.info("RecommendationDbThread - " + Thread.currentThread().getName() + " executed ...");
            while (true) {
                // 处理recommendations请求
                Recommendations recommendation = recommendationsQueue.poll();
                if (recommendation != null) {
                    Recommendations rec = recommendationsManager.getRecommendationByUserAndNewsId(recommendation.getUserId(), recommendation.getNewsId());
                    if (rec != null) {
                        if (rec.getFeedback() == RecommendationConstants.RECOMMENDATION_NOT_VIEWED)
                            recommendationsManager.updateFeedBackById(rec.getId(), RecommendationConstants.RECOMMENDATION_NOT_VIEWED);
                    }else{
                        recommendationsManager.insertRecommendations(recommendation);
                    }
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 处理newsLogs请求
                NewsLogs newsLogs = newsLogsQueue.poll();
                if (newsLogs != null) {
                    NewsLogs oldNewsLog = newsLogsManager.getUserLogByUserId(newsLogs.getUserId(), newsLogs.getNewsId());
                    if (oldNewsLog != null) {
                        newsLogsManager.updateById(oldNewsLog.getId());
                    } else {
                        newsLogsManager.insertNewsLogs(newsLogs);
                    }
                    // 更新用户浏览map, 更新推荐feedback
                    recommendationsManager.updateFeedBackByUserNewsId(newsLogs.getUserId(), newsLogs.getNewsId(),
                            RecommendationConstants.RECOMMENDATION_VIEWED);
                    userBrowsedMap.put(newsLogs.getUserId(), newsLogsManager.getNewsIdsByUser(newsLogs.getUserId()));
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
