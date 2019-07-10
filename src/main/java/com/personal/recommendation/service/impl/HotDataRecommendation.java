package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.component.thread.RecommendationCalculateThread;
import com.personal.recommendation.service.RecommendationAlgorithmFactory;
import com.personal.recommendation.utils.DateUtil;
import com.personal.recommendation.utils.SpringContextUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于热点推荐实现类
 */
@Service
public class HotDataRecommendation implements RecommendationAlgorithmService {

    // 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
    public static ArrayList<Long> topHotNewsList = new ArrayList<>();

    /**
     * 加入算法工程
     */
    @PostConstruct
    void init() {
        RecommendationAlgorithmFactory.addHandler(RecommendationEnum.HR.getCode(), this);
    }

    /**
     * 基于热点推荐方法
     * @param user Users
     * @param recNum int
     * @param recommendedNews List<Long>
     * @param browsedNews List<Long>
     * @return Set<Long>
     */
    @Override
    public Set<Long> recommend(Users user, int recNum, List<Long> recommendedNews, List<Long> browsedNews) {
        Long userId = user.getId();

        // 保存推荐结果
        LinkedHashSet<Long> toBeRecommended = new LinkedHashSet<>();

        if(topHotNewsList.isEmpty()){
            formTopHotNewsList();
        }

        try {
            // 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
            toBeRecommended = RecommendationCalculateThread.resultHandle(recommendedNews, browsedNews,
                    toBeRecommended, userId, RecommendationEnum.HR.getDesc(), recNum, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return toBeRecommended;

    }

    /**
     * 加载热点新闻
     */
    public static void formTopHotNewsList() {
        try {
            topHotNewsList.clear();
            NewsLogsManager newsLogsManager = (NewsLogsManager) SpringContextUtil.getBean("newsLogsManager");
            List<NewsLogs> hotNewsLogs = newsLogsManager.getHotNews(DateUtil.getDateBeforeDays(
                    RecommendationConstants.HOT_DATA_DAYS), RecommendationConstants.MAX_HOT_NUM);
            for(NewsLogs newsLog : hotNewsLogs) {
                if(newsLog.getVisitNum() > 0){
                    topHotNewsList.add(newsLog.getNewsId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
