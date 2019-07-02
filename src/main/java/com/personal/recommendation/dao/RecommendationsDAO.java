package com.personal.recommendation.dao;

import com.personal.recommendation.model.Recommendations;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * recommendations表DAO
 */
@Service
public interface RecommendationsDAO {

    String TABLE = "recommendations";

    @Select("select * from " + TABLE + " where user_id = #{userId} and feedback = 0 order by derive_time desc")
    List<Recommendations> getNewsByUserId(@Param("userId") long userId);

    @Select("select * from " + TABLE + " where user_id = #{userId} and feedback = 0 and derive_algorithm = #{type} order by derive_time desc limit #{limit}")
    List<Recommendations> getNewsByUserIdType(@Param("userId") long userId, @Param("limit") int limit, @Param("type") String type);

    @Select("select news_id from " + TABLE + " where user_id = #{userId}")
    List<Long> getNewsIdByUserId(@Param("userId") long userId);

    @Select("select news_id from " + TABLE + " where user_id = #{userId} and derive_time > #{deriveTime}")
    List<Long> getNewsIdByUserDeriveTime(@Param("userId") long userId, @Param("deriveTime") Date deriveTime);

    @Select("select count(*) from " + TABLE + " where derive_time > #{deriveTime} and user_id = #{userId} " +
            "group by user_id limit 1")
    int getUserRecNumByDeriveTime(@Param("userId") long userId, @Param("deriveTime") Date deriveTime);

    @Insert("insert into " + TABLE + " set user_id = #{userId},news_id = #{newsId},derive_algorithm = #{deriveAlgorithm}")
    void insertRecommendation(Recommendations recommendation);

    @Select("select * from " + TABLE + " where user_id = #{userId} and news_id = #{newsId}")
    Recommendations getRecommendationByUserAndNewsId(@Param("userId") Long userId, @Param("newsId") Long newsId);

    @Update("update " + TABLE + " set feedback = #{feedback} where id = #{id}")
    void updateFeedBackById(@Param("id") Long id, @Param("feedback") int feedback);
}
