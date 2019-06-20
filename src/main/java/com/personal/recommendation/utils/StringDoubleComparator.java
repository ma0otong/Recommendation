package com.personal.recommendation.utils;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * Map比较
 */
public class StringDoubleComparator implements Comparator<Entry<String, Double>> {

    @Override
    public int compare(Entry<String, Double> me1, Entry<String, Double> me2) {
        return me2.getValue().compareTo(me1.getValue());
    }
}