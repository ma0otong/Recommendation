package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.utils.*;
import org.ansj.app.keyword.Keyword;
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
    private final RecommendationsManager recommendationsManager;

    @Autowired
    public ContentBasedRecommendationImpl(UsersManager usersManager, NewsManager newsManager,
                                          NewsLogsManager newsLogsManager,
                                          RecommendationsManager recommendationsManager) {
        this.usersManager = usersManager;
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
        this.recommendationsManager = recommendationsManager;
    }

    @PostConstruct
    void init() {
        AlgorithmFactory.addHandler(RecommendationEnum.CB.getCode(), this);
    }

    @Override
    public BaseRsp recommend(List<Long> users) {
        long start = new Date().getTime();
        logger.info("CB start at " + start + ", userList : " + users);

        // 保存推荐结果
        Map<Users, List<News>> resultMap = new HashMap<>();

        try {
            int count = 0;
            // 新闻及对应关键词列表的Map
            HashMap<Long, List<Keyword>> newsKeyWordsMap = new HashMap<>();
            HashMap<Long, Integer> newsModuleMap = new HashMap<>();
            // 用户喜好关键词列表
            HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap =
                    usersManager.getUserPrefListMap(users);

            List<News> newsList = newsManager.getNewsByDateTime(DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS));
            for (News news : newsList) {
                newsKeyWordsMap.put(news.getId(), TfIdf.getTfIde(news.getTitle(),
                        news.getContent(), RecommendationConstants.TD_IDF_KEY_WORDS_NUM));
                newsModuleMap.put(news.getId(), news.getModuleId());
            }

            for (Long uid : users) {
                Map<Long, Double> tempMatchMap = new HashMap<>();
                Iterator<Long> ite = newsKeyWordsMap.keySet().iterator();
                if (ite.hasNext()) {
                    do {
                        Long newsId = ite.next();
                        int moduleId = newsModuleMap.get(newsId);
                        if (null != userPrefListMap.get(uid).get(moduleId)) {
                            tempMatchMap.put(newsId,
                                    RecommendationUtil.getMatchValue(userPrefListMap.get(uid).get(moduleId),
                                            newsKeyWordsMap.get(newsId)));
                        }
                    } while (ite.hasNext());
                }
                // 去除匹配值为0的项目
                RecommendationUtil.removeZeroItem(tempMatchMap);
                if (!(tempMatchMap.toString().equals("{}"))) {
                    tempMatchMap = RecommendationUtil.sortMapByValue(tempMatchMap);
                    Set<Long> toBeRecommended;
                    toBeRecommended = Objects.requireNonNull(tempMatchMap).keySet();

                    count = RecommendationUtil.resultHandle(recommendationsManager, newsLogsManager, newsManager,
                            usersManager, toBeRecommended, uid, count, resultMap, RecommendationEnum.CB.getCode());

                }
            }

            long end = new Date().getTime();
            logger.info("CB has contributed " + (count / users.size()) + " recommending news on average");
            logger.info("CB finished at " + end + ", time cost : " + (double)((end - start)/1000) + "s .");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseRsp(ResultEnum.FAILURE, ResultEnum.FAILURE.getMsg());
        }

        return new BaseRsp(ResultEnum.SUCCESS, resultMap);

    }


}
