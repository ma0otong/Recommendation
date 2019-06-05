package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于内容推荐实现类
 */
@Service
public class ContentBasedRecommendationImpl implements RecommendationAlgorithmService {

    private static final Logger logger = Logger.getLogger(ContentBasedRecommendationImpl.class);

    private final UsersManager usersManager;
    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;

    @Autowired
    public ContentBasedRecommendationImpl(UsersManager usersManager, NewsManager newsManager,
                                          NewsLogsManager newsLogsManager) {
        this.usersManager = usersManager;
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
    }

    @PostConstruct
    void init() {
        AlgorithmFactory.addHandler(RecommendationEnum.CB.getCode(), this);
    }

    @Override
    public Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews) {
        long start = new Date().getTime();
        Long userId = user.getId();
        logger.info(RecommendationEnum.CB.getDesc() + " start at " + start + ", userId : " + userId);
        logger.info("Recommended data not enough, " + RecommendationEnum.CB.getDesc() + " need fetch " + recNum);

        // prefList衰减更新
        decRefresh(user);
        // 更新用户prefList
        refresh(user);

        // 保存推荐结果
        Set<Long> toBeRecommended = new HashSet<>();

        try {
            // 新闻及对应关键词列表的Map
            HashMap<Long, List<Keyword>> newsKeyWordsMap = new HashMap<>();
            HashMap<Long, Integer> newsModuleMap = new HashMap<>();
            // 用户喜好关键词列表
            HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap =  new HashMap<>();
            userPrefListMap.put(user.getId(), JsonUtil.jsonPrefListToMap(user.getPrefList()));

            // 获取HOT_DATA_DAYS之前的数据
            List<News> newsList = newsManager.getNewsByDateTime(
                    DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS));
            for (News news : newsList) {
                newsKeyWordsMap.put(news.getId(), TFIDFAnalyzer.getTfIde(news.getTitle(),
                        RecommendationConstants.TD_IDF_KEY_WORDS_NUM));
                newsModuleMap.put(news.getId(), news.getModuleId());
            }

            Map<Long, Double> tempMatchMap = new HashMap<>();
            Iterator<Long> ite = newsKeyWordsMap.keySet().iterator();
            if (ite.hasNext()) {
                do {
                    Long newsId = ite.next();
                    int moduleId = newsModuleMap.get(newsId);
                    if (null != userPrefListMap.get(userId).get(moduleId)) {
                        tempMatchMap.put(newsId, RecommendationUtil.getMatchValue(
                                userPrefListMap.get(userId).get(moduleId), newsKeyWordsMap.get(newsId)));
                    }
                } while (ite.hasNext());
            }
            // 去除匹配值为0的项目
            RecommendationUtil.removeZeroItem(tempMatchMap);
            if (!(tempMatchMap.toString().equals("{}"))) {
                tempMatchMap = RecommendationUtil.sortMapByValue(tempMatchMap);
                toBeRecommended = Objects.requireNonNull(tempMatchMap).keySet();

                toBeRecommended = RecommendationUtil.resultHandle(recommendedNews, browsedNews,
                        toBeRecommended, userId, RecommendationEnum.CB.getCode(), recNum, false);

            }

            long end = new Date().getTime();
            if (!toBeRecommended.isEmpty())
                logger.info("CB has contributed " + toBeRecommended.size() + " recommending news on average");
            logger.info("CB finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toBeRecommended;

    }

    /**
     * 用户偏好更新
     * @param user Users
     */
    @SuppressWarnings("unchecked")
    private void refresh(Users user) {
        long start = new Date().getTime();
        logger.info("Start refreshing prefList at " + start);

        Long userId = user.getId();
        // 用户浏览新闻纪录：userBrowsedMap:<Long(userId),ArrayList<String>(newsId List)>
        HashMap<Long, ArrayList<Long>> userBrowsedMap = RefreshPrefUtil.getBrowsedHistoryMap(userId,
                newsLogsManager.getNewsLogsByUserViewTime(
                        DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS), userId,
                        RecommendationConstants.RECENT_VIEWED_NUM));

        // 如果前一天没有浏览记录（比如新闻门户出状况暂时关停的情况下，
        // 或者初期用户较少的时候均可能出现这种情况），则不需要执行后续更新步骤
        if (userBrowsedMap.size() == 0)
            return;

        // 用户喜好关键词列表：userPrefListMap:<String(userId),String(json))>
        HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = new HashMap<>();
        userPrefListMap.put(userId, JsonUtil.jsonPrefListToMap(user.getPrefList()));

        // 新闻对应关键词列表与模块ID
        HashMap<String, Object> newsTFIDFMap = RefreshPrefUtil.getNewsTFIDFMap(
                newsManager.getNewsByIds(RefreshPrefUtil.getBrowsedNewsSet(userBrowsedMap)));

        // 开始遍历用户浏览记录，更新用户喜好关键词列表
        // 对每个用户（外层循环），循环他所看过的每条新闻（内层循环），
        // 对每个新闻，更新它的关键词列表到用户的对应模块中
        ArrayList<Long> newsList = userBrowsedMap.get(userId);
        for (Long news : newsList) {
            Integer moduleId = (Integer) newsTFIDFMap.get(news + RecommendationConstants.MODULE_ID);
            // 获得对应模块的（关键词：喜好）map
            CustomizedHashMap<String, Double> rateMap = userPrefListMap.get(userId).get(moduleId);
            // 获得新闻的（关键词：TF-IDF值）map
            List<Keyword> keywordList = (List<Keyword>) newsTFIDFMap.get(news.toString());
            for (Keyword keyword : keywordList) {
                String name = keyword.getName();
                if (rateMap.containsKey(name)) {
                    rateMap.put(name, rateMap.get(name) + keyword.getTfIdfValue());
                } else {
                    rateMap.put(name, keyword.getTfIdfValue());
                }
            }
        }
        // 更新preference
        try {
            usersManager.updatePrefListById(userId, userPrefListMap.get(userId).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("Refresh finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

    /**
     * 衰减更新
     * @param user Users
     */
    private void decRefresh(Users user) {
        long start = new Date().getTime();
        logger.info("Start prefList reduction at " + start);

        try {
            // prefList为空则初始化prefList
            if (user.getPrefList() == null || user.getPrefList().isEmpty()) {
                // prefList为空则初始化prefList
                usersManager.initializePrefList(user, newsManager.getModuleIdCount());
            }

            // 用于删除喜好值过低的关键词
            ArrayList<String> keywordToDelete = new ArrayList<>();
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
                        double result = moduleMap.get(key) * RecommendationConstants.DEC_CODE;
                        if (result < 1) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("PrefList reduction finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

}
