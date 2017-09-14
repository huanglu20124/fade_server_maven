package com.hl.dao.impl;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hl.dao.NoteDao;
import com.hl.domain.Note;
import com.hl.domain.User;
import com.hl.util.Const;
import com.hl.util.TimeUtil;

public class NoteDaoImpl extends JdbcDaoSupport implements NoteDao {
    
	/**
	 * 5.6废弃，真的没办法及时更新啊,其实他们不属于热数据，并不用缓存
	 * noteDao缓存方法记录
	 * 核心的缓存
	 * (value="findNoteById",key="#type+'_'+#note_id")开头为0,1,2
	 * 
	 * (value = "findAllImage",key="'3_'+#note_id")
	 * (value = "findAllTag",key="'4_'+#note_id")
	 * (value = "getTwentyRelay",key="'5_'+#note_id+'_'+#start" )
	 * (value = "getTwentyGood",key="'6_'+#note_id+'_'+#start")
	 * (value = "getLatestGoodNum", key = "'7_'+#note_id")
	 * (value = "getNoteThreeNum", key = "'8_'+#note_id") 这个使用频率过低，暂时废弃
	 * (value = "findNoteForAddSecond", key = "'9_'+#note_id")
	 * 一个帖子信息被更新，必须删除的是0,1,2
	 */
	
	@Cacheable(value="findNoteById",key="#type+'_'+#note_id", unless="#result == null")
	@Override
	public Note findNoteById(Integer note_id,int type) {
		//根据note_id找到帖子，同时具有过滤功能
		String sql = null;
		if(type == Const.FANS)//首页存活
		   sql = "select*from note where note_id=? and isDie_fans=0";
		else if(type == Const.STRANGER)//发现页存活
		   sql = "select*from note where note_id=? and isDie_stranger=0";
		else  //直接找原贴
		   sql = "select*from note where note_id=?";
		try {
			return getJdbcTemplate().queryForObject(sql, new noteRowMapper(),note_id);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	@Override
	public List<Map<String, Object>> findMyNote(Integer user_id) {
		//找到自己的全部在首页还存活的帖子，并且是按照note_id大小排序，返回内容包括note_id post_time good_num
		String sql = "select * from note where user_id=? and isDie_fans=0";
		return getJdbcTemplate().queryForList(sql,user_id);
	}

	@Override
	public List<Map<String, Object>> findStarNote(Integer user_id) {
		//找到关注用户全部活帖子，并且是按照note_id大小排序，返回内容包括note_id post_time good_num
		String sql = "select a.* from note a join relation b on a.user_id=b.user_star  and b.user_fans = ? and a.isDie_fans=0";
		return getJdbcTemplate().queryForList(sql, user_id);
	}

	@CacheEvict(value="findNoteById",key="#type+'_'+#note_id")
	@Override
	public int updateNoteDie(Integer note_id,int type) {
		//更新帖子Die字段
		String sql = null;
		if(type == Const.FANS)
		    sql = "update note set isDie_fans=1 where note_id=?";
		else if(type == Const.STRANGER)
			sql = "update note set isDie_stranger=1 where note_id=?";
		return getJdbcTemplate().update(sql,note_id);
	}
	
	@Override
	public int[] updateNoteDieBatch(List<Integer> delete_list, int type) {
		//批量更新
		String sql = null;
		if(type == Const.FANS)
		    sql = "update note set isDie_fans=1 where note_id=";
		else if(type == Const.STRANGER)
			sql = "update note set isDie_stranger=1 where note_id=";
		
	    String[] sqls = new String[delete_list.size()];
	    for(int i = 0; i < delete_list.size(); i++){
	    	sqls[i] = (sql + delete_list.get(i));
	    }   
		return getJdbcTemplate().batchUpdate(sqls);		
	}

	@Override
	public Integer addNote(final Note note) {
		final String sql = "insert into note values(null,?,?,?,?,?,0,0,0,0,0,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1, note.getUser_id()); psm.setString(2, note.getNickname());  
				psm.setString(3, note.getHead_image_url()); psm.setString(4, note.getNote_content());  
				psm.setString(5, note.getPost_time()); psm.setInt(6, note.getIsRelay());
				psm.setString(7, note.getPost_area());
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public int addSecond(Integer note_id) {
		//续一秒
		String sql = "update note set good_num=good_num+1 where note_id=?";
		return getJdbcTemplate().update(sql,note_id);
	}

	@Cacheable(value = "getLatestGoodNum", key = "'7_'+#note_id", unless="#result == -1")
	@Override
	public int getLatestGoodNum(Integer note_id) {
		//查询最新续一秒数
		String sql = "select good_num from note where note_id=?";
		try {
			return getJdbcTemplate().queryForObject(sql, Integer.class,note_id);
		} catch (Exception e) {
			return -1;
		}
		
	}

	@Override
	public int[] addNoteTagBatch(Integer note_id, String tag_list) {
		//批量添加标签
		String sql = "insert into note_tag values(?,"+note_id+")";
		final String[] tag_list_array = tag_list.split(",");
		return getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {	
			@Override
			public void setValues(java.sql.PreparedStatement psm, int i) throws SQLException {
				// TODO Auto-generated method stub
				psm.setInt(1, Integer.valueOf(tag_list_array[i]));
			}		
			@Override
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return tag_list_array.length;
			}
		});
	}


	@Override
	public int[] saveImageBatch(Integer note_id, final List<String> note_image_list,final String[]image_size_list,final String []image_cooordinate_list,
			final Integer image_cut_size) {
		String sql = "insert into note_image values(null,?,"+note_id+",?,?,?)";
		return getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {		
			@Override
			public void setValues(java.sql.PreparedStatement psm, int i) throws SQLException {
				// TODO Auto-generated method stub
				psm.setString(1, note_image_list.get(i));
				psm.setDouble(2, Double.valueOf(image_size_list[i]));
				psm.setString(3, image_cooordinate_list[i]);
				psm.setInt(4, image_cut_size);
			}
			
			@Override
			public int getBatchSize() {
				// TODO Auto-generated method stub
				return note_image_list.size();
			}
		});		
	}
	
