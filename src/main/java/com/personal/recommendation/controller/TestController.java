package com.personal.recommendation.controller;

import com.personal.recommendation.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/recommend")
@SuppressWarnings("unused")
public class TestController {

    private final NewsService newsService;

    @Autowired
    TestController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping(value = "/index/{userId}")
    public String index(Map<String, Object> paramMap, @PathVariable("userId") Long userId, HttpServletRequest request) {
        newsService.userNewsList(userId, paramMap);
        return "index";
    }

    @GetMapping(value = "/news/{userId}/{newsId}")
    @ResponseBody
    public String getNewsDetail(@PathVariable("userId") Long userId, @PathVariable("newsId") Long newsId, HttpServletRequest request) {
        return newsService.newsDetail(userId, newsId);
    }

    @GetMapping(value = "/search/{userId}")
    public String getSearch(Map<String, Object> paramMap, @PathVariable("userId") Long userId,
                            @RequestParam("keyword") String keyword, HttpServletRequest request) {
        newsService.getSearch(keyword, userId, paramMap);
        return "search";
    }

    @GetMapping(value = "/getSuggestData")
    @ResponseBody
    public String getSuggestData(@RequestParam("userId") Long userId, @RequestParam("keyword") String keyword, HttpServletRequest request) {
        return newsService.getSuggestData(keyword, userId);
    }

}