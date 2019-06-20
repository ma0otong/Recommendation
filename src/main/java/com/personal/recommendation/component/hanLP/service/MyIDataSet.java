package com.personal.recommendation.component.hanLP.service;

import com.hankcs.hanlp.classification.corpus.Lexicon;
import com.hankcs.hanlp.classification.tokenizers.ITokenizer;
import com.personal.recommendation.component.hanLP.model.MyCatalog;
import com.personal.recommendation.component.hanLP.model.MyDocument;

import java.io.IOException;
import java.util.Map;

/**
 * 自定义IDataSet
 */
@SuppressWarnings("unused")
public interface MyIDataSet extends Iterable<MyDocument> {
    MyIDataSet load(String var1) throws IllegalArgumentException, IOException;

    MyIDataSet load(String var1, double var2) throws IllegalArgumentException, IOException;

    MyIDataSet load(String var1, String var2) throws IllegalArgumentException, IOException;

    MyIDataSet load(String var1, String var2, double var3) throws IllegalArgumentException, IOException;

    MyDocument add(String var1, String var2);

    MyDocument convert(String var1, String var2);

    MyIDataSet setTokenizer(ITokenizer var1);

    int size();

    ITokenizer getTokenizer();

    MyCatalog getCatalog();

    Lexicon getLexicon();

    void clear();

    boolean isTestingDataSet();

    MyIDataSet add(Map<String, String[]> var1);

    MyIDataSet shrink(int[] var1);
}
