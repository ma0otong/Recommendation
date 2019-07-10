package com.personal.recommendation.utils;

/**
 * 字符串工具类
 */
public class StringUtil {

    /**
     * 去除html标签
     * @param content String
     * @return String
     */
    public static String getContent(String content){
        return content.replaceAll("<[.[^>]]*>", "");
    }
}
