package com.personal.recommendation;

import com.hankcs.hanlp.classification.models.NaiveBayesModel;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.personal.recommendation.component.hanLP.service.MyIClassifier;
import com.personal.recommendation.component.hanLP.service.impl.MyNaiveBayesClassifier;
import com.personal.recommendation.utils.TestUtility;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

public class ClassificationTest {
    /**
     * 模型保存路径
     */
    private static final String MAIN_MODEL_PATH = "E:\\documents\\model";
    private static final String TRAIN_FILE_PATH = "E:\\documents\\多层级分类训练集\\mlc_dataset";
    private static final String MODEL_JAR_NAME = "classification-model.ser";
    private static final String SPLIT = "\\";

    public static void main(String[] args) {
        String str = "北京时间6月17日，当湖人以6换1的大手笔交易得到浓眉哥，紫金军团还会有其他大手笔运作吗？" +
                "如今詹姆斯好友发推再爆猛料，表示伦纳德将加盟湖人搭档詹姆斯与浓眉哥，而另外两位湖人首发则是库兹马与隆多，如此阵容成真无疑将是超级重磅炸弹，也将是“NBA新版大结局”。" +
                "当猛龙在总决赛以4-2力克勇士，夺得队史首冠创造历史后，有关伦纳德的去留问题依然悬而未决。尽管伦纳德在猛龙再一次站在总冠军之巅，" +
                "并且成为历史上首位在东西部都夺得FMVP的球员，但在去年夏天被交易到猛龙之前，他的团队曾经表示会在合同到期后离开。只是伦纳德帮助猛龙夺冠，似乎也让不少人相信他会留下来，" +
                "但如今詹姆斯的好友发了一条颇为耐人寻味的推特，他预测了湖人的阵容，其中三个名字是隆多、库兹马与浓眉哥，而皇冠自然是指代詹姆斯，" +
                "外加一个长手掌则是被指代伦纳德，毕竟伦纳德手掌长是极为知名的一个特征。";

        System.out.println("Predict text : " + str);
        String level1 = predict(str, MAIN_MODEL_PATH + SPLIT + MODEL_JAR_NAME, TRAIN_FILE_PATH);
        if(StringUtils.isNotBlank(level1)){
            System.out.println("Level 1 : " + level1);
            String level2 = predict(str, MAIN_MODEL_PATH + SPLIT + level1 + "_" + MODEL_JAR_NAME,
                    TRAIN_FILE_PATH + SPLIT + level1);
            if(StringUtils.isNotBlank(level2)){
                System.out.println("Level 2 : " + level2);
                String level3 = predict(str, MAIN_MODEL_PATH + SPLIT + level1 + "_" + level2 + "_" + MODEL_JAR_NAME,
                        TRAIN_FILE_PATH + SPLIT + level1 + SPLIT + level2);
                if(StringUtils.isNotBlank(level3)){
                    System.out.println("Level 3 : " + level3);
                }
            }

        }


//        // 训练root模型
//        trainOrLoadModel(MAIN_MODEL_PATH + SPLITOR + MODEL_JAR_NAME, TRAIN_FILE_PATH);
//        File file = new File(TRAIN_FILE_PATH);
//        File[] files = file.listFiles();
//        for(File f : files){
//            if(f.isDirectory()){
//                // 训练子模型
//                trainOrLoadModel(MAIN_MODEL_PATH + SPLITOR + f.getName() + "_" +
//                        MODEL_JAR_NAME, TRAIN_FILE_PATH + SPLITOR + f.getName());
//
//                File[] fs = f.listFiles();
//                for(File ff : fs){
//                    if(ff.isDirectory()){
//                        // 训练孙子模型
//                        trainOrLoadModel(MAIN_MODEL_PATH + SPLITOR + f.getName() + "_" + ff.getName() + "_" +
//                                MODEL_JAR_NAME, TRAIN_FILE_PATH + SPLITOR + f.getName() + SPLITOR + ff.getName());
//                    }
//                }
//            }
//        }

    }

    private static String predict(String text, String modelPath, String trainFilePath) {
        MyIClassifier classifier;
        classifier = new MyNaiveBayesClassifier(trainOrLoadModel(modelPath, trainFilePath));
        return classifier.classify(text);
    }

    private static NaiveBayesModel trainOrLoadModel(String modelPath, String trainFilePath) {
        NaiveBayesModel model = (NaiveBayesModel) IOUtil.readObjectFrom(modelPath);
        if (model != null) {
            System.out.println("model " + modelPath + " exist ...");
            return model;
        }else{
            System.out.println("model not exist, trainFilePath : " + trainFilePath);
        }

        String fileStr = TestUtility.ensureTestData(trainFilePath, null);

        File corpusFolder = new File(fileStr);
        if (!corpusFolder.exists() || !corpusFolder.isDirectory()) {
            System.exit(1);
        }

        MyIClassifier classifier = new MyNaiveBayesClassifier();
        try {
            classifier.train(fileStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model = (NaiveBayesModel) classifier.getModel();
        IOUtil.saveObjectTo(model, modelPath);
        return model;
    }

}
