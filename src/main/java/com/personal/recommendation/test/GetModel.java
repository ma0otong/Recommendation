package com.personal.recommendation.test;

import com.personal.recommendation.constants.ClassificationConstants;
import com.personal.recommendation.service.classification.impl.ClassificationServiceImpl;

import java.io.File;
import java.util.Objects;

@SuppressWarnings("unused")
public class GetModel {

    public static void main(String[] args){
        getModel(ClassificationConstants.TRAIN_FILE_PATH);
    }

    private static void getModel(String path){
//        File rootDir = new File(path);
//        File[] dirs = rootDir.listFiles();
//        assert dirs != null;
//        for(File file : dirs){
//            new ClassificationServiceImpl().getSVMModel(ClassificationConstants.MAIN_MODEL_PATH
//                    + ClassificationConstants.SPLIT + file.getName() + ClassificationConstants.JOINER
//                    + ClassificationConstants.SVM_MODEL_JAR_NAME, ClassificationConstants.TRAIN_FILE_PATH
//                    + ClassificationConstants.SPLIT + file.getName());
//            if(file.isDirectory()){
//                File[] deepDirs = file.listFiles();
//                if(deepDirs != null) {
//                    for (File f : deepDirs) {
//                        if (f.isDirectory()) {
//                            int count = 0;
//                            for(File ff : Objects.requireNonNull(f.listFiles())){
//                                if (ff.isDirectory()){
//                                    count++;
//                                }
//                            }
//                            if(count > 1) {
//                                System.out.println(f.getPath() + " 有" + count + "个子目录, 开始生产model .");
//                                new ClassificationServiceImpl().getSVMModel(ClassificationConstants.MAIN_MODEL_PATH
//                                                + ClassificationConstants.SPLIT + file.getName() + ClassificationConstants.JOINER + f.getName()
//                                                + ClassificationConstants.JOINER + ClassificationConstants.SVM_MODEL_JAR_NAME,
//                                        ClassificationConstants.TRAIN_FILE_PATH + ClassificationConstants.SPLIT + file.getName()
//                                                + ClassificationConstants.SPLIT + f.getName());
//                            }else{
//                                System.out.println(f.getPath() + " 无子目录, skip .");
//                            }
//                        }
//                    }
//                }
//            }
//        }
        System.out.println("生成主model...");
        new ClassificationServiceImpl().getSVMModel(ClassificationConstants.MAIN_MODEL_PATH
                + ClassificationConstants.SVM_MODEL_JAR_NAME, ClassificationConstants.TRAIN_FILE_PATH);
    }
}
