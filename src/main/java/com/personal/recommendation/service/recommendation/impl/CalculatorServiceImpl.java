package com.personal.recommendation.service.recommendation.impl;

import com.personal.recommendation.service.recommendation.CalculatorService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * 任务处理接口实现类
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {

    private static final Logger logger = Logger.getLogger(CalculatorServiceImpl.class);

    /**
     * 执行单次计算
     *
     * @param userId 用户id
     * @return BaseRsp
     */
    @Override
    public void executeInstantJob(Long userId) {
        requestQueue.add(userId);
        logger.info("UserId : " + userId + ", calculation task added .");
    }
}
