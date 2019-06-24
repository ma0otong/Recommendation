package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.models.NaiveBayesModel;
import com.hankcs.hanlp.classification.utilities.io.ConsoleLogger;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.utility.MathUtility;
import com.personal.recommendation.component.hanLP.model.MyBaseFeatureData;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;
import com.personal.recommendation.component.hanLP.utils.MyChiSquareFeatureExtractor;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 自定义分类器实现类
 */
@SuppressWarnings({"unchecked","unused"})
public class MyNaiveBayesClassifier extends MyClassifierImplement {

    private NaiveBayesModel model;

    public MyNaiveBayesClassifier(NaiveBayesModel naiveBayesModel) {
        this.model = naiveBayesModel;
    }

    public MyNaiveBayesClassifier() {
        this(null);
    }

    public NaiveBayesModel getNaiveBayesModel() {
        return this.model;
    }

    public void train(MyIDataSet dataSet) {
        ConsoleLogger.logger.out("原始数据集大小:%d\n", dataSet.size());
        MyBaseFeatureData featureData = this.selectFeatures(dataSet);
        this.model = new NaiveBayesModel();
        this.model.n = featureData.n;
        this.model.d = featureData.featureCategoryJointCount.length;
        this.model.c = featureData.categoryCounts.length;
        this.model.logPriors = new TreeMap();

        for(int category = 0; category < featureData.categoryCounts.length; ++category) {
            int sumCategory = featureData.categoryCounts[category];
            this.model.logPriors.put(category, Math.log((double)sumCategory / (double)this.model.n));
        }

        Map<Integer, Double> featureOccurrencesInCategory = new TreeMap();

        for (Integer category : this.model.logPriors.keySet()) {
            double featureOccSum = 0.0D;

            for (int feature = 0; feature < featureData.featureCategoryJointCount.length; ++feature) {
                featureOccSum = featureOccSum + (double) featureData.featureCategoryJointCount[feature][category];
            }

            featureOccurrencesInCategory.put(category, featureOccSum);
        }

        for (Integer category : this.model.logPriors.keySet()) {
            for (int feature = 0; feature < featureData.featureCategoryJointCount.length; ++feature) {
                int[] featureCategoryCounts = featureData.featureCategoryJointCount[feature];
                int count = featureCategoryCounts[category];
                double logLikelihood = Math.log(((double) count + 1.0D) / (featureOccurrencesInCategory.get(category) + (double) this.model.d));
                if (!this.model.logLikelihoods.containsKey(feature)) {
                    this.model.logLikelihoods.put(feature, new TreeMap());
                }

                ((Map) this.model.logLikelihoods.get(feature)).put(category, logLikelihood);
            }
        }

        ConsoleLogger.logger.out("贝叶斯统计结束\n");
        this.model.catalog = dataSet.getCatalog().toArray();
        this.model.tokenizer = dataSet.getTokenizer();
        this.model.wordIdTrie = featureData.wordIdTrie;
    }

    public AbstractModel getModel() {
        return this.model;
    }

    public Map<String, Double> predict(String text) throws IllegalArgumentException, IllegalStateException {
        if (this.model == null) {
            throw new IllegalStateException("未训练模型！无法执行预测！");
        } else if (text == null) {
            throw new IllegalArgumentException("参数 text == null");
        } else {
            MyDocument doc = new MyDocument(this.model.wordIdTrie, this.model.tokenizer.segment(text));
            return this.predict(doc);
        }
    }

    public double[] categorize(MyDocument document) throws IllegalArgumentException, IllegalStateException {
        double[] predictionScores = new double[this.model.catalog.length];

        Integer category;
        Double logprob;
        for(Iterator var7 = this.model.logPriors.entrySet().iterator(); var7.hasNext(); predictionScores[category] = logprob) {
            Map.Entry<Integer, Double> entry1 = (Map.Entry)var7.next();
            category = entry1.getKey();
            logprob = entry1.getValue();

            for (Map.Entry<Integer, int[]> integerEntry : document.tfMap.entrySet()) {
                Integer feature = (Integer) ((Map.Entry) integerEntry).getKey();
                if (this.model.logLikelihoods.containsKey(feature)) {
                    int occurrences = ((int[]) ((Map.Entry) integerEntry).getValue())[0];
                    logprob = logprob + (double) occurrences * (Double) ((Map) this.model.logLikelihoods.get(feature)).get(category);
                }
            }
        }

        if (this.configProbabilityEnabled) {
            MathUtility.normalizeExp(predictionScores);
        }

        return predictionScores;
    }

    private MyBaseFeatureData selectFeatures(MyIDataSet dataSet) {
        MyChiSquareFeatureExtractor chiSquareFeatureExtractor = new MyChiSquareFeatureExtractor();
        ConsoleLogger.logger.start("使用卡方检测选择特征中...");
        MyBaseFeatureData featureData = MyChiSquareFeatureExtractor.extractBasicFeatureData(dataSet);
        Map<Integer, Double> selectedFeatures = chiSquareFeatureExtractor.chi_square(featureData);
        int[][] featureCategoryJointCount = new int[selectedFeatures.size()][];
        featureData.wordIdTrie = new BinTrie();
        String[] wordIdArray = dataSet.getLexicon().getWordIdArray();
        int p = -1;

        for (Integer feature : selectedFeatures.keySet()) {
            ++p;
            featureCategoryJointCount[p] = featureData.featureCategoryJointCount[feature];
            featureData.wordIdTrie.put(wordIdArray[feature], p);
        }

        ConsoleLogger.logger.finish(",选中特征数:%d / %d = %.2f%%\n", featureCategoryJointCount.length,
                featureData.featureCategoryJointCount.length, (double)featureCategoryJointCount.length /
                        (double)featureData.featureCategoryJointCount.length * 100.0D);
        featureData.featureCategoryJointCount = featureCategoryJointCount;
        return featureData;
    }
}
