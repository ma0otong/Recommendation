package com.personal.recommendation.component.thread;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.recommendation.AlgorithmFactory;
import com.personal.recommendation.service.recommendation.CalculatorService;
import com.personal.recommendation.utils.DateUtil;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 推荐请求处理线程类
 */
public class RecommendServiceThread implements Runnable{

    private static final Logger logger = Logger.getLogger(RecommendServiceThread.class);

    private final UsersManager usersManager = (UsersManager)SpringContextUtil.getBean("usersManager");
    private final NewsManager newsManager = (NewsManager)SpringContextUtil.getBean("newsManager");
    private final NewsLogsManager newsLogsManager = (NewsLogsManager)SpringContextUtil.getBean("newsLogsManager");
    private final RecommendationsManager recommendationsManager =
            (RecommendationsManager)SpringContextUtil.getBean("recommendationsManager");

    @Override
    public void run() {
        try {
            logger.info("RecommendServiceThread - " + Thread.currentThread().getName() + " executed ...");
            while (true) {
                Long userId = CalculatorService.requestQueue.poll();
                if (userId != null) {
                    long start = new Date().getTime();
                    logger.info("Recommendation calculation start at " + start + ", userId : " + userId);

                    Users user = usersManager.getUserById(userId);
                    // 用户不存在
                    if (user != null) {
                        // prefList为空则初始化prefList
                        if (user.getPrefList() == null || user.getPrefList().isEmpty()) {
                            usersManager.initializePrefList(user, newsManager.getModuleLevel());
                        }

                        // 结果map
                        Set<Long> toBeRecommended = new HashSet<>();

                        // 让热点新闻推荐器预先生成今日的热点新闻
                        formTodayTopHotNewsList();

                        Set<Long> cfRecommended = new HashSet<>();
                        Set<Long> cbRecommended = new HashSet<>();
                        Set<Long> hrRecommended;

                        // 已经推荐过的新闻
                        List<Long> recommendedNews = recommendationsManager.getNewsIdByUserId(userId);
                        // 已经看过的新闻
                        List<Long> browsedNews = newsLogsManager.getNewsIdByUserId(userId);

                        // 若无browsedNews直接跳过
                        if (!browsedNews.isEmpty()) {
                            // 先计算协同过滤
                            int neededNum = (int) (RecommendationConstants.CF_RATE * RecommendationConstants.N);
                            toBeRecommended = AlgorithmFactory.getHandler(
                                    RecommendationEnum.CF.getCode()).recommend(user, neededNum, recommendedNews, browsedNews);
                            cfRecommended = new HashSet<>(toBeRecommended);
                            // 再用基于内容推荐, 若无browsedNews直接跳过
                            neededNum = neededNum - toBeRecommended.size() +
                                    (int) (RecommendationConstants.CB_RATE * RecommendationConstants.N);
                            toBeRecommended.addAll(AlgorithmFactory.getHandler(RecommendationEnum.CB.getCode()).recommend(
                                    user, neededNum, recommendedNews, browsedNews));
                            cbRecommended = new HashSet<>(toBeRecommended);
                            cbRecommended.removeAll(cfRecommended);
                        }

                        // 最后用hotList补充
                        if (toBeRecommended.size() < RecommendationConstants.N) {
                            toBeRecommended.addAll(AlgorithmFactory.getHandler(RecommendationEnum.HR.getCode()).recommend(user,
                                    RecommendationConstants.N - toBeRecommended.size(), recommendedNews, browsedNews));
                        }
                        hrRecommended = new HashSet<>(toBeRecommended);
                        hrRecommended.removeAll(cfRecommended);
                        hrRecommended.removeAll(cbRecommended);
                        logger.info(String.format("Total recommended size : %s, fromCF : %s, fromCB : %s, fromHR : %s .",
                                toBeRecommended.size(), cfRecommended.size(), cbRecommended.size(), hrRecommended.size()));

                        long end = new Date().getTime();
                        logger.info("Calculation finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
                    }
                } else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载热点新闻
     */
    private void formTodayTopHotNewsList() {
        if (!CalculatorService.topHotNewsList.isEmpty()) {
            return;
        }
        logger.info("Start initializing hot list .");
        CalculatorService.topHotNewsList.clear();
        ArrayList<Long> hotNewsTobeRecommended = new ArrayList<>();
        try {
            // 热点新闻的有效时间
            List<NewsLogs> newsLogsList = newsLogsManager.getHotNews(
                    DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS), RecommendationConstants.HOT_DATA_DAYS);
            for (NewsLogs newsLog : newsLogsList) {
                if (newsLog != null)
                    hotNewsTobeRecommended.add(newsLog.getNewsId());
            }
            CalculatorService.topHotNewsList.addAll(hotNewsTobeRecommended);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Hot list initialized, list size : " + CalculatorService.topHotNewsList.size());
    }

}
