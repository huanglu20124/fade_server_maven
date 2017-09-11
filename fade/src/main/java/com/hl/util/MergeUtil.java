package com.hl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hl.domain.Note;

public class MergeUtil {
	public static List<Map<String, Object>> mergeById(List<Map<String, Object>>star_list,List<Map<String, Object>>my_list){
		//归并排序
		List<Map<String, Object>>ans_list = new ArrayList<>();
		int i = 0, j = 0, k = 0;
		while(i < star_list.size() && j < my_list.size()){
			if((int)star_list.get(i).get(Const.NOTE_ID) <= (int)my_list.get(j).get(Const.NOTE_ID)){
				ans_list.add(k++, star_list.get(i++));
			}else {
				ans_list.add(k++, my_list.get(j++));
			}
		}
		while(i < star_list.size()){
			ans_list.add(k++, star_list.get(i++));
		}
		while(j < my_list.size()){
			ans_list.add(k++, my_list.get(j++));
		}
		return ans_list;
	}

	public static List<Note> mergeByIdForNote(List<Note> latest_notes_star, List<Note> latest_notes_me) {
		//归并排序
		List<Note>ans_list = new ArrayList<>();
		int i = 0, j = 0, k = 0;
		while(i < latest_notes_star.size() && j < latest_notes_me.size()){
			if(latest_notes_star.get(i).getNote_id() <= latest_notes_me.get(j).getNote_id()){
				ans_list.add(k++, latest_notes_star.get(i++));
			}else {
				ans_list.add(k++, latest_notes_me.get(j++));
			}
		}
		while(i < latest_notes_star.size()){
			ans_list.add(k++, latest_notes_star.get(i++));
		}
		while(j < latest_notes_me.size()){
			ans_list.add(k++, latest_notes_me.get(j++));
		}
		return ans_list;
	}
}
