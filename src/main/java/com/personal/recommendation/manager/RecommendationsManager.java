package com.personal.recommendation.manager;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.dao.RecommendationsDAO;
import com.personal.recommendation.model.Recommendations;
import com.personal.recommendation.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 推荐Manager
 */
@Service
@SuppressWarnings("unused")
public class RecommendationsManager {

    private final RecommendationsDAO recommendationsDAO;

    @Autowired
    public RecommendationsManager(RecommendationsDAO recommendationsDAO) {
        this.recommendationsDAO = recommendationsDAO;
    }

    public List<Long> getNewsIdByUserId(long userId){
        return recommendationsDAO.getNewsIdByUserId(userId);
    }


    public List<Long> getNewsIdByUserDeriveTime(long userId) {
        return recommendationsDAO.getNewsIdByUserDeriveTime(userId,
                DateUtil.getDateBeforeDays(RecommendationConstants.HOT_DATA_DAYS));
    }

    public int getUserRecNumByDeriveTime(long userId, Date deriveTime) {
        int result = 0;
        try {
            result = recommendationsDAO.getUserRecNumByDeriveTime(userId, deriveTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void insertRecommendations(Recommendations recommendation){
        recommendationsDAO.insertRecommendation(recommendation);
    }

}
