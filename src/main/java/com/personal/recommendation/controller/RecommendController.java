package com.personal.recommendation.controller;

import com.personal.recommendation.constant.ResultEnum;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("recommend")
public class RecommendController {

    private final CalculatorService calculatorService;
    @Autowired
    public RecommendController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping(value = "test")
    @ResponseBody
    public BaseRsp runTestData(@RequestParam("uid") Long uid, @RequestParam("type") int type) {
        List<Long> idList = new ArrayList<>();
        idList.add(uid);
        return new BaseRsp(ResultEnum.SUCCESS, calculatorService.executeInstantJob(idList, type));
    }

}
