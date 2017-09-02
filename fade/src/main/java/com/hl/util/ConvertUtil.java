package com.hl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hl.domain.Comment;
import com.hl.domain.Note;

public class ConvertUtil {
    public static List<Map<String, Object>> convertNote2ListMap(List<Note>list,
    		List<List<Map<String, Object>>>image_list,
    		List<List<Map<String, Object>>>tag_list,
    		List<List<Map<String, Object>>>comment_list,
    		Map<Integer,Object>relay_list_map,
    		List<Integer>isGood_List){
    	List<Map<String, Object>>list_ans = new ArrayList<>();
    	for(int i = 0; i < list.size(); i++){
    		Map<String, Object>map = new HashMap<>();
    		Note note = list.get(i);
    		map.put(Const.NOTE_ID, note.getNote_id());
    		map.put(Const.USER_ID, note.getUser_id());
    		map.put(Const.NICKNAME, note.getNickname());
    		map.put(Const.HEAD_IMAGE_URL, note.getHead_image_url());
    		map.put(Const.NOTE_CONTENT, note.getNote_content());
    		map.put(Const.POST_TIME, note.getPost_time());
    		map.put(Const.GOOD_NUM, note.getGood_num());
    		map.put(Const.COMMENT_NUM, note.getComment_num());
    		map.put(Const.RELAY_NUM, note.getRelay_num());
    		map.put(Const.ISRELAY, note.getIsRelay());
    		map.put(Const.POST_AREA, note.getPost_area());
    		//另外附带图片数组和标签数组以及评论数组
    		map.put(Const.COMMENT_LIST,comment_list.get(i));
    		map.put(Const.TAG_LIST, tag_list.get(i));
    		map.put(Const.IMAGE_LIST, image_list.get(i));	
    		//是转发帖的话，要加上一个转发内容链
    		if(note.getIsRelay() != 0){
    			if(relay_list_map != null)
    			map.put(Const.RELAY_LIST, relay_list_map.get(note.getNote_id()));
    		}
    		//添加是否点赞
    		map.put(Const.ISGOOD, isGood_List.get(i));
    		list_ans.add(map);
    	}
    	return list_ans;
    }
    
    public static Map<String, Object> convertNote2Map(Note note,
    		List<Map<String, Object>>image_list,
    		List<Map<String, Object>>tag_list,
    		List<Map<String,Object>>comment_list,
    		Map<Integer,Object>relay_list_map,
    		Integer isGood){
    	
    		Map<String, Object>map = new HashMap<>();
    		map.put(Const.NOTE_ID, note.getNote_id());
    		map.put(Const.USER_ID, note.getUser_id());
    		map.put(Const.NICKNAME, note.getNickname());
    		map.put(Const.HEAD_IMAGE_URL, note.getHead_image_url());
    		map.put(Const.NOTE_CONTENT, note.getNote_content());
    		map.put(Const.POST_TIME, note.getPost_time());
    		map.put(Const.GOOD_NUM, note.getGood_num());
    		map.put(Const.COMMENT_NUM, note.getComment_num());
    		map.put(Const.RELAY_NUM, note.getRelay_num());
    		map.put(Const.ISRELAY, note.getIsRelay());
    		map.put(Const.POST_AREA, note.getPost_area());
    		map.put(Const.IMAGE_LIST, image_list);
    		map.put(Const.TAG_LIST, tag_list);
    		map.put(Const.ISGOOD, isGood);
    		if(note.getIsRelay() != 0)
    		     map.put(Const.RELAY_LIST, relay_list_map.get(note.getNote_id()));
    		//转发帖加入转发链	
    		//加入评论数组
    		map.put(Const.COMMENT_LIST, comment_list);
    	return map;
    }
    
    public static List<Map<String, Object>> convertComment2ListMap(List<Comment>list,
    		Map<Integer, Object>origin_comment_map,
    		List<Integer>comment_isGood_list){
    	List<Map<String, Object>> ans_list = new ArrayList<>();
    	for(int i = 0 ; i < list.size(); i++){
    		Comment comment = list.get(i);
    		Map<String, Object>map = new HashMap<>();
    		map.put(Const.COMMENT_ID, comment.getComment_id());
    		map.put(Const.USER_ID, comment.getUser_id());
    		map.put(Const.NICKNAME, comment.getNickname());
    		map.put(Const.HEAD_IMAGE_URL, comment.getHead_image_url());
    		map.put(Const.TO_COMMENT_ID, comment.getTo_comment_id());
    		map.put(Const.COMMENT_TIME, comment.getComment_time());
    		map.put(Const.COMMENT_CONTENT,comment.getComment_content());
    		map.put(Const.COMMENT_GOOD_NUM, comment.getComment_good_num());
    		map.put(Const.COMMENT_ISGOOD, comment_isGood_list.get(i));
    		if(comment.getTo_comment_id() != 0){
    			map.put(Const.ORIGIN_COMMENT, origin_comment_map.get(comment.getComment_id()));
    		}
    		ans_list.add(map);
    	}
    	return ans_list;
    }
}
