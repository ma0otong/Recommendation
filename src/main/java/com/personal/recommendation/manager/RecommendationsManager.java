package com.personal.recommendation.manager;

import com.personal.recommendation.dao.RecommendationsDAO;
import com.personal.recommendation.model.Recommendations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Recommendations> getNewsByUserId(Long userId){
        return recommendationsDAO.getNewsByUserId(userId);
    }

    public List<Long> getNewsIdByUserId(Long userId){
        return recommendationsDAO.getNewsIdByUserId(userId);
    }

    public void insertRecommendations(Recommendations recommendation){
        recommendationsDAO.insertRecommendation(recommendation);
    }

    public Recommendations getRecommendationByUserAndNewsId(Long userId, Long newsId){
        return recommendationsDAO.getRecommendationByUserAndNewsId(userId, newsId);
    }

    public void updateFeedBackById(Long id, int feedback){
        recommendationsDAO.updateFeedBackById(id, feedback);
    }

}
