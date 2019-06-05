package com.personal.recommendation.constants;

/**
 * 推荐算法常量
 */
public interface RecommendationConstants {

    // 设置TF-IDF提取的关键词数目
    int KEY_WORDS_NUM = 5;

    // 最近浏览条数
    int RECENT_VIEWED_NUM = 10;

    //每日衰减系数
    double DEC_CODE = 0.8;

    // 热点数据天数
    int HOT_DATA_DAYS = 10;

    // TF-IDF提取关键词数
    int TD_IDF_KEY_WORDS_NUM = 5;

    // 推荐新闻数
    int N = 10;

    // 用户id列名
    String PREF_TABLE_USER_ID = "user_id";

    // 新闻id列名
    String PREF_TABLE_NEWS_ID = "news_id";

    // 用户浏览时间列名
    String PREF_TABLE_TIME = "view_time";

    // 浏览记录表明
    String PREF_TABLE = "news_logs";

    // 新闻模块id
    String MODULE_ID = "module_id";

}
