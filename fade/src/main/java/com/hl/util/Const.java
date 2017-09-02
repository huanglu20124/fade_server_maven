package com.hl.util;

public class Const {
	/**
	 * 图片上传路径
	 */
	public static final String UPLOAD_URL="http://192.168.137.1:8080/Fade/UploadServlet";
	
	/**
	 * 图片下载路径
	 */
	public static final String DOWNLOAD_URL="http://192.168.137.1:8080/Fade/headImage/";
	
	//User字段常量
	public static final String USER_ID      = "user_id";
	public static final String TELEPHONE    = "telephone";
	public static final String FADE_NAME    = "fade_name";
	public static final String NICKNAME     = "nickname";
	public static final String PASSWORD     = "password";
	public static final String SEX          = "sex";
	public static final String REGISTER_TIME= "register_time";
	public static final String SUMMARY      = "summary";
	public static final String HEAD_IMAGE_URL  = "head_image_url";
	
	//Note字段常量（有与user部分重叠的）
	public static final String NOTE_ID         = "note_id";
	public static final String NOTE_CONTENT    = "note_content";
	public static final String POST_TIME       = "post_time";
	public static final String ISDIE_FANS      = "isDie_fans";
	public static final String ISDIE_STRANGER  = "isDie_stranger";
	public static final String COMMENT_NUM     = "comment_num";
	public static final String RELAY_NUM       = "relay_num";
	public static final String GOOD_NUM        = "good_num";
	public static final String ISRELAY         = "isRelay";
	public static final String POST_AREA	   = "post_area";
	public static final String ISGOOD          = "isGood";
	//判断帖子的
	public static final int FANS     = 0; 
	public static final int STRANGER = 1;
	public static final int ALL      = 2;
	
	//7月22日新增
	public static final String CONCERN_NUM  = "concern_num";
	public static final String FANS_NUM     = "fans_num";
	public static final String AREA         = "area";
	public static final String WALLPAPER_URL= "wallpaper_url";
	
	public static final String WECHAT_ID    = "wechat_id";
	public static final String WEIBO_ID     = "weibo_id";
	public static final String QQ_ID        = "qq_id";
	
	//微信小程序相关：
	public static final String APP_ID       = "wx3e4e1ddf8d7f6773";
	public static final String AppSecret    = "0918c2522745b1f0d0066021cb124374";
	public static final String ANS          = "ans";
	
	//redis热门帖子表，上限1000条
	public static final String HOT_NOTE     = "hot_note";
	
	//帖子的标签表和图片表以及热评数组
	public static final String IMAGE_LIST   = "image_list";
	public static final String TAG_LIST     = "tag_list";
	public static final String COMMENT_LIST = "comment_list";
	
	//大请求的id_list
	public static final String ID_LIST      = "id_list";
	
	//用于获取列表信息，十分重要的检索起点
	public static final String START        = "start";
	
	//返回的列表信息内容的键
	public static final String RESULT       = "result";
	
	//转发帖才有的转发链
	public static final String RELAY_LIST   = "relay_list";	
	
	//点赞有关
	public static final String GOOD_ID      = "good_id";	
	public static final String GOOD_TIME    = "good_time";	
	
	//8月1号加入，评论表
	public static final String COMMENT_ID        ="comment_id";
	public static final String TO_COMMENT_ID      ="to_comment_id";
	public static final String COMMENT_TIME       ="comment_time";
	public static final String COMMENT_CONTENT    ="comment_content";
	public static final String COMMENT_GOOD_NUM   ="comment_good_num";
	public static final String START_NUM          ="start_num";
	public static final String ORIGIN_COMMENT     ="origin_comment";
	public static final String COMMENT_ISGOOD     = "comment_isGood";
	
	//评论点赞表
	public static final String COMMENT_GOOD_ID    ="comment_good_id";
	
	//8月2号更新，热门帖子的排序表的数组，大小为3
	public static final String HOT_NOTE_ARRAY    ="hot_note_array";
	public static final String HOT_ID            ="hot_id";
	
	//6号更新，图片的尺寸
	public static final String IMAGE_URL         = "image_url";
	public static final String IMAGE_SIZE        ="image_size";
	
	//8月23号重组
	public static final String ERR = "err";
	
}
