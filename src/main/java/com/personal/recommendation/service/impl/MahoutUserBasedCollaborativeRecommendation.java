package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.component.mahout.service.impl.MyGenericUserBasedRecommender;
import com.personal.recommendation.component.mahout.service.MyRecommender;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmFactory;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.service.RecommendationCalculator;
import com.personal.recommendation.utils.DBConnectionUtil;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于协同过滤推荐实现类
 */
@Service
public class MahoutUserBasedCollaborativeRecommendation implements RecommendationAlgorithmService {

    @PostConstruct
    void init() {
        RecommendationAlgorithmFactory.addHandler(RecommendationEnum.CF.getCode(), this);
    }

    /**
     * 给特定的一批用户进行新闻推荐
     */
    @Override
    public Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews) {
        Long userId = user.getId();

        // 保存推荐结果
        Set<Long> toBeRecommended = new HashSet<>();

        try {

            MySQLBooleanPrefJDBCDataModel dataModel = new DBConnectionUtil().getMySQLJDBCDataModel();

            // 构造相似度度量器
            UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
            // 构造近邻查找
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(RecommendationConstants.N, similarity, dataModel);
            // 构造推荐器
            MyRecommender recommender = new MyGenericUserBasedRecommender(dataModel, neighborhood, similarity);
            // 获取推荐结果
            List<RecommendedItem> recItems = recommender.recommend(userId, RecommendationConstants.N);

            for (RecommendedItem recItem : recItems) {
                toBeRecommended.add(recItem.getItemID());
            }

            toBeRecommended = RecommendationCalculator.resultHandle(recommendedNews, browsedNews, toBeRecommended,
                    userId, RecommendationEnum.CF.getDesc(), recNum, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return toBeRecommended;

    }

}
