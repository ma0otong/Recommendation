package com.personal.recommendation.component.mahout.service.impl;

import com.google.common.base.Preconditions;
import com.personal.recommendation.component.mahout.service.MyUserBasedRecommender;
import com.personal.recommendation.component.mahout.utils.MyTopItems;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.impl.recommender.EstimatedPreferenceCapper;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.LongPair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 自定义GenericUserBasedRecommender
 */
@SuppressWarnings("unused")
public class MyGenericUserBasedRecommender extends AbstractRecommender implements MyUserBasedRecommender {
    private final UserNeighborhood neighborhood;
    private final UserSimilarity similarity;
    private final RefreshHelper refreshHelper;
    private EstimatedPreferenceCapper capper;

    public MyGenericUserBasedRecommender(DataModel dataModel, UserNeighborhood neighborhood, UserSimilarity similarity) {
        super(dataModel);
        Preconditions.checkArgument(neighborhood != null, "neighborhood is null");
        this.neighborhood = neighborhood;
        this.similarity = similarity;
        this.refreshHelper = new RefreshHelper((Callable<Void>) () -> {
            MyGenericUserBasedRecommender.this.capper = MyGenericUserBasedRecommender.this.buildCapper();
            return null;
        });
        this.refreshHelper.addDependency(dataModel);
        this.refreshHelper.addDependency(similarity);
        this.refreshHelper.addDependency(neighborhood);
        this.capper = this.buildCapper();
    }

    public UserSimilarity getSimilarity() {
        return this.similarity;
    }

    public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
        Preconditions.checkArgument(howMany >= 1, "howMany must be at least 1");
        long[] theNeighborhood = this.neighborhood.getUserNeighborhood(userID);
        if (theNeighborhood.length == 0) {
            return Collections.emptyList();
        } else {
            FastIDSet allItemIDs = this.getAllOtherItems(theNeighborhood, userID);
            org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> estimator = new MyGenericUserBasedRecommender.Estimator(userID, theNeighborhood);
            return MyTopItems.getTopItems(howMany, allItemIDs.iterator(), rescorer, estimator);
        }
    }

    public float estimatePreference(long userID, long itemID) throws TasteException {
        DataModel model = this.getDataModel();
        Float actualPref = model.getPreferenceValue(userID, itemID);
        if (actualPref != null) {
            return actualPref;
        } else {
            long[] theNeighborhood = this.neighborhood.getUserNeighborhood(userID);
            return this.doEstimatePreference(userID, theNeighborhood, itemID);
        }
    }

    public long[] mostSimilarUserIDs(long userID, int howMany) throws TasteException {
        return this.mostSimilarUserIDs(userID, howMany, null);
    }

    public long[] mostSimilarUserIDs(long userID, int howMany, Rescorer<LongPair> rescorer) throws TasteException {
        org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> estimator = new MyGenericUserBasedRecommender.MostSimilarEstimator(userID, this.similarity, rescorer);
        return this.doMostSimilarUsers(howMany, estimator);
    }

    private long[] doMostSimilarUsers(int howMany, org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> estimator) throws TasteException {
        DataModel model = this.getDataModel();
        return TopItems.getTopUsers(howMany, model.getUserIDs(), null, estimator);
    }

    private float doEstimatePreference(long theUserID, long[] theNeighborhood, long itemID) throws TasteException {
        if (theNeighborhood.length == 0) {
            return (float) (Double.NaN);
        } else {
            DataModel dataModel = this.getDataModel();
            double preference = 0.0D;
            double totalSimilarity = 0.0D;
            int count = 0;

            for (long userID : theNeighborhood) {
                if (userID != theUserID) {
                    Float pref = dataModel.getPreferenceValue(userID, itemID);
                    if (pref != null) {
                        // 相似度计算
                        double theSimilarity = this.similarity.userSimilarity(theUserID, userID);
                        if (!Double.isNaN(theSimilarity)) {
                            preference += theSimilarity * (double) pref;
                            totalSimilarity += theSimilarity;
                            ++count;
                        }
                    }
                }
            }

            if (count < 1) {
                return (float) (Double.NaN);
            } else {
                float estimate = (float)(preference / totalSimilarity);
                if (this.capper != null) {
                    estimate = this.capper.capEstimate(estimate);
                }

                return estimate;
            }
        }
    }

    private FastIDSet getAllOtherItems(long[] theNeighborhood, long theUserID) throws TasteException {
        DataModel dataModel = this.getDataModel();
        FastIDSet possibleItemIDs = new FastIDSet();

        for (long userID : theNeighborhood) {
            possibleItemIDs.addAll(dataModel.getItemIDsFromUser(userID));
        }

        possibleItemIDs.removeAll(dataModel.getItemIDsFromUser(theUserID));

        return possibleItemIDs;
    }

    public void refresh(Collection<Refreshable> alreadyRefreshed) {
        this.refreshHelper.refresh(alreadyRefreshed);
    }

    public String toString() {
        return "GenericUserBasedRecommender[neighborhood:" + this.neighborhood + ']';
    }

    private EstimatedPreferenceCapper buildCapper() {
        DataModel dataModel = this.getDataModel();
        return Float.isNaN(dataModel.getMinPreference()) && Float.isNaN(dataModel.getMaxPreference()) ? null : new EstimatedPreferenceCapper(dataModel);
    }

    private final class Estimator implements org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> {
        private final long theUserID;
        private final long[] theNeighborhood;

        Estimator(long theUserID, long[] theNeighborhood) {
            this.theUserID = theUserID;
            this.theNeighborhood = theNeighborhood;
        }

        public double estimate(Long itemID) throws TasteException {
            return (double)MyGenericUserBasedRecommender.this.doEstimatePreference(this.theUserID, this.theNeighborhood, itemID);
        }
    }

    private static final class MostSimilarEstimator implements org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> {
        private final long toUserID;
        private final UserSimilarity similarity;
        private final Rescorer<LongPair> rescorer;

        private MostSimilarEstimator(long toUserID, UserSimilarity similarity, Rescorer<LongPair> rescorer) {
            this.toUserID = toUserID;
            this.similarity = similarity;
            this.rescorer = rescorer;
        }

        public double estimate(Long userID) throws TasteException {
            if (userID == this.toUserID) {
                return Double.NaN;
            } else if (this.rescorer == null) {
                return this.similarity.userSimilarity(this.toUserID, userID);
            } else {
                LongPair pair = new LongPair(this.toUserID, userID);
                if (this.rescorer.isFiltered(pair)) {
                    return Double.NaN;
                } else {
                    double originalEstimate = this.similarity.userSimilarity(this.toUserID, userID);
                    return this.rescorer.rescore(pair, originalEstimate);
                }
            }
        }
    }
}
