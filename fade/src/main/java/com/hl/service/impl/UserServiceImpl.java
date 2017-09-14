package com.hl.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.hl.dao.CommentDao;
import com.hl.dao.NoteDao;
import com.hl.dao.RedisDao;
import com.hl.dao.UserDao;
import com.hl.domain.User;
import com.hl.service.UserService;
import com.hl.util.Const;
import com.hl.util.TimeUtil;

@Service("userService")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class UserServiceImpl implements UserService {

	@Resource(name = "userDao")
	private UserDao userDao;

	@Resource(name = "redisDao")
	private RedisDao redisDao;
	
	@Resource(name = "noteDao")
	private NoteDao noteDao;
	
	@Resource(name = "commentDao")
	private CommentDao commentDao;

	@Override
	public String registerQueryTel(String telephone) {
		JSONObject jsonObject = new JSONObject();
		try {
			if (userDao.isFindUserByTel(telephone) == null) {
				jsonObject.put("ans", 0); // 没有注册
			} else {
				jsonObject.put("ans", 1); // 已经注册
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	// 普通注册方式 录入信息
	@Override
	public String registerByName(User user) {
		Map<String, Object> map = new HashMap<>();
		// 生成fade_name和注册时间
		String uuid = UUID.randomUUID().toString();
		String fade_name = "fade_" + uuid.substring(30);
		user.setFade_name(fade_name);
		user.setRegister_time(TimeUtil.getCurrentTime());
		try {
			Integer user_id = userDao.addUser(user);
			map.put(Const.USER_ID, user_id);
			map.put(Const.FADE_NAME, user.getFade_name());
			map.put(Const.REGISTER_TIME, user.getRegister_time());
		} catch (Exception e) {
			e.printStackTrace();
			map.put(Const.ERR, "注册失败，账号已存在");
		}
		return new Gson().toJson(map);
	}

	@Override
	public String loginUser(User user) {
		JSONObject jsonObject = new JSONObject();
		User user2;
		try {
			// 手机登录
			if (user.getTelephone() != null) {
				if ((user2 = (User) userDao.isFindUserByTelPwd(user.getTelephone(), user.getPassword())) == null) {
					jsonObject.put(Const.ERR, "账号不存在或者密码错误"); // 账号不存在
				} else {
					// 用户存在的话，返回昵称，fade号，手机号，头像url，性别 （需要什么后期再加）
					putUserAllJson(jsonObject, user2);
				}
			}
			// Fade账号登录
			else if (user.getFade_name() != null) {
				if ((user2 = (User) userDao.isFindUserByFadePwd(user.getFade_name(), user.getPassword())) == null) {
					jsonObject.put(Const.ERR, "账号不存在或者密码错误"); // 账号不存在
				} else {
					// 用户存在的话，返回昵称，手机号，头像url，性别 （需要什么后期再加）
					putUserAllJson(jsonObject, user2);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	@Override
	public String saveHeadImageUrl(String head_image_url, Integer user_id) {
		int save_ans = 0;
		Map<String, Object> map = new HashMap<>();
		String old_image_url = userDao.getHeadImageUrl(user_id);
		save_ans = userDao.saveImageUrlByUserId(head_image_url, user_id);
		if (save_ans == 0) {
			map.put(Const.ERR, "保存头像失败，账号不存在"); // 保存头像失败
		} else {
			map.put(Const.HEAD_IMAGE_URL, head_image_url);
			//保存头像成功，更新帖子头像，帖子点赞表，评论表，删除图片文件
			if(old_image_url != null){
				//说明是修改头像
				//1. 删除图片文件
				String direct_path = "/usr/java";
				File file;
				file = new File(direct_path + old_image_url.substring(19, old_image_url.length()));
				if (file.exists()) {
					file.delete();
				}
				//2.更新帖子头像，帖子点赞表，评论表
				noteDao.updateNoteHead(head_image_url,user_id);
				noteDao.updateNoteGoodHead(head_image_url,user_id);
				commentDao.updateCommentHead(head_image_url,user_id);
				//3.删除缓存
				//3.1帖子缓存，找到全部帖子的id
				List<Integer>my_note_ids = noteDao.findAllMyNoteId(user_id);
				for(Integer note_id : my_note_ids){
					redisDao.deleteCacheKey("0_"+note_id);
					redisDao.deleteCacheKey("1_"+note_id);
					redisDao.deleteCacheKey("2_"+note_id);
				}
				//3.2评论缓存，找到全部评论所属的note_id
				List<Integer>comment_noteIds = commentDao.findAllMyCommentNoteId(user_id);
				for(Integer comment_note_id : comment_noteIds){
					redisDao.deleteCacheKey("c1_" + comment_note_id);
					redisDao.deleteCacheKey("c4_" + comment_note_id);
				}
				
			}
			
		}
		return new Gson().toJson(map).toString();
	}

	@Override
	public String getHeadImageUrl(User user_arg) {
		String telephone = user_arg.getTelephone();
		String wechat_id = user_arg.getWechat_id();
		String weibo_id = user_arg.getWeibo_id();
		String qq_id = user_arg.getQq_id();
		String fade_name = user_arg.getFade_name();
		// TODO Auto-generated method stub
		User user = null;
		JSONObject jsonObject = new JSONObject();
		if (telephone != null) {
			user = (User) userDao.isFindUserByTel(telephone);
		} else if (wechat_id != null) {
			user = (User) userDao.isFindUserByOpenId(wechat_id, "wechat_id");
		} else if (weibo_id != null) {
			user = (User) userDao.isFindUserByOpenId(weibo_id, "weibo_id");
		} else if (qq_id != null) {
			user = (User) userDao.isFindUserByOpenId(qq_id, "qq_id");
		} else if (fade_name != null) {
			user = (User) userDao.isFindUserByFadeName(fade_name);
		}
		try {
			if (user == null) {
				jsonObject.put(Const.ERR, "获取头像失败，账号可能不存在");
			} else {
				if (user.getHead_image_url() != null && !(user.getHead_image_url().equals(""))) {
					jsonObject.put(Const.HEAD_IMAGE_URL, user.getHead_image_url());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	// 7月20日重组
	@Override
	public String updateByWechatId(String wechat_id) {
		JSONObject jsonObject = new JSONObject();
		User user2;
		try {
			if ((user2 = (User) userDao.isFindUserByOpenId(wechat_id, Const.WECHAT_ID)) != null) {
				// 用户存在的话，返回昵称，手机号，头像url，性别 （需要什么后期再加）
				putUserAllJson(jsonObject, user2);
			} else {
				jsonObject.put(Const.ERR, "用户不存在");// 该用户不存在
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	@Override
	public String registerWechatId(String js_code, User user) {
		// TODO Auto-generated method stub
		// 如果本地没有检测到，则发送小程序code、以及一些用户信息到服务器，服务器请求得到openid，返回给小程序
		System.out.println("js_code:  " + js_code);
		System.out.println("获取微信id");
		JSONObject jsonObject = new JSONObject();
		try {
			URL url = new URL("https://api.weixin.qq.com/sns/jscode2session?appid=" + Const.APP_ID + "&secret="
					+ Const.AppSecret + "&js_code=" + js_code + "&grant_type=authorization_code");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 得到输入流
			InputStream is = connection.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while (-1 != (len = is.read(buffer))) {
				baos.write(buffer, 0, len);
				baos.flush();
			}
			String str = baos.toString("utf-8");
			// jsonObject.put("向腾讯请求的结果", str);

			JSONObject temp = new JSONObject(str);
			System.out.println(temp.toString());
			String wechat_id = temp.getString("openid");
			user.setWechat_id(wechat_id);
			// 生成fade_name 注册时间
			String uuid = UUID.randomUUID().toString();
			String fade_name = "fade_" + uuid.substring(30);
			user.setFade_name(fade_name);
			user.setRegister_time(TimeUtil.getCurrentTime());
			if (userDao.isFindUserByOpenId(wechat_id, Const.WECHAT_ID) == null) {
				try {
					userDao.addUser(user);
					jsonObject.put(Const.ANS, 1);// 成功新建用户
					putUserAllJson(jsonObject, user);
				} catch (Exception e) {
					e.printStackTrace();
					jsonObject.put(Const.ERR, "新建用户失败");
				}
			} else {
				jsonObject.put("ans", 0);// 该openid已经被注册
				putUserAllJson(jsonObject, user);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	@Override
	public String editNickname(Integer user_id, String nickname) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editNickname(user_id, nickname) == 0) {
			map.put(Const.ERR, "用户不存在");
		}else{
			//更新帖子表、评论表、帖子点赞表
			noteDao.updateNoteNickname(nickname, user_id);
			noteDao.updateNoteGoodNickname(nickname,user_id);
			commentDao.updateCommentNickname(nickname,user_id);
			//删除缓存
			//3.1帖子缓存，找到全部帖子的id
			List<Integer>my_note_ids = noteDao.findAllMyNoteId(user_id);
			for(Integer note_id : my_note_ids){
				redisDao.deleteCacheKey("0_"+note_id);
				redisDao.deleteCacheKey("1_"+note_id);
				redisDao.deleteCacheKey("2_"+note_id);
			}
			//3.2评论缓存，找到全部评论所属的note_id
			List<Integer>comment_noteIds = commentDao.findAllMyCommentNoteId(user_id);
			for(Integer comment_note_id : comment_noteIds){
				redisDao.deleteCacheKey("c1_" + comment_note_id);
				redisDao.deleteCacheKey("c4_" + comment_note_id);
			}
			
		}
		return new Gson().toJson(map);
	}

	@Override
	public String editSummary(Integer user_id, String summary) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editSummary(user_id, summary) == 0) {
			map.put(Const.ERR, "用户不存在");
		}else{
			//更新点赞表
			noteDao.updateNoteGoodSummary(summary,user_id);
		}
		return new Gson().toJson(map);
	}

	@Override
	public String editSex(Integer user_id, String sex) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editSex(user_id, sex) == 0) {
			map.put(Const.ERR, "用户不存在");
		}
		return new Gson().toJson(map);
	}

	@Override
	public String editArea(Integer user_id, String area) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editArea(user_id, area) == 0) {
			map.put(Const.ERR, "用户不存在");
		}
		return new Gson().toJson(map);
	}

	@Override
	public String editSchool(Integer user_id, String school) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editSchool(user_id, school) == 0) {
			map.put(Const.ERR, "用户不存在");
		}
		return new Gson().toJson(map);
	}

	@Override
	public String editWallpaperUrl(Integer user_id, String wallpaper_url) {
		Map<String, Object> map = new HashMap<>();
		if (userDao.editWallpaperUrl(user_id, wallpaper_url) == 0) {
			map.put(Const.ERR, "用户不存在");
		} else {
			map.put(Const.WALLPAPER_URL, wallpaper_url);
		}
		return new Gson().toJson(map);
	}
	
	private void putUserAllJson(JSONObject jsonObject, User user) {
		try {
			jsonObject.put(Const.NICKNAME, user.getNickname());
			jsonObject.put(Const.HEAD_IMAGE_URL, user.getHead_image_url());
			jsonObject.put(Const.FADE_NAME, user.getFade_name());
			jsonObject.put(Const.TELEPHONE, user.getTelephone());
			jsonObject.put(Const.SEX, user.getSex());
			jsonObject.put(Const.USER_ID, user.getUser_id());
			jsonObject.put(Const.SUMMARY, user.getSummary());
			jsonObject.put(Const.WECHAT_ID, user.getWechat_id());
			jsonObject.put(Const.WEIBO_ID, user.getWeibo_id());
			jsonObject.put(Const.QQ_ID, user.getQq_id());

			jsonObject.put(Const.CONCERN_NUM, user.getConcern_num());
			jsonObject.put(Const.FANS_NUM, user.getFans_num());
			jsonObject.put(Const.AREA, user.getAera());
			jsonObject.put(Const.WALLPAPER_URL, user.getWallpapaer_url());
			jsonObject.put(Const.REGISTER_TIME, user.getRegister_time());
			jsonObject.put(Const.SCHOOL, user.getSchool());
			jsonObject.put(Const.FADE_NUM, user.getFade_num());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
