package com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.controller;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by ivan on 16/3/21.
 */
@Controller
@RequestMapping(value = "/user")
public class UserInfoController {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    private LogUserService logUserService;

    @RequestMapping(value = "/userhome")
    public String getUserHome() {
        return "user/userhome";
    }

    @RequestMapping(value = "/userdatevisittime")
    @ResponseBody
    public String getuserDateVisitTime(){
        return logUserService.getUserDateVisitTime("20160126");
    }

}
