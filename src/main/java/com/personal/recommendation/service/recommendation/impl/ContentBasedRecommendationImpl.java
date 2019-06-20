package com.personal.recommendation.service.recommendation.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.recommendation.AlgorithmFactory;
import com.personal.recommendation.service.recommendation.RecommendationAlgorithmService;
import com.personal.recommendation.utils.*;
import org.ansj.app.keyword.Keyword;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

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
            HashMap<Long, String> newsModuleMap = new HashMap<>();
            // 用户喜好关键词列表
            HashMap<Long, CustomizedHashMap<String, CustomizedHashMap<String, Double>>> userPrefListMap = new HashMap<>();
            userPrefListMap.put(user.getId(), JsonUtil.jsonPrefListToMap(user.getPrefList()));

            List<News> newsList = new ArrayList<>();
            // 获取用户profile
            Map<String, Integer> moduleMap = RecommendationUtil.getModuleListFromProfile(user.getUserProfile());
            for(String module : moduleMap.keySet()){
                newsList.addAll(newsManager.getNewsByModuleLimit(module, moduleMap.get(module), DateUtil.getDateBeforeDays(2)));
            }

            // 计算Item TF-IDF值
            for (News news : newsList) {
                List<Keyword> keywordList = TFIDFAnalyzer.getTfIdf(news.getContent());
                newsKeyWordsMap.put(news.getId(), keywordList);
                newsModuleMap.put(news.getId(), news.getModuleLevel1());
            }

            Map<Long, Double> tempMatchMap = new HashMap<>();
            Iterator<Long> ite = newsKeyWordsMap.keySet().iterator();
            // 比较新闻相似度
            if (ite.hasNext()) {
                do {
                    Long newsId = ite.next();
                    String moduleStr = newsModuleMap.get(newsId);
                    if (null != userPrefListMap.get(userId).get(moduleStr)) {
                        tempMatchMap.put(newsId, RecommendationUtil.getMatchValue(
                                userPrefListMap.get(userId).get(moduleStr), newsKeyWordsMap.get(newsId)));
                    }
                } while (ite.hasNext());
            }
            // 去除匹配值为0的项目
            RecommendationUtil.removeZeroItem(tempMatchMap);
            if (!(tempMatchMap.toString().equals("{}"))) {
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
     *
     * @param user Users
     */
    @SuppressWarnings("unchecked")
    private void refresh(Users user) {
        long start = new Date().getTime();
        logger.info("Start refreshing prefList at " + start);

        Long userId = user.getId();
        // 用户浏览新闻纪录：userBrowsedMap:<Long(userId),ArrayList<String>(newsId List)>
        List<NewsLogs> newsLogsList = newsLogsManager.getNewsLogsByUserViewTime(
                DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS), userId,
                RecommendationConstants.RECENT_VIEWED_NUM);
        List<Long> userBrowsedList = newsLogsList.stream().map(NewsLogs::getId).collect(Collectors.toList());

        // 如果前一天没有浏览记录（比如新闻门户出状况暂时关停的情况下，
        // 或者初期用户较少的时候均可能出现这种情况），则不需要执行后续更新步骤
        if (userBrowsedList.size() == 0)
            return;

        // 用户喜好关键词列表：userPrefListMap:<String(userId),String(json))>
        CustomizedHashMap<String, CustomizedHashMap<String, Double>> userPrefList =
                JsonUtil.jsonPrefListToMap(user.getPrefList());

        // 新闻对应关键词列表与模块ID
        List<News> newsList = newsManager.getNewsByIds(userBrowsedList);
        HashMap<String, Object> newsTFIDFMap = RefreshPrefUtil.getNewsTFIDFMap(newsList);

        // 开始遍历用户浏览记录，更新用户喜好关键词列表
        // 对每个用户（外层循环），循环他所看过的每条新闻（内层循环），
        // 对每个新闻，更新它的关键词列表到用户的对应模块中
        for (News news : newsList) {
            Long newsId = news.getId();
            String moduleStr = (String) newsTFIDFMap.get(newsId + RecommendationConstants.MODULE_ID_STR);
            // 获得对应模块的（关键词：喜好）map
            CustomizedHashMap<String, Double> rateMap = userPrefList.get(moduleStr) == null ? new CustomizedHashMap<>()
                    : userPrefList.get(moduleStr);
            // 获得新闻的（关键词：TF-IDF值）map
            List<Keyword> keywordList = (List<Keyword>) newsTFIDFMap.get(newsId.toString());
            for (Keyword keyword : keywordList) {
                String name = keyword.getName();
                if (rateMap.containsKey(name)) {
                    rateMap.put(name, rateMap.get(name) + keyword.getScore());
                } else if (keyword.getScore() > RecommendationConstants.MIN_DEC_RATE) {
                    rateMap.put(name, keyword.getScore());
                }
            }
            CustomizedHashMap<String, Double> sortedRateMap = RecommendationUtil.sortSDMapByValue(rateMap);
            userPrefList.put(moduleStr, sortedRateMap);
        }
        // 更新preference和用户profile
        try {
            String profile = RecommendationUtil.getUserProfile(newsLogsList, RecommendationConstants.CB_MAX_NEWS);
            user.setUserProfile(profile);
            usersManager.updatePrefAndProfileById(userId, userPrefList.toString(), profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("Refresh finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

    /**
     * 衰减更新
     *
     * @param user Users
     */
    private void decRefresh(Users user) {
        long start = new Date().getTime();
        logger.info("Start prefList reduction at " + start);

        try {
            // prefList为空则初始化prefList
            if (user.getPrefList() == null || user.getPrefList().isEmpty()) {
                // prefList为空则初始化prefList
                usersManager.initializePrefList(user, newsManager.getModuleLevel());
            }

            // 用于删除喜好值过低的关键词
            ArrayList<String> keywordToDelete = new ArrayList<>();
            StringBuilder newPrefStr = new StringBuilder("{");
            CustomizedHashMap<String, CustomizedHashMap<String, Double>> map = JsonUtil.jsonPrefListToMap(user.getPrefList());
            for (String moduleStr : map.keySet()) {
                // 用户对应模块的喜好不为空
                CustomizedHashMap<String, Double> moduleMap = map.get(moduleStr) == null
                        ? new CustomizedHashMap<>() : map.get(moduleStr);
                newPrefStr.append("\"").append(moduleStr).append("\":");
                // N:{"X1":n1,"X2":n2,.....}
                if (!(moduleMap.toString().equals("{}"))) {
                    for (String key : moduleMap.keySet()) {
                        // 累计TF-IDF值乘以衰减系数
                        double result = moduleMap.get(key) * RecommendationConstants.DEC_CODE;
                        if (result < RecommendationConstants.MIN_DEC_RATE) {
                            keywordToDelete.add(key);
                        }
                        moduleMap.put(key, result);
                    }
                }
                for (String deleteKey : keywordToDelete) {
                    moduleMap.remove(deleteKey);
                }
                keywordToDelete.clear();
                newPrefStr.append(moduleMap.toString()).append(RecommendationConstants.SEPARATOR);
            }
            newPrefStr = new StringBuilder(newPrefStr.substring(0, newPrefStr.length() - 1) + "}");
            // 更新users preference
//            usersManager.updatePrefListById(user.getId(), newPrefStr.toString());
            user.setPrefList(newPrefStr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("PrefList reduction finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

}
