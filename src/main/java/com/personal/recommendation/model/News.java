package com.personal.recommendation.model;

import java.util.Date;

/**
 * 新闻类
 */
@SuppressWarnings("unused")
public class News {

    private Long id;
    private String content;
    private Date newsTime;
    private String url;
    private String title;
    private String moduleLevel1;
    private String moduleLevel2;
    private String moduleLevel3;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getNewsTime() {
        return newsTime;
    }

    public void setNewsTime(Date newsTime) {
        this.newsTime = newsTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModuleLevel1(){
        return moduleLevel1;
    }

    public void setModuleLevel1(String moduleLevel1){
        this.moduleLevel1 = moduleLevel1;
    }

    public String getModuleLevel2(){
        return moduleLevel2;
    }

    public void setModuleLevel2(String moduleLevel2){
        this.moduleLevel2 = moduleLevel2;
    }

    public String getModuleLevel3(){
        return moduleLevel3;
    }

    public void setModuleLevel3(String moduleLevel3){
        this.moduleLevel3 = moduleLevel3;
    }

    @Override
    public String toString(){
        return id + ":" + moduleLevel1 + ":" + content;
    }
}