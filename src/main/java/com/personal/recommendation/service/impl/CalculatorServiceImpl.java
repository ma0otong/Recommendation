package com.personal.recommendation.service.impl;

import com.personal.recommendation.constant.RecommendEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.service.CalculatorService;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.utils.*;
import org.ansj.app.keyword.Keyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务处理接口实现类
 */
@Service
@SuppressWarnings("unused")
public class CalculatorServiceImpl implements CalculatorService {

    private final NewsLogsManager newsLogsManager;
    private final UsersManager usersManager;
    private final NewsManager newsManager;

    @Autowired
    public CalculatorServiceImpl(UsersManager usersManager, NewsManager newsManager, NewsLogsManager newsLogsManager) {
        this.newsLogsManager = newsLogsManager;
        this.usersManager = usersManager;
        this.newsManager = newsManager;
    }

    @Override
    public Object executeInstantJob(List<Long> userIdList, int type) {
        // 对测试数据的相关日期数据进行更新，以保证能够在测试运行中获得理想的推荐结果。
        refreshDBTime();
        // 让热点新闻推荐器预先生成今日的热点新闻
        formTodayTopHotNewsList();

        // 刷新用户preference
        if (type == RecommendEnum.CB.getCode())
            refresh(userIdList);

        // 计算推荐
        return AlgorithmFactory.getHandler(type).recommend(userIdList);
    }

