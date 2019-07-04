package com.personal.recommendation.manager;

import com.personal.recommendation.dao.NewsDAO;
import com.personal.recommendation.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 新闻Manager
 */
@Service
public class NewsManager  {

    private final NewsDAO newsDAO;
    @Autowired
    public NewsManager(NewsDAO newsDAO) {
        this.newsDAO = newsDAO;
    }

    public News getNewsById(long newsId){
        return newsDAO.getNewsById(newsId);
    }

    public List<News> getNewsByIds(List<Long> newsIds){
        return newsDAO.getNewsByIds(newsIds);
    }

    public List<String> getModuleLevel(){
        return newsDAO.getModuleLevel();
    }

    public List<News> getNewsByModuleLimit(String moduleLevel, int limit){
        return newsDAO.getNewsByModuleLimit(moduleLevel, limit);
    }

    public Long getMinId(){
        return newsDAO.getMinId();
    }

    public Long getMaxId(){
        return newsDAO.getMaxId();
    }


}
