package com.personal.recommendation;

import java.io.File;

import org.apache.mahout.cf.taste.eval.*;
import org.apache.mahout.cf.taste.impl.eval.*;
import org.apache.mahout.cf.taste.impl.model.file.*;
import org.apache.mahout.cf.taste.impl.neighborhood.*;
import org.apache.mahout.cf.taste.impl.recommender.*;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.*;
import org.apache.mahout.cf.taste.neighborhood.*;
import org.apache.mahout.cf.taste.similarity.*;

public class RecommendEvaluator {

    public static void main(String[] args) throws Exception {

        // 准备数据 这里是电影评分数据
        File file = new File("E:\\documents\\ml-20m\\ratings.csv");
        // 将数据加载到内存中，GroupLensDataModel是针对开放电影评论数据的
        DataModel dataModel = new FileDataModel(file);
        RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
        RecommenderBuilder recommenderBuilder = model -> {
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(4, similarity, model);
            return new GenericUserBasedRecommender(model, neighborhood, similarity);
        };
        // 计算推荐4个结果时的查准率和召回率
        // 使用评估器，并设定评估期的参数
        // 4表示"precision and recall at 4"即相当于推荐top4，然后在top-4的推荐上计算准确率和召回率
        IRStatistics stats = statsEvaluator.evaluate(recommenderBuilder, null, dataModel, null, 4, GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);
        System.out.println(stats.getPrecision());
        System.out.println(stats.getRecall());
    }

}