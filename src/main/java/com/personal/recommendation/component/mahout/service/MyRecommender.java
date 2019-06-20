package com.personal.recommendation.component.mahout.service;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.util.List;

/**
 * 自定义Recommender接口
 */
@SuppressWarnings("unused")
public interface MyRecommender extends Refreshable {

    List<RecommendedItem> recommend(long var1, int var3) throws TasteException;

    List<RecommendedItem> recommend(long var1, int var3, IDRescorer var4) throws TasteException;

    float estimatePreference(long var1, long var3) throws TasteException;

    void setPreference(long var1, long var3, float var5) throws TasteException;

    void removePreference(long var1, long var3) throws TasteException;

    DataModel getDataModel();
}
