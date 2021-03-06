package com.personal.recommendation.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 推荐算法常量
 */
public interface RecommendationConstants {

    // 最近浏览条数
    int RECENT_VIEWED_NUM = 50;

    // 每日衰减系数
    double DEC_CODE = 0.8;

    // 参与比较的最低权重
    double MIN_DEC_RATE = 3;

    // 热点数据天数
    int HOT_DATA_DAYS = 7;

    // 热点数据最大条数
    int MAX_HOT_NUM = 100;

    // 推荐新闻数
    int N = 20;

    // CF占比
    double CF_RATE = 0.2;

    // 基于内容最大相关条数
    int CB_MAX_NEWS = 200;

    // 用户profile个数
    int CB_PROFILE_MAX = 10;

    // score split
    String SCORE_SPLIT = ":";

    // separator
    String SEPARATOR = ",";

    // 新闻模块id
    String MODULE_ID_STR = "module_id_str";

    // 请求处理线程数
    int THREAD_NUM = 4;

    // 点击数大于此值重新计算推荐
    int MAX_RECOMMEND_VIEWS = 4;

    // rec viewed
    int RECOMMENDATION_VIEWED = 1;

    // rec not viewed
    int RECOMMENDATION_NOT_VIEWED = 0;

    // module map
    Map<String, String> MODULE_MAIN_STR_MAP = new HashMap<>();

    // module map
    Map<String, String> MODULE_MORE_STR_MAP = new HashMap<>();

    // 每个module内容池数量
    int MAIN_MODULE_POOL_SIZE = 1000;

    // 每个more module内容池数量
    int MORE_MODULE_POOL_SIZE = 500;

    // 取内容池比例
    int SHOWN_RATE = 10;

    // suggest数量
    int SUGGEST_NUM = 15;

    // 相似推荐数量
    int SEARCH_NUM = 21;

}
