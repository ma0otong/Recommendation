package com.personal.recommendation.component.hanLP.service;

import com.hankcs.hanlp.classification.models.AbstractModel;
import com.personal.recommendation.component.hanLP.model.MyDocument;

import java.io.IOException;
import java.util.Map;

/**
 * 自定义IClassifier
 */
@SuppressWarnings("unused")
public interface MyIClassifier {

    MyIClassifier enableProbability(boolean var1);

    Map<String, Double> predict(String var1) throws IllegalArgumentException, IllegalStateException;

    Map<String, Double> predict(MyDocument var1) throws IllegalArgumentException, IllegalStateException;

    double[] categorize(MyDocument var1) throws IllegalArgumentException, IllegalStateException;

    int label(MyDocument var1) throws IllegalArgumentException, IllegalStateException;

    String classify(String var1) throws IllegalArgumentException, IllegalStateException;

    String classify(MyDocument var1) throws IllegalArgumentException, IllegalStateException;

    void train(Map<String, String[]> var1) throws IllegalArgumentException;

    void train(String var1, String var2) throws IOException;

    void train(String var1) throws IOException;

    void train(MyIDataSet var1) throws IllegalArgumentException;

    AbstractModel getModel();
}
