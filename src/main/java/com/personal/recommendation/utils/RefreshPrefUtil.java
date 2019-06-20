package com.personal.recommendation.utils;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.model.News;
import java.util.HashMap;
import java.util.List;

public class RefreshPrefUtil {

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
                newsTFIDFMap.put(String.valueOf(news.getId()), TFIDFAnalyzer.getTfIdf(news.getContent()));
                newsTFIDFMap.put(news.getId() + RecommendationConstants.MODULE_ID_STR, news.getModuleLevel1());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsTFIDFMap;
    }

}
