package com.personal.recommendation.dao;

import com.personal.recommendation.model.News;
import org.apache.ibatis.annotations.Insert;
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
@SuppressWarnings("unused")
public interface NewsDAO {

    String TABLE = "news";

    @Select("select * from " + TABLE + " where id = #{newsId}")
    News getNewsById(@Param("newsId") long newsId);

    @Select({
            "<script>",
            "select * from " + TABLE + " where id in",
            "<foreach collection='newsIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<News> getNewsByIds(@Param("newsIds") List<Long> newsIds);

    @Select("select * from " + TABLE + " where news_time > #{dateTime}")
    List<News> getNewsByDateTime(@Param("dateTime") Date dateTime);

    @Update("update " + TABLE + " set news_time = #{newsTime}")
    void updateNewsTime(@Param("newsTime") Date newsTime);

    @Insert("insert into " + TABLE + " set id = #{id},content = #{content}," +
            "title = #{title},url = #{url},module_level_1=#{moduleLevel1}," +
            "module_level_2=#{moduleLevel2},module_level_3=#{moduleLevel3}")
    void insertNews(News news);

    @Select("select count(distinct(module_level_1)) from " + TABLE)
    int getModuleLevelCount();

    @Select("select distinct(module_level_1) from " + TABLE)
    List<String> getModuleLevel();

    @Select("select count(*) from " + TABLE)
    int getNewsCount();

    @Select("select * from " + TABLE + " limit #{limit} offset #{offset}")
    List<News> getNewsByLimitOffset(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select * from " + TABLE + " where module_level_1 = #{moduleLevel} and news_time > #{newsTime} limit #{limit}")
    List<News> getNewsByModuleLimit(@Param("moduleLevel") String moduleLevel, @Param("limit") int limit, @Param("newsTime") Date newsTime);

}
