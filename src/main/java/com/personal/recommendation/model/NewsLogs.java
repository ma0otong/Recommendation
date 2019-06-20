package com.personal.recommendation.model;

import java.util.Date;

/**
 * 新闻日志类
 */
@SuppressWarnings("unused")
public class NewsLogs {

    private Long id;
    private Long userId;
    private Long newsId;
    private Date viewTime;
    private String newsModule;

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

    public Date getViewTime() {
        return viewTime;
    }

    public void setViewTime(Date viewTime) {
        this.viewTime = viewTime;
    }

    public String getNewsModule() {
        return newsModule;
    }

    public void setNewsModule(String newsModule) {
        this.newsModule = newsModule;
    }
}
