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

    public List<NewsLogs> getHotNews(Date hotDateTime, int limit) {
        return newsLogsDAO.getHotNews(hotDateTime, limit);
    }

    public List<Long> getNewsIdByUserId(Long userId) {
        return newsLogsDAO.getNewsIdByUserId(userId);
    }

    public List<NewsLogs> getNewsLogsByUserViewTime(Date viewTime, Long userId, int recordNum) {
        return newsLogsDAO.getNewsLogsByUserViewTime(viewTime, userId, recordNum);
    }

    public void updateViewTimeById(Date viewTime, Long id) {
        newsLogsDAO.updateViewTime(viewTime, id);
    }

    public void insertNewsLogs(NewsLogs newsLog){
        newsLogsDAO.insertNewsLogs(newsLog);
    }

    public NewsLogs getUserLogByUserId(Long userId, Long newsId){
        return newsLogsDAO.getUserLogByUserId(userId, newsId);
    }

}
