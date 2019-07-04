package com.personal.recommendation.dao;

import com.personal.recommendation.model.News;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * news表DAO
 */
@Service
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

    @Insert("insert into " + TABLE + " set id = #{id},content = #{content}," +
            "title = #{title},url = #{url},module_level_1=#{moduleLevel1}," +
            "module_level_2=#{moduleLevel2},module_level_3=#{moduleLevel3}")
    void insertNews(News news);

    @Select("select distinct(module_level_1) from " + TABLE)
    List<String> getModuleLevel();

    @Select("select * from " + TABLE + " where limit #{limit} offset #{offset}")
    List<News> getNewsByLimitOffset(@Param("limit") int limit, @Param("offset") int offset);

    @Select("select * from " + TABLE + " where module_level_1 = #{moduleLevel} order by news_time desc limit #{limit}")
    List<News> getNewsByModuleLimit(@Param("moduleLevel") String moduleLevel, @Param("limit") int limit);

    @Select("select min(id) from " + TABLE)
    Long getMinId();

    @Select("select max(id) from " + TABLE)
    Long getMaxId();

}
