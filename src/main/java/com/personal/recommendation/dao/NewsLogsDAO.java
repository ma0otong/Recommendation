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

    @Select("select news_id,count(*) as visitNum from "
            + TABLE + " where view_time > #{hotDateTime} group by news_id order by visitNum desc limit #{limit}")
    List<NewsLogs> getHotNews(@Param("hotDateTime") Date hotDateTime, @Param("limit") int limit);

    @Select("select news_id from " + TABLE + " where user_id = #{userId}")
    List<Long> getNewsIdByUserId(@Param("userId") long userId);

    @Select("select * from " + TABLE + " where view_time > #{viewTime} and user_id = #{userId} " +
            "order by view_time desc limit #{recordNum}")
    List<NewsLogs> getNewsLogsByUserViewTime(@Param("viewTime") Date viewTime,
                                             @Param("userId") Long userId, @Param("recordNum") int recordNum);

    @Update("update " + TABLE + " set view_time = #{viewTime} where id = #{id}")
    void updateViewTime(@Param("viewTime") Date viewTime, @Param("id") Long id);

    @Insert("insert into " + TABLE + " set user_id = #{userId},news_id = #{newsId},news_module = #{newsModule}")
    void insertNewsLogs(NewsLogs newsLog);

    @Select("select * from " + TABLE + " where user_id = #{userId} and news_id = #{newsId} limit 1")
    NewsLogs getUserLogByUserId(@Param("userId") Long userId, @Param("newsId") Long newsId);

}
