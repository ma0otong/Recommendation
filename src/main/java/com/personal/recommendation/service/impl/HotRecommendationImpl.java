package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.utils.RecommendationUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于热点推荐实现类
 */
@Service
public class HotRecommendationImpl implements RecommendationAlgorithmService {

    private static final Logger logger = Logger.getLogger(HotRecommendationImpl.class);

    @PostConstruct
    void init() {
        AlgorithmFactory.addHandler(RecommendationEnum.HR.getCode(), this);
    }

    @Override
    public Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews) {
        long start = new Date().getTime();
        Long userId = user.getId();
        logger.info(RecommendationEnum.HR.getDesc() + " start at " + start + ", userId : " + userId);
        logger.info("Recommended data not enough, " + RecommendationEnum.HR.getDesc() + " need fetch " + recNum);

        // 保存推荐结果
        Set<Long> toBeRecommended = new HashSet<>();

        try {
            // 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
            toBeRecommended = RecommendationUtil.resultHandle(recommendedNews, browsedNews,
                    toBeRecommended, userId, RecommendationEnum.HR.getCode(), recNum, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = new Date().getTime();
        if(!toBeRecommended.isEmpty())
            logger.info("HR has contributed " + toBeRecommended.size() + " recommending news on average");
        logger.info("HR finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");

        return toBeRecommended;

    }

}
