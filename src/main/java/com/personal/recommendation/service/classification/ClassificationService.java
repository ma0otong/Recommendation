package com.personal.recommendation.service.classification;

import com.hankcs.hanlp.classification.models.NaiveBayesModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 内容分类服务接口
 */
public interface ClassificationService {

    Map<String, NaiveBayesModel> modelMap = new HashMap<>();

    /**
     * 根据模型预测内容分类
     * @param text String
     * @return String
     */
    String predict(String text);

    /**
     * 获取朴素贝叶斯训练模型
     * @param modelPath String
     * @param trainFilePath String
     * @return NaiveBayesModel
     */
    NaiveBayesModel trainOrLoadModel(String modelPath, String trainFilePath);

}
