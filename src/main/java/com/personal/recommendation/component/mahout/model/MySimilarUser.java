package com.personal.recommendation.component.mahout.model;

import org.apache.mahout.common.RandomUtils;

/**
 * 自定义similarUser
 */
public final class MySimilarUser implements Comparable<MySimilarUser> {
    private final long userID;
    private final double similarity;

    public MySimilarUser(long userID, double similarity) {
        this.userID = userID;
        this.similarity = similarity;
    }

    public long getUserID() {
        return this.userID;
    }

    public double getSimilarity() {
        return this.similarity;
    }

    public int hashCode() {
        return (int) this.userID ^ RandomUtils.hashDouble(this.similarity);
    }

    public boolean equals(Object o) {
        if (!(o instanceof org.apache.mahout.cf.taste.impl.recommender.SimilarUser)) {
            return false;
        } else {
            MySimilarUser other = (MySimilarUser) o;
            return this.userID == other.getUserID() && this.similarity == other.getSimilarity();
        }
    }

    public String toString() {
        return "SimilarUser[user:" + this.userID + ", similarity:" + this.similarity + ']';
    }

    public int compareTo(MySimilarUser other) {
        double otherSimilarity = other.getSimilarity();
        if (this.similarity > otherSimilarity) {
            return -1;
        } else if (this.similarity < otherSimilarity) {
            return 1;
        } else {
            long otherUserID = other.getUserID();
            if (this.userID < otherUserID) {
                return -1;
            } else {
                return this.userID > otherUserID ? 1 : 0;
            }
        }
    }
}
