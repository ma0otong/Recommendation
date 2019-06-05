package com.personal.recommendation.service;

import com.personal.recommendation.model.Users;

import java.util.List;
import java.util.Set;

/**
 * 推荐算法接口
 */
public interface RecommendationAlgorithmService {


    /**
     * 针对特定用户返回推荐结果
     * @param user Users
     * @return Object
     */
    Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews);

}

