package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.utilities.CollectionUtility;
import com.hankcs.hanlp.classification.utilities.io.ConsoleLogger;
import com.hankcs.hanlp.utility.MathUtility;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIClassifier;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 自定义MyIClassifier实现类
 */
@SuppressWarnings({"unchecked","unused"})
public abstract class MyClassifierImplement implements MyIClassifier {
    boolean configProbabilityEnabled = true;

    public MyIClassifier enableProbability(boolean enable) {
        return this;
    }

    public String classify(String text) throws IllegalArgumentException, IllegalStateException {
        Map<String, Double> scoreMap = this.predict(text);
        return CollectionUtility.max(scoreMap);
    }


    public String classify(MyDocument document) throws IllegalArgumentException, IllegalStateException {
        Map<String, Double> scoreMap = this.predict(document);
        return CollectionUtility.max(scoreMap);
    }

    public void train(String folderPath, String charsetName) throws IOException {
        MyIDataSet dataSet = new MyMemoryDataSet();
        dataSet.load(folderPath, charsetName);
        this.train(dataSet);
    }

    public void train(Map<String, String[]> trainingDataSet) throws IllegalArgumentException {
        MyIDataSet dataSet = new MyMemoryDataSet();
        ConsoleLogger.logger.start("正在构造训练数据集...");
        int total = trainingDataSet.size();
        int cur = 0;

        for (Entry<String, String[]> stringEntry : trainingDataSet.entrySet()) {
            String category = (String) ((Entry) stringEntry).getKey();
            ConsoleLogger.logger.out("[%s]...", category);
            String[] var8 = (String[]) ((Entry) stringEntry).getValue();
            int var9 = var8.length;

            for (String doc : var8) {
                dataSet.add(category, doc);
            }

            ++cur;
            ConsoleLogger.logger.out("%.2f%%...", MathUtility.percentage((double) cur, (double) total));
        }

        ConsoleLogger.logger.finish(" 加载完毕\n");
        this.train(dataSet);
    }

    public void train(String folderPath) throws IOException {
        this.train(folderPath, "UTF-8");
    }

    public Map<String, Double> predict(MyDocument document) {
        AbstractModel model = this.getModel();
        if (model == null) {
            throw new IllegalStateException("未训练模型！无法执行预测！");
        } else if (document == null) {
            throw new IllegalArgumentException("参数 text == null");
        } else {
            double[] pros = this.categorize(document);
            Map<String, Double> scoreMap = new TreeMap();

            for(int i = 0; i < pros.length; ++i) {
                scoreMap.put(model.catalog[i], pros[i]);
            }

            return scoreMap;
        }
    }

    public int label(MyDocument document) throws IllegalArgumentException, IllegalStateException {
        AbstractModel model = this.getModel();
        if (model == null) {
            throw new IllegalStateException("未训练模型！无法执行预测！");
        } else if (document == null) {
            throw new IllegalArgumentException("参数 text == null");
        } else {
            double[] pros = this.categorize(document);
            double max = -1.0D;
            int best = -1;

            for(int i = 0; i < pros.length; ++i) {
                if (pros[i] > max) {
                    max = pros[i];
                    best = i;
                }
            }

            return best;
        }
    }
}
