package com.personal.recommendation.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * TF-IDF算法原理参考：http://www.cnblogs.com/ywl925/p/3275878.html
 * 部分实现思路参考jieBa分词：https://github.com/fxsjy/jieba
 */
public class TFIDFAnalyzer {

    private static HashMap<String, Double> IDF_MAP;
    private static HashSet<String> STOP_WORD_SET;
    private static double IDF_MEDIAN;

    /**
     * TF-IDF
     *
     * @param content 需要分析的文本/文档内容
     * @param topN    需要返回的tfIdf值最高的N个关键词，若超过content本身含有的词语上限数目，则默认返回全部
     * @return List<Keyword>
     */
    private List<Keyword> analyze(String content, int topN) {
        List<Keyword> keywordList = new ArrayList<>();

        if (STOP_WORD_SET == null) {
            STOP_WORD_SET = new HashSet<>();
            loadStopWords(STOP_WORD_SET, this.getClass().getResourceAsStream("/stop_words.txt"));
        }
        if (IDF_MAP == null) {
            IDF_MAP = new HashMap<>();
            loadIDFMap(IDF_MAP, this.getClass().getResourceAsStream("/idf_dict.txt"));
        }

        Map<String, Double> tfMap = getTF(content);
        for (String word : tfMap.keySet()) {
            // 若该词不在idf文档中，则使用平均的idf值(可能定期需要对新出现的网络词语进行纳入)
            if (IDF_MAP.containsKey(word)) {
                keywordList.add(new Keyword(word, IDF_MAP.get(word) * tfMap.get(word)));
            } else
                keywordList.add(new Keyword(word, IDF_MEDIAN * tfMap.get(word)));
        }

        Collections.sort(keywordList);

        if (keywordList.size() > topN) {
            int num = keywordList.size() - topN;
            for (int i = 0; i < num; i++) {
                keywordList.remove(topN);
            }
        }
        return keywordList;
    }

    /**
     * tf值计算公式
     * tf=N(i,j)/(sum(N(k,j) for all k))
     * N(i,j)表示词语Ni在该文档d（content）中出现的频率，sum(N(k,j))代表所有词语在文档d中出现的频率之和
     *
     * @param content String
     * @return Map<String, Double>
     */
    private Map<String, Double> getTF(String content) {
        Map<String, Double> tfMap = new HashMap<>();
        if (content == null || content.equals(""))
            return tfMap;

        List<String> segments = new JiebaSegmenter().sentenceProcess(content);
        Map<String, Integer> freqMap = new HashMap<>();

        int wordSum = 0;
        for (String segment : segments) {
            //停用词不予考虑，单字词不予考虑
            if (!STOP_WORD_SET.contains(segment) && segment.length() > 1) {
                wordSum++;
                if (freqMap.containsKey(segment)) {
                    freqMap.put(segment, freqMap.get(segment) + 1);
                } else {
                    freqMap.put(segment, 1);
                }
            }
        }

        // 计算double型的tf值
        for (String word : freqMap.keySet()) {
            tfMap.put(word, (double)freqMap.get(word) / wordSum);
        }

        return tfMap;
    }

    /**
     * 默认jieBa分词的停词表
     * url:https://github.com/yanyiwu/nodejieba/blob/master/dict/stop_words.utf8
     *
     * @param set set
     * @param in  InputStream
     */
    private static void loadStopWords(Set<String> set, InputStream in) {
        BufferedReader buff;
        try {
            buff = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = buff.readLine()) != null) {
                set.add(line.trim());
            }
            try {
                buff.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * idf值本来需要语料库来自己按照公式进行计算，不过jieBa分词已经提供了一份很好的idf字典，所以默认直接使用jieBa分词的idf字典
     * url:https://raw.githubusercontent.com/yanyiwu/nodejieba/master/dict/idf.utf8
     *
     * @param map map
     * @param in  InputStream
     */
    private static void loadIDFMap(Map<String, Double> map, InputStream in) {
        BufferedReader buff;
        try {
            buff = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = buff.readLine()) != null) {
                String[] kv = line.trim().split(" ");
                map.put(kv[0], Double.parseDouble(kv[1]));
            }
            try {
                buff.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 计算idf值的中位数
            List<Double> idfList = new ArrayList<>(map.values());
            Collections.sort(idfList);
            IDF_MEDIAN = idfList.get(idfList.size() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param content 文本内容
     * @param keyNums 返回的关键词数目
     * @return List<Keyword>
     */
    public static List<Keyword> getTfIde(String content, int keyNums) {
        return new TFIDFAnalyzer().analyze(content.replaceAll("\"",""), keyNums);
    }

    public static void main(String[] args) {
        String content = "孩子上了幼儿园 安全防拐教育要做好";
        int topN = 5;
        TFIDFAnalyzer tfidfAnalyzer = new TFIDFAnalyzer();
        List<Keyword> list = tfidfAnalyzer.analyze(content, topN);
        for (Keyword word : list)
            System.out.print(word.getName() + ":" + word.getTfIdfValue() + ",");
    }
}

