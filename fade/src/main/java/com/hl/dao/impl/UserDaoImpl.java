package com.hl.dao.impl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hl.dao.UserDao;
import com.hl.domain.User;
import com.hl.util.Const;
import com.mysql.jdbc.Statement;

@Repository("userDao")
public class UserDaoImpl extends JdbcDaoSupport implements UserDao{
	
	//查找手机号是否已被注册
	@Override
	public Object isFindUserByTel(String telephone) {
		// TODO Auto-generated method stub
		String sql = "select*from user where telephone=? ";
		try {
			return this.getJdbcTemplate().queryForObject(sql, new UserRowMapper(),telephone);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	//通过昵称查找用户
	@Override
	public Object isFindUserByNick(String nickname) {
		// TODO Auto-generated method stub
		String sql = "select*from user where nickname = ?";
		try {
			return this.getJdbcTemplate().queryForObject(sql, new UserRowMapper(),nickname);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}	
	}

	//增加新用户
	@Override
	public Integer addUser(final User user) {
		final String sql = "insert into user values(null,?,?,?,?,?,?,?,?,?,?,?,0,0,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement psm = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setString(1, user.getTelephone()); psm.setString(2, user.getFade_name());
				psm.setString(3, user.getNickname());  psm.setString(4, user.getPassword());
				psm.setString(5, user.getSex());       psm.setString(6, user.getHead_image_url());
				psm.setString(7, user.getRegister_time());psm.setString(8, user.getSummary());
				psm.setString(9, user.getWechat_id()); psm.setString(10, user.getWeibo_id());
				psm.setString(11, user.getQq_id());    psm.setString(12, user.getAera());
				psm.setString(13, user.getWallpapaer_url());
				psm.setString(14, user.getSchool());
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}


	//通过账号密码登录，手机号
	@Override
	public Object isFindUserByTelPwd(String telephone,String password) {
		// TODO Auto-generated method stub
		String sql = "select*from user where telephone = ? and password = ?";
		try {
			return this.getJdbcTemplate().queryForObject(sql, new UserRowMapper(),telephone,password);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	//通过账号密码登录，Fade号
	@Override
   public Object isFindUserByFadePwd(String fade_name,String password) {
		// TODO Auto-generated method stub
		String sql = "select*from user where fade_name = ? and password = ?";
		try {
			return this.getJdbcTemplate().queryForObject(sql, new UserRowMapper(),fade_name,password);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}	
	}
	
	//通过手机保持头像url
	@Override
	public int saveImageUrlByTel(String imageUrl, String telephone) {
		// TODO Auto-generated method stub
		String sql = "update user set head_image_url=? where telephone =?";
		return this.getJdbcTemplate().update(sql,imageUrl,telephone);
	}
	//通过OpenId更新头像url
	@Override
	public int saveImageUrlByOpenId(String imageUrl, String openId, String type) {
		// TODO Auto-generated method stub
		String sql = null;
		if(type.equals("wechat_id")) 
		sql = "update user set head_image_url=? where wechat_id =?";
		if(type.equals("weibo_id")) 
		sql = "update user set head_image_url=? where weibo_id =?";
		if(type.equals("qq_id")) 
		sql = "update user set head_image_url=? where qq_id =?";
		return this.getJdbcTemplate().update(sql,imageUrl,openId);
	}
	

	//查找openid是否被注册了
	@Override
	public Object isFindUserByOpenId(String opendId, String type) {
		// TODO Auto-generated method stub
		String sql = null;
		if(type.equals("wechat_id")){
			sql = "select*from user where wechat_id = ? ";
		}
		else if(type.equals("weibo_id")){
			sql = "select*from user where weibo_id = ? ";
		}
		else if(type.equals("qq_id")){
			sql = "select*from user where qq_id = ? ";
		}
		try {
			return this.getJdbcTemplate().queryForObject(sql, new UserRowMapper(),opendId);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
	}

	//7月21日新增
	@Override
	public Object isFindUserByFadeName(String fade_name) {
		// TODO Auto-generated method stub
		String sql = "select*from user where fade_name = ? ";
		try {
			return getJdbcTemplate().queryForObject(sql, new UserRowMapper(),fade_name);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}	
	}
	
	//通过user_id存储头像url
	@Override
	public int saveImageUrlByUserId(String imageUrl, Integer user_id) {
		// TODO Auto-generated method stub
		String sql = "update user set head_image_url=? where user_id =?";
		return getJdbcTemplate().update(sql,imageUrl,user_id);
	}

	@Override
	public String getUserSummary(Integer user_id) {
		//得到用户的个性签名
		String sql = "select summary from user where user_id=?";
		return getJdbcTemplate().queryForObject(sql, String.class,user_id);
	}

	@Override
	public User getUserByUserId(Integer user_id) {
		//通过id找到用户
		String sql = "select*from user where user_id=?";
		return getJdbcTemplate().queryForObject(sql, new UserRowMapper(),user_id);
	}
	
	@Override
	public List<User> findStarUser(Integer user_id) {
		//找到关注用户  内连接查询
		String sql = "select*from relation, user where relation.user_star = user.user_id and relation.user_fans = ?";
		return getJdbcTemplate().query(sql, new UserRowMapper(),user_id);			
	}

	class UserRowMapper implements RowMapper<User>{

		@Override
		public User mapRow(ResultSet resultSet, int arg1) throws SQLException {
			User user = new User();
			user.setUser_id(resultSet.getInt(Const.USER_ID));
			user.setNickname(resultSet.getString(Const.NICKNAME));
			user.setTelephone(resultSet.getString(Const.TELEPHONE));
			user.setPassword(resultSet.getString(Const.PASSWORD));
			user.setFade_name(resultSet.getString(Const.FADE_NAME));
			user.setSex(resultSet.getString(Const.SEX));
			//邮箱暂未设置
			user.setHead_image_url(resultSet.getString(Const.HEAD_IMAGE_URL));
			user.setRegister_time(resultSet.getString(Const.REGISTER_TIME));
			user.setSummary(resultSet.getString(Const.SUMMARY));
			user.setWechat_id(resultSet.getString(Const.WECHAT_ID));
			user.setWeibo_id(resultSet.getString(Const.WEIBO_ID));
			user.setQq_id(resultSet.getString(Const.QQ_ID));
			user.setConcern_num(resultSet.getInt(Const.CONCERN_NUM));
			user.setFans_num(resultSet.getInt(Const.FANS_NUM));
			user.setAera(resultSet.getString(Const.AREA));
			user.setWallpapaer_url(resultSet.getString(Const.WALLPAPER_URL));
			user.setSchool(resultSet.getString(Const.SCHOOL));
			return user;
		}
		
	}

	@Override
	public int editWallpaperUrl(Integer user_id, String wallpaper_url) {
		String sql = "update user set wallpaper_url = ? where user_id=?";
		return getJdbcTemplate().update(sql,wallpaper_url,user_id);
	}

	@Override
	public int editNickname(Integer user_id, String nickname) {
		String sql = "update user set nickname = ? where user_id=?";
		return getJdbcTemplate().update(sql,nickname,user_id);
	}

	@Override
	public int editSummary(Integer user_id, String summary) {
		String sql = "update user set summary = ? where user_id=?";
		return getJdbcTemplate().update(sql,summary,user_id);
	}

	@Override
	public int editSex(Integer user_id, String sex) {
		String sql = "update user set sex = ? where user_id=?";
		return getJdbcTemplate().update(sql,sex,user_id);
	}

	@Override
	public int editArea(Integer user_id, String area) {
		String sql = "update user set area = ? where user_id=?";
		return getJdbcTemplate().update(sql,area,user_id);
	}

	@Override
	public int editSchool(Integer user_id, String school) {
		String sql = "update user set school = ? where user_id=?";
		return getJdbcTemplate().update(sql,school,user_id);
	}

	@Override
	public String getHeadImageUrl(Integer user_id) {
		String sql = "select head_image_url from user where user_id=?";
		return getJdbcTemplate().queryForObject(sql,String.class,user_id);
	}

}
