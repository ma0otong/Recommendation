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
     * @param userIDList 用户id列表
     * @return Object
     */
    BaseRsp executeInstantJob(List<Long> userIDList, int type);

    /**
     * 生成热点信息表
     */
    void formTodayTopHotNewsList();

    /**
     * 按照推荐频率调用的方法，一般为一天执行一次。
     * 定期根据前一天所有用户的浏览记录，在对用户进行喜好关键词列表TF-IDF值衰减的后，
     * 将用户前一天看的新闻的关键词及相应TF-IDF值更新到列表中去。
     * @param userIdsCol 用户ids
     */
    void refresh(List<Long> userIdsCol);

    /**
     * 所有用户的喜好关键词列表TF-IDF值随时间进行自动衰减更新
     */
    @SuppressWarnings("unused")
    void autoDecRefresh();

    /**
     * 所有用户的喜好关键词列表TF-IDF值随时间进行自动衰减更新
     */
    void autoDecRefresh(List<Long> userIdsCol);

    /**
     * 提取出当天有浏览行为的用户及其各自所浏览过的新闻id列表
     * @return HashMap<Long, ArrayList<Long>>
     */
    HashMap<Long, ArrayList<Long>> getBrowsedHistoryMap(List<Long> userList);


    /**
     * 获得浏览过的新闻的集合
     * @return HashSet<Long>
     */
    List<Long> getBrowsedNewsSet(HashMap<Long, ArrayList<Long>> browsedMap);

    /**
     * 将所有当天被浏览过的新闻提取出来，以便进行TF-IDF求值操作，以及对用户喜好关键词列表的更新。
     * @return HashMap<String, Object>
     */
    HashMap<String, Object> getNewsTFIDFMap(List<Long> userList, HashMap<Long, ArrayList<Long>> browsedMap);

}
