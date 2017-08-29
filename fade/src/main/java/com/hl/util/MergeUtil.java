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
}
