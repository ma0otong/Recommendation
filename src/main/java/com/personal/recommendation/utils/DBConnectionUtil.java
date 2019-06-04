package com.personal.recommendation.utils;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.personal.recommendation.constants.RecommendationConstants;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;

import javax.sql.DataSource;

/**
 * 数据库工具类
 */
public class DBConnectionUtil {

    private static final Logger logger = Logger.getLogger(DBConnectionUtil.class);

    private static C3p0Plugin CP;

    public static String URL;
    public static String USERNAME;
    public static String PASSWORD;

    /**
     * 初始化
     */
    private void initialize() {
        try {
            CP = new C3p0Plugin(URL, USERNAME, PASSWORD);

            ActiveRecordPlugin arp = new ActiveRecordPlugin(CP);

            if (CP.start() && arp.start())
                logger.info("数据库连接池插件启动成功......");
            else
                logger.error("c3p0插件启动失败！");

            logger.info("数据库初始化工作完毕！");
        } catch (Exception e) {
            logger.error("数据库连接初始化错误！");
        }
    }

    private DataSource getDataSource() {
        if (CP == null)
            initialize();
        return CP.getDataSource();
    }

    public MySQLBooleanPrefJDBCDataModel getMySQLJDBCDataModel() {
        return new MySQLBooleanPrefJDBCDataModel(getDataSource(), RecommendationConstants.PREF_TABLE,
                RecommendationConstants.PREF_TABLE_USER_ID, RecommendationConstants.PREF_TABLE_NEWS_ID,
                RecommendationConstants.PREF_TABLE_TIME);
    }
}
