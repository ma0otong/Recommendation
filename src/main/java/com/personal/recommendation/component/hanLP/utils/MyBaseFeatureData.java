package com.personal.recommendation.component.hanLP.utils;

import com.hankcs.hanlp.classification.corpus.Lexicon;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.personal.recommendation.component.hanLP.model.MyCatalog;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.util.Iterator;
import java.util.Map;

/**
 * 自定义BaseFeatureData
 */
public class MyBaseFeatureData {
    public int n;
    public int[][] featureCategoryJointCount;
    public int[] categoryCounts;
    public BinTrie<Integer> wordIdTrie;

    MyBaseFeatureData(MyIDataSet dataSet) {
        MyCatalog catalog = dataSet.getCatalog();
        Lexicon lexicon = dataSet.getLexicon();
        this.n = dataSet.size();
        this.featureCategoryJointCount = new int[lexicon.size()][catalog.size()];
        this.categoryCounts = new int[catalog.size()];

        for (MyDocument document : dataSet) {
            int var10002 = this.categoryCounts[document.category]++;

            Map.Entry entry;
            for (Iterator var6 = document.tfMap.entrySet().iterator(); var6.hasNext(); var10002 = this.featureCategoryJointCount[(Integer) entry.getKey()][document.category]++) {
                entry = (Map.Entry) var6.next();
            }
        }

    }
}
