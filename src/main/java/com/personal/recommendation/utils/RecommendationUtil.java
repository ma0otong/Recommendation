package com.personal.recommendation.utils;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.model.News;
import org.ansj.app.keyword.Keyword;

import java.util.*;

/**
 * 推荐算法工具类
 */
public class RecommendationUtil {

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
     * @return Map<String, Integer>
     */
    public static CustomizedHashMap<String, Double> sortSDMapByValue(Map<String, Double> oriMap) {
        CustomizedHashMap<String, Double> sortedMap = new CustomizedHashMap<>();
        if (oriMap == null || oriMap.isEmpty()) {
            return sortedMap;
        }

        List<Map.Entry<String, Double>> entryList = new ArrayList<>(
                oriMap.entrySet());
        entryList.sort(new StringDoubleComparator());
        Iterator<Map.Entry<String, Double>> iter = entryList.iterator();
        Map.Entry<String, Double> tmpEntry;
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
    public static CustomizedHashMap<Long, Double> sortLDMapByValue(Map<Long, Double> oriMap) {
        CustomizedHashMap<Long, Double> sortedMap = new CustomizedHashMap<>();
        if (oriMap == null || oriMap.isEmpty()) {
            return sortedMap;
        }

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
        // 提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
        for (News news : newsList) {
            try {
                List<Keyword> keywords = getKeywords(news.getTag());
                newsTFIDFMap.put(String.valueOf(news.getId()), keywords);
                newsTFIDFMap.put(news.getId() + RecommendationConstants.MODULE_ID_STR, news.getModule());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newsTFIDFMap;
    }

    /**
     * List按news module分类
     *
     * @param map Map<String, List<News>>
     * @return List<News>
     */
    public static List<News> groupList(Map<String, List<News>> map) {
        List<News> newList = new ArrayList<>();
        try {
            for(String key : map.keySet()){
                newList.addAll(map.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    /**
     * 将keyword字符串转化为List
     * @param keywordsStr String
     * @return List
     */
    public static List<Keyword> getKeywords(String keywordsStr){
        List<Keyword> keywords = new ArrayList<>();
        String[] strs = keywordsStr.split(RecommendationConstants.SEPARATOR);
        for(String name : strs){
            keywords.add(new Keyword(name, 1D));
        }
        return keywords;
    }

    /**
     * 整理详情页html内容
     *
     * @param userId        Long
     * @param newsCrawl        NewsConfig
     * @param relatedNews List<NewsConfig>
     * @return String
     */
    public static String formatHtmlContent(Long userId, News newsCrawl, List<News> relatedNews) {
        StringBuilder relatedSb = new StringBuilder();
        if(relatedNews != null && !relatedNews.isEmpty()) {
            relatedSb.append("<hr /><p style='text-align:left'>相关推荐:</p>");
            relatedSb.append("<table style='text-align:center;margin-left:5%;border-collapse: collapse;'>");
            for (News rNews : relatedNews) {
                if(rNews.getId().equals(newsCrawl.getId())){
                    continue;
                }
                String linkUrl = "/recommend/news/" + userId + "/" + rNews.getId();
                relatedSb.append("<tr style='border-top:1px solid black;'>");
                relatedSb.append("<td style='width:80%;text-align:left;'><a target='_blank' href=").append(linkUrl);
                relatedSb.append("><p>").append(rNews.getTitle()).append("</p>");
                relatedSb.append("<img src=\"").append(rNews.getImageUrl()).append("\" alt=\"\" />").append("</a></td>");
                relatedSb.append("<td style='width:20%'>").append(rNews.getSource()).append("</td>");
                relatedSb.append("</tr>");
            }
            relatedSb.append("</table>");
        }

        return "<html><head><h1 style='text-align:center'>" +
                newsCrawl.getTitle() +
                "</h1><h4 style='color:gray;text-align:left'>" +
                newsCrawl.getSource() + "    " + newsCrawl.getNewsTime() +
                "</h4></head><meta charset=\"UTF-8\"><title>" +
                newsCrawl.getTitle() +
                "</title></head><body style='width:80%;margin-left:10%'>" +
                newsCrawl.getContent() +
                "</body><br/>" +
                "<p style='color:gray;text-align:right'>文章来源:<a href=" +
                newsCrawl.getUrl() + ">" + newsCrawl.getUrl() + "</a></p>" + relatedSb.toString() + "</html>";
    }

}
