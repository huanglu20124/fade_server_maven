package com.hl.domain;

import java.io.Serializable;
import java.util.Date;

public class Tag implements Serializable{
	private Integer tag_id;
	private Integer tag_name;
	private String tag_image_url;
	private Date tag_register_time;
	private String tag_summary;
	private Integer tag_good_sum;
	private Integer tag_concern_sum;
	
	public Integer getTag_id() {
		return tag_id;
	}
	public void setTag_id(Integer tag_id) {
		this.tag_id = tag_id;
	}
	public Integer getTag_name() {
		return tag_name;
	}
	public void setTag_name(Integer tag_name) {
		this.tag_name = tag_name;
	}
	public String getTag_image_url() {
		return tag_image_url;
	}
	public void setTag_image_url(String tag_image_url) {
		this.tag_image_url = tag_image_url;
	}
	public Date getTag_register_time() {
		return tag_register_time;
	}
	public void setTag_register_time(Date tag_register_time) {
		this.tag_register_time = tag_register_time;
	}
	public String getTag_summary() {
		return tag_summary;
	}
	public void setTag_summary(String tag_summary) {
		this.tag_summary = tag_summary;
	}
	public Integer getTag_good_sum() {
		return tag_good_sum;
	}
	public void setTag_good_sum(Integer tag_good_sum) {
		this.tag_good_sum = tag_good_sum;
	}
	public Integer getTag_concern_sum() {
		return tag_concern_sum;
	}
	public void setTag_concern_sum(Integer tag_concern_sum) {
		this.tag_concern_sum = tag_concern_sum;
	}
	
	
}
