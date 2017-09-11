package com.hl.service.impl;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.junit.runner.RunWith;
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
import com.hl.dao.UserDao;
import com.hl.domain.Note;
import com.hl.domain.User;
import com.hl.factory.BasicFactory;
import com.hl.service.CommentService;
import com.hl.service.NoteService;
import com.hl.util.Const;
import com.hl.util.ConvertUtil;
import com.hl.util.MergeUtil;
import com.hl.util.TimeUtil;
import com.mchange.v2.async.StrandedTaskReporting;
import com.mysql.jdbc.Field;

@Service("noteService")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class NoteServiceImpl implements NoteService {
	// private NoteDao noteDao =
	// BasicFactory.getFactory().getInstance(NoteDao.class);
	// private CommentDao commentDao =
	// BasicFactory.getFactory().getInstance(CommentDao.class);
	// private RedisDao redisDao =
	// BasicFactory.getFactory().getInstance(RedisDao.class);

	@Resource(name = "noteDao")
	private NoteDao noteDao;

	@Resource(name = "userDao")
	private UserDao userDao;

	@Resource(name = "commentDao")
	private CommentDao commentDao;

	@Resource(name = "redisDao")
	private RedisDao redisDao;

	@Resource(name = "commentService")
	private CommentService commentService;

	public void dealWithRelay(Map<Integer, Object> relay_list_map, Note note) {
		List<Map<String, Object>> list_item = new ArrayList<>();
		Note note_relay = note;
		while (true) {
			note_relay = noteDao.findNoteById(note_relay.getIsRelay(), Const.ALL);
			if (note_relay == null) {
				// 遇到有帖子删除的情况，直接停止
				break;
			}
			Map<String, Object> map_note = new HashMap<>();
			// 这是转发
			map_note.put(Const.NICKNAME, note_relay.getNickname());
			map_note.put(Const.USER_ID, note_relay.getUser_id());
			map_note.put(Const.NOTE_ID, note_relay.getNote_id());
			map_note.put(Const.NOTE_CONTENT, note_relay.getNote_content());
			map_note.put(Const.POST_TIME, note_relay.getPost_time());

			if (note_relay.getIsRelay() == 0) {
				// 说明已经到原创贴了，再加上头像图片啥的
				map_note.put(Const.HEAD_IMAGE_URL, note_relay.getHead_image_url());
				// 最终原创贴的图片
				List<Map<String, Object>> image_list = noteDao.findAllImage(note_relay.getNote_id());
				map_note.put(Const.IMAGE_LIST, image_list);
				list_item.add(map_note);
				break;
			}
			list_item.add(map_note);
		}
		relay_list_map.put(note.getNote_id(), list_item);
	}

	@Override
	public String getBigSectionHome(Integer user_id, Integer start) {
		Map<String, Object> map = new HashMap<>();
		// 大请求 得到帖子集合，并且是按note_id排序的
		List<Map<String, Object>> star_list = noteDao.findStarNote(user_id);
		List<Map<String, Object>> my_list = noteDao.findMyNote(user_id);
		List<Map<String, Object>> note_list_merge = MergeUtil.mergeById(star_list, my_list); // 两个数组归并

		int size = note_list_merge.size();
		// 假如小于100条
		if (size <= 20) {
			// 对于要发送内容的，先计算过滤一遍过期的,对于活着的，并且当前需要的，才去索要一个帖子的全部内容
			List<Note> update_list = updateNoteDieForMap(note_list_merge, Const.FANS);
			// 反转一下
			Collections.reverse(update_list);
			if (update_list.size() == 0) {
				map.put(Const.ERR, "0");
			} else {
				// 添加标签表和图片表
				List<List<Map<String, Object>>> image_list = new ArrayList<>();
				List<List<Map<String, Object>>> tag_list = new ArrayList<>();
				List<List<Map<String, Object>>> comment_list = new ArrayList<>();
				Map<Integer, Object> relay_list_map = new HashMap<>();
				// 2017.8.31号新增，是否点赞的数组
				List<Integer> isGood_List = new ArrayList<>();

				for (Note note : update_list) {
					Integer note_id = note.getNote_id();
					image_list.add(noteDao.findAllImage(note_id));
					tag_list.add(noteDao.findAllTag(note_id));
					// 24号新增，转发帖获取转发链的内容
					if (note.getIsRelay() != 0) {
						// 是转发帖的话
						dealWithRelay(relay_list_map, note);
					}
					// 3号新增,热评数组
					comment_list.add(commentService.getThreeCommentForNote(user_id, note_id));
					if (noteDao.isHaveGood(user_id, note_id) != null) {
						isGood_List.add(1);
					} else {
						isGood_List.add(0);
					}
				}
				map.put(Const.RESULT, ConvertUtil.convertNote2ListMap(update_list, image_list, tag_list, comment_list,
						relay_list_map, isGood_List));
			}
		} else {
			List<Map<String, Object>> result_list = new ArrayList<>();
			List<Integer> id_list = new ArrayList<>();
			// 找到起始点
			int next = 0;
			if (start == 0) {
				next = size - 1;
			} else {
				for (int i = size - 1; i >= 0; i--) {
					if ((int) note_list_merge.get(i).get(Const.NOTE_ID) < start) {
						next = i;
						break;
					}
				}
			}
			System.out.println("next:" + next);
			System.out.println("next的id" + (int) note_list_merge.get(next).get(Const.NOTE_ID));

			allocNote(user_id, note_list_merge, result_list, id_list, next);
			if (result_list.size() == 0) {
				map.put(Const.ERR, "0");
			}
			// 对于要发送内容的，先计算过滤一遍过期的，并且加上帖子数组和和图片数组
			// 附加的note_id则暂时不需要其他过滤操作
			map.put(Const.RESULT, result_list);
			map.put(Const.ID_LIST, id_list);
		}

		return new Gson().toJson(map);
	}

	@Override
	public List<Note> updateNoteDieForMap(List<Map<String, Object>> note_list, int type) {
		// 对note表中的isDie_fans进行更新，写入数据库，同时筛选出来我们需要的帖子
		List<Note> list_ans = new ArrayList<>();
		// 8月22更新，引入批量操作
		List<Integer> delete_list = new ArrayList<>();
		Date current_date = new Date();
		if (type == Const.FANS) {
			for (Map<String, Object> map : note_list) {
				Integer note_id = (Integer) map.get(Const.NOTE_ID);
				Date post_date = TimeUtil.getTimeDate((String) map.get(Const.POST_TIME));
				// 当前时间减去发帖时间，分钟数
				int minute1 = (int) ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
				int time_fans = 60 + (int) map.get(Const.GOOD_NUM) * 5;
				int isDie_fans = minute1 > time_fans ? 1 : 0;
				if (isDie_fans == 1) {
					// 宣布一个帖子fans方向 死亡
					// noteDao.updateNoteDie(note.getNote_id(),Consti.FANS);
					// 8月22更新，引入批量操作
					delete_list.add(note_id);
				} else {
					// 先要验证帖子是不是存在
					Note note_temp = noteDao.findNoteById(note_id, Const.ALL);
					if (note_temp != null)
						list_ans.add(note_temp);
				}
			}
			if (delete_list.size() > 0)
				noteDao.updateNoteDieBatch(delete_list, Const.FANS);
			for (Integer delete_id : delete_list) {
				// 删除对应缓存
				redisDao.deleteCacheKey(type + "_" + delete_id);// 还在对应type的存活状态
				redisDao.deleteCacheKey("2_" + delete_id);// 原贴
			}

		} else {
			for (Map<String, Object> map : note_list) {
				Integer note_id = (Integer) map.get(Const.NOTE_ID);
				Date post_date = TimeUtil.getTimeDate((String) map.get(Const.POST_TIME));
				// 当前时间减去发帖时间，分钟数
				int minute2 = (int) ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
				int time_stranger = 15 + (int) map.get(Const.GOOD_NUM) * 5;
				int isDie_stranger = minute2 > time_stranger ? 1 : 0;
				if (isDie_stranger == 1) {
					// 宣布一个帖子stranger方向 死亡
					// noteDao.updateNoteDie(note.getNote_id(),Const.STRANGER);
					// 8月22更新，引入批量操作
					delete_list.add(note_id);
					// 更新redis
					redisDao.deleteNote(note_id);
				} else {
					// 先要验证帖子是不是存在
					Note note_temp = noteDao.findNoteById(note_id, Const.ALL);
					if (note_temp != null)
						list_ans.add(note_temp);
				}
			}
			if (delete_list.size() > 0)
				noteDao.updateNoteDieBatch(delete_list, Const.STRANGER);
			for (Integer delete_id : delete_list) {
				// 删除对应缓存
				redisDao.deleteCacheKey(type + "_" + delete_id);// 对应type的存活状态
				redisDao.deleteCacheKey("2_" + delete_id);// 原贴
			}
		}
		return list_ans;
	}

	@Override
	public List<Note> updateNoteDieForNote(List<Note> note_list, int type) {
		// 对note表中的isDie_fans进行更新，写入数据库，同时筛选出来我们需要的帖子
		List<Note> list_ans = new ArrayList<>();
		// 8月22更新，引入批量操作
		List<Integer> delete_list = new ArrayList<>();
		Date current_date = new Date();
		if (type == Const.FANS) {
			for (Note note : note_list) {
				Integer note_id = note.getNote_id();
				Date post_date = TimeUtil.getTimeDate(note.getPost_time());
				// 当前时间减去发帖时间，分钟数
				int minute1 = (int) ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
				int time_fans = 60 + note.getGood_num() * 5;
				int isDie_fans = minute1 > time_fans ? 1 : 0;
				if (isDie_fans == 1) {
					// 宣布一个帖子fans方向 死亡
					// noteDao.updateNoteDie(note.getNote_id(),Consti.FANS);
					// 8月22更新，引入批量操作
					delete_list.add(note_id);
					// 删除对应缓存
					redisDao.deleteCacheKey("2_" + Const.FANS + note_id);
				} else {
					// 先要验证帖子是不是存在
					Note note_temp = noteDao.findNoteById(note_id, Const.ALL);
					if (note_temp != null)
						list_ans.add(note_temp);
				}
			}
			if (delete_list.size() > 0)
				noteDao.updateNoteDieBatch(delete_list, Const.FANS);
			for (Integer delete_id : delete_list) {
				// 删除对应缓存
				redisDao.deleteCacheKey(type + "_" + delete_id);// 对应type的存活状态
				redisDao.deleteCacheKey("2_" + delete_id);// 原贴
			}

		} else {
			for (Note note : note_list) {
				Integer note_id = note.getNote_id();
				Date post_date = TimeUtil.getTimeDate(note.getPost_time());
				// 当前时间减去发帖时间，分钟数
				int minute2 = (int) ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
				int time_stranger = 15 + note.getGood_num() * 5;
				int isDie_stranger = minute2 > time_stranger ? 1 : 0;
				if (isDie_stranger == 1) {
					// 宣布一个帖子stranger方向 死亡
					// noteDao.updateNoteDie(note.getNote_id(),Const.STRANGER);
					// 8月22更新，引入批量操作
					delete_list.add(note_id);
					// 更新redis
					redisDao.deleteNote(note_id);
					// 删除对应缓存
					redisDao.deleteCacheKey("2_" + Const.STRANGER + note_id);
				} else {
					// 先要验证帖子是不是存在
					Note note_temp = noteDao.findNoteById(note_id, Const.ALL);
					if (note_temp != null)
						list_ans.add(note_temp);
				}
			}
			if (delete_list.size() > 0)
				noteDao.updateNoteDieBatch(delete_list, Const.STRANGER);
			for (Integer delete_id : delete_list) {
				// 删除对应缓存
				redisDao.deleteCacheKey(type + "_" + delete_id);// 对应type的存活状态
				redisDao.deleteCacheKey("2_" + delete_id);// 原贴
			}
		}
		return list_ans;
	}

	// 头20个放入内容，后面80个则放入note_id
	public void allocNote(Integer user_id, List<Map<String, Object>> list_original,
			List<Map<String, Object>> result_list, List<Integer> id_list, int next) {
		int count = 0;
		for (int q = next; q >= 0; q--) {
			Map<String, Object> one_note_map = list_original.get(q);
			Integer note_id = (Integer) one_note_map.get(Const.NOTE_ID);
			if (count < 20) {
				// 发送前先判断是否存活
				Date post_date = TimeUtil.getTimeDate((String) one_note_map.get(Const.POST_TIME));
				// 当前时间减去发帖时间，分钟数
				Date current_date = new Date();
				int minute1 = (int) ((current_date.getTime() - post_date.getTime()) / (1000 * 60));
				int time_fans = 60 + (int) one_note_map.get(Const.GOOD_NUM) * 5;
				int isDie_fans = minute1 > time_fans ? 1 : 0;
				if (isDie_fans == 1) {
					// 宣布一个帖子fans方向 死亡
					noteDao.updateNoteDie(note_id, Const.FANS);
					// 删除对应缓存
					redisDao.deleteCacheKey("2_" + Const.FANS + note_id);
				} else {
					count++;
					Note note = noteDao.findNoteById(note_id, Const.ALL);
					// 活着的话则添加图片数组，标签数组
					List<Map<String, Object>> image_list = noteDao.findAllImage(note_id);
					List<Map<String, Object>> tag_list = noteDao.findAllTag(note_id);
					List<Map<String, Object>> comment_list = commentService.getThreeCommentForNote(user_id, note_id);
					Integer isGood = 0;
					if (noteDao.isHaveGood(user_id, note_id) != null) {
						isGood = 1;
					}
					Map<Integer, Object> relay_list_map = new HashMap<>();
					if (note.getIsRelay() != 0) {
						// 对于转发帖，调用前面的方法
						dealWithRelay(relay_list_map, note);
					}
					result_list.add(ConvertUtil.convertNote2Map(note, image_list, tag_list, comment_list,
							relay_list_map, isGood));
				}

			} else {
				id_list.add(note_id);
				count++;
			}
			// 这样的话，前面过滤掉了一部分挂掉的,一直找活的，凑到20个为止。
			if (count == 100) {
				break;
			}
		}
	}

	@Override
	public String getSmallSectionHome(Integer user_id, String bunch) {
		// 小请求，得到的是过滤的、更新的数组
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		String[] array = bunch.split(",");
		List<Note> list = new ArrayList<>();
		for (String str : array) {
			Note note;
			if ((note = noteDao.findNoteById(Integer.valueOf(str), Const.FANS)) != null)
				list.add(note);
		}
		// 小请求更新并检查数据库
		List<Note> update_list = updateNoteDieForNote(list, Const.FANS);

		if (update_list.size() == 0) {
			map.put(Const.ERR, "0");
		} else {
			// 添加标签表和图片表
			List<List<Map<String, Object>>> image_list = new ArrayList<>();
			List<List<Map<String, Object>>> tag_list = new ArrayList<>();
			List<List<Map<String, Object>>> comment_list = new ArrayList<>();
			List<Integer> isGood_list = new ArrayList<>();
			Map<Integer, Object> relay_list_map = new HashMap<>();
			for (Note note : update_list) {
				Integer note_id = note.getNote_id();
				image_list.add(noteDao.findAllImage(note_id));
				tag_list.add(noteDao.findAllTag(note_id));
				// 24号新增，转发帖获取转发链的内容
				if (note.getIsRelay() != 0) {
					// 是转发帖的话
					dealWithRelay(relay_list_map, note);
				}
				// 3号新增,热评数组
				comment_list.add(commentService.getThreeCommentForNote(user_id, note.getNote_id()));
				// 添加是否点赞
				if (noteDao.isHaveGood(user_id, note_id) != null) {
					isGood_list.add(1);
				} else {
					isGood_list.add(0);
				}
			}

			map.put(Const.RESULT, ConvertUtil.convertNote2ListMap(update_list, image_list, tag_list, comment_list,
					relay_list_map, isGood_list));
		}

		return gson.toJson(map);
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, readOnly = false)
	@Override
	public String addOriginNote(Note note, String tag_list) {
		Map<String, Object> map = new HashMap<>();
		// 增加创建时间
		note.setPost_time(TimeUtil.getCurrentTime());
		System.out.println(note.getNote_content());
		// 添加原创贴
		// 1. 将帖子加到数据库 redis更新
		Integer note_id = noteDao.addNote(note);
		map.put(Const.NOTE_ID, note_id);
		// 2. 添加帖子标签表,标签为空就不添加
		if (tag_list != null)
			noteDao.addNoteTagBatch(note_id, tag_list);
		// 3.redis更新
		redisDao.updateAddNoteRank(note_id, 0);
		return new Gson().toJson(map);
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, readOnly = false)
	@Override
	public String addRelayNote(Note note, String tag_list) {
		// 添加转发帖
		Map<String, Object> map = new HashMap<>();
		Gson gson = new Gson();
		Integer origin_id = note.getIsRelay();

		// 增加创建时间
		note.setPost_time(TimeUtil.getCurrentTime());
		// 1. 将帖子加到数据库
		Integer note_id = noteDao.addNote(note);
		map.put(Const.NOTE_ID, note_id);
		// 2. 添加帖子标签表,标签为空就不添加
		if (tag_list != null)
			noteDao.addNoteTagBatch(note_id, tag_list);
		// 3.间接续一秒， 原帖子续一秒增加，更新数据库和redis
		addSecond(note.getUser_id(), origin_id, 0, 1);
		// 4.原贴转发数量 relay_num加一
		noteDao.addRelayNum(origin_id);
		// 5.原贴缓存
		redisDao.deleteCacheKey("0_" + origin_id);
		redisDao.deleteCacheKey("1_" + origin_id);
		redisDao.deleteCacheKey("2_" + origin_id);
		redisDao.deleteCacheKey("7_" + origin_id);
		redisDao.deleteCacheKey("9_" + origin_id);
		// 5.1删除转发列表头20条缓存
		redisDao.deleteCacheKey("5_" + origin_id + "_0");
		return gson.toJson(map);
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, readOnly = false)
	@Override
	public String addSecond(Integer user_id, Integer note_id, Integer isRelay, int type) {
		// 续一秒操作
		Map<String, Object> map = new HashMap<>();
		Gson gson = new Gson();
		int latest_num = 0;
		if (type == 0) {
			// 直接续一秒
			if (noteDao.addSecond(note_id) == 1) {
				// 1.先找到该点赞者
				User user = userDao.getUserByUserId(user_id);
				// 2.将点赞者录入帖子点赞表
				noteDao.addGoodUser(note_id, user);
				// 2.1.删除点赞表头20条的缓存
				redisDao.deleteCacheKey("6_" + note_id + "_0");
				// 4. 删除被点赞帖的缓存，以及点赞数量缓存
				redisDao.deleteCacheKey("0_" + note_id);
				redisDao.deleteCacheKey("1_" + note_id);
				redisDao.deleteCacheKey("2_" + note_id);
				redisDao.deleteCacheKey("7_" + note_id);
				redisDao.deleteCacheKey("9_" + note_id);
				latest_num = noteDao.getLatestGoodNum(note_id);
				map.put(Const.GOOD_NUM, latest_num);
				// 3.只有在帖子是原创贴的情况下，才能加入到排行榜
				if (isRelay == 0) {
					// 更新redis排名
					redisDao.updateAddNoteRank(note_id, latest_num);
				}
				// 5.如果是转发帖，则原贴也续一秒
				if (isRelay != 0) {
					noteDao.addSecond(isRelay);
					// 6.删除原贴的缓存
					redisDao.deleteCacheKey("0_" + isRelay);
					redisDao.deleteCacheKey("1_" + isRelay);
					redisDao.deleteCacheKey("2_" + isRelay);
					redisDao.deleteCacheKey("7_" + isRelay);
					redisDao.deleteCacheKey("9_" + isRelay);
					Map<String, Object> map_origin = noteDao.findNoteForAddSecond(isRelay);
					Integer more_isRelay = (Integer) map_origin.get(Const.ISRELAY);
					if (more_isRelay == 0) {
						// 如果原贴是原创贴的话，才加到redis排行榜
						int latest_num_origin = noteDao.getLatestGoodNum(isRelay);
						// 更新redis排名
						redisDao.updateAddNoteRank(isRelay, latest_num_origin);
					}
				}
			}

		} else {
			// 间接续秒
			if (noteDao.addSecond(note_id) == 1) {
				// 6.删除原贴的缓存
				redisDao.deleteCacheKey("0_" + note_id);
				redisDao.deleteCacheKey("1_" + note_id);
				redisDao.deleteCacheKey("2_" + note_id);
				redisDao.deleteCacheKey("7_" + note_id);
				redisDao.deleteCacheKey("9_" + note_id);
				latest_num = noteDao.getLatestGoodNum(note_id);
				// 只有在帖子是原创贴的情况下，才能加入或者更新到排行榜
				if (noteDao.findIsRelay(note_id) == 0) {
					// 更新redis排名
					redisDao.updateAddNoteRank(note_id, latest_num);
				}
			}
		}
		return gson.toJson(map);
	}

	@Override
	public String getSectionDiscoverRecommond(HttpServletRequest request, Integer user_id, Integer start,
			String hot_id) {
		// 发现的推荐版块
		// 8月2日修改，从ServletContext里拿数据
		Map<String, Object> map = new HashMap<>();
		Gson gson = new Gson();
		// List<Integer>list_note_id = redisDao.getNextTwentyNote(start);
		List<Object> All_note_id = null; // 排序表
		List<Map<String, Object>> hot_note_array = (List<Map<String, Object>>) request.getServletContext()
				.getAttribute(Const.HOT_NOTE_ARRAY);
		if (start == 0) {
			// 首次加载，要返回hot_id
			Map<String, Object> hot_note = hot_note_array.get(hot_note_array.size() - 1);// 最新的排序
			map.put(Const.HOT_ID, hot_note.get(Const.HOT_ID));
			All_note_id = (List<Object>) hot_note.get(Const.HOT_NOTE);
		} else {
			Boolean isFind = false;
			int hot_note_array_size = hot_note_array.size();
			// 非首次加载，后往前，查看有没有对应的排序表
			for (int i = hot_note_array_size -1; i >= 0; i--) {

				Map<String, Object> hot_note = hot_note_array.get(i);
				String hot_id_temp = (String) hot_note.get(Const.HOT_ID);
				if (hot_id.equals(hot_id_temp)) {
					// 找到对应时刻的，
					All_note_id = (List<Object>) hot_note.get(Const.HOT_NOTE);
					isFind = true;
					break;
				} 
			}
			if(isFind == false){
				//没找到的话，使用最新排序表(最后一个)
				All_note_id = (List<Object>) hot_note_array.get(hot_note_array_size-1).get(Const.HOT_NOTE);
			}
		}
		Boolean isEmpty = false;
		List<Object> list_note_id = null;
		if (start * 20 + 19 <= All_note_id.size() - 1) {
			list_note_id = All_note_id.subList(start.intValue() * 20, start.intValue() * 20 + 20);
		} 
		else if(start*20 <= All_note_id.size()-1){
			list_note_id = All_note_id.subList(start.intValue() * 20, All_note_id.size());
		}
		else {
			isEmpty = true;
			map.put(Const.ERR, "0");
		}
		
		if(isEmpty == false){
			// 再发送内容
			List<Note> list = new ArrayList<>();
			for (Object note_id : list_note_id) {
				Note note_temp;
				if ((note_temp = noteDao.findNoteById(new Integer(note_id.toString()), Const.STRANGER)) != null)
					list.add(note_temp);
			}

			List<Note> update_list = updateNoteDieForNote(list, Const.STRANGER);
			if (update_list.size() == 0) {
				map.put(Const.ERR, "0");
			} else {
				// 添加标签表和图片表
				List<List<Map<String, Object>>> image_list = new ArrayList<>();
				List<List<Map<String, Object>>> tag_list = new ArrayList<>();
				List<List<Map<String, Object>>> comment_list = new ArrayList<>();
				List<Integer> isGood_list = new ArrayList<>();
				// Map<String,Object>relay_list_map = new HashMap<>();
				for (Note note : update_list) {
					Integer note_id = note.getNote_id();
					image_list.add(noteDao.findAllImage(note_id));
					tag_list.add(noteDao.findAllTag(note_id));
					// 3号新增,热评数组
					comment_list.add(commentService.getThreeCommentForNote(user_id, note_id));

					if (noteDao.isHaveGood(user_id, note_id) != null) {
						isGood_list.add(1);
					} else {
						isGood_list.add(0);
					}
				}
				map.put(Const.RESULT, ConvertUtil.convertNote2ListMap(update_list, image_list, tag_list, comment_list, null,
						isGood_list));
			}
		}
		return gson.toJson(map);
	}

	@Override
	public String saveNoteImageUrl(List<String> note_image_list, String[] image_size_list, Integer note_id) {
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<>();
		// 更新帖子图片表
		try {
			if(note_image_list.size() == 0){
				map.put(Const.ERR, "上传图片失败，请检查你的网络");
			}else{
				noteDao.saveImageBatch(note_id, note_image_list, image_size_list);
				List<String> url_list = new ArrayList<>();
				for (int i = 0; i < note_image_list.size(); i++) {
					url_list.add(note_image_list.get(i));
				}
				map.put(Const.IMAGE_LIST, url_list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put(Const.ERR, "保存图片失败，原贴不存在");
		}
		String ans_str = gson.toJson(map);
		return ans_str;
	}

	@Override
	public String getLatestGoodNum(Integer note_id) {
		// 对一个note_id更新good_num的请求
		Map<String, Object> ans_map = new HashMap<>();
		ans_map.put(Const.GOOD_NUM, noteDao.getLatestGoodNum(note_id));
		return new Gson().toJson(ans_map);
	}

	@Override
	public String getLatestGoodNumMul(String note_id_list) {
		// 更新多个id续一秒数量的请求
		String[] note_ids = note_id_list.split(",");
		List<Integer> list = new ArrayList<>();
		for (String note_id_str : note_ids) {
			Integer note_id = Integer.valueOf(note_id_str);
			// 只是单纯的返回续一秒数量，无其他操作
			list.add(noteDao.getLatestGoodNum(note_id));
		}
		return new Gson().toJson(list);
	}

	@Override
	public String getLatestThreeNumAndSummary(Integer note_id, Integer user_id) {
		// 打开详情页，更新三大数量以及用户签名
		Map<String, Object> map = noteDao.getNoteThreeNum(note_id);
		String summary = userDao.getUserSummary(user_id);
		map.put(Const.SUMMARY, summary);
		return new Gson().toJson(map);
	}

	@Override
	public String getTwentyRelay(Integer note_id, Integer start) {
		// 打开详情页，在转发列表一次获取20条转发
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> list;
		list = noteDao.getTwentyRelay(note_id, start);
		if (list.size() == 0)
			map.put(Const.ERR, "0");
		else {
			map.put(Const.RESULT, list);
		}
		return new Gson().toJson(map);
	}

	@Override
	public String getTwentyGood(Integer note_id, Integer start) {
		// 打开详情页，在转发列表一次获取20条续一秒列表
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> list;
		list = noteDao.getTwentyGood(note_id, start);
		if (list.size() == 0)
			map.put(Const.ERR, "0");
		else {
			map.put(Const.RESULT, list);
		}
		return new Gson().toJson(map);
	}

	@Override
	public String getNoteDetail(Integer user_id, Integer note_id) {
		// 详情页的转发列表中，点击一项跳转到对应的详情页，获取详情页信息的请求
		Note note = noteDao.findNoteById(note_id, Const.ALL);
		Map<String, Object> map = new HashMap<>();
		if (note == null) {
			map.put(Const.ERR, "帖子已被删除");
		} else {
			// 先添加图片数组，标签数组
			List<Map<String, Object>> image_list = noteDao.findAllImage(note_id);
			List<Map<String, Object>> tag_list = noteDao.findAllTag(note_id);
			List<Map<String, Object>> comment_list = commentService.getThreeCommentForNote(user_id, note_id);
			Map<Integer, Object> relay_map = new HashMap<>();
			Integer isGood = 0;
			if (noteDao.isHaveGood(user_id, note_id) != null) {
				isGood = 1;
			}
			if (note.getIsRelay() != 0) {
				// 加入转发链
				dealWithRelay(relay_map, note);
			}
			map.put(Const.RESULT,
					ConvertUtil.convertNote2Map(note, image_list, tag_list, comment_list, relay_map, isGood));
		}
		return new Gson().toJson(map);
	}

	@Override
	public String deleteNote(Integer note_id) {
		// 删除帖子
		Map<String, Object> map = new HashMap<>();
		// 先保存从note_image表拿到的数据
		List<Map<String, Object>> all_images = noteDao.findAllImage(note_id);
		// 删除数据库的数据
		if (noteDao.deleteNote(note_id) == 1) {
			// 删除数据库成功
			// 删除redis排行榜的记录
			redisDao.deleteNote(note_id);
			// 剩余的redis缓存
			redisDao.deleteCacheKey("0_" + note_id);
			redisDao.deleteCacheKey("1_" + note_id);
			redisDao.deleteCacheKey("2_" + note_id);
			// 删除图片文件
			String direct_path = "/usr/java";
			File file;
			String image_url;
			if (all_images != null || all_images.size() > 0) {
				for (Map<String, Object> map2 : all_images) {
					image_url = (String) map2.get(Const.IMAGE_URL);
					file = new File(direct_path + image_url.substring(19, image_url.length()));
					if (file.exists()) {
						file.delete();
					}
				}
			}

		} else {
			map.put(Const.ERR, "原贴不存在");
		}
		return new Gson().toJson(map);
	}

	@Override
	public String topReload(Integer user_id, String bunch) {
		Map<String, Object> map = new HashMap<>();
		// 更新显示过的帖子的续一秒数量，同时加上新发布的帖子
		String[] old_notes = bunch.split(",");
		Integer last_one = Integer.valueOf(old_notes[old_notes.length - 1]);
		List<Note> latest_notes_star = noteDao.findLatestNoteStar(user_id, last_one);
		List<Note> latest_notes_me = noteDao.findLatestNoteMe(user_id, last_one);
		List<Note> latest_notes_merge = MergeUtil.mergeByIdForNote(latest_notes_star, latest_notes_me);
		List<Note> update_list = updateNoteDieForNote(latest_notes_merge, Const.FANS);
		if (update_list.size() == 0) {
			map.put(Const.ERR, "0");
		} else {
			// 添加标签表和图片表
			List<List<Map<String, Object>>> image_list = new ArrayList<>();
			List<List<Map<String, Object>>> tag_list = new ArrayList<>();
			List<List<Map<String, Object>>> comment_list = new ArrayList<>();
			List<Integer> isGood_list = new ArrayList<>();
			Map<Integer, Object> relay_list_map = new HashMap<>();
			for (Note note : update_list) {
				Integer note_id = note.getNote_id();
				image_list.add(noteDao.findAllImage(note_id));
				tag_list.add(noteDao.findAllTag(note_id));
				// 24号新增，转发帖获取转发链的内容
				if (note.getIsRelay() != 0) {
					// 是转发帖的话
					dealWithRelay(relay_list_map, note);
				}
				// 3号新增,热评数组
				comment_list.add(commentService.getThreeCommentForNote(user_id, note.getNote_id()));
				// 添加是否点赞
				if (noteDao.isHaveGood(user_id, note_id) != null) {
					isGood_list.add(1);
				} else {
					isGood_list.add(0);
				}
			}
			map.put(Const.RESULT, ConvertUtil.convertNote2ListMap(update_list, image_list, tag_list, comment_list,
					relay_list_map, isGood_list));
		}
		// 更新续一秒数量
		List<Integer> good_num_list = new ArrayList<>();
		for (String note_id_str : old_notes) {
			good_num_list.add(noteDao.getLatestGoodNum(Integer.valueOf(note_id_str)));
		}
		map.put(Const.GOOD_NUM_LIST, good_num_list);
		return new Gson().toJson(map);
	}
}
