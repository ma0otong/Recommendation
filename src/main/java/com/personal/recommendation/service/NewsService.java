package com.personal.recommendation.service;

import java.util.Map;

/**
 * 请求处理服务
 */
public interface NewsService {

    /**
     * 处理用户浏览页请求
     * @param userId Long
     * @param paramMap Map<String, Object>
     */
    void userNewsList(Long userId, Map<String, Object> paramMap);

    /**
     * 处理详情页请求
     * @param userId Long
     * @param newsId Long
     * @return String
     */
    String newsDetail(Long userId, Long newsId);

    /**
     * 获取搜索结果
     * @param keyword String
     */
    void getSearch(String keyword, Long userId, Map<String, Object> paramMap);

    String getSuggestData(String keyword, Long userId);

}
