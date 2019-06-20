package com.personal.recommendation.controller;

import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.service.recommendation.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("recommend")
public class RecommendController {

    private final CalculatorService calculatorService;

    @Autowired
    public RecommendController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping(value = "/test/{userId}")
    @ResponseBody
    public BaseRsp runTestData(@PathVariable("userId") Long userId) {
        calculatorService.executeInstantJob(userId);
        return new BaseRsp(ResultEnum.SUCCESS, ResultEnum.SUCCESS.getMsg());
    }



}
