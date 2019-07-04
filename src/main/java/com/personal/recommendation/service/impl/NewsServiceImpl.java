package com.personal.recommendation.service.impl;

import com.personal.recommendation.component.thread.RecommendationDbThread;
import com.personal.recommendation.component.thread.RecommendationNewsPoolThread;
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
import com.personal.recommendation.component.thread.RecommendationCalculateThread;
import com.personal.recommendation.utils.RecommendationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求实现类
 */
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

    /**
     * 返回用户浏览页内容
     * @param userId Long
     * @param paramMap Map<String, Object>
     */
    @Override
    public void userNewsList(Long userId, Map<String, Object> paramMap) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return;

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

        // 创建新闻池
        Map<Long, News> newsPool = new HashMap<>(RecommendationNewsPoolThread.NEWS_POOL_MAP);

        // 获取用户浏览历史, 并清除对应recommendation; 筛选内容池
        List<Long> browsedIds = newsLogsManager.getNewsIdsByUser(userId);
        List<Long> recIdList = userRecList.stream().map(Recommendations::getNewsId).collect(Collectors.toList());
        for(Long browsedId : browsedIds){
            if(recIdList.contains(browsedId)) {
                recommendationsManager.updateFeedBackById(browsedId, RecommendationConstants.RECOMMENDATION_VIEWED);
            }
            newsPool.remove(browsedId);
        }
        List<News> recommendationList;
        // 推荐不足则加入热点推荐
        if (recList.size() < RecommendationConstants.N) {
            // 获取用户logList
            int hotNeeded = RecommendationConstants.N - recList.size();
            browsedIds = browsedIds == null ? new ArrayList<>() : browsedIds;

            int hotCount = 0, hotGot = 0;
            // 遍历热点列表
            while (hotGot < hotNeeded && HotDataRecommendation.topHotNewsList.size() > hotCount) {
                // 只要1.用户没有浏览过的;2,不存在于推荐中的;3.不存在于内容池中的内容
                if (!browsedIds.contains(HotDataRecommendation.topHotNewsList.get(hotCount))
                        && !recIdList.contains(HotDataRecommendation.topHotNewsList.get(hotCount))
                        && !newsPool.containsKey(HotDataRecommendation.topHotNewsList.get(hotCount))) {
                    Recommendations rec = new Recommendations();
                    rec.setNewsId(HotDataRecommendation.topHotNewsList.get(hotCount));
                    rec.setDeriveAlgorithm(RecommendationEnum.HR.getDesc());
                    recList.add(rec);
                    hotGot++;
                }
                hotCount++;
            }
        }
        // 加入推荐
        recommendationList = new ArrayList<>(newsManager.getNewsByIds(
                recList.stream().map(Recommendations::getNewsId).collect(Collectors.toList())));
        for (Recommendations rec : recList) {
            // 删除此次推荐中存在的内容
            newsPool.remove(rec.getNewsId());
        }
        // 构造新闻池
        List<News> newsList = new ArrayList<>();
        int poolMaxSize = newsPool.size() >= RecommendationConstants.MAX_SHOWN_NEWS_POOL
                ? RecommendationConstants.MAX_SHOWN_NEWS_POOL : newsPool.size();
        int poolSize = 0;
        for (Long newsId : newsPool.keySet()) {
            if(poolSize > poolMaxSize){
                break;
            }else {
                newsList.add(newsPool.get(newsId));
                poolSize++;
            }
        }

        paramMap.put("newsList", RecommendationUtil.groupList(newsList));
        paramMap.put("recommendationList", recommendationList);
        paramMap.put("moduleMap", RecommendationConstants.MODULE_STR_MAP);
        paramMap.put("userId", userId);
    }

    /**
     * 返回内容详情
     * @param userId Long
     * @param newsId Long
     * @return String
     */
    @Override
    public String newsDetail(Long userId, Long newsId) {
        Users user = usersManager.getUserById(userId);
        if (user == null)
            return "User not found !";

        News news = newsManager.getNewsById(newsId);
        if (news != null) {
            // 异步更新recommendation
            Recommendations rec = new Recommendations();
            rec.setUserId(userId);
            rec.setNewsId(newsId);
            RecommendationDbThread.addRecommendationsRequest(rec);

            // 异步更新newsLogs
            NewsLogs newsLogs = new NewsLogs();
            newsLogs.setNewsModule(news.getModuleLevel1());
            newsLogs.setNewsId(news.getId());
            newsLogs.setUserId(userId);
            RecommendationDbThread.addNewsLogsRequest(newsLogs);

            // 增加点击
            if (RecommendationCalculateThread.userLogMap.containsKey(userId)) {
                int num = RecommendationCalculateThread.userLogMap.get(userId);
                if (num >= RecommendationConstants.MAX_RECOMMEND_VIEWS) {
                    // 需要重新计算推荐
                    RecommendationCalculateThread.addRequest(userId);
                    RecommendationCalculateThread.userLogMap.put(userId, 1);
                } else {
                    num = num + 1;
                    RecommendationCalculateThread.userLogMap.put(userId, num);
                }
            } else {
                RecommendationCalculateThread.userLogMap.put(userId, 1);
            }
            return RecommendationUtil.formatHtmlContent(news);
        }
        return "";
    }
}
