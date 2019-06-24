package com.personal.recommendation.service.classification;

import com.hankcs.hanlp.classification.models.NaiveBayesModel;
import com.personal.recommendation.component.hanLP.model.MyLinearSVMModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 内容分类服务接口
 */
public interface ClassificationService {

    Map<String, NaiveBayesModel> bayesModelMap = new HashMap<>();

    Map<String, MyLinearSVMModel> svmModelMap = new HashMap<>();

    /**
     * 根据模型预测内容分类
     * @param text String
     * @return String
     */
    String predict(String text);

}
