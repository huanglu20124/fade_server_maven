package com.hl.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.hl.dao.CommentDao;
import com.hl.domain.Comment;
import com.hl.util.Const;

public class CommentDaoImpl extends JdbcDaoSupport implements CommentDao {
	/**
	 * commentDao缓存方法记录
	 * 缓存3暂时废弃，因为分页查询暂时无法及时更新
	 * (value="findTenCommentByGood",key="'c1_'+#note_id", unless="#result == null")
	 * (value="findOriginComment",key="'c2_'+#to_comment_id", unless="#result == null")
	 * (value="findTwentyCommentByTime",key="'c3_'+#note_id+'_'+#start", unless="#result == null")
	 * (value="getThreeComment",key="'c4_'+#note_id", unless="#result == null")
	 */

	@Cacheable(value="findTenCommentByGood",key="'c1_'+#note_id", unless="#result == null")
	@Override
	public List<Comment> findTenCommentByGood(Integer note_id) {
		//打开详情页时，一次获取10条热门评论列表，按照点赞量
		String sql = "select*from comment where  note_id=? and comment_good_num>=10 order by comment_good_num desc LIMIT 10";
		return this.getJdbcTemplate().query(sql,new CommentRowMapper(),note_id);
	}

	@CacheEvict(value="findTwentyCommentByTime",key="'c3_'+#comment.getNote_id()+'_0'")
	@Override
	public Integer addComment(final Comment comment) {
		final String sql = "insert into comment values(null,?,?,?,?,?,?,?,0)";
		//返回主键
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			//返回主键
			getJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
					psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
					psm.setInt(1, comment.getUser_id()); psm.setString(2,comment.getNickname());  
					psm.setString(3, comment.getHead_image_url()); psm.setInt(4, comment.getTo_comment_id());  
					psm.setInt(5, comment.getNote_id()); psm.setString(6, comment.getComment_time());
					psm.setString(7, comment.getComment_content());
					return psm;
				}
			},keyHolder);
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return keyHolder.getKey().intValue();
	}

	@Cacheable(value="findOriginComment",key="'c2_'+#to_comment_id", unless="#result == null")
	@Override
	public Map<String, Object> findOriginComment(Integer to_comment_id) {
		//找到原评论内容
		try {
			String sql = "select user_id,nickname,comment_content from comment where comment_id=?";
			return getJdbcTemplate().queryForMap(sql,to_comment_id);
		} catch (Exception e) {
			return null;
		}
	}

	//@Cacheable(value="findTwentyCommentByTime",key="'c3_'+#note_id+'_'+#start", unless="#result == null")
	@Override
	public List<Comment> findTwentyCommentByTime(Integer note_id, Integer start) {
		//打开详情页时，一次获取20条评论列表，按照时间顺序
	    String sql;
		if(start == 0){
			sql = "select*from comment where note_id=? order by comment_id desc LIMIT 20";
			return getJdbcTemplate().query(sql, new CommentRowMapper(),note_id);
		}else {
			sql = "select*from comment where note_id=? and comment_id<? order by comment_id desc LIMIT 20";
			return getJdbcTemplate().query(sql, new CommentRowMapper(),note_id,start);
		}

	}

	@Override
	public Integer isHaveCommentGood(Integer comment_id, Integer user_id) {
		//对评论点赞
		try {
			String sql = "select comment_id from comment_good where comment_id=? and user_id=?";
			return getJdbcTemplate().queryForObject(sql, Integer.class,comment_id,user_id);
		} catch (Exception e) {
			return null;
		}
	}

	@CacheEvict(value="findTenCommentByGood",key="'c1_'+#note_id")
	@Override
	public int addCommentGood(Integer comment_id, Integer user_id,Integer note_id) {
		String sql = "insert into comment_good values(null,?,?)";
		return getJdbcTemplate().update(sql,comment_id,user_id);
	}

	@Override
	public int updateCommentGoodNum(Integer comment_id) {
		//评论点赞数量加一
		String sql = "update comment set comment_good_num=comment_good_num+1 where comment_id=?";
		return getJdbcTemplate().update(sql,comment_id);	
	}

	@Cacheable(value="getThreeComment",key="'c4_'+#note_id", unless="#result == null")
	@Override
	public List<Comment> getThreeComment(Integer note_id) {
		//取三条热门的评论
		String sql = "select * from comment where note_id=? order by comment_good_num desc LIMIT 3";
		return getJdbcTemplate().query(sql, new CommentRowMapper(),note_id);
	}

	@Override
	public int deleteComment(Integer comment_id){
		String sql = "delete from comment where comment_id = ?";
		return getJdbcTemplate().update(sql,comment_id);
	}
	
	class CommentRowMapper implements RowMapper<Comment>{
		@Override
		public Comment mapRow(ResultSet rs, int arg1) throws SQLException {
			Comment comment = new Comment();
			comment.setComment_id(rs.getInt(Const.COMMENT_ID));
			comment.setUser_id(rs.getInt(Const.USER_ID));
			comment.setNickname(rs.getString(Const.NICKNAME));
			comment.setHead_image_url(rs.getString(Const.HEAD_IMAGE_URL));
			comment.setTo_comment_id(rs.getInt(Const.TO_COMMENT_ID));
			comment.setNote_id(rs.getInt(Const.NOTE_ID));
			comment.setComment_time(rs.getString(Const.COMMENT_TIME));
			comment.setComment_content(rs.getString(Const.COMMENT_CONTENT));
			comment.setComment_good_num(rs.getInt(Const.COMMENT_GOOD_NUM));
			return comment;
		}
		
	}

	@Override
	public int updateCommentHead(String head_image_url,Integer user_id) {
		//更新评论头像
		String sql="update comment set head_image_url=? where user_id=?";
		return getJdbcTemplate().update(sql,head_image_url,user_id);
	}

	@Override
	public List<Integer> findAllMyCommentNoteId(Integer user_id) {
		//找到自己评论过的所有帖子，用于删除缓存
		String sql = "select note_id from comment where user_id = ?";
		return getJdbcTemplate().queryForList(sql,Integer.class,user_id);
	}

	@Override
	public int updateCommentNickname(String nickname, Integer user_id) {
		String sql = "update comment set nickname=? where user_id=?";
		return getJdbcTemplate().update(sql,nickname,user_id);
	}
}
