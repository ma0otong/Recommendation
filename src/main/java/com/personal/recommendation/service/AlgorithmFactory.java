package com.personal.recommendation.service;

import com.personal.recommendation.service.RecommendationAlgorithmService;

import java.util.HashMap;
import java.util.Map;

/**
 * 算法工厂
 */
public class AlgorithmFactory {

    private static Map<Integer, RecommendationAlgorithmService> ALGORITHM_HANDLER_MAP = new HashMap<>();

    public static void addHandler(Integer algorithmType, RecommendationAlgorithmService handler) {
        ALGORITHM_HANDLER_MAP.put(algorithmType, handler);
    }

    public static RecommendationAlgorithmService getHandler(Integer algorithmType) {
        return ALGORITHM_HANDLER_MAP.get(algorithmType);
    }

}
