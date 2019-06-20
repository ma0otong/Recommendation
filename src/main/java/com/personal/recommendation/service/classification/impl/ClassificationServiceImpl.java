package com.personal.recommendation.service.classification.impl;

import com.hankcs.hanlp.classification.models.NaiveBayesModel;
import com.hankcs.hanlp.corpus.io.IOUtil;
import com.personal.recommendation.constants.ClassificationConstants;
import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.component.hanLP.service.impl.MyNaiveBayesClassifier;
import com.personal.recommendation.component.hanLP.service.MyIClassifier;
import com.personal.recommendation.service.classification.ClassificationService;
import com.personal.recommendation.utils.TestUtility;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ClassificationServiceImpl implements ClassificationService {

    private static final Logger logger = Logger.getLogger(ClassificationServiceImpl.class);

    @Override
    public String predict(String text){
        logger.info("Start level1 prediction text : " + text);
        String root = predict(text, ClassificationConstants.MAIN_MODEL_PATH
                + ClassificationConstants.SPLIT + ClassificationConstants.MODEL_JAR_NAME,
                ClassificationConstants.TRAIN_FILE_PATH);
        if(StringUtils.isNotBlank(root)){
            logger.info("Start level2 prediction text : " + text);
            String level2 = predict(text, ClassificationConstants.MAIN_MODEL_PATH
                    + ClassificationConstants.SPLIT + root + ClassificationConstants.JOINER
                    + ClassificationConstants.MODEL_JAR_NAME, ClassificationConstants.TRAIN_FILE_PATH
                    + ClassificationConstants.SPLIT + root);
            if(StringUtils.isNotBlank(level2)){
                logger.info("Start level3 prediction text : " + text);
                String level3 = predict(text, ClassificationConstants.MAIN_MODEL_PATH
                        + ClassificationConstants.SPLIT + root + ClassificationConstants.JOINER + level2
                        + ClassificationConstants.JOINER + ClassificationConstants.MODEL_JAR_NAME,
                        ClassificationConstants.TRAIN_FILE_PATH + ClassificationConstants.SPLIT + root
                                + ClassificationConstants.SPLIT + level2);
                if(StringUtils.isNotBlank(level3))
                    return root + RecommendationConstants.SEPARATOR + level2 + RecommendationConstants.SEPARATOR + level3;
                else
                    return root + RecommendationConstants.SEPARATOR + level2;
            }
        }
        return root;
    }

    private String predict(String text, String modelPath, String trainFilePath) {
        MyIClassifier classifier;
        classifier = new MyNaiveBayesClassifier(trainOrLoadModel(modelPath, trainFilePath));
        return classifier.classify(text);
    }

    @Override
    public NaiveBayesModel trainOrLoadModel(String modelPath, String trainFilePath) {
        if(modelMap.containsKey(modelPath)){
            logger.info("model " + modelPath + " exist in cache map ...");
            return modelMap.get(modelPath);
        }
        NaiveBayesModel model = (NaiveBayesModel) IOUtil.readObjectFrom(modelPath);
        if (model != null) {
            logger.info("model " + modelPath + " exist, add into cache map ...");
            modelMap.put(modelPath, model);
            return model;
        }else{
            logger.info("model not exist, trainFilePath : " + trainFilePath);
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

        logger.info("model " + modelPath + " created, add into cache map ...");
        modelMap.put(modelPath, model);
        return model;
    }
}
