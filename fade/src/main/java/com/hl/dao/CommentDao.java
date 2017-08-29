package com.hl.dao;

import java.util.List;
import java.util.Map;

import com.hl.domain.Comment;

public interface CommentDao {

	List<Comment> findTenCommentByGood(Integer note_id);

	Integer addComment(Comment comment);

	Map<String, Object> findOriginComment(Integer to_comment_id);

	List<Comment> findTwentyCommentByTime(Integer comment_id, Integer start);

	Integer isHaveCommentGood(Integer comment_id, Integer user_id);

	int addCommentGood(Integer comment_id, Integer user_id);

	int updateCommentGoodNum(Integer comment_id);

	List<Map<String, Object>> getThreeComment(Integer note_id);

}
