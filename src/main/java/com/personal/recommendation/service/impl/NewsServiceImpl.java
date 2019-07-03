package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Recommendations;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.NewsService;
import com.personal.recommendation.service.RecommendationCalculator;
import com.personal.recommendation.utils.DateUtil;
import com.personal.recommendation.utils.RecommendationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;
    private final UsersManager usersManager;
    private final RecommendationsManager recommendationsManager;

    @Autowired
    NewsServiceImpl(NewsManager newsManager, NewsLogsManager newsLogsManager, UsersManager usersManager
            , RecommendationsManager recommendationsManager) {
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
        this.usersManager = usersManager;
        this.recommendationsManager = recommendationsManager;
    }
    @Override
    public void userNewsList(Long userId, Map<String, Object> paramMap) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return;
        List<News> newsList = new ArrayList<>();
        List<News> recommendationList = new ArrayList<>();
        // 获取推荐, 按比例整理推荐
        List<Recommendations> userRecList = recommendationsManager.getNewsByUserId(userId);
        List<Recommendations> recList = new ArrayList<>();
        List<Recommendations> cbRecList = new ArrayList<>();
        List<Recommendations> cfRecList = new ArrayList<>();
        for(Recommendations rec : userRecList){
            if(RecommendationEnum.CF.getDesc().equals(rec.getDeriveAlgorithm())){
                cfRecList.add(rec);
            }
            if(RecommendationEnum.CB.getDesc().equals(rec.getDeriveAlgorithm())){
                cbRecList.add(rec);
            }
        }
        if(cbRecList.size() >= RecommendationConstants.N * (1 - RecommendationConstants.CF_RATE)){
            recList.addAll(cbRecList.subList(0, (int) (RecommendationConstants.N * (1 - RecommendationConstants.CF_RATE))));
        }else{
            recList.addAll(cbRecList);
        }
        if(cfRecList.size() >= RecommendationConstants.N * RecommendationConstants.CF_RATE){
            recList.addAll(cfRecList.subList(0, (int) (RecommendationConstants.N * RecommendationConstants.CF_RATE)));
        }else{
            recList.addAll(cfRecList);
        }

        Map<String, String> moduleMap = new HashMap<>();
        // 推荐不足则加入热点推荐
        if (recList == null || recList.size() < RecommendationConstants.N) {
            int hotNeeded = RecommendationConstants.N - recList.size();
            List<Long> logsList = newsLogsManager.getNewsIdsByUser(userId);
            logsList = logsList == null ? new ArrayList<>() : logsList;
            int hotCount = 0, hotGot = 0;
            while (hotGot < hotNeeded && HotDataRecommendation.topHotNewsList.size() >= hotCount) {
                if (!logsList.contains(HotDataRecommendation.topHotNewsList.get(hotCount))) {
                    Recommendations rec = new Recommendations();
                    rec.setNewsId(HotDataRecommendation.topHotNewsList.get(hotCount));
                    rec.setDeriveAlgorithm(RecommendationEnum.HR.getDesc());
                    recList.add(rec);
                    hotGot++;
                }
                hotCount++;
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
    }

    @Override
    public String newsDetail(Long userId, Long newsId) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return "User not found !";

        News news = newsManager.getNewsById(newsId);
        if (news != null) {
            // 更新recommendation
            Recommendations rec = recommendationsManager.getRecommendationByUserAndNewsId(userId, newsId);
            if (rec != null) {
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
            if (RecommendationCalculator.userLogMap.containsKey(userId)) {
                int num = RecommendationCalculator.userLogMap.get(userId);
                if (num >= RecommendationConstants.MAX_RECOMMEND_VIEWS) {
                    // 需要重新计算推荐
                    new RecommendationCalculator().addRequest(userId);
                    RecommendationCalculator.userLogMap.put(userId, 0);
                } else {
                    num = num + 1;
                    RecommendationCalculator.userLogMap.put(userId, num);
                }
            } else {
                RecommendationCalculator.userLogMap.put(userId, 1);
            }
            return RecommendationUtil.formatHtmlContent(news);
        }
        return "";
    }
}
