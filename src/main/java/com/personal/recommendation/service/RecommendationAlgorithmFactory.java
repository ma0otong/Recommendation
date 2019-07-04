package com.personal.recommendation.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 算法工厂
 */
public class RecommendationAlgorithmFactory {

    private static Map<Integer, RecommendationAlgorithmService> ALGORITHM_HANDLER_MAP = new HashMap<>();

    /**
     * 加入算法map
     * @param algorithmType Integer
     * @param handler RecommendationAlgorithmService
     */
    public static void addHandler(Integer algorithmType, RecommendationAlgorithmService handler) {
        ALGORITHM_HANDLER_MAP.put(algorithmType, handler);
    }

    /**
     * 返回算法
     * @param algorithmType Integer
     * @return RecommendationAlgorithmService
     */
    public static RecommendationAlgorithmService getHandler(Integer algorithmType) {
        return ALGORITHM_HANDLER_MAP.get(algorithmType);
    }

}
