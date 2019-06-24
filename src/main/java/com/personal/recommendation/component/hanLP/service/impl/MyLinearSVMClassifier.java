package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.features.*;
import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.tokenizers.ITokenizer;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.utility.MathUtility;
import com.personal.recommendation.component.hanLP.model.MyLinearSVMModel;
import com.personal.recommendation.component.hanLP.model.MyBaseFeatureData;
import com.personal.recommendation.component.hanLP.model.MyDfFeatureData;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;
import com.personal.recommendation.component.hanLP.utils.MyChiSquareFeatureExtractor;
import de.bwaldvogel.liblinear.*;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * SVM分类器
 */
@SuppressWarnings("unused")
public class MyLinearSVMClassifier extends MyAbstractClassifier {
    private static final Logger logger = Logger.getLogger(MyLinearSVMClassifier.class);

    private MyLinearSVMModel model;

    public MyLinearSVMClassifier() {
    }

    public MyLinearSVMClassifier(MyLinearSVMModel model) {
        this.model = model;
    }

    public Map<String, Double> predict(String text) throws IllegalArgumentException, IllegalStateException {
        if (model == null) {
            throw new IllegalStateException("未训练模型！无法执行预测！");
        }
        if (text == null) {
            throw new IllegalArgumentException("参数 text == null");
        }

        // 分词，创建文档
        MyDocument document = new MyDocument(model.wordIdTrie, model.tokenizer.segment(text));

        return predict(document);
    }

    @Override
    public double[] categorize(MyDocument document) throws IllegalArgumentException, IllegalStateException {
        FeatureNode[] x = buildDocumentVector(document, model.featureWeighter);
        double[] prob = new double[model.svmModel.getNrClass()];
        Linear.predictProbability(model.svmModel, x, prob);
        return prob;
    }

    @Override
    public void train(MyIDataSet dataSet) {
        if (dataSet.size() == 0) throw new IllegalArgumentException("训练数据集为空,无法继续训练");
        // 选择特征
        MyDfFeatureData featureData = selectFeatures(dataSet);
        // 构造权重计算逻辑
        IFeatureWeighter weighter = new TfIdfFeatureWeighter(dataSet.size(), featureData.df);
        // 构造SVM问题
        Problem problem = createLibLinearProblem(dataSet, featureData, weighter);
        // 释放内存
        BinTrie<Integer> wordIdTrie = featureData.wordIdTrie;
        ITokenizer tokenizer = dataSet.getTokenizer();
        String[] catalog = dataSet.getCatalog().toArray();
        // 求解SVM问题
        Model svmModel = solveLibLinearProblem(problem);
        // 将有用的数据留下来
        model = new MyLinearSVMModel();
        model.tokenizer = tokenizer;
        model.wordIdTrie = wordIdTrie;
        model.catalog = catalog;
        model.svmModel = svmModel;
        model.featureWeighter = weighter;
    }

    public AbstractModel getModel() {
        return model;
    }

    private Model solveLibLinearProblem(Problem problem) {
        Parameter lparam = new Parameter(SolverType.L1R_LR,
                500.,
                0.01);
        return Linear.train(problem, lparam);
    }

    private Problem createLibLinearProblem(MyIDataSet dataSet, MyBaseFeatureData baseFeatureData, IFeatureWeighter weighter) {
        Problem problem = new Problem();
        int n = dataSet.size();
        problem.l = n;
        problem.n = baseFeatureData.featureCategoryJointCount.length;
        problem.x = new FeatureNode[n][];
        problem.y = new double[n];  // 最新版libLinear的y数组是浮点数
        Iterator<MyDocument> iterator = dataSet.iterator();
        for (int i = 0; i < n; i++) {
            // 构造文档向量
            MyDocument document = iterator.next();
            problem.x[i] = buildDocumentVector(document, weighter);
            // 设置样本的y值
            problem.y[i] = document.category;
        }

        return problem;
    }

    private FeatureNode[] buildDocumentVector(MyDocument document, IFeatureWeighter weighter) {
        int termCount = document.tfMap.size();  // 词的个数
        FeatureNode[] x = new FeatureNode[termCount];
        Iterator<Map.Entry<Integer, int[]>> tfMapIterator = document.tfMap.entrySet().iterator();
        for (int j = 0; j < termCount; j++) {
            Map.Entry<Integer, int[]> tfEntry = tfMapIterator.next();
            int feature = tfEntry.getKey();
            int frequency = tfEntry.getValue()[0];
            x[j] = new FeatureNode(feature + 1,  // liblinear 要求下标从1开始递增
                    weighter.weight(feature, frequency));
        }
        // 对向量进行归一化
        double normalizer = 0;
        for (int j = 0; j < termCount; j++) {
            double weight = x[j].getValue();
            normalizer += weight * weight;
        }
        normalizer = Math.sqrt(normalizer);
        for (int j = 0; j < termCount; j++) {
            double weight = x[j].getValue();
            x[j].setValue(weight / normalizer);
        }

        return x;
    }

    /**
     * 统计特征并且执行特征选择，返回一个FeatureStats对象，用于计算模型中的概率
     *
     * @param dataSet MyIDataSet
     * @return MyDfFeatureData
     */
    private MyDfFeatureData selectFeatures(MyIDataSet dataSet) {
        MyChiSquareFeatureExtractor chiSquareFeatureExtractor = new MyChiSquareFeatureExtractor();

        // FeatureStats对象包含文档中所有特征及其统计信息
        MyDfFeatureData featureData = new MyDfFeatureData(dataSet); //执行统计

        logger.info("使用卡方检测选择特征中...");
        // 传入这些统计信息到特征选择算法中,得到特征与其分值
        Map<Integer, Double> selectedFeatures = chiSquareFeatureExtractor.chi_square(featureData);

        // 获取dataset中的字符串数组
        String[] wordIdArray = dataSet.getLexicon().getWordIdArray();
        int[] idMap = new int[wordIdArray.length];
        Arrays.fill(idMap, -1);
        featureData.wordIdTrie = new BinTrie<>();
        featureData.df = new int[selectedFeatures.size()];
        int p = -1;
        for (Integer feature : selectedFeatures.keySet()) {
            ++p;
            featureData.wordIdTrie.put(wordIdArray[feature], p);
            featureData.df[p] = MathUtility.sum(featureData.featureCategoryJointCount[feature]);
            idMap[feature] = p;
        }
        logger.info(String.format("选中特征数:%d / %d = %.2f%%\n", selectedFeatures.size(),
                featureData.featureCategoryJointCount.length,
                MathUtility.percentage(selectedFeatures.size(), featureData.featureCategoryJointCount.length)));
        logger.info("缩减训练数据中...");
        int n = dataSet.size();
        dataSet.shrink(idMap);
        logger.info(String.format("缩减了 %d 个样本,剩余 %d 个样本\n", n - dataSet.size(), dataSet.size()));

        return featureData;
    }
}
