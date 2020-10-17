package com.hc.manage;

import com.hc.manage.controller.IndexController;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HcManageApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

	@Test
	void contextLoads() {
	}

	@Test
	public void test() {
		logger.debug("fdsafsd");
	}

}
