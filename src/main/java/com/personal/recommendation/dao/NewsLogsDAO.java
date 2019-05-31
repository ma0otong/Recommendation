package com.personal.recommendation.dao;

import com.personal.recommendation.model.NewsLogs;
import org.apache.ibatis.annotations.Insert;
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

    @Select("select news_id,count(*) as visitNums from "
            + TABLE + " where view_time > #{hotDateTime} group by news_id order by visitNums desc")
    List<NewsLogs> getHotNews(@Param("hotDateTime") Date hotDateTime);

    @Select("select news_id from " + TABLE + " where user_id = #{userId}")
    List<Long> getNewsIdByUserId(@Param("userId") long userId);

    @Select("select * from " + TABLE)
    List<NewsLogs> getAll();

    @Select("select * from " + TABLE + " where view_time > #{viewTime} and user_id = #{userId}")
    List<NewsLogs> getNewsLogsByUserViewTime(@Param("viewTime") Date viewTime, @Param("userId") Long userId);

    @Update("update " + TABLE + " set view_time = #{viewTime} where id > 0")
    void updateViewTime(@Param("viewTime") Date viewTime);

    @Select("select * from " + TABLE + " where user_id = #{userId}")
    List<NewsLogs> getNewsByUserId(@Param("userId") Long userId);

    @Insert("insert into " + TABLE + " set user_id = #{userId},news_id = #{newsId}")
    void insertNewsLogs(NewsLogs newsLog);

}
