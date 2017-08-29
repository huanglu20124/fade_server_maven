package com.hl.dao;

import java.util.List;

public interface RedisDao {
	public List<Object> getNextTwentyNote(Integer start);
	public int deleteNote(Integer note_id);
	public void updateAddNoteRank(Integer note_id, Integer good_num);
	public List<Object> getAllNote();
	
	//以上用于更新排序表
	public void deleteCacheKey(String key);
}
