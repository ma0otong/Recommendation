package com.personal.recommendation.manager;

import com.personal.recommendation.dao.NewsDAO;
import com.personal.recommendation.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 新闻Manager
 */
@Service
@SuppressWarnings("unused")
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
        List<News> list = new ArrayList<>();
        for(Long id : newsIds){
            News news = newsDAO.getNewsById(id);
            if(news != null){
                list.add(news);
            }
        }
        return list;
    }

    public List<News> getNewsByDateTime(Date dateTime){
        return newsDAO.getNewsByDateTime(dateTime);
    }

    public void updateNewsTime(Date newsTime){
        newsDAO.updateNewsTime(newsTime);
    }

    public Long getRandomNewsByModule(Long moduleId){
        return newsDAO.getRandomNewsByModule(moduleId);
    }

    public int getModuleIdCount(){
        return newsDAO.getModuleIdCount();
    }
}
