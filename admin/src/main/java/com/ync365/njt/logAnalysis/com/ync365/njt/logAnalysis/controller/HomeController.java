package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.controller;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogErrorService;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogFunctionService;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogInfoService;
import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ivan on 16/3/10.
 */
@Controller
@RequestMapping(value = "/homepage")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(LogErrorService.class);

    @Autowired
    private LogErrorService logErrorService;

    @Autowired
    private LogInfoService logInfoService;

    @Autowired
    private LogUserService logUserService;

    @Autowired
    private LogFunctionService logFunctionService;

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getIndex() {
        return "index";
    }

    @RequestMapping(value = "/index1", method = RequestMethod.GET)
    public String getIndex1() {
        return "index1";
    }

    @RequestMapping(value = "/pvtotal", method = RequestMethod.GET)
    @ResponseBody
    public String getPVTotal() {
        return logUserService.getPV();
    }

    @RequestMapping(value = "/uvtotal", method = RequestMethod.GET)
    @ResponseBody
    public String getUVTotal() {
        return logUserService.getUV();
    }

    @RequestMapping(value = "/errornum", method = RequestMethod.GET)
    @ResponseBody
    public String getErrorNum() {
        return logErrorService.getLogErrorNum();
    }
    @RequestMapping(value="/functiontime",method = RequestMethod.GET)
    @ResponseBody
    public String getFunctionTime(){
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return logFunctionService.getLogFunctionUseTimeByDate("20160126");
    }
    @RequestMapping(value="/functionvisitnum",method = RequestMethod.GET)
    @ResponseBody
    public String getFunctionvisitnum() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return logFunctionService.getLogFunctionByDate("20160126");
    }
}

