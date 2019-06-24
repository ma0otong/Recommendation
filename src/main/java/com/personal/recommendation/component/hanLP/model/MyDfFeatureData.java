package com.personal.recommendation.component.hanLP.model;

import com.personal.recommendation.component.hanLP.service.MyIDataSet;

public class MyDfFeatureData extends MyBaseFeatureData {
    public int[] df;

    public MyDfFeatureData(MyIDataSet dataSet) {
        super(dataSet);
    }
}