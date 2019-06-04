package com.personal.recommendation;

import org.apache.hadoop.fs.Path;
import org.apache.mahout.clustering.conversion.InputDriver;

import java.io.IOException;

public class ClusteringTest {

    public static void main(String[] args){
        Path input = new Path("E:\\documents\\p04-17.txt");
        Path output = new Path("E:\\test-data");

        try {
            InputDriver.runJob(input,output,"org.apache.mahout.math.RandomAccessSparseVector");
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
