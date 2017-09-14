package com.hl.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import com.hl.dao.RedisDao;
import com.hl.util.Const;
import com.hl.util.JedisPoolUtils;

import redis.clients.jedis.Jedis;

public class RedisDaoImpl implements RedisDao {

	// 8月26号更新，统一redis连接池，加入spring
	private RedisTemplate<String, Object> redisTemplate;
	
	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public List<Object> getNextTwentyNote(Integer start_note_id) {
		// // 检索20条note_id
		// Jedis jedis = JedisPoolUtils.getJedis();
		// Integer index_start;
		// //先找到起始点
		// if(start_note_id == 0){
		// index_start = (Integer) 0;
		// }else{
		// index_start = jedis.zrevrank(Const.HOT_NOTE,
		// start_note_id.toString()).intValue() + 1;
		// }
		// System.out.println(index_start);
		// Set<String>set = jedis.zrevrange(Const.HOT_NOTE, index_start,
		// index_start+19);
		// List<String>list1 = new ArrayList<>();
		// list1.addAll(set);
		// List<Integer>list2 = new ArrayList<>();
		// for(String num_str : list1){
		// System.out.println(num_str);
		// list2.add(Integer.valueOf(num_str));
		// }
		// return list2;
		Integer index_start;
		if (start_note_id == 0) {
			index_start =  0;
		} else {
			index_start = redisTemplate.opsForZSet().reverseRank(Const.HOT_NOTE, start_note_id).intValue()+1;
		}
		Set<Object>set = redisTemplate.opsForZSet().reverseRange(Const.HOT_NOTE, index_start, index_start+19);
		List<Object>list1 = new ArrayList<>();
		list1.addAll(set);
		return list1;
	}

	@Override
	public int deleteNote(Integer note_id) {
		// 删除note
//		Jedis jedis = JedisPoolUtils.getJedis();
//		return jedis.zrem(Const.HOT_NOTE, note_id.toString()).intValue();
		return redisTemplate.opsForZSet().remove(Const.HOT_NOTE, note_id.toString()).intValue();
	}

	@Override
	public void updateAddNoteRank(Integer note_id, Integer good_num) {
		// 更新排名
//		Jedis jedis = JedisPoolUtils.getJedis();
//		Map<String, Double> map = new HashMap<>();
//		map.put(note_id.toString(), integer.doubleValue());
//		jedis.zadd(Const.HOT_NOTE, map);
		redisTemplate.opsForZSet().add(Const.HOT_NOTE, note_id.toString(),good_num);
	}

	@Override
	public List<Object> getAllNote() {
		// 取出所有排序
//		Jedis jedis = JedisPoolUtils.getJedis();
//		Set<String> set = jedis.zrevrange(Const.HOT_NOTE, 0, -1);
//		List<String> hot_note = new ArrayList<>();
//		hot_note.addAll(set);
//		return hot_note;
		if(redisTemplate == null){
			ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
			redisTemplate = (RedisTemplate<String, Object>) applicationContext.getBean("redisTemplate");
		}
		Set<Object> set = redisTemplate.opsForZSet().reverseRange(Const.HOT_NOTE, 0l, -1l);
		List<Object> hot_note = new ArrayList<>();
		hot_note.addAll(set);
		return hot_note;
	}

	
	//以上用于更新排序表
	@Override
	public void deleteCacheKey(String key){
		//先删除值本身
		redisTemplate.delete(key);
		//再删除zset里面的东西	
//		redisTemplate.opsForZSet().remove(value_sb.toString(), key);	
	}
	


}
