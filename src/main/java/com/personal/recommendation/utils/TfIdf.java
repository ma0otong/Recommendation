package com.personal.recommendation.utils;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import java.util.List;

/**
 * TF-IDF实现类
 */
public class TfIdf {

    /**
     *
     * @param title 文本标题
     * @param content 文本内容
     * @param keyNums 返回的关键词数目
     * @return List<Keyword>
     */
    public static List<Keyword> getTfIde(String title, String content, int keyNums) {
        KeyWordComputer kwc = new KeyWordComputer(keyNums);
        return kwc.computeArticleTfidf(title, content);
    }

    /**
     *
     * @param content 文本内容
     * @param keyNums 返回的关键词数目
     * @return List<Keyword>
     */
    @SuppressWarnings("unused")
    public static List<Keyword> getTfIde(String content, int keyNums) {
        KeyWordComputer kwc = new KeyWordComputer(keyNums);
        return kwc.computeArticleTfidf(content);
    }
}
