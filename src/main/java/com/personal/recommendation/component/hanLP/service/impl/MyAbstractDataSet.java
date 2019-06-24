package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.corpus.Lexicon;
import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.tokenizers.HanLPTokenizer;
import com.hankcs.hanlp.classification.tokenizers.ITokenizer;
import com.hankcs.hanlp.classification.utilities.TextProcessUtility;
import com.hankcs.hanlp.classification.utilities.io.ConsoleLogger;
import com.hankcs.hanlp.utility.MathUtility;
import com.personal.recommendation.component.hanLP.model.MyCatalog;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MyIDataSet实现类
 */
@SuppressWarnings("unused")
public abstract class MyAbstractDataSet implements MyIDataSet {
    private ITokenizer tokenizer;
    private MyCatalog catalog;
    private Lexicon lexicon;
    private boolean testingDataSet;

    MyAbstractDataSet(AbstractModel model) {
        this.lexicon = new Lexicon(model.wordIdTrie);
        this.tokenizer = model.tokenizer;
        this.catalog = new MyCatalog(model.catalog);
        this.testingDataSet = true;
    }

    MyAbstractDataSet() {
        this.tokenizer = new HanLPTokenizer();
        this.catalog = new MyCatalog();
        this.lexicon = new Lexicon();
    }

    public MyIDataSet setTokenizer(ITokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    public MyDocument convert(String category, String text) {
        String[] tokenArray = this.tokenizer.segment(text);
        return this.testingDataSet ? new MyDocument(this.catalog.categoryId, this.lexicon.wordId, category, tokenArray) : new MyDocument(this.catalog, this.lexicon, category, tokenArray);
    }

    public ITokenizer getTokenizer() {
        return this.tokenizer;
    }

    public MyCatalog getCatalog() {
        return this.catalog;
    }

    public Lexicon getLexicon() {
        return this.lexicon;
    }

    public MyIDataSet load(String folderPath, String charsetName) throws IllegalArgumentException {
        return this.load(folderPath, charsetName, 1.0D);
    }

    public MyIDataSet load(String folderPath) throws IllegalArgumentException {
        return this.load(folderPath, "UTF-8");
    }

    public boolean isTestingDataSet() {
        return this.testingDataSet;
    }

    public MyIDataSet load(String folderPath, String charsetName, double percentage) throws IllegalArgumentException {
        if (folderPath == null) {
            throw new IllegalArgumentException("参数 folderPath == null");
        } else {
            File root = new File(folderPath);
            if (!root.exists()) {
                throw new IllegalArgumentException(String.format("目录 %s 不存在", root.getAbsolutePath()));
            } else if (!root.isDirectory()) {
                throw new IllegalArgumentException(String.format("目录 %s 不是一个目录", root.getAbsolutePath()));
            } else if (percentage <= 1.0D && percentage >= -1.0D) {
                File[] folders = root.listFiles();
                if (folders == null) {
                    return null;
                } else {
                    ConsoleLogger.logger.start("模式:%s\n文本编码:%s\n根目录:%s\n加载中...\n", this.testingDataSet ? "测试集" : "训练集", charsetName, folderPath);
                    int var8 = folders.length;

                    int totalCount = 0;
                    for (File folder : folders) {
                        if (!folder.isFile()) {
                            File[] files = folder.listFiles();
                            if (files != null) {
                                String category = folder.getName();
                                ConsoleLogger.logger.out("[%s]...", category);
                                int b;
                                int e;
                                if (percentage > 0.0D) {
                                    b = 0;
                                    e = (int) ((double) files.length * percentage);
                                } else {
                                    b = (int) ((double) files.length * (1.0D + percentage));
                                    e = files.length;
                                }

                                int logEvery = (int) Math.ceil((double) ((float) (e - b) / 10000.0F));

                                int fileCount = 0;
                                for (int i = b; i < e; ++i) {
                                    boolean fileAccess = false;
                                    // 遍历目录下所有文件进行训练
                                    List<File> fileList = new ArrayList<>();
                                    getFileList(files[i], fileList);
                                    for(File file : fileList){
                                        try {
                                            // 加入训练集
                                            this.add(folder.getName(), TextProcessUtility.readTxt(file, charsetName));
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }
                                        fileCount++;
                                    }
                                    if (i % logEvery == 0) {
                                        ConsoleLogger.logger.out("%c[%s]...%.2f%%", 13, category, MathUtility.percentage((double) (i - b + 1), (double) (e - b)));
                                    }
                                }
                                totalCount += fileCount;
                                ConsoleLogger.logger.out(" %d 篇文档\n", fileCount);
                            }
                        }
                    }

                    ConsoleLogger.logger.finish(" 加载了 %d 个类目,共 %d 篇文档\n", this.getCatalog().size(), totalCount);
                    return this;
                }
            } else {
                throw new IllegalArgumentException("percentage 的绝对值必须介于[0, 1]之间");
            }
        }
    }

    public MyIDataSet load(String folderPath, double rate) throws IllegalArgumentException {
        return null;
    }

    public MyIDataSet add(Map<String, String[]> testingDataSet) {

        for (Entry<String, String[]> stringEntry : testingDataSet.entrySet()) {
            String[] var4 = (String[]) ((Entry) stringEntry).getValue();

            for (String document : var4) {
                this.add((String) ((Entry) stringEntry).getKey(), document);
            }
        }

        return this;
    }

    private static void getFileList(File dir, List<File> fileList) {
        if(!dir.isDirectory()){
            fileList.add(dir);
        }else {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getFileList(file, fileList);
                    } else {
                        fileList.add(file);
                    }
                }
            }
        }
    }

}
