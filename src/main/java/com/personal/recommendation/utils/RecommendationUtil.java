package com.personal.recommendation.utils;

import com.personal.recommendation.model.Recommendations;
import com.personal.recommendation.service.CalculatorService;
import org.apache.log4j.Logger;

import java.util.*;

public class RecommendationUtil {

    private static final Logger logger = Logger.getLogger(RecommendationUtil.class);

    /**
     * 获得用户的关键词列表和新闻关键词列表的匹配程度
     *
     * @param map  map
     * @param list list
     * @return double
     */
    public static double getMatchValue(CustomizedHashMap<String, Double> map, List<Keyword> list) {
        Set<String> keywordsSet = map.keySet();
        double matchValue = 0;
        for (Keyword keyword : list) {
            if (keywordsSet.contains(keyword.getName())) {
                matchValue += keyword.getTfIdfValue() * map.get(keyword.getName());
            }
        }
        return matchValue;
    }

    /**
     * 格式化map
     *
     * @param map map
     */
    public static void removeZeroItem(Map<Long, Double> map) {
        HashSet<Long> toBeDeleteItemSet = new HashSet<>();
        Iterator<Long> ite = map.keySet().iterator();
        if (ite.hasNext()) {
            do {
                Long newsId = ite.next();
                if (map.get(newsId) <= 0) {
                    toBeDeleteItemSet.add(newsId);
                }
            } while (ite.hasNext());
        }
        for (Long item : toBeDeleteItemSet) {
            map.remove(item);
        }
    }

    /**
     * 使用 Map按value进行排序
     *
     * @param oriMap map
     * @return Map<Long, Double>
     */
    public static Map<Long, Double> sortMapByValue(Map<Long, Double> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<Long, Double> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<Long, Double>> entryList = new ArrayList<>(
                oriMap.entrySet());
        entryList.sort(new MapValueComparator());

        Iterator<Map.Entry<Long, Double>> iter = entryList.iterator();
        Map.Entry<Long, Double> tmpEntry;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    /**
     * 去除数量上超过为算法设置的推荐结果上限值的推荐结果
     *
     * @param set set
     */
    private static void removeOverNews(Set<Long> set, int recNum) {
        int i = 0;
        Iterator<Long> ite = set.iterator();
        while (ite.hasNext()) {
            if (i >= recNum) {
                ite.remove();
                ite.next();
            } else {
                ite.next();
            }
            i++;
        }
    }

    public static Set<Long> resultHandle(List<Long> recommendedNews, List<Long> browsedNews,
                                    Set<Long> set, Long uid, int algorithmType, int recNum, boolean fetchFromHotList) {
        HashSet<Long> toBeRecommended = new HashSet<>(set);
        int count = toBeRecommended.size();
        // 不从hotList补充, 开始过滤
        if(!fetchFromHotList) {
            logger.info("ToBeRecommended size (before filter) : " + toBeRecommended.size());
            // 过滤掉已经推荐过的新闻
            toBeRecommended.removeAll(recommendedNews);
            logger.info("ToBeRecommended size (after recommended filter) : " + toBeRecommended.size());
            // 过滤掉用户已经看过的新闻
            toBeRecommended.removeAll(browsedNews);
            logger.info("ToBeRecommended size (after browsed news filter) : " + toBeRecommended.size());
            // 如果可推荐新闻数目超过了recNum, 则去掉一部分多余的可推荐新闻
            if (toBeRecommended.size() > recNum) {
                RecommendationUtil.removeOverNews(toBeRecommended, recNum);
                logger.info("ToBeRecommended size (after oversize filter) : " + toBeRecommended.size());
            }

            // 写入recommendation结果表
            for (Long recId : toBeRecommended) {
                Recommendations recommendation = new Recommendations();
                recommendation.setDeriveAlgorithm(algorithmType);
                recommendation.setUserId(uid);
                recommendation.setNewsId(recId);
//            recommendationsManager.insertRecommendations(recommendation);
            }
        }else {
            // 算法推荐数量不够, 从热点新闻中补充
            if (recNum > count) {
                // 从热点新闻中补充
                int replenish = recNum - count;
                int replenished = 0, index = 0;
                while (replenished < replenish) {
                    Long repId = CalculatorService.topHotNewsList.get(index++);
                    if (repId != null && repId != 0) {
                        if (recommendedNews.contains(repId) || browsedNews.contains(repId)) {
                            logger.info("NewsId : " + repId + " from hot list was browsed or recommended, skip .");
                        } else {
                            if (!toBeRecommended.contains(repId)) {
                                toBeRecommended.add(repId);
                                replenished++;
                            } else {
                                logger.info("Recommendation already added, skip .");
                            }
                        }
                    } else {
                        logger.info("Hot list replenish ended, list size : " + CalculatorService.topHotNewsList.size());
                        break;
                    }
                }
            }
        }
        logger.info("Recommendation finished ! Total recommendation size : " + toBeRecommended.size());
        return toBeRecommended;
    }

}
