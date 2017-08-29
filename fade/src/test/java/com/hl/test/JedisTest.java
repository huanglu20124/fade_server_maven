package com.hl.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hl.util.JedisPoolUtils;

import redis.clients.jedis.Jedis;

public class JedisTest {
	public static void main(String[] args) {
		test2();
	}
	
	public static void test1(){
		Jedis jedis1 = JedisPoolUtils.getJedis();
		Map<String, Double>map = new HashMap<>();
		map.put("huanglu", 3.0);
		map.put("huangyuan", 2.0);
		map.put("huangjin", 1.0);
		map.put("huangjian", 4.0);
		jedis1.zadd("test2", map);
	}
	
	public static void test2(){
		Jedis jedis1 = JedisPoolUtils.getJedis();
		System.out.println("成员数量"+jedis1.zcard("test2"));
		System.out.println("获得分数："+jedis1.zscore("test2", "huangyuan"));
	    //删除
		System.out.println(jedis1.zrem("test2", "huangyusfsf"));
	}
	
	public static void test3() {
		//返回从大到小
		Jedis jedis1 = JedisPoolUtils.getJedis();
		Set<String> set = jedis1.zrevrange("test2", 0, 3);
		//System.out.println(set.size());
		List<String>list = new ArrayList<>();
		list.addAll(set);
		for(String str:list){
			System.out.println(str);
		}	
	}
	
	public static void test4(){
		Jedis jedis = JedisPoolUtils.getJedis();
		Long i = jedis.zrevrank("test2", "huanglu");
		Set<String>set = jedis.zrevrange("test2", i, i+2);
		List<String>list = new ArrayList<>();
		list.addAll(set);
		for(String str:list){
			System.out.println(str);
		}	
	}
}
