package com.personal.recommendation.component.hanLP.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MyCatalog
 */
@SuppressWarnings({"unchecked","unused"})
public class MyCatalog implements Serializable {
    public Map<String, Integer> categoryId;
    private List<String> idCategory;

    public MyCatalog() {
        this.categoryId = new TreeMap();
        this.idCategory = new ArrayList();
    }

    public MyCatalog(String[] catalog) {
        this();

        for(int i = 0; i < catalog.length; ++i) {
            this.categoryId.put(catalog[i], i);
            this.idCategory.add(catalog[i]);
        }

    }

    int addCategory(String category) {
        Integer id = this.categoryId.get(category);
        if (id == null) {
            id = this.categoryId.size();
            this.categoryId.put(category, id);

            assert this.idCategory.size() == id;

            this.idCategory.add(category);
        }

        return id;
    }

    public Integer getId(String category) {
        return this.categoryId.get(category);
    }

    public String getCategory(int id) {
        assert 0 <= id;

        assert id < this.idCategory.size();

        return this.idCategory.get(id);
    }

    public List<String> getCategories() {
        return this.idCategory;
    }

    public int size() {
        return this.idCategory.size();
    }

    public String[] toArray() {
        String[] catalog = new String[this.idCategory.size()];
        this.idCategory.toArray(catalog);
        return catalog;
    }

    public String toString() {
        return this.idCategory.toString();
    }
}
