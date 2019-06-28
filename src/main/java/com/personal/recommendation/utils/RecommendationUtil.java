package com.personal.recommendation.utils;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.model.News;
import org.ansj.app.keyword.Keyword;
import org.apache.log4j.Logger;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings("unused")
public class RecommendationUtil {

    private static final Logger logger = Logger.getLogger(RecommendationUtil.class);

    /**
     * 获得用户的关键词列表和新闻关键词列表的匹配程度
     *
     * @param map  map
     * @param list list
     * @return double
     */
    public static double getMatchValue(LinkedHashMap<String, Double> map, List<Keyword> list) {
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
    public static LinkedHashMap<Long, Double> sortLDMapByValue(LinkedHashMap<Long, Double> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        LinkedHashMap<Long, Double> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<Long, Double>> entryList = new ArrayList<>(
                oriMap.entrySet());
        entryList.sort(new LongDoubleComparator());

        Iterator<Map.Entry<Long, Double>> iter = entryList.iterator();
        Map.Entry<Long, Double> tmpEntry;

        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    /**
     * 使用 Map按value进行排序
     *
     * @param oriMap map
     * @return Map<String, Integer>
     */
    public static CustomizedHashMap<String, Double> sortSDMapByValue(Map<String, Double> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        CustomizedHashMap<String, Double> sortedMap = new CustomizedHashMap<>();
        List<Map.Entry<String, Double>> entryList = new ArrayList<>(
                oriMap.entrySet());
        entryList.sort(new StringDoubleComparator());

        Iterator<Map.Entry<String, Double>> iter = entryList.iterator();
        Map.Entry<String, Double> tmpEntry;
        int maxSize = 1;
        while (iter.hasNext() && maxSize < RecommendationConstants.CB_MAX_MODULE_KEYWORD_SIZE) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
            maxSize++;
        }

        return sortedMap;
    }

    /**
     * 去除数量上超过为算法设置的推荐结果上限值的推荐结果
     *
     * @param set set
     */
    public static void removeOverNews(Set<Long> set, int recNum) {
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

    /**
     * 将所有当天被浏览过的新闻提取出来，以便进行TF-IDF求值操作，以及对用户喜好关键词列表的更新。
     *
     * @return TF-IDF计算后的结果
     */
    public static HashMap<String, Object> getNewsTFIDFMap(List<News> newsList) {
        HashMap<String, Object> newsTFIDFMap = new HashMap<>();
        try {
            // 提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
            for (News news : newsList) {
                List<Keyword> keywords = TFIDFAnalyzer.getTfIdf(news.getContent());
                newsTFIDFMap.put(String.valueOf(news.getId()), keywords);
                newsTFIDFMap.put(news.getId() + RecommendationConstants.MODULE_ID_STR, news.getModuleLevel1());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsTFIDFMap;
    }

    /**
     * List按news module分类
     *
     * @param list List<News>
     * @return List<News>
     */
    public static List<News> groupList(List<News> list, Map<String, String> moduleMap) {
        List<News> newList = new ArrayList<>();
        Map<String, List<News>> map = list.stream().collect(groupingBy(News::getModuleLevel1));
        for (String key : map.keySet()) {
            for (News news : map.get(key)) {
                newList.add(news);
                String module = RecommendationConstants.MODULE_STR_MAP.get(news.getModuleLevel1());
                if(module != null) {
                    if (!moduleMap.containsKey(module)) {
                        moduleMap.put(news.getModuleLevel1(), module);
                    }
                }
            }
        }
        return newList;
    }

    /**
     * 根据范围随机获取count个随机数
     *
     * @param min   Long
     * @param max   Long
     * @param count int
     * @return List<Long>
     */
    public static List<Long> getRandomLongList(Long min, Long max, int count) {
        int minInt = min.intValue();
        int maxInt = max.intValue();
        Random random = new Random();
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int num = new Random().nextInt(maxInt - minInt) + minInt;
            list.add(Long.parseLong(String.valueOf(num)));
        }
        return list;
    }

    public static String formatHtmlContent(News news) {
        return "<html><head><h1 style='text-align:center'>" +
                news.getTitle() +
                "</h1><h4 style='color:gray;text-align:right'>" +
                DateUtil.formatDate(news.getNewsTime()) +
                "</h4></head><meta charset=\"UTF-8\"><title>" +
                news.getTitle() +
                "</title></head><body style='width:80%;margin-left:10%'>" +
                news.getContent() +
                "</body><br/>" +
                "<p style='color:gray;text-align:right'>文章来源:<a href=" +
                news.getUrl() +">" + news.getUrl() + "</a></p></html>";
    }

}
