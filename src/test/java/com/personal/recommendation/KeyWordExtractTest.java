package com.personal.recommendation;

import com.hankcs.hanlp.HanLP;

import java.io.*;
import java.util.List;

public class KeyWordExtractTest {

    public static void main(String[] args) {
        int fileIndex = 1;
        String pathname = "E:\\documents\\testdata.txt";
        String folderPath = "E:\\documents\\内容分类训练集";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                String module = line.split("\t")[0];
                String text = line.split("\t")[1];

                String pathStr = folderPath + "\\" + module + "\\" + fileIndex;
                File writeName = new File(pathStr);
                if(writeName.createNewFile()){
                    System.out.println("create file : " + pathStr);
                }
                try (FileWriter writer = new FileWriter(writeName);
                     BufferedWriter out = new BufferedWriter(writer)
                ) {
                    List<String> keywordList = HanLP.extractKeyword(text, 10);
                    String content = keywordList.toString().replace("[","").replace("]","");
                    out.write(content);
                }
                fileIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
