package com.hl.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.hl.dao.NoteDao;
import com.hl.dao.RedisDao;
import com.hl.domain.Note;
import com.hl.service.NoteService;
import com.hl.util.Const;
import com.hl.util.JsonUtil;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NoteTest {
//	private static NoteDao noteDao = BasicFactory.getFactory().getInstance(NoteDao.class);
//	private static NoteService noteService = BasicFactory.getFactory().getInstance(NoteService.class);
	
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "redisDao")
	private RedisDao redisDao;
	
	@Resource(name = "noteService")
	private NoteService noteService;
	
	public  void test1() {
		for(int i = 1; i <= 20; i++){
			try {
				Note note = new Note();
				note.setUser_id(7);
				note.setNickname("黄浩伦");
				note.setHead_image_url("https://sysufade.cn/Fade/image/head/8af431_1500709261912.png");
				note.setIsRelay(0);
				note.setNote_content("原创水贴"+i);
				noteService.addOriginNote(note, "标签1,标签2,标签3");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	public  void test2(){
		int k = 1;
			for(int q = 0; q < 10; q++){
				//8号点赞
				//间接点赞的方式可无限刷
				noteService.addSecond(8,68, 67,1);
			}
			k++;
	}
	
	@Test
	public void test3() throws Exception {
		List<Integer>list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		System.out.println(list.subList(0, 3));
	}
	
	@Test
	public void test4() throws Exception {
		System.out.println(noteDao.findAllImage(2));
		System.out.println(noteDao.findAllTag(2));	
	}
	
	@Test
	public void test5() throws Exception {
		System.out.println(noteDao.findAllTag(100));
	}
	
	

	

	

}
