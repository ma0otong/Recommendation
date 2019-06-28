package com.personal.recommendation.utils;

class StringUtil {

    /**
     * 去除html标签
     * @param content String
     * @return String
     */
    static String getContent(String content){
        return content.replaceAll("<[.[^>]]*>", "");
    }
}
