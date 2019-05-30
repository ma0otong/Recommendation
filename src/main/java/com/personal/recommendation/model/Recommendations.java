package com.personal.recommendation.model;

import java.util.Date;

/**
 * 推荐内容类
 */
@SuppressWarnings("unused")
public class Recommendations {

    private Long id;
    private Long userId;
    private Long newsId;
    private Date deriveTime;
    private Boolean feedback;
    private Integer deriveAlgorithm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public Date getDeriveTime() {
        return deriveTime;
    }

    public void setDeriveTime(Date deriveTime) {
        this.deriveTime = deriveTime;
    }

    public Boolean getFeedback() {
        return feedback;
    }

    public void setFeedback(Boolean feedback) {
        this.feedback = feedback;
    }

    public Integer getDeriveAlgorithm() {
        return deriveAlgorithm;
    }

    public void setDeriveAlgorithm(Integer deriveAlgorithm) {
        this.deriveAlgorithm = deriveAlgorithm;
    }

}