	@Cacheable(value = "findAllImage",key="'3_'+#note_id", unless="#result == null")
	@Override
	public List<Map<String, Object>> findAllImage(Integer note_id) {
		//找到所有图片
		String sql = "select image_url, image_size, image_coordinate, image_cut_size from note_image where note_id=?";
		return (List<Map<String, Object>>) getJdbcTemplate().queryForList(sql,note_id);
		
	}

	@Cacheable(value = "findAllTag",key="'4_'+#note_id", unless="#result == null")
	@Override
	public List<Map<String, Object>> findAllTag(Integer note_id) {
		//找到所有标签
		String sql = "select a.tag_id,a.tag_name from tag a join note_tag b  where a.tag_id=b.tag_id and b.note_id=?";
		return getJdbcTemplate().queryForList(sql,note_id);
	}


	@Override
	public int addGoodUser(Integer note_id, User user) {
		//添加点赞者到帖子点赞表
		String sql = "insert into note_good values(null,?,?,?,?,?,?)";
		String good_time = TimeUtil.getCurrentTime();
		return getJdbcTemplate().update(sql,note_id,user.getUser_id(),
				user.getNickname(),good_time,user.getSummary(),user.getHead_image_url());
	}

	@Cacheable(value = "getNoteThreeNum", key = "'8_'+#note_id", unless="#result == null")
	@Override
	public Map<String, Object> getNoteThreeNum(Integer note_id) {
		String sql = "select comment_num,relay_num,good_num from note where note_id=?";
		return getJdbcTemplate().queryForMap(sql,note_id);
	}


//	@Cacheable(value = "getTwentyRelay",key="'5_'+#note_id+'_'+#start" )
	@Override
	public List<Map<String, Object>> getTwentyRelay(Integer note_id, Integer start) {
		//打开详情页，在转发列表一次获取20条转发
		//分页查询
		String sql;
		if(start == 0){
			sql = "select note_id,head_image_url,note_content, post_time,nickname,user_id from note "
					+ "where isRelay=?  order by note_id desc LIMIT 20";
			return getJdbcTemplate().queryForList(sql,note_id);
		}else{
			sql = "select note_id,head_image_url,note_content, post_time,nickname,user_id from note "
					+ "where isRelay=? and note_id<? order by note_id desc LIMIT 20";
			return getJdbcTemplate().queryForList(sql,note_id,start);
		}	
	}

	//@Cacheable(value = "getTwentyGood",key="'6_'+#note_id+'_'+#start")
	@Override
	public List<Map<String, Object>> getTwentyGood(Integer note_id, Integer start) {
		//打开详情页，在列表一次获取20条续一秒
		String sql = null;
		if(start == 0){
			sql = "select head_image_url,good_time,nickname,user_id,summary,good_time,good_id from note_good "
					+ "where note_id=?  order by good_id desc LIMIT 20";
			return getJdbcTemplate().queryForList(sql,note_id);
		}else{
			   sql = "select head_image_url,good_time,nickname,user_id,summary,good_time,good_id from note_good "
						+ "where note_id=? and good_id<? order by good_id desc LIMIT 20";
			return getJdbcTemplate().queryForList(sql,note_id,start);
		}		
	}

	@Override
	public int addRelayNum(Integer origin_id) {
		//原贴转发数量 relay_num加一
		String sql = "update note set relay_num=relay_num+1 where note_id=?";
		return getJdbcTemplate().update(sql,origin_id);
	}

