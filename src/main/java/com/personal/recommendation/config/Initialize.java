package com.personal.recommendation.config;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.service.RecommendationCalculator;
import com.personal.recommendation.service.impl.HotDataRecommendation;
import com.personal.recommendation.utils.DBConnectionUtil;
import com.personal.recommendation.utils.RecommendationUtil;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Initialize {

    private static final Logger logger = Logger.getLogger(Initialize.class);

    public static void initialize(ApplicationContext ctx){

        // 为协同过滤配置提供数据库连接
        DBConnectionUtil.URL = ctx.getEnvironment().getProperty("spring.datasource.url");
        DBConnectionUtil.USERNAME = ctx.getEnvironment().getProperty("spring.datasource.username");
        DBConnectionUtil.PASSWORD = ctx.getEnvironment().getProperty("spring.datasource.password");

        // 创建推荐请求消费线程
        ExecutorService recommendService = Executors.newFixedThreadPool(RecommendationConstants.THREAD_NUM);
        for(int i=0;i<RecommendationConstants.THREAD_NUM;i++) {
            recommendService.execute(new RecommendationCalculator());
        }

        // 初始化新闻池
        formNewsPoolList();

        // 初始化热点表
        HotDataRecommendation.formTopHotNewsList();

        // 初始化module转化表
        initModuleStrMap();

    }

    /**
     * 初始化moduleMap
     */
    private static void initModuleStrMap() {
//        RecommendationConstants.MODULE_STR_MAP.put("news_tech", "科技");
//        RecommendationConstants.MODULE_STR_MAP.put("news_car", "汽车");
//        RecommendationConstants.MODULE_STR_MAP.put("news_entertainment", "娱乐");
//        RecommendationConstants.MODULE_STR_MAP.put("news_finance", "财经");
//        RecommendationConstants.MODULE_STR_MAP.put("news_game", "游戏");
//        RecommendationConstants.MODULE_STR_MAP.put("news_sports", "体育");
//        RecommendationConstants.MODULE_STR_MAP.put("news_essay", "美文");
        RecommendationConstants.MODULE_STR_MAP.put("news_agriculture", "建筑");
        RecommendationConstants.MODULE_STR_MAP.put("news_astrology", "天文");
        RecommendationConstants.MODULE_STR_MAP.put("news_baby", "育婴");
        RecommendationConstants.MODULE_STR_MAP.put("news_career", "职业");
        RecommendationConstants.MODULE_STR_MAP.put("news_collect", "收藏");
        RecommendationConstants.MODULE_STR_MAP.put("news_comic", "漫画");
        RecommendationConstants.MODULE_STR_MAP.put("news_culture", "文化");
        RecommendationConstants.MODULE_STR_MAP.put("news_design", "设计");
        RecommendationConstants.MODULE_STR_MAP.put("news_edu", "教育");
        RecommendationConstants.MODULE_STR_MAP.put("news_fashion", "时尚");
        RecommendationConstants.MODULE_STR_MAP.put("news_food", "美食");
        RecommendationConstants.MODULE_STR_MAP.put("news_health", "健康");
        RecommendationConstants.MODULE_STR_MAP.put("news_history", "历史");
        RecommendationConstants.MODULE_STR_MAP.put("news_home", "家庭");
        RecommendationConstants.MODULE_STR_MAP.put("news_house", "房产");
        RecommendationConstants.MODULE_STR_MAP.put("news_media", "视频");
        RecommendationConstants.MODULE_STR_MAP.put("news_military", "军事");
        RecommendationConstants.MODULE_STR_MAP.put("news_pet", "宠物");
        RecommendationConstants.MODULE_STR_MAP.put("news_photography", "摄影");
        RecommendationConstants.MODULE_STR_MAP.put("news_politics", "政治");
        RecommendationConstants.MODULE_STR_MAP.put("news_society", "社会");
        RecommendationConstants.MODULE_STR_MAP.put("news_story", "故事");
        RecommendationConstants.MODULE_STR_MAP.put("news_traditional_culture", "传统");
        RecommendationConstants.MODULE_STR_MAP.put("news_travel", "旅游");
    }

    /**
     * 加载热点新闻
     */
    public static void formNewsPoolList() {
        long start = new Date().getTime();
        logger.info("Start initializing hot list .");
        try {
            RecommendationConstants.NEWS_POOL_LIST.clear();
            NewsManager newsManager = (NewsManager) SpringContextUtil.getBean("newsManager");
            Long min = newsManager.getMinId();
            Long max = newsManager.getMaxId();
            while (RecommendationConstants.NEWS_POOL_LIST.size() < RecommendationConstants.MAX_NEWS_POOL_NUM) {
                // 获取随机id列表
                List<Long> randomIdList = RecommendationUtil.getRandomLongList(min, max, 100);
                List<News> newsList = newsManager.getNewsByIds(randomIdList);
                for (News news : newsList) {
                    if (!RecommendationConstants.NEWS_POOL_LIST.contains(news.getId())
                            && StringUtils.isNotBlank(news.getContent())
                            && RecommendationConstants.NEWS_POOL_LIST.size() < RecommendationConstants.MAX_NEWS_POOL_NUM) {
                        RecommendationConstants.NEWS_POOL_LIST.add(news.getId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("Hot list initialized, list size : " + RecommendationConstants.NEWS_POOL_LIST.size() +
                ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

}
