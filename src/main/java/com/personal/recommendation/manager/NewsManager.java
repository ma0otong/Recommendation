package com.personal.recommendation.manager;

import com.personal.recommendation.dao.NewsDAO;
import com.personal.recommendation.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 新闻爬虫内容表Manager
 */
@Service
public class NewsManager {

    private final NewsDAO newsDAO;

    @Autowired
    public NewsManager(NewsDAO newsDAO) {
        this.newsDAO = newsDAO;
    }

    public News getNewsById(Long newsId){
        return newsDAO.getNewsById(newsId);
    }

    public List<News> getNewsByIds(List<Long> newsIds){
        return newsDAO.getNewsByIds(newsIds);
    }

    public List<News> getNewsByModuleLimit(String module, int limit){
        return newsDAO.getNewsByModuleLimit(module, limit);
    }

    public String getTagById(Long id){
        return newsDAO.getTagById(id);
    }

}
