package com.personal.recommendation.service.recommendation;


import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 推荐任务处理接口
 */
public interface CalculatorService {

    // 请求队列
    LinkedBlockingDeque<Long> requestQueue = new LinkedBlockingDeque<>();

    // 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
    ArrayList<Long> topHotNewsList = new ArrayList<>();

    /**
     * 加入任务队列
     * @param userId 用户id
     */
    void executeInstantJob(Long userId);

}
