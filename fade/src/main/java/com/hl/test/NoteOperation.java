package com.hl.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.RedisDao;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NoteOperation {
	@Resource(name = "redisDao")
	private RedisDao redisDao;
	
	public void updateRedisGoodNum(Integer note_id, Integer good_num){
		redisDao.updateAddNoteRank(note_id, good_num);
	}
	
	@Test
	public void test1() throws Exception {
		updateRedisGoodNum(32, 1000000);
	}
}