	@Override
	public Integer findIsRelay(Integer isRelay) {
		//得到isRelay
		String sql = "select isRelay from note where note_id=?";
		try {
			return getJdbcTemplate().queryForObject(sql, Integer.class,isRelay);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}	
	}

	@Override
	public Integer isHaveGood(Integer user_id,Integer note_id) {
		//查找是否被赞过
		String sql = "select good_id from note_good where note_id=? and user_id=?";
		try {
			return getJdbcTemplate().queryForObject(sql, Integer.class,note_id,user_id);
		} catch (Exception e) {
			//没有点赞过
			return null;
		}
		
	}

	@Override
	public int addCommentNum(Integer note_id) {
		//评论数量加一
		String sql = "update note set comment_num=comment_num+1 where note_id=?";
		return getJdbcTemplate().update(sql,note_id);
	}

	@Cacheable(value = "findNoteForAddSecond", key = "'9_'+#note_id", unless="#result == null")
	@Override
	public Map<String, Object> findNoteForAddSecond(Integer note_id) {
		//返回续一秒操作需要的，user_id good_num isRelay
		String sql = "select user_id,good_num,isRelay from note where note_id=?";
		return getJdbcTemplate().queryForMap(sql,note_id);
	}
	
	@Override
	public int deleteNote(Integer note_id) {
		//删除帖子
		String sql = "delete from note where note_id=?";
		return getJdbcTemplate().update(sql,note_id);
	}
	
	class noteRowMapper implements RowMapper<Note>{
		@Override
		public Note mapRow(ResultSet rs, int arg1) throws SQLException {
			Note note = new Note();
			note.setNote_id(rs.getInt(Const.NOTE_ID));
			note.setUser_id(rs.getInt(Const.USER_ID));
			note.setNickname(rs.getString(Const.NICKNAME));
			note.setHead_image_url(rs.getString(Const.HEAD_IMAGE_URL));
			note.setNote_content(rs.getString(Const.NOTE_CONTENT));
			note.setPost_time(rs.getString(Const.POST_TIME));
			note.setIsDie_fans(rs.getInt(Const.ISDIE_FANS));
			note.setIsDie_stranger(rs.getInt(Const.ISDIE_STRANGER));
			note.setComment_num(rs.getInt(Const.COMMENT_NUM));
			note.setGood_num(rs.getInt(Const.GOOD_NUM));
			note.setRelay_num(rs.getInt(Const.RELAY_NUM));
			note.setIsRelay(rs.getInt(Const.ISRELAY));
			note.setPost_area(rs.getString(Const.POST_AREA));
			return note;
		}
		
	}

	@Override
	public List<Note> findLatestNoteStar(Integer user_id, Integer last_one) {
		String sql = "select a.* from note a join relation b on a.user_id=b.user_star  "
				+ "and b.user_fans = ? and a.isDie_fans=0 and a.note_id > ?";
		return getJdbcTemplate().query(sql, new noteRowMapper(),user_id,last_one);
	}

	@Override
	public List<Note> findLatestNoteMe(Integer user_id, Integer last_one) {
		String sql = "select* from note where user_id=? and note_id>? and isDie_fans=0";
		return getJdbcTemplate().query(sql, new noteRowMapper(),user_id,last_one);
	}

	@Override
	public int updateNoteHead(String head_image_url,Integer user_id) {
		//更新帖子头像
		String sql = "update note set head_image_url = ? where user_id=?";
		return getJdbcTemplate().update(sql,head_image_url,user_id);
	}

	@Override
	public int updateNoteGoodHead(String head_image_url,Integer user_id) {
		//更新帖子点赞表头像
		String sql = "update note_good set head_image_url = ? where user_id=?";
		return getJdbcTemplate().update(sql,head_image_url,user_id);
	}

	@Override
	public List<Integer> findAllMyNoteId(Integer user_id) {
		//找到个人全部帖子id
		String sql = "select note_id from note where user_id=?";
		return getJdbcTemplate().queryForList(sql,Integer.class,user_id);
	}

	@Override
	public int delCommentNum(Integer note_id) {
		//评论数量减一
		String sql = "update note set comment_num = comment_num-1 where note_id=?";
		return getJdbcTemplate().update(sql,note_id);
	}

	
	@Override
	public int  updateNoteNickname(String nickname, Integer user_id) {
		String sql = "update note set nickname=? where user_id=?";
		return getJdbcTemplate().update(sql,nickname,user_id);
	}
	

	@Override
	public int updateNoteGoodNickname(String nickname, Integer user_id) {
		String sql = "update note_good set nickname=? where user_id=?";
		return getJdbcTemplate().update(sql,nickname,user_id);
	}

	@Override
	public int updateNoteGoodSummary(String summary, Integer user_id) {
		String sql = "update note_good set summary=? where user_id=?";
		return getJdbcTemplate().update(summary,user_id);
	}
	
	







	
}
