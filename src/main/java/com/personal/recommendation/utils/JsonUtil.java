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
    public static CustomizedHashMap<Integer, CustomizedHashMap<String, Double>> jsonPrefListToMap(String srcJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        CustomizedHashMap<Integer, CustomizedHashMap<String, Double>> map = null;
        try {
            map = objectMapper.readValue(srcJson, new TypeReference<CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}
