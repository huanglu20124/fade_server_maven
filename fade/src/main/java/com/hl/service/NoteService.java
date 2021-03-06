package com.hl.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hl.domain.Note;

public interface NoteService {

	String getBigSectionHome(Integer user_id, Integer start );

	String getSmallSectionHome(Integer user_id,String bunch);

	String addOriginNote(Note note, String parameter);

	String addRelayNote(Note note, String parameter);
	
	String addSecond(Integer user_id,Integer note_id, Integer isRelay,int type);

	String getSectionDiscoverRecommond(HttpServletRequest request, Integer user_id,Integer start,String hot_id);
	
	public String saveNoteImageUrl(List<String> note_image_list,String[]image_size_list, String []image_cooordinate_list,
			Integer image_cut_size,Integer note_id);

	String getLatestGoodNum(Integer note_id);

	String getLatestGoodNumMul(String note_id_list);

	String getLatestThreeNumAndSummary(Integer note_id,Integer user_id);

	String getTwentyRelay(Integer note_id, Integer start);

	String getTwentyGood(Integer note_id, Integer start);

	String getNoteDetail(Integer user_id,Integer note_id);
	
	public  List<Note> updateNoteDieForMap(List<Map<String, Object>> note_list, int type);
	
	public  List<Note> updateNoteDieForNote(List<Note> note_list, int type);
	
	public String deleteNote(Integer note_id);
	
	public String topReload(Integer user_id,String bunch);

}
