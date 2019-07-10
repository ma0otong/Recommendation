package com.personal.recommendation.config;

import com.personal.recommendation.component.thread.RecommendationDbThread;
import com.personal.recommendation.component.thread.RecommendationNewsPoolThread;
import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.component.thread.RecommendationCalculateThread;
import com.personal.recommendation.service.impl.HotDataRecommendation;
import com.personal.recommendation.utils.DBConnectionUtil;
import com.personal.recommendation.utils.SolrUtil;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 初始化类
 */
public class Initialize {

    /**
     * 初始化方法
     * @param ctx ApplicationContext
     */
    public static void initialize(ApplicationContext ctx) {

        // 为协同过滤配置提供数据库连接, 初始化dataModel
        DBConnectionUtil.URL = ctx.getEnvironment().getProperty("spring.datasource.url");
        DBConnectionUtil.USERNAME = ctx.getEnvironment().getProperty("spring.datasource.username");
        DBConnectionUtil.PASSWORD = ctx.getEnvironment().getProperty("spring.datasource.password");
        DBConnectionUtil.getDataModel();

        // Solr服务url
        SolrUtil.SOLR_URL = ctx.getEnvironment().getProperty("solr.url");

        // 初始化热点表
        HotDataRecommendation.formTopHotNewsList();

        // 初始化module转化表
        initModuleStrMap();

        // 创建推荐请求消费线程
        ExecutorService recommendService = Executors.newFixedThreadPool(RecommendationConstants.THREAD_NUM);
        for (int i = 0; i < RecommendationConstants.THREAD_NUM; i++) {
            recommendService.execute(new RecommendationCalculateThread());
        }
        recommendService.shutdown();

        // 创建异步insert线程
        ExecutorService insertService = Executors.newSingleThreadExecutor();
        insertService.execute(new RecommendationDbThread());
        insertService.shutdown();

        // 创建异步内容池线程
        ExecutorService newsPoolService = Executors.newSingleThreadExecutor();
        newsPoolService.execute(new RecommendationNewsPoolThread());
        newsPoolService.shutdown();

    }

    /**
     * 初始化moduleMap
     */
    private static void initModuleStrMap() {
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_tech", "科技");
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_car", "汽车");
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_entertainment", "娱乐");
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_finance", "财经");
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_game", "游戏");
        RecommendationConstants.MODULE_MAIN_STR_MAP.put("news_sports", "体育");

        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_baby", "育婴");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_discovery", "探索");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_essay", "美文");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_fashion", "时尚");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_food", "美食");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_history", "历史");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_military", "军事");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_regimen", "养生");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_travel", "旅游");
        RecommendationConstants.MODULE_MORE_STR_MAP.put("news_world", "国际");
    }

}
