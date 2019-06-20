package com.personal.recommendation.component.hanLP.service.impl;

import com.hankcs.hanlp.classification.collections.FrequencyMap;
import com.hankcs.hanlp.classification.models.AbstractModel;
import com.personal.recommendation.component.hanLP.model.MyDocument;
import com.personal.recommendation.component.hanLP.service.MyIDataSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * 自定义MemoryDataSet子类
 */
@SuppressWarnings({"unused","unchecked"})
public class MyMemoryDataSet extends MyAbstractDataSet {
    private List<MyDocument> documentList = new LinkedList();
    private boolean editMode;

    MyMemoryDataSet(){
        super();
    }

    public MyMemoryDataSet(AbstractModel model) {
        super(model);
    }

    public MyDocument add(String category, String text) {
        if (this.editMode) {
            return null;
        } else {
            MyDocument document = this.convert(category, text);
            this.documentList.add(document);
            return document;
        }
    }

    public int size() {
        return this.documentList.size();
    }

    public void clear() {
        this.documentList.clear();
    }

    public MyIDataSet shrink(int[] idMap) {
        Iterator iterator = this.iterator();

        while(iterator.hasNext()) {
            MyDocument document = (MyDocument)iterator.next();
            FrequencyMap tfMap = new FrequencyMap();

            for (Entry<Integer, int[]> integerEntry : document.tfMap.entrySet()) {
                Integer feature = (Integer) ((Entry) integerEntry).getKey();
                if (idMap[feature] != -1) {
                    tfMap.put(idMap[feature], ((Entry) integerEntry).getValue());
                }
            }

            if (tfMap.size() == 0) {
                iterator.remove();
            } else {
                document.tfMap = tfMap;
            }
        }

        return this;
    }

    public Iterator<MyDocument> iterator() {
        return this.documentList.iterator();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
