package com.personal.recommendation.dao;

import com.personal.recommendation.model.News;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * news表DAO
 */
@Service
public interface NewsDAO {

    String TABLE = "news";

    @Select("select * from " + TABLE + " where id = #{newsId}")
    News getNewsById(@Param("newsId") long newsId);

    @Select("select * from " + TABLE + " where news_time > #{dateTime}")
    List<News> getNewsByDateTime(@Param("dateTime") Date dateTime);

    @Update("update " + TABLE + " set news_time = #{newsTime}")
    void updateNewsTime(@Param("newsTime") Date newsTime);
}
