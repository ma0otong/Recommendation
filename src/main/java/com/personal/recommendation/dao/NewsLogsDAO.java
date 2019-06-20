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
            + TABLE + " where view_time > #{hotDateTime} group by news_id order by visitNums desc limit #{limit}")
    List<NewsLogs> getHotNews(@Param("hotDateTime") Date hotDateTime, @Param("limit") int limit);

    @Select("select news_id from " + TABLE + " where user_id = #{userId}")
    List<Long> getNewsIdByUserId(@Param("userId") long userId);

    @Select("select * from " + TABLE)
    List<NewsLogs> getAll();

    @Select("select * from " + TABLE + " where view_time > #{viewTime} and user_id = #{userId} " +
            "order by view_time desc limit #{recordNum}")
    List<NewsLogs> getNewsLogsByUserViewTime(@Param("viewTime") Date viewTime,
                                             @Param("userId") Long userId, @Param("recordNum") int recordNum);

    @Update("update " + TABLE + " set view_time = #{viewTime} where id > 0")
    void updateViewTime(@Param("viewTime") Date viewTime);

    @Select("select * from " + TABLE + " where user_id = #{userId}")
    List<NewsLogs> getNewsByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "select * from " + TABLE + " where id in",
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<NewsLogs> getNewsByUserIds(@Param("userIds") List<Long> userIds);

    @Insert("insert into " + TABLE + " set user_id = #{userId},news_id = #{newsId},news_module = #{newsModule}")
    void insertNewsLogs(NewsLogs newsLog);

}
