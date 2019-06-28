package com.personal.recommendation.controller;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.*;
import com.personal.recommendation.service.RecommendationCalculator;
import com.personal.recommendation.service.impl.HotDataRecommendation;
import com.personal.recommendation.utils.RecommendationUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = Logger.getLogger(TestController.class);

    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;
    private final UsersManager usersManager;
    private final RecommendationsManager recommendationsManager;

    @Autowired
    TestController(NewsManager newsManager, NewsLogsManager newsLogsManager, UsersManager usersManager
            , RecommendationsManager recommendationsManager) {
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
        this.usersManager = usersManager;
        this.recommendationsManager = recommendationsManager;
    }

    @RequestMapping(value = "/index/{userId}")
    public String index(Map<String, Object> paramMap, @PathVariable("userId") Long userId) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return "error";
        List<News> newsList = new ArrayList<>();
        List<News> recommendationList = new ArrayList<>();
        // 获取推荐
        List<Recommendations> recList = recommendationsManager.getNewsByUserId(userId, RecommendationConstants.N);
        Map<String, String> moduleMap = new HashMap<>();
        // 推荐不足则加入热点推荐
        if(recList == null || recList.size() < RecommendationConstants.N) {
            int hotNeeded = RecommendationConstants.N - recList.size();
            for(int i=0;i<hotNeeded;i++){
                if(HotDataRecommendation.topHotNewsList.size() <= i)
                    break;
                Recommendations rec = new Recommendations();
                rec.setNewsId(HotDataRecommendation.topHotNewsList.get(i));
                rec.setDeriveAlgorithm(RecommendationEnum.HR.getDesc());
                recList.add(rec);
            }
        }
        if (recList != null && !recList.isEmpty()) {
            for (Recommendations rec : recList) {
                News recNews = newsManager.getNewsById(rec.getNewsId());
                recNews.setAlgorithm(rec.getDeriveAlgorithm());
                recommendationList.add(recNews);
            }
        }
        // 加入新闻池
        if (!RecommendationConstants.NEWS_POOL_LIST.isEmpty()) {
            newsList.addAll(RecommendationUtil.groupList(newsManager.getNewsByIds(RecommendationConstants.NEWS_POOL_LIST), moduleMap));
        }
        paramMap.put("newsList", newsList);
        paramMap.put("recommendationList", recommendationList);
        paramMap.put("moduleMap", moduleMap);
        paramMap.put("userId", userId);
        return "index";
    }

    @GetMapping(value = "/news/{userId}/{newsId}")
    @ResponseBody
    public String getNewsDetail(@PathVariable("userId") Long userId, @PathVariable("newsId") Long newsId) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return "User not found !";

        News news = newsManager.getNewsById(newsId);
        if (news != null) {
            // 更新recommendation
            Recommendations rec = recommendationsManager.getRecommendationByUserAndNewsId(userId, newsId);
            if(rec != null){
                recommendationsManager.updateFeedBackById(rec.getId(), RecommendationConstants.RECOMMENDATION_VIEWED);
            }
            // 更新newsLogs
            NewsLogs newsLog = newsLogsManager.getUserLogByUserId(userId, newsId);
            if (newsLog != null) {
                newsLogsManager.updateViewTimeById(new Date(), newsLog.getId());
            } else {
                NewsLogs newsLogs = new NewsLogs();
                newsLogs.setNewsModule(news.getModuleLevel1());
                newsLogs.setNewsId(news.getId());
                newsLogs.setUserId(userId);
                newsLogsManager.insertNewsLogs(newsLogs);
            }
            // 增加点击
            if(RecommendationCalculator.userLogMap.containsKey(userId)){
                int num = RecommendationCalculator.userLogMap.get(userId);
                if(num >= RecommendationConstants.MAX_RECOMMEND_VIEWS){
                    logger.info("User : " + userId + " start new recommend calculation ...");
                    // 需要重新计算推荐
                    new RecommendationCalculator().addRequest(userId);
                    RecommendationCalculator.userLogMap.put(userId, 0);
                }else{
                    num = num + 1;
                    RecommendationCalculator.userLogMap.put(userId, num);
                    logger.info("User : " + userId + " new log num : " + num);
                }
            }else{
                RecommendationCalculator.userLogMap.put(userId, 1);
            }

            return RecommendationUtil.formatHtmlContent(news);
        } else {
            return ResultEnum.FAILURE.getMsg();
        }
    }



}