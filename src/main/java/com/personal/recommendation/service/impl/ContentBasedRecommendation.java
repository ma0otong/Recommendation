package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmFactory;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.service.RecommendationCalculator;
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
public class ContentBasedRecommendation implements RecommendationAlgorithmService {

    private static final Logger logger = Logger.getLogger(ContentBasedRecommendation.class);

    private final UsersManager usersManager;
    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;

    @Autowired
    public ContentBasedRecommendation(UsersManager usersManager, NewsManager newsManager,
                                      NewsLogsManager newsLogsManager) {
        this.usersManager = usersManager;
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
    }

    @PostConstruct
    void init() {
        RecommendationAlgorithmFactory.addHandler(RecommendationEnum.CB.getCode(), this);
    }

    @Override
    public Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews) {
        long start = new Date().getTime();
        Long userId = user.getId();
        logger.info(RecommendationEnum.CB.getDesc() + " start at " + start + ", userId : " + userId);
        logger.info("Recommended data not enough, " + RecommendationEnum.CB.getDesc() + " need fetch " + recNum);

        // prefList衰减更新
        userPrefDecRefresh(user);
        // 更新用户prefList
        userPrefRefresh(user);

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
            Map<String, Integer> moduleMap = getModuleListFromProfile(user.getUserProfile());
            for(String module : moduleMap.keySet()){
                newsList.addAll(newsManager.getNewsByModuleLimit(module, moduleMap.get(module),
                        DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS)));
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
                toBeRecommended = RecommendationCalculator.resultHandle(recommendedNews, browsedNews,
                        toBeRecommended, userId, RecommendationEnum.CB.getDesc(), recNum, false);

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
    private void userPrefRefresh(Users user) {
        long start = new Date().getTime();
        logger.info("Start refreshing prefList at " + start);

        Long userId = user.getId();
        // 用户浏览新闻纪录：userBrowsedMap:<Long(userId),ArrayList<String>(newsId List)>
        List<NewsLogs> newsLogsList = newsLogsManager.getNewsLogsByUserViewTime(
                DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS), userId,
                RecommendationConstants.RECENT_VIEWED_NUM);
        List<Long> userBrowsedList = newsLogsList.stream().map(NewsLogs::getNewsId).collect(Collectors.toList());
        // 如果前一天没有浏览记录（比如新闻门户出状况暂时关停的情况下，
        // 或者初期用户较少的时候均可能出现这种情况），则不需要执行后续更新步骤
        if (userBrowsedList.size() == 0)
            return;

        // 用户喜好关键词列表：userPrefListMap:<String(userId),String(json))>
        CustomizedHashMap<String, CustomizedHashMap<String, Double>> userPrefList =
                JsonUtil.jsonPrefListToMap(user.getPrefList());

        // 新闻对应关键词列表与模块ID
        List<News> newsList = newsManager.getNewsByIds(userBrowsedList);
        HashMap<String, Object> newsTFIDFMap = RecommendationUtil.getNewsTFIDFMap(newsList);

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
            List<Keyword> keywordList = (List<Keyword>) newsTFIDFMap.get(String.valueOf(newsId));
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
            String profile = getUserProfile(newsLogsList);
            user.setUserProfile(profile);
            usersManager.updatePrefAndProfileById(userId, userPrefList.toString(), profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("Refresh finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

    /**
     * 用户偏好衰减更新
     * @param user Users
     */
    private void userPrefDecRefresh(Users user) {
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
            user.setPrefList(newPrefStr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = new Date().getTime();
        logger.info("PrefList reduction finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
    }

    /**
     * 获取用户profile
     * @param newsLogsList List<NewsLogs>
     * @return String
     */
    private static String getUserProfile(List<NewsLogs> newsLogsList){
        Map<String, Double> moduleScoreMap = new HashMap<>();
        for(NewsLogs newsLogs : newsLogsList){
            String module = newsLogs.getNewsModule();
            if(moduleScoreMap.containsKey(module)){
                moduleScoreMap.put(module, moduleScoreMap.get(module) + 1);
            }else{
                moduleScoreMap.put(module, 1D);
            }
        }
        Map<String, Double> newMap = RecommendationUtil.sortSDMapByValue(moduleScoreMap);
        StringBuilder sb = new StringBuilder();
        int limit = 0;
        assert newMap != null;
        for(String module : newMap.keySet()){
            if(limit >= RecommendationConstants.CB_PROFILE_MAX){
                break;
            }
            Double num = (newMap.get(module)/newsLogsList.size()) * RecommendationConstants.CB_MAX_NEWS;
            sb.append(module).append(RecommendationConstants.SCORE_SPLIT).append(num).append(RecommendationConstants.SEPARATOR);
            limit++;
        }
        return sb.toString().substring(0,sb.length()-1);
    }

    /**
     * 获取profile中的module
     * @param profile String
     * @return Map<String, Integer>
     */
    private static Map<String, Integer> getModuleListFromProfile(String profile){
        Map<String, Integer> map = new HashMap<>();
        try {
            String[] modules = profile.split(RecommendationConstants.SEPARATOR);
            for (String module : modules) {
                String[] strArray = module.split(RecommendationConstants.SCORE_SPLIT);
                if(strArray.length > 1)
                    map.put(strArray[0], Integer.parseInt(strArray[1].split("\\.")[0]));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return map;
    }

}
