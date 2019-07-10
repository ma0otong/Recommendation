package com.personal.recommendation.dao;

import com.personal.recommendation.model.News;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 爬虫新闻内容表DAO
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

    @Select("select * from " + TABLE + " where module = #{module} order by news_time desc limit #{limit}")
    List<News> getNewsByModuleLimit(@Param("module") String module, @Param("limit") int limit);

    @Select("select tag from " + TABLE + " where id = #{id}")
    String getTagById(@Param("id") Long id);

}
