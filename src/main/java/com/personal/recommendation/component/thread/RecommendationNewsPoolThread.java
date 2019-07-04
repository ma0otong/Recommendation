package com.personal.recommendation.component.thread;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.utils.RecommendationUtil;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 异步生成内容池
 */
public class RecommendationNewsPoolThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RecommendationNewsPoolThread.class);

    // 生成随机新闻池
    public static HashMap<Long, News> NEWS_POOL_MAP = new HashMap<>();

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
        while (true) {
            if(NEWS_POOL_MAP.size() < RecommendationConstants.MAX_NEWS_POOL_NUM){
                try {
                    Long min = newsManager.getMinId();
                    Long max = newsManager.getMaxId();
                    while (NEWS_POOL_MAP.size() < RecommendationConstants.MAX_NEWS_POOL_NUM) {
                        final int RANDOM_LIMIT = 100;
                        int limit = (RecommendationConstants.MAX_NEWS_POOL_NUM - NEWS_POOL_MAP.size()) > RANDOM_LIMIT
                                ? RANDOM_LIMIT : RecommendationConstants.MAX_NEWS_POOL_NUM - NEWS_POOL_MAP.size();
                        // 获取随机id列表
                        Set<Long> randomIdSet = RecommendationUtil.getRandomLongSet(min, max, limit);
                        List<News> newsList = newsManager.getNewsByIds(new ArrayList<>(randomIdSet));
                        for (News news : newsList) {
                            if (StringUtils.isNotBlank(news.getContent())
                                    && NEWS_POOL_MAP.size() < RecommendationConstants.MAX_NEWS_POOL_NUM) {
                                NEWS_POOL_MAP.put(news.getId(), news);
                            }
                        }
                    }
                    logger.info("News pool initialized, size : " + NEWS_POOL_MAP.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
