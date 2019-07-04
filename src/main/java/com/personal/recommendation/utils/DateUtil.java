package com.personal.recommendation.utils;


import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理工具类
 */
public class DateUtil {

    /**
     * 获取当前时间多天前的时间
     *
     * @param days 天数
     * @return none
     */
    public static Date getDateBeforeDays(int days) {
        // 得到日历
        Calendar calendar = Calendar.getInstance();
        // 设置为前beforeNum天
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return new Timestamp(calendar.getTime().getTime());
    }

}
