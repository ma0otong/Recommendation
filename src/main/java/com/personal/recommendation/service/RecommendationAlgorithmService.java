package com.personal.recommendation.service;

import com.personal.recommendation.model.BaseRsp;

import java.util.List;

/**
 * 推荐算法接口
 */
public interface RecommendationAlgorithmService {


    /**
     * 针对特定用户返回推荐结果
     * @param users List<Long>
     * @return Object
     */
    BaseRsp recommend(List<Long> users);
}

