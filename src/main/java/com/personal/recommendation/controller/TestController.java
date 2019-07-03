package com.personal.recommendation.controller;

import com.personal.recommendation.service.NewsService;
import com.personal.recommendation.utils.IpUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/recommend")
public class TestController {

    private static final Logger logger = Logger.getLogger(TestController.class);

    private final NewsService newsService;

    @Autowired
    TestController(NewsService newsService) {
        this.newsService = newsService;
    }

    @RequestMapping(value = "/index/{userId}")
    public String index(Map<String, Object> paramMap, @PathVariable("userId") Long userId) {
        newsService.userNewsList(userId, paramMap);
        return "index";
    }

    @GetMapping(value = "/news/{userId}/{newsId}")
    @ResponseBody
    public String getNewsDetail(@PathVariable("userId") Long userId, @PathVariable("newsId") Long newsId, HttpServletRequest request) {
        String ip = IpUtil.getIpAddr(request);
        logger.info("Client ip : " + ip + ", url : /news/" + userId + "/" + newsId);
        return newsService.newsDetail(userId, newsId);
    }

}