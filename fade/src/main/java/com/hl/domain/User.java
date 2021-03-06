package com.hl.domain;

import java.io.Serializable;

public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2838701809575809429L;
	private Integer user_id;
	private String nickname;
	private String telephone;
	private String password;
	private String fade_name;
	
	private String sex;
	private String mail;
	private String head_image_url;
	private String register_time;
	private String summary;
	private String school;

	//第三方提供的id
	private String wechat_id;
	public Integer getConcern_num() {
		return concern_num;
	}
	public void setConcern_num(Integer concern_num) {
		this.concern_num = concern_num;
	}
	public Integer getFans_num() {
		return fans_num;
	}
	public void setFans_num(Integer fans_num) {
		this.fans_num = fans_num;
	}
	public String getAera() {
		return aera;
	}
	public void setAera(String aera) {
		this.aera = aera;
	}
	public String getWallpapaer_url() {
		return wallpapaer_url;
	}
	public void setWallpapaer_url(String wallpapaer_url) {
		this.wallpapaer_url = wallpapaer_url;
	}
	private String weibo_id;
	private String qq_id;	
	
	//7月22日新增属性
	private Integer concern_num;
	private Integer fans_num;
	private String aera;
	private String wallpapaer_url;
	
	//fade数量
	private Integer fade_num;
	
	
	
	public Integer getFade_num() {
		return fade_num;
	}
	public void setFade_num(Integer fade_num) {
		this.fade_num = fade_num;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public String getHead_image_url() {
		return head_image_url;
	}
	public void setHead_image_url(String head_image_url) {
		this.head_image_url = head_image_url;
	}
	public String getFade_name() {
		return fade_name;
	}
	public void setFade_name(String fade_name) {
		this.fade_name = fade_name;
	}
	public String getWeibo_id() {
		return weibo_id;
	}
	public void setWeibo_id(String weibo_id) {
		this.weibo_id = weibo_id;
	}
	public String getQq_id() {
		return qq_id;
	}
	public void setQq_id(String qq_id) {
		this.qq_id = qq_id;
	}
	
	

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getRegister_time() {
		return register_time;
	}
	public void setRegister_time(String register_time) {
		this.register_time = register_time;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getWechat_id() {
		return wechat_id;
	}
	public void setWechat_id(String wechat_id) {
		this.wechat_id = wechat_id;
	}
	
	
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "nickname="+nickname
				+"user_id="+user_id;
	}
}