    @Override
    public void formTodayTopHotNewsList() {
        topHotNewsList.clear();
        ArrayList<Long> hotNewsTobeRecommended = new ArrayList<>();
        try {
            // 热点新闻的有效时间
            List<NewsLogs> newsLogsList = newsLogsManager.getHotNews(DateUtil.getDateBeforeDays(RecommendationAlgorithmService.HOT_DATA_DAYS));
            for (NewsLogs newsLog : newsLogsList) {
                if (newsLog != null)
                    hotNewsTobeRecommended.add(newsLog.getNewsId());
            }
            topHotNewsList.addAll(hotNewsTobeRecommended);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 按照推荐频率调用的方法，一般为一天执行一次。
     * 定期根据前一天所有用户的浏览记录，在对用户进行喜好关键词列表TF-IDF值衰减的后，将用户前一天看的新闻的关键词及相应TF-IDF值更新到列表中去。
     *
     * @param userIdsCol 用户idList
     */
    @SuppressWarnings("unchecked")
    @Override
    public void refresh(List<Long> userIdsCol) {
        // 首先对用户的喜好关键词列表进行衰减更新
        autoDecRefresh(userIdsCol);

        // 用户浏览新闻纪录：userBrowsedMap:<Long(userId),ArrayList<String>(newsId List)>
        HashMap<Long, ArrayList<Long>> userBrowsedMap = getBrowsedHistoryMap();
        // 如果前一天没有浏览记录（比如新闻门户出状况暂时关停的情况下，或者初期用户较少的时候均可能出现这种情况），则不需要执行后续更新步骤
        if (userBrowsedMap.size() == 0)
            return;

        // 用户喜好关键词列表：userPrefListMap:<String(userId),String(json))>
        HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = usersManager.getUserPrefListMap(new ArrayList(userBrowsedMap.keySet()));

        // 新闻对应关键词列表与模块ID：newsTFIDFMap:<String(newsId),List<Keyword>>,<String(newsModuleId),Integer(moduleId)>
        HashMap<String, Object> newsTFIDFMap = getNewsTFIDFMap();

        // 开始遍历用户浏览记录，更新用户喜好关键词列表
        // 对每个用户（外层循环），循环他所看过的每条新闻（内层循环），对每个新闻，更新它的关键词列表到用户的对应模块中
        for (Long userId : userBrowsedMap.keySet()) {
            ArrayList<Long> newsList = userBrowsedMap.get(userId);
            for (Long news : newsList) {
                Integer moduleId = (Integer) newsTFIDFMap.get(news + MODULE_ID);
                // 获得对应模块的（关键词：喜好）map
                CustomizedHashMap<String, Double> rateMap = userPrefListMap.get(userId).get(moduleId);
                // 获得新闻的（关键词：TF-IDF值）map
                List<Keyword> keywordList = (List<Keyword>) newsTFIDFMap.get(news.toString());
                for (Keyword keyword : keywordList) {
                    String name = keyword.getName();
                    if (rateMap.containsKey(name)) {
                        rateMap.put(name, rateMap.get(name) + keyword.getScore());
                    } else {
                        rateMap.put(name, keyword.getScore());
                    }
                }
                userPrefListMap.get(userId);
            }
        }
        // 更新preference
        for (Long userId : userBrowsedMap.keySet()) {
            try {
                usersManager.updatePrefListById(userId, userPrefListMap.get(userId).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 所有用户的喜好关键词列表TF-IDF值随时间进行自动衰减更新
     */
    @Override
    public void autoDecRefresh() {
        autoDecRefresh(usersManager.getAllUserIds());
    }

    /**
     * 所有用户的喜好关键词列表TF-IDF值随时间进行自动衰减更新
     */
    @Override

    public void autoDecRefresh(List<Long> userIdsCol) {
        try {
            List<Users> userList = usersManager.getUsersByIds(userIdsCol);
            // 用于删除喜好值过低的关键词
            ArrayList<String> keywordToDelete = new ArrayList<>();
            for (Users user : userList) {
                StringBuilder newPrefStr = new StringBuilder("{");
                HashMap<Integer, CustomizedHashMap<String, Double>> map = JsonUtil.jsonPrefListToMap(user.getPrefList());
                for (Integer moduleId : map.keySet()) {
                    // 用户对应模块的喜好不为空
                    CustomizedHashMap<String, Double> moduleMap = map.get(moduleId);
                    newPrefStr.append("\"").append(moduleId).append("\":");
                    // N:{"X1":n1,"X2":n2,.....}
                    if (!(moduleMap.toString().equals("{}"))) {
                        for (String key : moduleMap.keySet()) {
                            // 累计TF-IDF值乘以衰减系数
                            double result = moduleMap.get(key) * DEC_CODE;
                            if (result < 10) {
                                keywordToDelete.add(key);
                            }
                            moduleMap.put(key, result);
                        }
                    }
                    for (String deleteKey : keywordToDelete) {
                        moduleMap.remove(deleteKey);
                    }
                    keywordToDelete.clear();
                    newPrefStr.append(moduleMap.toString()).append(",");
                }
                newPrefStr = new StringBuilder(newPrefStr.substring(0, newPrefStr.length() - 1) + "}");
                // 更新users preference
                usersManager.updatePrefListById(user.getId(), newPrefStr.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取出当天有浏览行为的用户及其各自所浏览过的新闻id列表
     *
     * @return 用户历史行为结果
     */
    @Override
    public HashMap<Long, ArrayList<Long>> getBrowsedHistoryMap() {
        HashMap<Long, ArrayList<Long>> userBrowsedMap = new HashMap<>();
        try {
            List<NewsLogs> newsLogsList = newsLogsManager.getNewsLogsByViewTime(DateUtil.getDateBeforeDays(RecommendationAlgorithmService.HOT_DATA_DAYS));
            for (NewsLogs newslogs : newsLogsList) {
                if (userBrowsedMap.containsKey(newslogs.getUserId())) {
                    userBrowsedMap.get(newslogs.getUserId()).add(newslogs.getNewsId());
                } else {
                    userBrowsedMap.put(newslogs.getUserId(), new ArrayList<>());
                    userBrowsedMap.get(newslogs.getUserId()).add(newslogs.getNewsId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userBrowsedMap;
    }


    /**
     * 获得浏览过的新闻的集合
     *
     * @return 浏览过的新闻
     */
    @Override
    public List<Long> getBrowsedNewsSet() {
        HashMap<Long, ArrayList<Long>> browsedMap = getBrowsedHistoryMap();
        List<Long> newsIdList = new ArrayList<>();
        for (Long aLong : getBrowsedHistoryMap().keySet()) {
            newsIdList.addAll(browsedMap.get(aLong));
        }
        return newsIdList;
    }

    /**
     * 将所有当天被浏览过的新闻提取出来，以便进行TF-IDF求值操作，以及对用户喜好关键词列表的更新。
     *
     * @return TF-IDF计算后的结果
     */
    @Override
    public HashMap<String, Object> getNewsTFIDFMap() {
        HashMap<String, Object> newsTFIDFMap = new HashMap<>();

        try {
            // 提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
            List<News> newsList = newsManager.getNewsByIds(getBrowsedNewsSet());
            for (News news : newsList) {
                newsTFIDFMap.put(String.valueOf(news.getId()), TfIdf.getTfIde(news.getTitle(), news.getContent(), KEY_WORDS_NUM));
                newsTFIDFMap.put(news.getId() + MODULE_ID, news.getModuleId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsTFIDFMap;
    }

    private void refreshDBTime() {
        newsManager.updateNewsTime(new Date());
        for (int id = 1; id < 8; id++) {
            usersManager.updateUserTimeStamp(DateUtil.getDateBeforeDays(25 + id), (long) id);
        }
        newsLogsManager.updateViewTime(new Date());
    }
}
