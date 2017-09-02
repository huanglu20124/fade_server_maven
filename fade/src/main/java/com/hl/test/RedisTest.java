package com.hl.test;


import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.RedisDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class RedisTest {
	
	@Resource(name = "redisDao")
	private RedisDao redisDao;
	@Test
	public void test1() throws Exception {
		System.out.println(redisDao.getAllNote().get(0) instanceof Integer);
		//redisDao.updateAddNoteRank(2, 10000);
	}
}
