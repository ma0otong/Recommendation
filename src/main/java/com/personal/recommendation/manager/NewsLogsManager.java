package com.personal.recommendation.manager;

import com.personal.recommendation.dao.NewsLogsDAO;
import com.personal.recommendation.model.NewsLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 新闻日志Manager
 */
@Service
public class NewsLogsManager {

    private final NewsLogsDAO newsLogsDAO;
    @Autowired
    public NewsLogsManager(NewsLogsDAO newsLogsDAO) {
        this.newsLogsDAO = newsLogsDAO;
    }

    public List<NewsLogs> getHotNews(Date hotDateTime){
        return newsLogsDAO.getHotNews(hotDateTime);
    }

    public List<Long> getFilterBrowsedNews(Long userId) {
        return newsLogsDAO.getNewsIdByUserId(userId);
    }

    public List<NewsLogs> getAll(){
        return newsLogsDAO.getAll();
    }

    public List<NewsLogs> getNewsLogsByViewTime(Date viewTime){
        return newsLogsDAO.getNewsLogsByViewTime(viewTime);
    }

    public void updateViewTime(Date viewTime){
        newsLogsDAO.updateViewTime(viewTime);
    }

}
