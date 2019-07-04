package com.personal.recommendation.component.thread;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.Recommendations;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmFactory;
import com.personal.recommendation.service.impl.HotDataRecommendation;
import com.personal.recommendation.utils.RecommendationUtil;
import com.personal.recommendation.utils.SpringContextUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 异步推荐请求处理
 */
public class RecommendationCalculateThread implements Runnable {

    private static final Logger logger = Logger.getLogger(RecommendationCalculateThread.class);

    // 请求队列
    private static LinkedBlockingDeque<Long> requestQueue = new LinkedBlockingDeque<>();

    // 用户浏览数据
    public static ConcurrentHashMap<Long, Integer> userLogMap = new ConcurrentHashMap<>();

    private static UsersManager usersManager = (UsersManager) SpringContextUtil.getBean("usersManager");
    private static NewsManager newsManager = (NewsManager) SpringContextUtil.getBean("newsManager");
    private static NewsLogsManager newsLogsManager = (NewsLogsManager) SpringContextUtil.getBean("newsLogsManager");
    private static RecommendationsManager recommendationsManager =
            (RecommendationsManager) SpringContextUtil.getBean("recommendationsManager");

    /**
     * 执行单次计算
     *
     * @param userId 用户id
     */
    public static void addRequest(Long userId) {
        logger.info("Add recommendation calculation task for user : " + userId);
        requestQueue.add(userId);
    }

    /**
     * 消费request队列
     */
    @Override
    public void run() {
        try {
            logger.info("RecommendationCalculateThread - " + Thread.currentThread().getName() + " executed ...");
            while (true) {
                Long userId = requestQueue.poll();
                if (userId != null) {
                    long start = new Date().getTime();
                    Users user = usersManager.getUserById(userId);
                    // 用户存在
                    if (user != null) {
                        // prefList为空则初始化prefList
                        if (user.getPrefList() == null || user.getPrefList().isEmpty()) {
                            usersManager.initializePrefList(user, newsManager.getModuleLevel());
                        }

                        // 结果map
                        Set<Long> toBeRecommended = new HashSet<>();

                        Set<Long> cfRecommended = new HashSet<>();
                        Set<Long> cbRecommended = new HashSet<>();
                        Set<Long> hrRecommended;

                        // 已经推荐过的新闻
                        List<Long> recommendedNews = recommendationsManager.getNewsIdByUserId(userId);
                        // 已经看过的新闻
                        List<Long> browsedNews = newsLogsManager.getNewsIdByUserId(userId);

                        // 若无browsedNews直接跳过
                        if (!browsedNews.isEmpty()) {
                            // 先计算协同过滤
                            int neededNum = (int) (RecommendationConstants.CF_RATE * RecommendationConstants.N);
                            toBeRecommended = RecommendationAlgorithmFactory.getHandler(
                                    RecommendationEnum.CF.getCode()).recommend(user, neededNum, recommendedNews, browsedNews);
                            cfRecommended = new HashSet<>(toBeRecommended);
                            // 再用基于内容推荐, 若无browsedNews直接跳过
                            neededNum = neededNum - toBeRecommended.size() +
                                    (int) ((1 - RecommendationConstants.CF_RATE) * RecommendationConstants.N);
                            toBeRecommended.addAll(RecommendationAlgorithmFactory.getHandler(RecommendationEnum.CB.getCode()).recommend(
                                    user, neededNum, recommendedNews, browsedNews));
                            cbRecommended = new HashSet<>(toBeRecommended);
                            cbRecommended.removeAll(cfRecommended);
                        }

                        // 最后用hotList补充
                        if (toBeRecommended.size() < RecommendationConstants.N && !HotDataRecommendation.topHotNewsList.isEmpty()) {
                            toBeRecommended.addAll(RecommendationAlgorithmFactory.getHandler(RecommendationEnum.HR.getCode()).recommend(user,
                                    RecommendationConstants.N - toBeRecommended.size(), recommendedNews, browsedNews));
                        }
                        hrRecommended = new HashSet<>(toBeRecommended);
                        hrRecommended.removeAll(cfRecommended);
                        hrRecommended.removeAll(cbRecommended);
                        logger.info(String.format("Total recommended size : %s, fromCF : %s, fromCB : %s, fromHR : %s ," +
                                        " time cost : %s s, user : %s, requestQueue size : %s.",
                                toBeRecommended.size(), cfRecommended.size(), cbRecommended.size(), hrRecommended.size(),
                                (double) (new Date().getTime() - start) / 1000, userId, requestQueue.size()));
                    }
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理计算结果
     *
     * @param recommendedNews  List<Long>
     * @param browsedNews      List<Long>
     * @param set              Set<Long>
     * @param uid              Long
     * @param algorithmType    int
     * @param recNum           int
     * @param fetchFromHotList boolean
     * @return Set<Long>
     */
    public static Set<Long> resultHandle(List<Long> recommendedNews, List<Long> browsedNews,
                                         Set<Long> set, Long uid, String algorithmType, int recNum, boolean fetchFromHotList) {
        HashSet<Long> toBeRecommended = new HashSet<>(set);
        int count = toBeRecommended.size();
        // 不从hotList补充, 开始过滤
        if (!fetchFromHotList) {
            // 过滤掉已经推荐过的新闻
            toBeRecommended.removeAll(recommendedNews);
            // 过滤掉用户已经看过的新闻
            toBeRecommended.removeAll(browsedNews);
            // 如果可推荐新闻数目超过了recNum, 则去掉一部分多余的可推荐新闻
            if (toBeRecommended.size() > recNum) {
                RecommendationUtil.removeOverNews(toBeRecommended, recNum);
            }

            // 写入recommendation结果表
            for (Long recId : toBeRecommended) {
                Recommendations recommendation = new Recommendations();
                recommendation.setDeriveAlgorithm(algorithmType);
                recommendation.setUserId(uid);
                recommendation.setNewsId(recId);
                recommendation.setFeedback(RecommendationConstants.RECOMMENDATION_NOT_VIEWED);
                RecommendationDbThread.addRecommendationsRequest(recommendation);
            }
        } else {
            // 算法推荐数量不够, 从热点新闻中补充
            if (recNum > count) {
                // 从热点新闻中补充
                int replenish = recNum - count;
                int replenished = 0, index = 0;
                while (replenished < replenish) {
                    if (index >= HotDataRecommendation.topHotNewsList.size()) {
                        break;
                    }
                    Long repId = HotDataRecommendation.topHotNewsList.get(index++);
                    if (repId != null && repId != 0) {
                        if (!recommendedNews.contains(repId) && !browsedNews.contains(repId)
                                && !toBeRecommended.contains(repId)) {
                            toBeRecommended.add(repId);
                            replenished++;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        return toBeRecommended;
    }

}

