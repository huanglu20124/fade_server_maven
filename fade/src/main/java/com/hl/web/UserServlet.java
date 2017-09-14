package com.hl.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hl.domain.User;
import com.hl.service.UserService;
import com.hl.util.Const;

/**
 * Servlet implementation class UserServlet
 */
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public UserServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		User user = new User();
		PrintWriter writer = response.getWriter();
		String answer = "";
		//UserService userService = BasicFactory.getFactory().getInstance(UserService.class);
		ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		UserService userService = (UserService) applicationContext.getBean("userService");
		
		try {
			switch (request.getParameter("code")) {
			case "01":
				//如果小程序本地检测到openid，更新这个openid的数据发回给小程序
				answer = userService.updateByWechatId(request.getParameter(Const.WECHAT_ID));
				break;
				
			case "02":
				//如果本地没有检测到，则发送小程序code、以及一些用户信息到服务器，服务器请求得到openid，返回给小程序
				BeanUtils.populate(user, request.getParameterMap());
				answer = userService.registerWechatId(request.getParameter("js_code"), user);
				break;
			
			case "03":
				//查询手机号是否已被注册
				answer = userService.registerQueryTel(request.getParameter(Const.TELEPHONE));
				break;
				
			case "04":
				//安卓端昵称密码的注册方式
				BeanUtils.populate(user, request.getParameterMap());
				answer = userService.registerByName(user);
				break;
				
			case "05":
				//安卓端登录登录
				BeanUtils.populate(user, request.getParameterMap());
				answer = userService.loginUser(user);
				break;
				
			case "06":
				//获取用户头像
				BeanUtils.populate(user, request.getParameterMap());
				answer = userService.getHeadImageUrl(user);
			    break;
			
			case "07":
				//修改昵称
				answer = userService.editNickname(Integer.valueOf(request.getParameter(Const.USER_ID)),
						request.getParameter(Const.NICKNAME));
				break;
			
			case "08":
				//修改用户签名
				answer = userService.editSummary(Integer.valueOf(request.getParameter(Const.USER_ID)),
						request.getParameter(Const.SUMMARY));
				break;
			
		    case "09":
		    	//修改用户性别
				answer = userService.editSex(Integer.valueOf(request.getParameter(Const.USER_ID)),
						request.getParameter(Const.SEX));
		    	break;
			
		    case "10":
		    	//修改用户地区
		    	answer = userService.editArea(Integer.valueOf(request.getParameter(Const.USER_ID)),
		    			request.getParameter(Const.AREA));
		        break;
		    case "11":
		    	//修改用户学校
		    	answer = userService.editSchool(Integer.valueOf(request.getParameter(Const.USER_ID)),
		    			request.getParameter(Const.SCHOOL));
		        break;
		    
		    
			default:
				break;
		}
		writer.write(answer);
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			writer.flush();
			writer.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
