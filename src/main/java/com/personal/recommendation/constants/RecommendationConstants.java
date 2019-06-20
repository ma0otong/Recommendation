package com.personal.recommendation.constants;

/**
 * 推荐算法常量
 */
public interface RecommendationConstants {

    // 设置TF-IDF提取的关键词数目
    int KEY_WORDS_NUM = 10;

    // 每个module的关键字数量
    int CB_MAX_MODULE_KEYWORD_SIZE = 10;

    // 最近浏览条数
    int RECENT_VIEWED_NUM = 50;

    // 每日衰减系数
    double DEC_CODE = 0.8;

    // 参与比较的最低权重
    double MIN_DEC_RATE = 3;

    // 热点数据天数
    int HOT_DATA_DAYS = 10;

    // 热点数据最大条数
    int MAX_HOT_NUM = 1000;

    // 推荐新闻数
    int N = 10;

    // CF占比
    double CF_RATE = 0.5;

    // CB占比
    double CB_RATE = 0.5;

    // 基于内容最大相关条数
    int CB_MAX_NEWS = 1000;

    // 用户profile个数
    int CB_PROFILE_MAX = 10;

    // score split
    String SCORE_SPLIT = ":";

    // separator
    String SEPARATOR = ",";

    // 用户id列名
    String PREF_TABLE_USER_ID = "user_id";

    // 新闻id列名
    String PREF_TABLE_NEWS_ID = "news_id";

    // 用户浏览时间列名
    String PREF_TABLE_TIME = "view_time";

    // 浏览记录表明
    String PREF_TABLE = "news_logs";

    // 新闻模块id
    String MODULE_ID_STR = "module_id_str";

    // 请求处理线程数
    int THREAD_NUM = 4;

}
