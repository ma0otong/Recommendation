package com.personal.recommendation.test;

import com.personal.recommendation.dao.NewsDAO;
import com.personal.recommendation.dao.NewsLogsDAO;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

@SuppressWarnings("unused")
public class GetTestData {

    public static void setNewsLogsData(ConfigurableApplicationContext ctx) {
        NewsLogsDAO newsLogsDAO = ctx.getBean(NewsLogsDAO.class);
        NewsDAO newsDAO = ctx.getBean(NewsDAO.class);

        int start = 1;
        int limit = 500;
        int offset = (start - 1) * (limit/2);
        NewsLogs newsLog = new NewsLogs();
        for (int i = start; i < 1001; i++) {
            List<News> newsList = newsDAO.getNewsByLimitOffset(limit, offset);
            // 随机生成user
            for (News news : newsList) {
                // 写入newsLogs
                newsLog.setUserId((long) i);
                newsLog.setNewsId(news.getId());
                newsLog.setNewsModule(news.getModuleLevel1());
                newsLogsDAO.insertNewsLogs(newsLog);
                System.out.println(String.format("Auto add new newsLog record ... userId : %s, newsId : %s, module : %s .",
                        i, news.getId(), news.getModuleLevel1()));
            }
            offset += limit / 2;
        }
    }

    public static void setNewsData(ConfigurableApplicationContext ctx) {
        try {
            String rootPath = "E:\\documents\\多层级分类训练集\\mlc_dataset";
            NewsDAO newsDAO = ctx.getBean(NewsDAO.class);
            File dir = new File(rootPath);
            File[] folders = dir.listFiles();
            assert folders != null;
            for (File f : folders) {
                String level1 = f.getName();
                if ("news_agriculture".equals(level1) || "news_astrology".equals(level1)) {
                    continue;
                }
                News news = new News();
                news.setTitle(f.getName());
                news.setUrl("test");
                news.setModuleLevel1(level1);
                news.setModuleLevel2("");
                news.setModuleLevel3("");

                if (f.isDirectory()) {
                    File[] folders2 = f.listFiles();
                    assert folders2 != null;
                    for (File f2 : folders2) {
                        String level2 = f2.getName();
                        if (f2.isDirectory()) {
                            File[] folders3 = f2.listFiles();
                            assert folders3 != null;
                            for (File f3 : folders3) {
                                String level3 = f3.getName();
                                if (f3.isDirectory()) {
                                    File[] folder4 = f3.listFiles();
                                    assert folder4 != null;
                                    for (File f4 : folder4) {
                                        if (f4.isFile()) {
                                            BufferedReader br = new BufferedReader(new FileReader(f4));
                                            news.setContent(br.readLine());
                                            news.setId(Long.parseLong(f4.getName()));
                                            news.setModuleLevel2(level2);
                                            news.setModuleLevel2(level3);
                                            try {
                                                newsDAO.insertNews(news);
                                                System.out.println(news.getModuleLevel1() + ":" + news.getModuleLevel2() + ":" + news.getModuleLevel3() + " - " + news.getId());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } else {
                                    BufferedReader br = new BufferedReader(new FileReader(f3));
                                    news.setContent(br.readLine());
                                    news.setId(Long.parseLong(f3.getName()));
                                    news.setModuleLevel2(level2);
                                    try {
                                        newsDAO.insertNews(news);
                                        System.out.println(news.getModuleLevel1() + ":" + news.getModuleLevel2() + " - " + news.getId());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            BufferedReader br = new BufferedReader(new FileReader(f2));
                            news.setContent(br.readLine());
                            news.setId(Long.parseLong(f2.getName()));
                            try {
                                newsDAO.insertNews(news);
                                System.out.println(news.getModuleLevel1() + " - " + news.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
