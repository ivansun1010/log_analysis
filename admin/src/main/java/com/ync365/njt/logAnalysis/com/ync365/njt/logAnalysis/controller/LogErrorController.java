package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.controller;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by ivan on 16/3/8.
 */
@Controller
@RequestMapping(value = "/error")
public class LogErrorController {

    private static final Logger logger = LoggerFactory.getLogger(LogErrorController.class);

    @Autowired
    private LogErrorService logErrorService;

    @RequestMapping(value="list",method = RequestMethod.GET)
    public String list(){
        return "error/errorNum";
    }


    @RequestMapping(value = "/getNum", method = RequestMethod.GET)
    @ResponseBody
    public String num() {
        return logErrorService.getLogErrorByDateRange();
    }

}
