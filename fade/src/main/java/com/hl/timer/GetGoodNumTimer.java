package com.hl.timer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;

import javax.servlet.ServletContext;

public class GetGoodNumTimer {
	private Timer timer;
	private ServletContext servletContext;
	public GetGoodNumTimer(ServletContext servletContext){
		this.servletContext = servletContext;
	}
	public void startTimer(){
		timer = new Timer();
		GregorianCalendar gc = new GregorianCalendar();
		//系统启动一分钟后开始执行任务
		gc.setTime(new Date());
		gc.add(Calendar.SECOND, 10);
		
		//每10分钟执行一次
		timer.schedule(new GetGoodNumTask(servletContext), gc.getTime(),600000);
	}
	
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			System.out.println("定时器关闭了");
		}
	}
	
}
