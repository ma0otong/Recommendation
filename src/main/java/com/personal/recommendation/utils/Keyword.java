package com.personal.recommendation.utils;

/**
 * Keyword
 */
@SuppressWarnings("unused")
public class Keyword implements Comparable<Keyword> {
    private double tfIdfValue;
    private String name;

    /**
     * @return the tfIdfValue
     */
    public double getTfIdfValue() {
        return tfIdfValue;
    }

    /**
     * @param tfIdfValue the tfIdfValue to set
     */
    public void setTfIdfValue(double tfIdfValue) {
        this.tfIdfValue = tfIdfValue;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    Keyword(String name, double tfIdfValue) {
        this.name = name;
        // tfIdf值只保留3位小数
        this.tfIdfValue = (double) Math.round(tfIdfValue * 10000) / 10000;
    }

    /**
     * 为了在返回tdIdf分析结果时，可以按照值的从大到小顺序返回，故实现Comparable接口
     */
    @Override
    public int compareTo(Keyword o) {
        return this.tfIdfValue - o.tfIdfValue > 0 ? -1 : 1;
    }

    /**
     * 重写hashcode方法，计算方式与原生String的方法相同
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        long temp;
        temp = Double.doubleToLongBits(tfIdfValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Keyword other = (Keyword) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }


}

