package com.hl.dao;

import java.util.List;
import java.util.Map;

import com.hl.domain.Note;
import com.hl.domain.User;

public interface NoteDao {
	public List<Map<String, Object>> findStarNote(Integer user_id);
	public List<Map<String, Object>> findMyNote(Integer user_id);
	public int updateNoteDie(Integer note_id,int type);
	public Note findNoteById(Integer note_id,int type);
	public Integer addNote(Note note);
	public int addSecond(Integer note_id);
	public int getLatestGoodNum(Integer note_id);
	public List<Map<String, Object>> findAllImage(Integer note_id);
	public List<Map<String, Object>> findAllTag(Integer note_id);
	public int addGoodUser(Integer note_id, User user);
	public Map<String, Object> getNoteThreeNum(Integer note_id);
	public List<Map<String, Object>> getTwentyRelay(Integer note_id, Integer start);
	public List<Map<String, Object>> getTwentyGood(Integer note_id, Integer start);
	public int addRelayNum(Integer origin_id);
	public Integer findIsRelay(Integer isRelay);
	public Integer isHaveGood(Integer user_id, Integer note_id);
	public int addCommentNum(Integer note_id);
	public int[] updateNoteDieBatch(List<Integer> delete_list, int type);
	public int[] addNoteTagBatch(Integer note_id, String tag_list);
	public int[] saveImageBatch(Integer note_id, List<String> note_image_list,String[]image_size_list,String []image_cooordinate_list,
			Integer image_cut_size);
	public Map<String,Object> findNoteForAddSecond(Integer note_id);
	public int deleteNote(Integer note_id);
	public List<Note> findLatestNoteStar(Integer user_id, Integer last_one);
	public List<Note> findLatestNoteMe(Integer user_id, Integer last_one);
	public int updateNoteHead(String head_image_url,Integer user_id);
	public int updateNoteGoodHead(String head_image_url,Integer user_id);
	public List<Integer> findAllMyNoteId(Integer user_id);
	public int delCommentNum(Integer note_id);
	public int updateNoteNickname(String nickname, Integer user_id);
	public int updateNoteGoodNickname(String nickname, Integer user_id);
	public int updateNoteGoodSummary(String summary, Integer user_id);
}
