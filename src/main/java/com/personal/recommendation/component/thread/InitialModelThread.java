package com.personal.recommendation.component.thread;

import com.hankcs.hanlp.corpus.io.IOUtil;
import com.personal.recommendation.component.hanLP.model.MyLinearSVMModel;
import com.personal.recommendation.constants.ClassificationConstants;
import com.personal.recommendation.service.classification.ClassificationService;
import org.apache.log4j.Logger;

import java.io.File;

public class InitialModelThread implements Runnable {

    private static final Logger logger = Logger.getLogger(InitialModelThread.class);

    @Override
    public void run() {
        logger.info("Start initializing model files .");
        File modelDir = new File(ClassificationConstants.MAIN_MODEL_PATH);
        File[] files = modelDir.listFiles();
        assert files != null;
        for(File f : files){
            MyLinearSVMModel model = (MyLinearSVMModel) IOUtil.readObjectFrom(f.getPath());
            if(model != null) {
                ClassificationService.svmModelMap.put(f.getPath(), model);
                logger.info(f.getPath() + " initialized .");
            }
        }
        logger.info("InitialModelThread end  .");
    }
}
