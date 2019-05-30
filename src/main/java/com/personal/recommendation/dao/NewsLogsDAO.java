package com.personal.recommendation.dao;

import com.personal.recommendation.model.NewsLogs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * news_logs表DAO
 */
@Service
public interface NewsLogsDAO {

    String TABLE = "news_logs";

    //用户id列名
    String PREF_TABLE_USER_ID = "user_id";
    //新闻id列名
    String PREF_TABLE_NEWS_ID = "news_id";
    //用户浏览时间列名
    String PREF_TABLE_TIME = "view_time";

    @Select("select news_id,count(*) as visitNums from "
            + TABLE + " where view_time > #{hotDateTime} group by news_id order by visitNums desc")
    List<NewsLogs> getHotNews(@Param("hotDateTime") Date hotDateTime);

    @Select("select news_id from " + TABLE + " where user_id = #{userId}")
    List<Long> getNewsIdByUserId(@Param("userId") long userId);

    @Select("select * from " + TABLE)
    List<NewsLogs> getAll();

    @Select("select * from " + TABLE + " where view_time > #{viewTime}")
    List<NewsLogs> getNewsLogsByViewTime(@Param("viewTime") Date viewTime);

    @Update("update " + TABLE + " set view_time = #{viewTime}")
    void updateViewTime(@Param("viewTime") Date viewTime);
}
