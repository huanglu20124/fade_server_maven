package com.hl.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.CommentDao;
import com.hl.domain.Comment;
import com.hl.factory.BasicFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class CommentTest {
	
	@Resource(name = "commentDao")
	private  CommentDao commentDao;
	@Test
	public void test2() throws Exception {
		commentDao.addCommentGood(999, 999, 999);
	}
}
