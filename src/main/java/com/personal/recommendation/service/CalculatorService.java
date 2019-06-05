package com.personal.recommendation.service;

import com.personal.recommendation.model.BaseRsp;

import java.util.*;

/**
 * 推荐任务处理接口
 */
public interface CalculatorService {

    // 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
    ArrayList<Long> topHotNewsList = new ArrayList<>();

    /**
     * 执行单个任务
     * @param userId 用户id
     * @return Object
     */
    BaseRsp executeInstantJob(Long userId);

}
