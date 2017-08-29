package com.hl.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

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
	 * 
	 */

	@Override
	public List<Comment> findTenCommentByGood(Integer note_id) {
		//打开详情页时，一次获取10条热门评论列表，按照点赞量
		String sql = "select*from comment where  note_id=? and comment_good_num>=10 order by comment_good_num desc LIMIT 10";
		return this.getJdbcTemplate().query(sql,new CommentRowMapper(),note_id);
	}

	@Override
	public Integer addComment(final Comment comment) {
		final String sql = "insert into comment values(null,?,?,?,?,?,?,?,?,?,0)";
		//返回主键
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1, comment.getUser_id()); psm.setString(2,comment.getNickname());  
				psm.setString(3, comment.getHead_image_url()); psm.setInt(4, comment.getTo_comment_id());  
				psm.setInt(5, comment.getTo_user_id()); psm.setString(6, comment.getTo_user_nickname());
				psm.setInt(7, comment.getNote_id()); psm.setString(8, comment.getComment_time());
				psm.setString(9, comment.getComment_content());
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public Map<String, Object> findOriginComment(Integer to_comment_id) {
		//找到原评论内容
		String sql = "select user_id,nickname,comment_content from comment where comment_id=?";
		return getJdbcTemplate().queryForMap(sql,to_comment_id);
	}

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
			return 0;
		}
		
	}

	@Override
	public int addCommentGood(Integer comment_id, Integer user_id) {
		String sql = "insert into comment_good values(null,?,?)";
		return getJdbcTemplate().update(sql,comment_id,user_id);
	}

	@Override
	public int updateCommentGoodNum(Integer comment_id) {
		//评论点赞数量加一
		String sql = "update comment set comment_good_num=comment_good_num+1 where comment_id=?";
		return getJdbcTemplate().update(sql,comment_id);	
	}

	@Override
	public List<Map<String, Object>> getThreeComment(Integer note_id) {
		//取三条非回复类型的评论
		String sql = "select user_id,nickname,comment_content,to_comment_id,to_user_id,to_user_nickname "
				+ "from comment where note_id=? order by comment_id desc LIMIT 3";
		return getJdbcTemplate().queryForList(sql,note_id);
		
		

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
			comment.setTo_user_id(rs.getInt(Const.TO_USER_ID));
			comment.setTo_user_nickname(rs.getString(Const.TO_USER_NICKNAME));
			comment.setNote_id(rs.getInt(Const.NOTE_ID));
			comment.setComment_time(rs.getString(Const.COMMENT_TIME));
			comment.setComment_content(rs.getString(Const.COMMENT_CONTENT));
			comment.setComment_good_num(rs.getInt(Const.COMMENT_GOOD_NUM));
			return comment;
		}
		
	}
}
