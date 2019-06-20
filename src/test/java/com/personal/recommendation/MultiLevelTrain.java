package com.personal.recommendation;

import org.apache.commons.lang.StringUtils;

import java.io.*;

public class MultiLevelTrain {
    public static void main(String[] args) {
        getDirs();
    }

    private static void getFile(){
        String pathname = "E:\\documents\\多层级分类训练集\\mlc_dataset_part_ad";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            int fileIndex = 3000000;
            while ((line = br.readLine()) != null) {
                String folderPath = "E:\\documents\\多层级分类训练集\\mlc_dataset";
                String[] labels;
                String outputPath = "";
                try {
                    String labelStr = line.split("\\|,\\|")[1].split(",")[0];
                    labels = labelStr.split("/");
                } catch (Exception e) {
                    continue;
                }
                if(!labels[0].startsWith("news_")){
                    continue;
                }
                for (int i = 0; i < labels.length; i++) {
                    // 校验目录
                    folderPath += "\\" + labels[i];
                    File folder = new File(folderPath);
                    if (!folder.isDirectory()) {
                        if (folder.mkdir()) {
                            System.out.println("create folder : " + folderPath);
                        }
                    }
                    if (i == labels.length - 1) {
                        outputPath = folderPath;
                    }
                }
                // 写入文件
                String keyWords;
                try{
                    keyWords = line.split("\\|,\\|")[3];
                }catch(Exception e){
                    continue;
                }
                if (StringUtils.isBlank(keyWords)) {
                    continue;
                }
                String fileName = outputPath + "\\" + fileIndex;
                File writeName = new File(fileName);
                if (writeName.createNewFile()) {
                    System.out.println("create file : " + fileName);
                }
                try (FileWriter writer = new FileWriter(writeName);
                     BufferedWriter out = new BufferedWriter(writer)
                ) {
                    out.write(keyWords);
                }
                fileIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void cleanFolder(){
        String pathname = "E:\\documents\\多层级分类训练集\\mlc_dataset";
        File file = new File(pathname);
        File[] fileList = file.listFiles();
        assert fileList != null;
        for(File f : fileList){
            if(!f.getName().startsWith("news_")){
                cleanDir(f);
            }
            if(f.isDirectory()) {
                File[] fs = f.listFiles();
                if (fs == null || fs.length == 0) {
                    if (f.delete()) {
                        System.out.println("File deleted : " + f.getPath());
                    }
                }
            }
        }
    }

    private static void cleanDir(File file){
        if(file.isFile()){
            if(file.delete()){
                System.out.println("File deleted : " + file.getPath());
            }
        }else{
            if(!file.delete()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    cleanDir(f);
                }
            }
        }
    }

    private static void getDirs(){
        File file = new File("E:\\documents\\多层级分类训练集\\mlc_dataset");
        File[] files = file.listFiles();
        for(File f : files){
            if(f.isDirectory()) {
                String filepath = f.getPath() + "\\general";
                File newf = new File(filepath);
                if(!newf.isDirectory()){
                    if(newf.mkdir()){
                        System.out.println("mkdir : " + newf.getPath());
                    }
                }
                File[] fs = f.listFiles();
                for(File ff : fs){
                    if(ff.isFile()){
                        if(ff.renameTo(new File(filepath + "\\" + ff.getName()))){
                            System.out.println("remove file : " + ff.getName());
                        }
                    }
                }
            }
        }
    }
}
