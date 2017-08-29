package com.hl.service;

import com.hl.domain.User;

public interface UserService {
	public String registerByName(User user);
	public String registerQueryTel(String telephone);
	
	public String loginUser(User user);

	public String saveHeadImageUrl(String image_url,Integer user_id);

	public String getHeadImageUrl(User user);
	
	// 7月20日重组
	
	public String updateByWechatId(String wechat_id);
	public String registerWechatId(String js_code,User user);
	
	//测试事务回滚用
	public String test1();
}
