package com.personal.recommendation.service;

import java.util.Map;

public interface NewsService {

    void userNewsList(Long userId, Map<String, Object> paramMap);

    String newsDetail(Long userId, Long newsId);

}
