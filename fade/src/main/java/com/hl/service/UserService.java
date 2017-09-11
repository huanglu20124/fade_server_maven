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
	
	//编辑用户信息部分
	String editWallpaperUrl(Integer user_id, String wallpaper_url);
	String editNickname(Integer user_id, String nickname);
	String editSummary(Integer user_id, String summary);
	String editSex(Integer user_id, String sex);
	String editArea(Integer user_id, String area);
	String editSchool(Integer user_id, String school);
	
}
