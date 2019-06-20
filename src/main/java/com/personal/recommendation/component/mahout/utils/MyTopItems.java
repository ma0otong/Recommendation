package com.personal.recommendation.component.mahout.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.personal.recommendation.component.mahout.model.MySimilarUser;
import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.ByValueRecommendedItemComparator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.GenericUserSimilarity;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import java.util.*;

/**
 * 自定义topItems
 */
@SuppressWarnings({"unused","unchecked"})
public final class MyTopItems {
    private static final long[] NO_IDS = new long[0];

    private MyTopItems() {
    }

    public static List<RecommendedItem> getTopItems(int howMany, LongPrimitiveIterator possibleItemIDs, IDRescorer rescorer, org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> estimator) throws TasteException {
        Preconditions.checkArgument(possibleItemIDs != null, "possibleItemIDs is null");
        Preconditions.checkArgument(estimator != null, "estimator is null");
        Queue<RecommendedItem> topItems = new PriorityQueue(howMany + 1, Collections.reverseOrder(ByValueRecommendedItemComparator.getInstance()));
        boolean full = false;
        double lowestTopValue = -1.0D / 0.0;

        while(true) {
            long itemID;
            double rescoredPref;
            do {
                do {
                    double preference;
                    while(true) {
                        do {
                            if (!possibleItemIDs.hasNext()) {
                                int size = topItems.size();
                                if (size == 0) {
                                    return Collections.emptyList();
                                }

                                List<RecommendedItem> result = Lists.newArrayListWithCapacity(size);
                                result.addAll(topItems);
                                result.sort(ByValueRecommendedItemComparator.getInstance());
                                return result;
                            }

                            itemID = possibleItemIDs.next();
                        } while(rescorer != null && rescorer.isFiltered(itemID));

                        try {
                            preference = estimator.estimate(itemID);
                            break;
                        } catch (NoSuchItemException ignored) {
                        }
                    }

                    rescoredPref = rescorer == null ? preference : rescorer.rescore(itemID, preference);
                } while(Double.isNaN(rescoredPref));
            } while(full && rescoredPref <= lowestTopValue);

            topItems.add(new GenericRecommendedItem(itemID, (float)rescoredPref));
            if (full) {
                topItems.poll();
            } else if (topItems.size() > howMany) {
                full = true;
                topItems.poll();
            }

            assert topItems.peek() != null;
            lowestTopValue = (double)(topItems.peek()).getValue();
        }
    }

    public static long[] getTopUsers(int howMany, LongPrimitiveIterator allUserIDs, IDRescorer rescorer, org.apache.mahout.cf.taste.impl.recommender.TopItems.Estimator<Long> estimator) throws TasteException {
        Queue<MySimilarUser> topUsers = new PriorityQueue(howMany + 1, Collections.reverseOrder());
        boolean full = false;
        double lowestTopValue = -1.0D / 0.0;

        while(true) {
            long userID;
            double rescoredSimilarity;
            do {
                do {
                    double similarity;
                    while(true) {
                        do {
                            if (!allUserIDs.hasNext()) {
                                int size = topUsers.size();
                                if (size == 0) {
                                    return NO_IDS;
                                }

                                List<MySimilarUser> sorted = Lists.newArrayListWithCapacity(size);
                                sorted.addAll(topUsers);
                                Collections.sort(sorted);
                                long[] result = new long[size];
                                int i = 0;

                                MySimilarUser similarUser;
                                for(Iterator i$ = sorted.iterator(); i$.hasNext(); result[i++] = similarUser.getUserID()) {
                                    similarUser = (MySimilarUser)i$.next();
                                }

                                return result;
                            }

                            userID = allUserIDs.next();
                        } while(rescorer != null && rescorer.isFiltered(userID));

                        try {
                            similarity = estimator.estimate(userID);
                            break;
                        } catch (NoSuchUserException ignored) {
                        }
                    }

                    rescoredSimilarity = rescorer == null ? similarity : rescorer.rescore(userID, similarity);
                } while(Double.isNaN(rescoredSimilarity));
            } while(full && rescoredSimilarity <= lowestTopValue);

            topUsers.add(new MySimilarUser(userID, rescoredSimilarity));
            if (full) {
                topUsers.poll();
            } else if (topUsers.size() > howMany) {
                full = true;
                topUsers.poll();
            }

            assert topUsers.peek() != null;
            lowestTopValue = (topUsers.peek()).getSimilarity();
        }
    }

    public static List<GenericItemSimilarity.ItemItemSimilarity> getTopItemItemSimilarities(int howMany, Iterator<GenericItemSimilarity.ItemItemSimilarity> allSimilarities) {
        Queue<GenericItemSimilarity.ItemItemSimilarity> topSimilarities = new PriorityQueue(howMany + 1, Collections.reverseOrder());
        boolean full = false;
        double lowestTopValue = -1.0D / 0.0;

        while(true) {
            GenericItemSimilarity.ItemItemSimilarity similarity;
            double value;
            do {
                do {
                    if (!allSimilarities.hasNext()) {
                        int size = topSimilarities.size();
                        if (size == 0) {
                            return Collections.emptyList();
                        }

                        List<GenericItemSimilarity.ItemItemSimilarity> result = Lists.newArrayListWithCapacity(size);
                        result.addAll(topSimilarities);
                        Collections.sort(result);
                        return result;
                    }

                    similarity = allSimilarities.next();
                    value = similarity.getValue();
                } while(Double.isNaN(value));
            } while(full && value <= lowestTopValue);

            topSimilarities.add(similarity);
            if (full) {
                topSimilarities.poll();
            } else if (topSimilarities.size() > howMany) {
                full = true;
                topSimilarities.poll();
            }

            assert topSimilarities.peek() != null;
            lowestTopValue = (topSimilarities.peek()).getValue();
        }
    }

    public static List<GenericUserSimilarity.UserUserSimilarity> getTopUserUserSimilarities(int howMany, Iterator<GenericUserSimilarity.UserUserSimilarity> allSimilarities) {
        Queue<GenericUserSimilarity.UserUserSimilarity> topSimilarities = new PriorityQueue(howMany + 1, Collections.reverseOrder());
        boolean full = false;
        double lowestTopValue = -1.0D / 0.0;

        while(true) {
            GenericUserSimilarity.UserUserSimilarity similarity;
            double value;
            do {
                do {
                    if (!allSimilarities.hasNext()) {
                        int size = topSimilarities.size();
                        if (size == 0) {
                            return Collections.emptyList();
                        }

                        List<GenericUserSimilarity.UserUserSimilarity> result = Lists.newArrayListWithCapacity(size);
                        result.addAll(topSimilarities);
                        Collections.sort(result);
                        return result;
                    }

                    similarity = allSimilarities.next();
                    value = similarity.getValue();
                } while(Double.isNaN(value));
            } while(full && value <= lowestTopValue);

            topSimilarities.add(similarity);
            if (full) {
                topSimilarities.poll();
            } else if (topSimilarities.size() > howMany) {
                full = true;
                topSimilarities.poll();
            }

            assert topSimilarities.peek() != null;
            lowestTopValue = (topSimilarities.peek()).getValue();
        }
    }

    public interface Estimator<T> {
        double estimate(T var1) throws TasteException;
    }
}
