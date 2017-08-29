package com.hl.timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class GetGoodNumListener implements ServletContextListener{
	private GetGoodNumTimer timer;
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		timer.stopTimer();
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		System.out.println("启动监听器1");
		timer = new GetGoodNumTimer(sce.getServletContext());
		timer.startTimer();
	}
}
