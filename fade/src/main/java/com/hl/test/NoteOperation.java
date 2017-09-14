package com.hl.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.NoteDao;
import com.hl.dao.RedisDao;
import com.hl.service.NoteService;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NoteOperation {
	@Resource(name = "redisDao")
	private RedisDao redisDao;
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	@Resource(name = "noteService")
	private NoteService noteService;
	
	public void updateRedisGoodNum(Integer note_id){
		redisDao.updateAddNoteRank(note_id, noteDao.getLatestGoodNum(note_id));
	}
	
	@Test
	public void forever() throws Exception {
		//为帖子设置永久
		updateRedisGoodNum(202);
	}
	
}
