package com.personal.recommendation.constant;

/**
 * 推荐算法枚举类
 */
@SuppressWarnings("unused")
public enum RecommendEnum {

    CF(0,"协同过滤"),
    CB(1,"基于内容"),
    HR(2,"基于热点");

    private int code;
    private String desc;

    RecommendEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
