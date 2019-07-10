package com.personal.recommendation.service.impl;

import com.personal.recommendation.component.thread.RecommendationDbThread;
import com.personal.recommendation.component.thread.RecommendationNewsPoolThread;
import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.manager.*;
import com.personal.recommendation.model.*;
import com.personal.recommendation.service.NewsService;
import com.personal.recommendation.component.thread.RecommendationCalculateThread;
import com.personal.recommendation.utils.RecommendationUtil;
import com.personal.recommendation.utils.SolrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    NewsServiceImpl(NewsLogsManager newsLogsManager, UsersManager usersManager
            , RecommendationsManager recommendationsManager, NewsManager newsCrawlManager) {
        this.newsLogsManager = newsLogsManager;
        this.usersManager = usersManager;
        this.recommendationsManager = recommendationsManager;
        this.newsManager = newsCrawlManager;
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
        List<Long> recIdList = recommendationsManager.getNotViewedIdsByUserId(userId);

        // 获取用户浏览历史, 并清除对应recommendation; 筛选内容池
        List<Long> browsedIds;
        if(RecommendationDbThread.userBrowsedMap.get(userId) != null){
            browsedIds = RecommendationDbThread.userBrowsedMap.get(userId);
        }else{
            browsedIds = newsLogsManager.getNewsIdsByUser(userId);
            RecommendationDbThread.userBrowsedMap.put(userId, browsedIds);
        }

        List<News> recommendationList = new ArrayList<>();
        // 推荐不足则加入热点推荐
        if (recIdList.size() < RecommendationConstants.N) {
            // 获取用户logList
            int hotNeeded = RecommendationConstants.N - recIdList.size();
            browsedIds = browsedIds == null ? new ArrayList<>() : browsedIds;

            int hotCount = 0, hotGot = 0;
            // 遍历热点列表
            while (hotGot < hotNeeded && HotDataRecommendation.topHotNewsList.size() > hotCount) {
                // 只要1.用户没有浏览过的;2,不存在于推荐中的;3.不存在于内容池中的内容
                if (!browsedIds.contains(HotDataRecommendation.topHotNewsList.get(hotCount))
                        && !recIdList.contains(HotDataRecommendation.topHotNewsList.get(hotCount))) {
                    recIdList.add(HotDataRecommendation.topHotNewsList.get(hotCount));
                    hotGot++;
                }
                hotCount++;
            }
        }
        recIdList = recIdList.size() > RecommendationConstants.N ? recIdList.subList(0, RecommendationConstants.N) : recIdList;

        // 加入推荐
        if(!recIdList.isEmpty()) {
            recommendationList = new ArrayList<>(newsManager.getNewsByIds(recIdList));
        }

        // 构造用户新闻池
        // 创建新闻池
        Map<String, List<News>> newsPool = new HashMap<>();
        for(String module : RecommendationNewsPoolThread.NEWS_POOL_MAP.keySet()){
            int neededCount = RecommendationNewsPoolThread.NEWS_POOL_MAP.get(module).size()/RecommendationConstants.SHOWN_RATE;
            List<News> subList = new ArrayList<>();
            List<News> poolList = RecommendationNewsPoolThread.NEWS_POOL_MAP.get(module);
            int index = 0;
            while(subList.size() < neededCount && index < RecommendationNewsPoolThread.NEWS_POOL_MAP.get(module).size()){
                if(!browsedIds.contains(poolList.get(index).getId()) && !recIdList.contains(poolList.get(index).getId())){
                    subList.add(poolList.get(index));
                }
                index++;
            }
            newsPool.put(module, subList);
        }
        // 返回前端数据
        paramMap.put("newsList", RecommendationUtil.groupList(newsPool));
        paramMap.put("recommendationList", recommendationList);
        paramMap.put("moduleMap", RecommendationConstants.MODULE_MORE_STR_MAP);
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
        News news = newsManager.getNewsById(newsId);
        if (news != null) {
            // 异步更新recommendation
            Recommendations rec = new Recommendations();
            rec.setUserId(userId);
            rec.setNewsId(newsId);
            RecommendationDbThread.addRecommendationsRequest(rec);

            // 异步更新newsLogs
            NewsLogs newsLogs = new NewsLogs();
            newsLogs.setNewsModule(news.getModule());
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

            List<News> relatedNews = new ArrayList<>();
            // 从solr根据标题查询相似内容
            HttpSolrClient solr = new HttpSolrClient.Builder(SolrUtil.SOLR_URL)
                    .withConnectionTimeout(10000)
                    .withSocketTimeout(60000).build();
            try {
                relatedNews = SolrUtil.querySolrByTitle(solr, news.getTitle(), RecommendationConstants.SEARCH_NUM);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 整理html;增加相关推荐
            return RecommendationUtil.formatHtmlContent(userId, news, relatedNews);
        }
        return "";
    }

    /**
     * 获取搜索结果页面
     * @param keyword String
     * @param userId Long
     * @param paramMap Map
     */
    public void getSearch(String keyword, Long userId, Map<String, Object> paramMap){
        if(StringUtils.isNotBlank(keyword)){
            HttpSolrClient solr = new HttpSolrClient.Builder(SolrUtil.SOLR_URL)
                    .withConnectionTimeout(10000)
                    .withSocketTimeout(60000).build();
            try {
                List<News> newsList = SolrUtil.querySolrByTitle(solr, keyword, RecommendationConstants.SEARCH_NUM);
                // 返回前端数据
                paramMap.put("newsList", newsList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            paramMap.put("userId", userId);
        }
    }

    /**
     * 获取suggest
     * @param keyword String
     * @param userId Long
     * @return List
     */
    public String getSuggestData(String keyword, Long userId){
        String value = "";
        StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotBlank(keyword) && keyword.length() > 1){
            HttpSolrClient solr = new HttpSolrClient.Builder(SolrUtil.SOLR_URL)
                    .withConnectionTimeout(10000)
                    .withSocketTimeout(60000).build();
            try {
                List<News> newsList = SolrUtil.querySolrByTitle(solr, keyword, RecommendationConstants.SUGGEST_NUM);
                for(News news : newsList){
                    sb.append("<div style='background:white'><a style=\"text-decoration :none;background:white;font-size:18px;color:black;font-weight:bold;\" " +
                            "onmouseover=\"this.style.cssText='background:lightblue;text-decoration:none;font-size:18px;color:black;font-weight:bold;'\" " +
                            "onmouseout=\"this.style.cssText='background:white;text-decoration:none;font-size:18px;color:black;font-weight:bold;'\"" +
                            " href=\"/recommend/news/").append(userId).append("/").append(news.getId()).append("\" target=\"_blank\">")
                            .append(news.getTitle()).append("</a></div>");
                }
                if(StringUtils.isNotBlank(sb))
                    value = sb.substring(0, sb.length()-1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }
    
}
