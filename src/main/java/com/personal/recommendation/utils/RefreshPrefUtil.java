package com.personal.recommendation.utils;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RefreshPrefUtil {


    /**
     * 提取出当天有浏览行为的用户及其各自所浏览过的新闻id列表
     *
     * @return 用户历史行为结果
     */
    public static HashMap<Long, ArrayList<Long>> getBrowsedHistoryMap(Long userId, List<NewsLogs> newsLogsList) {
        HashMap<Long, ArrayList<Long>> userBrowsedMap = new HashMap<>();
        try {
            for (NewsLogs newslogs : newsLogsList) {
                if (userBrowsedMap.containsKey(newslogs.getUserId())) {
                    userBrowsedMap.get(userId).add(newslogs.getNewsId());
                } else {
                    userBrowsedMap.put(userId, new ArrayList<>());
                    userBrowsedMap.get(userId).add(newslogs.getNewsId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userBrowsedMap;
    }


    /**
     * 获得浏览过的新闻的集合
     *
     * @return 浏览过的新闻
     */
    public static List<Long> getBrowsedNewsSet(HashMap<Long, ArrayList<Long>> browsedMap) {
        List<Long> newsIdList = new ArrayList<>();
        for (Long aLong : browsedMap.keySet()) {
            newsIdList.addAll(browsedMap.get(aLong));
        }
        return newsIdList;
    }

    /**
     * 将所有当天被浏览过的新闻提取出来，以便进行TF-IDF求值操作，以及对用户喜好关键词列表的更新。
     *
     * @return TF-IDF计算后的结果
     */
    public static HashMap<String, Object> getNewsTFIDFMap(List<News> newsList) {
        HashMap<String, Object> newsTFIDFMap = new HashMap<>();
        try {
            // 提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
            for (News news : newsList) {
                newsTFIDFMap.put(String.valueOf(news.getId()), TFIDFAnalyzer.getTfIde(news.getTitle(),
                        RecommendationConstants.KEY_WORDS_NUM));
                newsTFIDFMap.put(news.getId() + RecommendationConstants.MODULE_ID, news.getModuleId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsTFIDFMap;
    }

}
