package com.personal.recommendation.component.thread;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 异步生成内容池
 */
public class RecommendationNewsPoolThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RecommendationNewsPoolThread.class);

    // 生成随机新闻池
    public static HashMap<String, List<News>> NEWS_POOL_MAP = new HashMap<>();

    private static NewsManager newsManager = (NewsManager) SpringContextUtil.getBean("newsManager");


    /**
     * 清空内容池
     */
    public static void clear(){
        NEWS_POOL_MAP.clear();
    }

    /**
     * 生成内容池
     */
    @Override
    public void run() {
        while(true) {
            try {
                if(NEWS_POOL_MAP.isEmpty()) {
                    long start = new Date().getTime();
                    int size = 0;
                    for (String module : RecommendationConstants.MODULE_MAIN_STR_MAP.keySet()) {
                        List<News> list = newsManager.getNewsByModuleLimit(module, RecommendationConstants.MAIN_MODULE_POOL_SIZE);
                        NEWS_POOL_MAP.put(module, list);
                        size += list.size();
                    }
                    for (String module : RecommendationConstants.MODULE_MORE_STR_MAP.keySet()) {
                        List<News> list = newsManager.getNewsByModuleLimit(module, RecommendationConstants.MAIN_MODULE_POOL_SIZE);
                        NEWS_POOL_MAP.put(module, newsManager.getNewsByModuleLimit(module, RecommendationConstants.MORE_MODULE_POOL_SIZE));
                        size += list.size();
                    }
                    logger.info("News pool initialized, size : " + size + ", time cost : "
                            + (double) (new Date().getTime() - start) / 1000 + "s.");
                }else{
                    Thread.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
