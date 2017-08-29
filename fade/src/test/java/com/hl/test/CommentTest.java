package com.hl.test;

import com.hl.dao.CommentDao;
import com.hl.domain.Comment;
import com.hl.factory.BasicFactory;

public class CommentTest {
	private static CommentDao commentDao = BasicFactory.getFactory().getInstance(CommentDao.class);
	public static void main(String[] args) {
		test1();
	}
	
	public static void test1(){
		for(int i = 0; i < 40; i++){
			Comment comment = new Comment();
			comment.setUser_id(7);
			comment.setNote_id(579);
			comment.setNickname("黄浩伦");
			comment.setComment_content("测试"+i);
			comment.setTo_comment_id(0);
			comment.setHead_image_url("头像未设置no");
			commentDao.addComment(comment);
		}

	}
}
