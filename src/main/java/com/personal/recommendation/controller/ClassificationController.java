package com.personal.recommendation.controller;

import com.personal.recommendation.constants.ResultEnum;
import com.personal.recommendation.model.BaseRsp;
import com.personal.recommendation.service.classification.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("classification")
public class ClassificationController {

    private final ClassificationService classificationService;

    @Autowired
    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping(value = "/test")
    @ResponseBody
    public BaseRsp runTestData(@RequestParam("text") String text) {
        return new BaseRsp(ResultEnum.SUCCESS, classificationService.predict(text));
    }
}
