package com.hl.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.UserDao;
import com.hl.domain.User;
import com.hl.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class UserTest {
	
	@Resource(name = "userDao")
	private UserDao dao;
	
	@Resource(name = "userService")
	private UserService service;
	
	@Test
	public void test1() throws Exception {
	    User user = new User();
	    user.setNickname("我操你妈");
	    user.setTelephone("13763359943");
		System.out.println(service.registerByName(user));
	}
	
	@Test
	public void test2() throws Exception {
		System.out.println(dao.findStarUser(8));
	}

	@Test
	public void test3() throws Exception {
		System.out.println(dao.isFindUserByNick("asdfas"));
	}
	
	@Test
	public void test5() throws Exception {
		User user = new User();
		user.setFade_name("adasd");
		user.setNickname("呼呼");
		user.setPassword("1111");
		System.out.println(dao.addUser(user));
	}
	
	@Test
	public void test6() throws Exception {
		User user = new User();
		user.setPassword("137137lu");
		user.setTelephone("13763359943");
		System.out.println(service.loginUser(user));
	}
	
}
