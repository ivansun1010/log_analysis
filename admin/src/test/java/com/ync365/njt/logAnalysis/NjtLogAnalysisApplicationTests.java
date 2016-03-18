package com.ync365.njt.logAnalysis;

import com.ync365.njt.logAnalysis.com.ync365.njt.logAnalysis.service.LogInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NjtLogAnalysisApplication.class)
@WebAppConfiguration
public class NjtLogAnalysisApplicationTests {

	@Autowired
	private LogInfoService logInfoService;
	@Test
	public void contextLoads() {
	}

	@Test
	public void test() {
		logInfoService.test();
	}

}
