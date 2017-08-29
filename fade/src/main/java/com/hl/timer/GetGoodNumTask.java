package com.hl.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import com.hl.dao.NoteDao;
import com.hl.dao.RedisDao;
import com.hl.factory.BasicFactory;
import com.hl.util.Const;

public class GetGoodNumTask extends TimerTask {
 
	private ServletContext servletContext;
	public GetGoodNumTask(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void run() {
		//要执行的任务
		NoteDao noteDao = BasicFactory.getFactory().getInstance(NoteDao.class);
		RedisDao redisDao = BasicFactory.getFactory().getInstance(RedisDao.class);
//		System.out.println("重新写入redis");
//		Jedis jedis = JedisPoolUtils.getJedis();
//		
//		List<Map<String, Double>>list = noteDao.getAllGoodNum();
//		for(int i = 0; i < list.size(); i++){
//			Map<String, Double>map = list.get(i);
//			jedis.zadd(Const.HOT_NOTE, map);
//		}
		//8月2日添加,从redis中提取排序表，存到servlet中，每10分钟更新一次
		//采用大小为3的数组，最多存三个排序表，即是用户最多能得到30分钟前的排序
		//每次更新，数组第一个插入最新排序表，后面的排序表往后挪，最后一个排序表被抛弃,就是队列,先进先出
		List<Map<String, Object>>hot_note_array = (List<Map<String, Object>>) servletContext.getAttribute(Const.HOT_NOTE_ARRAY);
		if(hot_note_array == null){
			System.out.println("11111");
			hot_note_array = new ArrayList<>();
			addNewMap(hot_note_array, redisDao);
		}
		else if (hot_note_array.size() < 3) {
			System.out.println("2222");
			addNewMap(hot_note_array, redisDao);
		}
		else if (hot_note_array.size() == 3) {
			System.out.println("3333");
			hot_note_array.remove(0);
			addNewMap(hot_note_array, redisDao);
		}
		System.out.println("重新写入servletContext完成");
	}

	public String getRandomString(int length) { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }
	
	public void addNewMap(List<Map<String, Object>>hot_note_array,RedisDao redisDao){
			Map<String, Object>map = new HashMap<>();
			map.put(Const.HOT_ID, getRandomString(5)); //5位数的随即字符串作为排序表标识
			map.put(Const.HOT_NOTE, redisDao.getAllNote());
			hot_note_array.add(map);
		    System.out.println(hot_note_array);
		    servletContext.setAttribute(Const.HOT_NOTE_ARRAY, hot_note_array);
	}
	
}
