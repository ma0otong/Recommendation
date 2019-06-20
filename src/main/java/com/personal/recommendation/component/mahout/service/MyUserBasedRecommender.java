package com.personal.recommendation.component.mahout.service;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;

/**
 * 自定义UserBasedRecommender
 */
@SuppressWarnings("unused")
public interface MyUserBasedRecommender extends MyRecommender {

    long[] mostSimilarUserIDs(long var1, int var3) throws TasteException;

    long[] mostSimilarUserIDs(long var1, int var3, Rescorer<LongPair> var4) throws TasteException;
}
