package com.personal.recommendation.service.impl;

import com.personal.recommendation.constants.RecommendationConstants;
import com.personal.recommendation.constants.RecommendationEnum;
import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.manager.NewsLogsManager;
import com.personal.recommendation.manager.NewsManager;
import com.personal.recommendation.manager.RecommendationsManager;
import com.personal.recommendation.manager.UsersManager;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.model.News;
import com.personal.recommendation.model.NewsLogs;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.service.RecommendationAlgorithmService;
import com.personal.recommendation.service.AlgorithmFactory;
import com.personal.recommendation.utils.DBConnectionUtil;
import com.personal.recommendation.utils.DateUtil;
import com.personal.recommendation.utils.RecommendationUtil;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于协同过滤推荐实现类
 */
@Service
public class MahoutUserBasedCollaborativeRecommendationImpl implements RecommendationAlgorithmService {

    private static final Logger logger = Logger.getLogger(MahoutUserBasedCollaborativeRecommendationImpl.class);

    private final UsersManager usersManager;
    private final NewsManager newsManager;
    private final NewsLogsManager newsLogsManager;
    private final RecommendationsManager recommendationsManager;

    @Autowired
    public MahoutUserBasedCollaborativeRecommendationImpl(UsersManager usersManager, NewsManager newsManager,
                                                          NewsLogsManager newsLogsManager,
                                                          RecommendationsManager recommendationsManager) {
        this.usersManager = usersManager;
        this.newsManager = newsManager;
        this.newsLogsManager = newsLogsManager;
        this.recommendationsManager = recommendationsManager;
    }

    @PostConstruct
    void init() {
        AlgorithmFactory.addHandler(RecommendationEnum.CF.getCode(), this);
    }

    /**
     * 给特定的一批用户进行新闻推荐
     */
    @Override
    public BaseRsp recommend(List<Long> users) {
        long start = new Date().getTime();
        logger.info("CF start at " + start + ", userList : " + users);

        // 保存推荐结果
        Map<Users, List<News>> resultMap = new HashMap<>();

        int count = 0;
        try {
            logger.info("CF start at " + new Date());

            MySQLBooleanPrefJDBCDataModel dataModel = new DBConnectionUtil().getMySQLJDBCDataModel();

            List<NewsLogs> newsLogList = newsLogsManager.getNewsByUsers(users);
            logger.info("Get newsLogs size : " + newsLogList.size());

            // 移除过期的用户浏览新闻行为，这些行为对计算用户相似度不再具有较大价值
            for (NewsLogs newsLog : newsLogList) {
                if (newsLog.getViewTime().before(DateUtil.getDateBeforeDays(RecommendationConstants.CF_VALID_DAYS))) {
                    newsLogList.remove(newsLog);
                }
            }
            logger.info("Get newsLogs size (after filter) : " + newsLogList.size());

            UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);

            // NearestNeighborhood的数量有待考察
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(RecommendationConstants.N, similarity, dataModel);

            Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

            for (Long uid : users) {
                List<RecommendedItem> recItems = recommender.recommend(uid, RecommendationConstants.N);
                Set<Long> toBeRecommended = new HashSet<>();

                for (RecommendedItem recItem : recItems) {
                    toBeRecommended.add(recItem.getItemID());
                }

                count = RecommendationUtil.resultHandle(recommendationsManager, newsLogsManager, newsManager,
                        usersManager, toBeRecommended, uid, count, resultMap, RecommendationEnum.CF.getCode());

            }
        } catch (TasteException e) {
            logger.error("CF算法构造偏好对象失败！");
            e.printStackTrace();
            return new BaseRsp(ResultEnum.FAILURE, ResultEnum.FAILURE.getMsg());

        } catch (Exception e) {
            logger.error("CF算法数据库操作失败！");
            e.printStackTrace();
            return new BaseRsp(ResultEnum.FAILURE, ResultEnum.FAILURE.getMsg());

        }

        long end = new Date().getTime();
        logger.info("CF has contributed " + (count / users.size()) + " recommending news on average");
        logger.info("CF finished at " + end + ", time cost : " + (double)((end - start)/1000) + "s .");

        return new BaseRsp(ResultEnum.SUCCESS, resultMap);

    }

}
