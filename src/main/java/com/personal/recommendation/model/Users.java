package com.personal.recommendation.model;

import java.util.Date;

/**
 * 用户类
 */
@SuppressWarnings("unused")
public class Users {

    private Long id;
    private String prefList;
    private Date latestLogTime;
    private String name;
    private String userProfile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrefList() {
        return prefList;
    }

    public void setPrefList(String prefList) {
        this.prefList = prefList;
    }

    public Date getLatestLogTime() {
        return latestLogTime;
    }

    public void setLatestLogTime(Date latestLogTime) {
        this.latestLogTime = latestLogTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name + ":" + prefList;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
}
