package com.hl.service;

import java.util.Map;

import com.hl.domain.Comment;

public interface CommentService {

	String getTenCommentByGood(Integer note_id);

	String addComment(Comment comment);

	Map<String, Object> getTwentyCommentByTime(Integer note_id, Integer start);

	String addCommentGood(Integer comment_id, Integer user_id, Integer note_id);
	
}
