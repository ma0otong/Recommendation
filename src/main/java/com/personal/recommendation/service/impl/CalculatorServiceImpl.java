package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.service.CalculatorService;
import com.personal.recommendation.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务处理接口实现类
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {

    private static final Logger logger = Logger.getLogger(CalculatorServiceImpl.class);

    private final UsersManager usersManager;
    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;
    private final RecommendationsManager recommendationsManager;


    @Autowired
    public CalculatorServiceImpl(NewsLogsManager newsLogsManager,UsersManager usersManager,
                                 RecommendationsManager recommendationsManager, NewsManager newsManager) {
        this.newsLogsManager = newsLogsManager;
        this.usersManager = usersManager;
        this.recommendationsManager = recommendationsManager;
        this.newsManager = newsManager;
    }

    @Override
    public BaseRsp executeInstantJob(Long userId) {
        long start = new Date().getTime();
        logger.info("Recommendation calculation start at " + start + ", userId : " + userId);

        Users user = usersManager.getUserById(userId);
        // 用户不存在
        if(user == null){
            return new BaseRsp(ResultEnum.PARA_ERR, ResultEnum.PARA_ERR.getMsg());
        }
        // prefList为空则初始化prefList
        usersManager.initializePrefList(user, newsManager.getModuleIdCount());

        // 让热点新闻推荐器预先生成今日的热点新闻
        formTodayTopHotNewsList();
        // 结果map
        Set<Long> toBeRecommended;

        // 已经推荐过的新闻
        List<Long> recommendedNews = recommendationsManager.getNewsIdByUserId(userId);
        // 已经看过的新闻
        List<Long> browsedNews = newsLogsManager.getNewsIdByUserId(userId);

        // 先计算协同过滤推荐
        toBeRecommended = AlgorithmFactory.getHandler(
                RecommendationEnum.CF.getCode()).recommend(user, RecommendationConstants.N,
                recommendedNews, browsedNews);
        int cf_count = toBeRecommended.size();
        // 再用基于内容推荐
        if (toBeRecommended.size() < RecommendationConstants.N) {
            toBeRecommended.addAll(AlgorithmFactory.getHandler(RecommendationEnum.CB.getCode()).recommend(user,
                    RecommendationConstants.N - toBeRecommended.size(), recommendedNews, browsedNews));
        }
        int cb_count = toBeRecommended.size() - cf_count;
        // 最后用hotList补充
        if (toBeRecommended.size() < RecommendationConstants.N) {
            toBeRecommended.addAll(AlgorithmFactory.getHandler(RecommendationEnum.HR.getCode()).recommend(user,
                    RecommendationConstants.N - toBeRecommended.size(), recommendedNews, browsedNews));
        }
        int hr_count = toBeRecommended.size() - cf_count - cb_count;

        logger.info(String.format("Total recommended size : %s, fromCF : %s, fromCB : %s, fromHR : %s .",
                toBeRecommended.size(), cf_count, cb_count, hr_count));

        // 获取resultMap
        Map<Long, List<News>> resultMap = new HashMap<>();
        List<News> newsList = new ArrayList<>();
        for(Long id : toBeRecommended){
            newsList.add(newsManager.getNewsById(id));
        }
        resultMap.put(userId, newsList);

        long end = new Date().getTime();
        logger.info("Calculation finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");

        return new BaseRsp(ResultEnum.SUCCESS, resultMap);

    }

    private void formTodayTopHotNewsList() {
        if (!topHotNewsList.isEmpty()) {
            return;
        }
        logger.info("Start initializing hot list .");
        topHotNewsList.clear();
        ArrayList<Long> hotNewsTobeRecommended = new ArrayList<>();
        try {
            // 热点新闻的有效时间
            List<NewsLogs> newsLogsList = newsLogsManager.getHotNews(
                    DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS));
            for (NewsLogs newsLog : newsLogsList) {
                if (newsLog != null)
                    hotNewsTobeRecommended.add(newsLog.getNewsId());
            }
            topHotNewsList.addAll(hotNewsTobeRecommended);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Hot list initialized, list size : " + topHotNewsList.size());
    }

}
