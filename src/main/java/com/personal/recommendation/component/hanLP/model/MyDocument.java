package com.personal.recommendation.component.hanLP.model;

import com.hankcs.hanlp.classification.collections.FrequencyMap;
import com.hankcs.hanlp.classification.corpus.BagOfWordsDocument;
import com.hankcs.hanlp.classification.corpus.Lexicon;
import com.hankcs.hanlp.collection.trie.ITrie;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * MyDocument
 */
@SuppressWarnings({"unchecked","unused"})
public class MyDocument extends BagOfWordsDocument {
    public int category;

    public MyDocument(MyCatalog catalog, Lexicon lexicon, String category, String[] tokenArray) {
        assert catalog != null;

        assert lexicon != null;

        this.category = catalog.addCategory(category);

        for (String s : tokenArray) {
            this.tfMap.add(lexicon.addWord(s));
        }

    }

    public MyDocument(ITrie<Integer> wordIdTrie, String[] tokenArray) {
        for (String s : tokenArray) {
            Integer id =  wordIdTrie.get(s.toCharArray());
            if (id != null) {
                this.tfMap.add(id);
            }
        }

    }

    public MyDocument(Map<String, Integer> categoryId, BinTrie<Integer> wordId, String category, String[] tokenArray) {
        this(wordId, tokenArray);
        Integer id = categoryId.get(category);
        if (id == null) {
            id = -1;
        }

        this.category = id;
    }

    public MyDocument(DataInputStream in) throws IOException {
        this.category = in.readInt();
        int size = in.readInt();
        this.tfMap = new FrequencyMap();

        for(int i = 0; i < size; ++i) {
            this.tfMap.put(in.readInt(), new int[]{in.readInt()});
        }

    }
}

