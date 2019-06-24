package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.utilities.CollectionUtility;
import com.hankcs.hanlp.classification.utilities.io.ConsoleLogger;
import com.hankcs.hanlp.utility.MathUtility;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIClassifier;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused")
public abstract class MyAbstractClassifier implements MyIClassifier {

    MyAbstractClassifier() {
    }

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

        for (Map.Entry<String, String[]> stringEntry : trainingDataSet.entrySet()) {
            String category = (String) ((Map.Entry) stringEntry).getKey();
            ConsoleLogger.logger.out("[%s]...", category);
            String[] var8 = (String[]) ((Map.Entry) stringEntry).getValue();

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
            double[] probs = this.categorize(document);
            Map<String, Double> scoreMap = new TreeMap();

            for(int i = 0; i < probs.length; ++i) {
                scoreMap.put(model.catalog[i], probs[i]);
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
            double[] prob = this.categorize(document);
            double max = -1.0D / 0.0;
            int best = -1;

            for(int i = 0; i < prob.length; ++i) {
                if (prob[i] > max) {
                    max = prob[i];
                    best = i;
                }
            }

            return best;
        }
    }
}
