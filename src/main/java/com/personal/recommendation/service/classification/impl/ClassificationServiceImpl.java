package com.personal.recommendation.service.classification.impl;

import com.hankcs.hanlp.corpus.io.IOUtil;
import com.personal.recommendation.component.hanLP.model.MyLinearSVMModel;
import com.personal.recommendation.component.hanLP.service.impl.MyLinearSVMClassifier;
import com.personal.recommendation.constants.ClassificationConstants;
import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.component.hanLP.service.MyIClassifier;
import com.personal.recommendation.service.classification.ClassificationService;
import com.personal.recommendation.utils.TestUtility;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class ClassificationServiceImpl implements ClassificationService {

    private static final Logger logger = Logger.getLogger(ClassificationServiceImpl.class);

    @Override
    public String predict(String text){
        long start = new Date().getTime();
        logger.info("Start level1 prediction text : " + text);
        String modelPath = ClassificationConstants.MAIN_MODEL_PATH
                + ClassificationConstants.SPLIT + ClassificationConstants.SVM_MODEL_JAR_NAME;
        String root = predict(text, modelPath,
                ClassificationConstants.TRAIN_FILE_PATH);
        if(StringUtils.isNotBlank(root)){
            logger.info("Level1 prediction : " + root);
            modelPath = ClassificationConstants.MAIN_MODEL_PATH
                    + ClassificationConstants.SPLIT + root + ClassificationConstants.JOINER
                    + ClassificationConstants.SVM_MODEL_JAR_NAME;
            if(new File(modelPath).exists()) {
                String level2 = predict(text, modelPath, ClassificationConstants.TRAIN_FILE_PATH
                        + ClassificationConstants.SPLIT + root);
                if (StringUtils.isNotBlank(level2)) {
                    logger.info("Level2 prediction : " + level2);
                    modelPath = ClassificationConstants.MAIN_MODEL_PATH
                            + ClassificationConstants.SPLIT + root + ClassificationConstants.JOINER + level2
                            + ClassificationConstants.JOINER + ClassificationConstants.SVM_MODEL_JAR_NAME;
                    if(new File(modelPath).exists()) {
                        String level3 = predict(text, modelPath,
                                ClassificationConstants.TRAIN_FILE_PATH + ClassificationConstants.SPLIT + root
                                        + ClassificationConstants.SPLIT + level2);
                        if (StringUtils.isNotBlank(level3)) {
                            logger.info("Level3 prediction : " + level3);
                            long end = new Date().getTime();
                            logger.info("Classification finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
                            return root + RecommendationConstants.SEPARATOR + level2 + RecommendationConstants.SEPARATOR + level3;
                        }
                    }
                    long end = new Date().getTime();
                    logger.info("Classification finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
                    return root + RecommendationConstants.SEPARATOR + level2;
                }
            }
        }

        long end = new Date().getTime();
        logger.info("Classification finished at " + end + ", time cost : " + (double) ((end - start) / 1000) + "s .");
        return root;
    }

    private String predict(String text, String modelPath, String trainFilePath) {
        MyIClassifier classifier;
        classifier = new MyLinearSVMClassifier(getSVMModel(modelPath, trainFilePath));
        return classifier.classify(text);
    }

    public MyLinearSVMModel getSVMModel(String modelPath, String trainFilePath) {
        if(svmModelMap.containsKey(modelPath)){
            logger.info("model " + modelPath + " exist in cache map ...");
            return svmModelMap.get(modelPath);
        }
        MyLinearSVMModel model = (MyLinearSVMModel) IOUtil.readObjectFrom(modelPath);
        if (model != null) {
            logger.info("model " + modelPath + " exist, add into cache map ...");
            svmModelMap.put(modelPath, model);
            return model;
        }else{
            logger.info("model not exist, trainFilePath : " + trainFilePath);
        }

        String fileStr = TestUtility.ensureTestData(trainFilePath, null);

        File corpusFolder = new File(fileStr);
        if (!corpusFolder.exists() || !corpusFolder.isDirectory()) {
            System.exit(1);
        }

        MyLinearSVMClassifier classifier = new MyLinearSVMClassifier();
        try {
            classifier.train(fileStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model = (MyLinearSVMModel) classifier.getModel();
        IOUtil.saveObjectTo(model, modelPath);

        logger.info("model " + modelPath + " created, add into cache map ...");
        svmModelMap.put(modelPath, model);
        return model;
    }

}
