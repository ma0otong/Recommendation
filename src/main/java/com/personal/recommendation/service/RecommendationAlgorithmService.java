package com.personal.recommendation.service;

import java.util.List;

/**
 * 推荐算法接口
 */
public interface RecommendationAlgorithmService {

    // 热点数据天数
    int HOT_DATA_DAYS = 10;

    // 协同过滤有效天数
    int CF_VALID_DAYS = -10;

    // TF-IDF提取关键词数
    int TD_IDF_KEY_WORDS_NUM = 10;

    // 推荐新闻数
    int N = 5;

    /**
     * 针对特定用户返回推荐结果
     */
    Object recommend(List<Long> users);
}

