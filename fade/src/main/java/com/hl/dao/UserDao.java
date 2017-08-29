package com.hl.dao;

import java.util.List;

import com.hl.domain.User;

public interface UserDao {

	//注册
	Object isFindUserByNick(String nickname);
	Object isFindUserByTel(String telephone);
	Object isFindUserByOpenId(String opendId, String type);
	Object isFindUserByFadeName(String fade_name);
	
	//登录
	Object isFindUserByTelPwd(String telephone,String password);
	Object isFindUserByFadePwd(String fade_name,String password);
	
	//添加用户
	Integer addUser(User user);

	//废弃：通过手机号存储头像url
	int saveImageUrlByTel(String imageUrl, String telephone);
	
	//废弃：通过openId存储头像url
	int saveImageUrlByOpenId(String imageUrl, String openId, String type);
	
	//通过user_id存储头像url
	int saveImageUrlByUserId(String imageUrl, Integer user_id);
	String getUserSummary(Integer user_id);
	User getUserByUserId(Integer user_id);
	public List<User> findStarUser(Integer user_id);

}
