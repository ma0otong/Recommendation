package com.personal.recommendation.utils;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * Map比较
 */
public class MapValueComparator implements Comparator<Entry<Long, Double>> {

    @Override
    public int compare(Entry<Long, Double> me1, Entry<Long, Double> me2) {
        return me1.getValue().compareTo(me2.getValue());
    }
}