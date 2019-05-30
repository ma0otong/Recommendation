package com.personal.recommendation.service.impl;

import com.personal.recommendation.constant.RecommendEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.CalculatorService;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.utils.DateUtil;
import com.personal.recommendation.utils.RecommendationUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.*;

/**
 * 基于热点推荐实现类
 */
@Service
public class HotRecommendationImpl implements RecommendationAlgorithmService {

    private static final Logger logger = Logger.getLogger(HotRecommendationImpl.class);

    private final UsersManager usersManager;
    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;
    private final RecommendationsManager recommendationsManager;

    @Autowired
    public HotRecommendationImpl(UsersManager usersManager, NewsManager newsManager, NewsLogsManager newsLogsManager,
                                 RecommendationsManager recommendationsManager) {
        this.usersManager = usersManager;
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
        this.recommendationsManager = recommendationsManager;
    }

    @PostConstruct
    void init() {
        AlgorithmFactory.addHandler(RecommendEnum.HR.getCode(), this);
    }

    @Override
    public Object recommend(List<Long> users) {
        // 保存推荐结果
        Map<Users, List<News>> resultMap = new HashMap<>();

        int count = 0;
        Timestamp timestamp = DateUtil.getCertainTimestamp();
        for (Long uid : users) {
            try {
                // 获得已经预备为当前用户推荐的新闻，若数目不足达不到单次的最低推荐数目要求，则用热点新闻补充
                int tmpRecNums = recommendationsManager.getUserRecNumByDeriveTime(uid, timestamp);

                // 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
                int TOTAL_REC_NUM = 20;
                int delta = TOTAL_REC_NUM - tmpRecNums;
                Set<Long> toBeRecommended = new HashSet<>();
                if (delta > 0) {
                    int i = CalculatorService.topHotNewsList.size() > delta ? delta : CalculatorService.topHotNewsList.size();
                    while (i-- > 0)
                        toBeRecommended.add(CalculatorService.topHotNewsList.get(i));
                }

                count = RecommendationUtil.resultHandle(recommendationsManager, newsLogsManager, newsManager, usersManager, toBeRecommended, uid, count, resultMap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info(
                "HR has contributed " + (users.size() == 0 ? 0 : count / users.size()) + " recommending news on average");
        logger.info("HR end at " + new Date());

        return resultMap;

    }

}
