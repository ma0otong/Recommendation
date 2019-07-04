package com.personal.recommendation.utils;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;

import javax.sql.DataSource;

/**
 * mahout据库工具类
 */
public class DBConnectionUtil {

    private static final Logger logger = Logger.getLogger(DBConnectionUtil.class);

    private static C3p0Plugin CP;

    public static String URL;
    public static String USERNAME;
    public static String PASSWORD;

    private static DataModel dataModel = null;

    /**
     * 初始化
     */
    private static void initialize() {
        try {
            CP = new C3p0Plugin(URL, USERNAME, PASSWORD);

            ActiveRecordPlugin arp = new ActiveRecordPlugin(CP);

            if (!CP.start() || !arp.start())
                logger.error("cp start or arp start failed !");
        } catch (Exception e) {
            logger.error("DB connection failed !");
        }
    }

    /**
     * 获取DataSource
     * @return DataSource
     */
    private static DataSource getDataSource() {
        if (CP == null)
            initialize();
        return CP.getDataSource();
    }

    /**
     * 获取MySQLBooleanPrefJDBCDataModel
     * @return MySQLBooleanPrefJDBCDataModel
     */
    private static MySQLBooleanPrefJDBCDataModel getMySQLJDBCDataModel() {
        // 构造MySQL偏好表
        // 用户浏览时间列名
        String PREF_TABLE_TIME = "view_time";
        // 浏览记录表明
        String PREF_TABLE = "news_logs";
        // 新闻id列名
        String PREF_TABLE_NEWS_ID = "news_id";
        // 用户id列名
        String PREF_TABLE_USER_ID = "user_id";

        return new MySQLBooleanPrefJDBCDataModel(getDataSource(), PREF_TABLE, PREF_TABLE_USER_ID, PREF_TABLE_NEWS_ID,
                PREF_TABLE_TIME);
    }

    /**
     * 获取DataModel
     * @return DataModel
     */
    public static DataModel getDataModel(){
        if(dataModel == null) {
            JDBCDataModel dm = getMySQLJDBCDataModel();
            try {
                dataModel = new ReloadFromJDBCDataModel(dm);
            } catch (TasteException e) {
                e.printStackTrace();
            }
        }
        return dataModel;
    }
}
