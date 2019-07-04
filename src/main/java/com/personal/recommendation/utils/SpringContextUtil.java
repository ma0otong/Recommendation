package com.personal.recommendation.utils;

import org.springframework.context.ApplicationContext;

/**
 * 静态注入工具类
 */
public class SpringContextUtil {

    //spring上下文
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     *
     * @param applicationContext ApplicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (null == SpringContextUtil.applicationContext)
            SpringContextUtil.applicationContext = applicationContext;
    }

    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name String
     * @return Object
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

}
