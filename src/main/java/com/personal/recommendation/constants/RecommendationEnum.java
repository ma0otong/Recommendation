package com.personal.recommendation.constants;

/**
 * 推荐算法枚举类
 */
@SuppressWarnings("unused")
public enum RecommendationEnum {

    CF(0,"协同过滤推荐"),
    CB(1,"基于内容推荐"),
    HR(2,"基于热点推荐");

    private int code;
    private String desc;

    RecommendationEnum(int code, String desc){
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
