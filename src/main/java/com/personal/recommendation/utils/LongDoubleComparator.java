package com.personal.recommendation.utils;

import java.util.Comparator;
import java.util.Map;

/**
 * map比较
 */
public class LongDoubleComparator implements Comparator<Map.Entry<Long, Double>> {

    @Override
    public int compare(Map.Entry<Long, Double> me1, Map.Entry<Long, Double> me2) {
        return me2.getValue().compareTo(me1.getValue());
    }
}
