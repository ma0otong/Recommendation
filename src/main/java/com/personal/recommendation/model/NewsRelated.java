package com.personal.recommendation.model;

/**
 * 相关内容
 */
@SuppressWarnings("unused")
public class NewsRelated {

    private Long id;
    private Long newsId;
    private String relatedNewsIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public String getRelatedNewsIds() {
        return relatedNewsIds;
    }

    public void setRelatedNewsIds(String relatedNewsIds) {
        this.relatedNewsIds = relatedNewsIds;
    }
}
