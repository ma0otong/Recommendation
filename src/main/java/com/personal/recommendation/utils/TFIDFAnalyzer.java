package com.personal.recommendation.utils;

import com.personal.recommendation.constants.RecommendationConstants;
import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import java.util.*;

/**
 * 基于Ansj分词的TF-IDF算法
 */
public class TFIDFAnalyzer {

    /**
     *
     * @param content 文本内容
     * @return List<Keyword>
     */
    @SuppressWarnings("unchecked")
    public static List<Keyword> getTfIdf(String content) {
        content = StringUtil.getContent(content);
        return new KeyWordComputer(RecommendationConstants.KEY_WORDS_NUM).computeArticleTfidf(content);
    }

    public static void main(String[] args) {
        long start = new Date().getTime();
        String content = "孩子上了幼儿园 安全防拐教育要做好";
        List<Keyword> list = TFIDFAnalyzer.getTfIdf(content);
        for (Keyword word : list)
            System.out.println(word.getName() + ":" + word.getScore() + ",");
        System.out.println(new Date().getTime() - start);
    }
}

