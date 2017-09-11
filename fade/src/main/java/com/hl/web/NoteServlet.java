package com.hl.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.net.SyslogAppender;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hl.domain.Note;
import com.hl.service.NoteService;
import com.hl.util.Const;

/**
 * Servlet implementation class NoteServlet
 */
public class NoteServlet extends HttpServlet {
     
    /**
	 * 
	 */
	private static final long serialVersionUID = -294665136993090626L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public NoteServlet() {
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
		String ans_str = "";
		//NoteService noteService = BasicFactory.getFactory().getInstance(NoteService.class);
		//ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		NoteService noteService = (NoteService) applicationContext.getBean("noteService");
		switch (request.getParameter("code")) {
		case "00": //首页大请求
			ans_str = noteService.getBigSectionHome(Integer.valueOf(request.getParameter(Const.USER_ID)),Integer.valueOf(request.getParameter(Const.START)));
			break;
			
		case "01": //首页小请求
			ans_str = noteService.getSmallSectionHome(Integer.valueOf(request.getParameter(Const.USER_ID)),request.getParameter("bunch"));
			break;
			
		case "05": //发现的推荐版块请求
			ans_str = noteService.getSectionDiscoverRecommond(request,Integer.valueOf(request.getParameter(Const.USER_ID)),Integer.valueOf(request.getParameter(Const.START)),
					request.getParameter(Const.HOT_ID));
		   break;
			
		case "03"://发原创贴
			Note note = new Note();
			try {
				BeanUtils.populate(note, request.getParameterMap());
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ans_str = noteService.addOriginNote(note,request.getParameter(Const.TAG_LIST));
			break;
		
		case "04"://发转发帖
			Note note2 = new Note();
			try {
				BeanUtils.populate(note2, request.getParameterMap());
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ans_str = noteService.addRelayNote(note2,request.getParameter(Const.TAG_LIST));
			break;
		
		case "07"://续一秒
			ans_str = noteService.addSecond(Integer.valueOf(request.getParameter(Const.USER_ID)),Integer.valueOf(request.getParameter(Const.NOTE_ID)),
					Integer.valueOf(request.getParameter(Const.ISRELAY)),0);	
			break;
			
		case "08"://更新单个帖续一秒数量的请求
			ans_str = noteService.getLatestGoodNum(Integer.valueOf(request.getParameter(Const.NOTE_ID)));
			break;
			
		case "06"://更新多个帖续一秒数量的请求
			ans_str = noteService.getLatestGoodNumMul(request.getParameter(Const.NOTE_ID));
			break;
			
		case "09"://打开详情页，更新三大数量以及得到用户签名
			ans_str = noteService.getLatestThreeNumAndSummary(Integer.valueOf(request.getParameter(Const.NOTE_ID))
					,Integer.valueOf(request.getParameter(Const.USER_ID)));
			break;
			
		case "10"://打开详情页，在转发列表一次获取20条转发
			ans_str = noteService.getTwentyRelay(Integer.valueOf(request.getParameter(Const.NOTE_ID)),Integer.valueOf(request.getParameter(Const.START)));
		   break;
		
		case "11"://打开详情页，在转发列表一次获取20条续一秒列表
			ans_str = noteService.getTwentyGood(Integer.valueOf(request.getParameter(Const.NOTE_ID)),Integer.valueOf(request.getParameter(Const.START)));
		   break;
		
		case "12"://详情页的转发列表中，点击一项跳转到对应的详情页，获取详情页信息的请求,即是获取一个完整帖子信息的请求
			ans_str = noteService.getNoteDetail(Integer.valueOf(request.getParameter(Const.USER_ID)),Integer.valueOf(request.getParameter(Const.NOTE_ID)));
			break;
			
		case "13"://删除帖子的请求
			ans_str = noteService.deleteNote(Integer.valueOf(request.getParameter(Const.NOTE_ID)));
			break;
		
		case "14"://顶部下拉刷新请求
			ans_str = noteService.topReload(Integer.valueOf(request.getParameter(Const.USER_ID)),
					request.getParameter("bunch"));
			break;
		default:
			break;
		}
		
		response.getWriter().write(ans_str);
		response.getWriter().flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
