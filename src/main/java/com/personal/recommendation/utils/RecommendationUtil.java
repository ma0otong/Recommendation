package com.personal.recommendation.utils;

import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import org.ansj.app.keyword.Keyword;

import java.util.*;

public class RecommendationUtil {

    /**
     * 获得用户的关键词列表和新闻关键词列表的匹配程度
     * @param map map
     * @param list list
     * @return double
     */
    public static double getMatchValue(CustomizedHashMap<String, Double> map, List<Keyword> list) {
        Set<String> keywordsSet = map.keySet();
        double matchValue = 0;
        for (Keyword keyword : list) {
            if (keywordsSet.contains(keyword.getName())) {
                matchValue += keyword.getScore() * map.get(keyword.getName());
            }
        }
        return matchValue;
    }

    /**
     * 格式化map
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
     *  @param set set
     *
     */
    private static void removeOverNews(Set<Long> set) {
        int i = 0;
        Iterator<Long> ite = set.iterator();
        while (ite.hasNext()) {
            if (i >= RecommendationAlgorithmService.N) {
                ite.remove();
                ite.next();
            } else {
                ite.next();
            }
            i++;
        }
    }

    public static int resultHandle(RecommendationsManager recommendationsManager, NewsLogsManager newsLogsManager,
                                    NewsManager newsManager, UsersManager usersManager, Set<Long> toBeRecommended,
                                   Long uid, int count, Map<Users, List<News>> resultMap){

        // 过滤掉已经推荐过的新闻
        toBeRecommended.removeAll(recommendationsManager.getFilterRecordNews(uid));
        // 过滤掉用户已经看过的新闻
        toBeRecommended.removeAll(newsLogsManager.getFilterBrowsedNews(uid));
        // 如果可推荐新闻数目超过了系统默认为CB算法设置的单日推荐上限数（N），则去掉一部分多余的可推荐新闻，剩下的N个新闻才进行推荐
        if (toBeRecommended.size() > RecommendationAlgorithmService.N) {
            RecommendationUtil.removeOverNews(toBeRecommended);
        }
        count += toBeRecommended.size();
        // 暂时不写入recommendation结果表
        // 保存结果
        List<News> resultList = newsManager.getNewsByIds(new ArrayList<>(toBeRecommended));
        Users user = usersManager.getUserById(uid);
        resultMap.put(user, resultList);

        return count;

    }

}
