package com.hl.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.hl.dao.CommentDao;
import com.hl.dao.NoteDao;
import com.hl.dao.RedisDao;
import com.hl.domain.Comment;
import com.hl.service.CommentService;
import com.hl.util.Const;
import com.hl.util.ConvertUtil;
import com.hl.util.TimeUtil;

@Service("commentService")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class CommentServiceImpl implements CommentService {
//	private CommentDao commentDao = BasicFactory.getFactory().getInstance(CommentDao.class);
//	private NoteDao noteDao = BasicFactory.getFactory().getInstance(NoteDao.class);
	@Resource(name = "commentDao")
	private CommentDao commentDao;
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	@Resource(name = "redisDao")
	private RedisDao redisDao;
	@Override
	public String getTenCommentByGood(Integer user_id,Integer note_id) {
		//打开详情页时，获取10条热门评论列表
		Map<String, Object>map = new HashMap<>();
		List<Comment>list = commentDao.findTenCommentByGood(note_id);
		if(list == null || list.size() == 0){
			map.put(Const.ERR, "0");
		}else{
			Map<Integer, Object>origin_comment_map = new HashMap<>();
			List<Integer>comment_isGood_list = new ArrayList<>();
			for(Comment comment : list){
				if(comment.getTo_comment_id() != 0){
					//说明是评论的回复,要加上原来的评论
					origin_comment_map.put(comment.getComment_id(), commentDao.findOriginComment(comment.getTo_comment_id()));
				}
				Integer comment_isGood = (commentDao.isHaveCommentGood(comment.getComment_id(),user_id)) == null ? 0 : 1;
				comment_isGood_list.add(comment_isGood);
			}
			map.put(Const.RESULT, ConvertUtil.convertComment2ListMap(list,origin_comment_map,comment_isGood_list));
		}
		return new Gson().toJson(map);
	}

	@Override
	public String addComment(Comment comment) {
		//发评论
		Map<String, Object>map = new HashMap<>();
		//添加评论时间
		comment.setComment_time(TimeUtil.getCurrentTime());
		Integer comment_id ;
		if((comment_id = commentDao.addComment(comment)) == 0){
			map.put(Const.ERR, "添加评论失败，原贴不存在");
		}else {
			try {
				map.put(Const.COMMENT_ID, String.valueOf(comment_id));
				//同时该帖子的评论数量加一
				noteDao.addCommentNum(comment.getNote_id());
			} catch (Exception e) {
				map.put(Const.ERR, "添加评论失败，原贴不存在");
			}	
		}
		return new Gson().toJson(map);
	}

	@Override
	public Map<String, Object> getTwentyCommentByTime(Integer user_id,Integer note_id, Integer start) {
		//按照时间顺序   一次获取20条评论列表
		Map<String, Object>map = new HashMap<>();
		List<Comment>list = commentDao.findTwentyCommentByTime(note_id,start);
		if(list == null || list.size() == 0){
			map.put(Const.ERR, "0");
		}else{
			Map<Integer, Object>origin_comment_map = new HashMap<>();
			List<Integer>comment_isGood_list = new ArrayList<>();
			for(Comment comment : list){
				if(comment.getTo_comment_id() != 0){
					//说明是评论的回复,要加上原来的评论
					origin_comment_map.put(comment.getComment_id(), commentDao.findOriginComment(comment.getTo_comment_id()));
				}
				Integer comment_isGood = (commentDao.isHaveCommentGood(comment.getComment_id(),user_id)) == null ? 0 : 1;
				comment_isGood_list.add(comment_isGood);
			}
			map.put(Const.RESULT, ConvertUtil.convertComment2ListMap(list,origin_comment_map,comment_isGood_list));
		}
		//return new Gson().toJson(map);
		return map;
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRED,readOnly=false)
	@Override
	public String addCommentGood(Integer comment_id, Integer user_id, Integer note_id) {
		//对评论点赞
		Map<String, Object>map = new HashMap<>();
		commentDao.addCommentGood(comment_id,user_id,note_id);
		//更新评论表的点赞数量
		commentDao.updateCommentGoodNum(comment_id);
		return new Gson().toJson(map);
}
	
	@Override
	public List<Map<String, Object>> getThreeCommentForNote(Integer user_id,Integer note_id) {
		List<Comment>list = commentDao.getThreeComment(note_id);
		if(list == null || list.size() == 0){
			return null;
		}else{
			Map<Integer, Object>origin_comment_map = new HashMap<>();
			List<Integer>comment_isGood_list = new ArrayList<>();
			for(Comment comment : list){
				if(comment.getTo_comment_id() != 0){
					//说明是评论的回复,要加上原来的评论
					origin_comment_map.put(comment.getComment_id(), commentDao.findOriginComment(comment.getTo_comment_id()));
				}
				Integer comment_isGood = (commentDao.isHaveCommentGood(comment.getComment_id(),user_id)) == null ? 0 : 1;
				comment_isGood_list.add(comment_isGood);
			}
			return ConvertUtil.convertComment2ListMap(list,origin_comment_map,comment_isGood_list);
		}
	}

	
	@Override
	public String deleteComment(Integer note_id,Integer comment_id,Integer start) {
		Map<String, Object>map = new HashMap<>();
		//删除评论
		if(commentDao.deleteComment(comment_id) == 1){
			//删除原贴所有评论缓存
			redisDao.deleteCacheKey("c1_"+note_id);
			redisDao.deleteCacheKey("c2_"+comment_id);
			redisDao.deleteCacheKey("c4_"+note_id);
			redisDao.deleteCacheKey("c3_"+note_id+"_"+start);
		}else{
			map.put(Const.ERR, "删除失败，原评论不存在");
		}
		return null;
	}	

	
	
}
