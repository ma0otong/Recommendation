package com.personal.recommendation.component.hanLP.utils;

import com.hankcs.hanlp.algorithm.MaxHeap;
import com.hankcs.hanlp.classification.statistics.ContinuousDistributions;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings({"unused","unchecked"})
public class MyChiSquareFeatureExtractor {
    private double chisquareCriticalValue = 10.83D;

    public MyChiSquareFeatureExtractor() {
    }

    public static MyBaseFeatureData extractBasicFeatureData(MyIDataSet dataSet) {
        return new MyBaseFeatureData(dataSet);
    }

    public Map<Integer, Double> chi_square(MyBaseFeatureData stats) {
        HashMap selectedFeatures = new HashMap();

        int feature;
        for(feature = 0; feature < stats.featureCategoryJointCount.length; ++feature) {
            int[] categoryList = stats.featureCategoryJointCount[feature];
            double N1dot = 0.0D;
            int var21 = categoryList.length;

            for (int count : categoryList) {
                N1dot += (double) count;
            }

            double N0dot = (double)stats.n - N1dot;

            for(int category = 0; category < categoryList.length; ++category) {
                double N11 = (double)categoryList[category];
                double N01 = (double)stats.categoryCounts[category] - N11;
                double N00 = N0dot - N01;
                double N10 = N1dot - N11;
                double chisquareScore = (double)stats.n * Math.pow(N11 * N00 - N10 * N01, 2.0D) / ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00));
                if (chisquareScore >= this.chisquareCriticalValue) {
                    Double previousScore = (Double)selectedFeatures.get(feature);
                    if (previousScore == null || chisquareScore > previousScore) {
                        selectedFeatures.put(feature, chisquareScore);
                    }
                }
            }
        }

        if (selectedFeatures.size() == 0) {
            for(feature = 0; feature < stats.featureCategoryJointCount.length; ++feature) {
                selectedFeatures.put(feature, 0.0D);
            }
        }

        int maxSize = 1000000;
        if (selectedFeatures.size() > maxSize) {
            MaxHeap maxHeap = new MaxHeap(maxSize, (Comparator<Map.Entry<Integer, Double>>)
                    (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));
            Iterator var26 = selectedFeatures.entrySet().iterator();

            Map.Entry entry;
            while(var26.hasNext()) {
                entry = (Map.Entry)var26.next();
                maxHeap.add(entry);
            }

            selectedFeatures.clear();
            var26 = maxHeap.iterator();

            while(var26.hasNext()) {
                entry = (Map.Entry)var26.next();
                selectedFeatures.put(entry.getKey(), entry.getValue());
            }
        }

        return selectedFeatures;
    }

    public double getChisquareCriticalValue() {
        return this.chisquareCriticalValue;
    }

    public void setChisquareCriticalValue(double chisquareCriticalValue) {
        this.chisquareCriticalValue = chisquareCriticalValue;
    }

    public MyChiSquareFeatureExtractor setALevel(double aLevel) {
        this.chisquareCriticalValue = ContinuousDistributions.ChisquareInverseCdf(aLevel, 1);
        return this;
    }

    public double getALevel() {
        return ContinuousDistributions.ChisquareCdf(this.chisquareCriticalValue, 1);
    }
}
