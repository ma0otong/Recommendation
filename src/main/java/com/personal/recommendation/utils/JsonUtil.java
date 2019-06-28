package com.personal.recommendation.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;

/**
 * Json处理工具类
 */
public class JsonUtil {

    /**
     * 将用户的喜好关键词列表字符串转换为map
     */
    public static CustomizedHashMap<String, CustomizedHashMap<String, Double>> jsonPrefListToMap(String srcJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        CustomizedHashMap<String, CustomizedHashMap<String, Double>> map = null;
        try {
            map = objectMapper.readValue(srcJson, new TypeReference<CustomizedHashMap<String, CustomizedHashMap<String, Double>>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}
