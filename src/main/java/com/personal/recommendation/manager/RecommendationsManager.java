package com.personal.recommendation.manager;

import com.personal.recommendation.dao.RecommendationsDAO;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 推荐Manager
 */
@Service
public class RecommendationsManager {

    private final RecommendationsDAO recommendationsDAO;
    @Autowired
    public RecommendationsManager(RecommendationsDAO recommendationsDAO) {
        this.recommendationsDAO = recommendationsDAO;
    }

    public List<Long> getFilterRecordNews(long userId) {
        return recommendationsDAO.getNewsIdByUserDeriveTime(userId, DateUtil.getDateBeforeDays(RecommendationAlgorithmService.HOT_DATA_DAYS));
    }

    public int getUserRecNumByDeriveTime(long userId, Date deriveTime) {
        return recommendationsDAO.getUserRecNumByDeriveTime(userId, deriveTime);
    }


}
