package com.hl.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;
import com.hl.domain.Comment;
import com.hl.factory.BasicFactory;
import com.hl.service.CommentService;
import com.hl.util.Const;

/**
 * Servlet implementation class CommentServlet
 */
public class CommentServlet extends HttpServlet {
       
    /**
	 * 
	 */
	private static final long serialVersionUID = -8401782949098406354L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public CommentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		//CommentService commentService = BasicFactory.getFactory().getInstance(CommentService.class);
		ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
		CommentService commentService = (CommentService) applicationContext.getBean("commentService");
		String ans_str = null;
		switch (request.getParameter("code")) {
		case "00"://按照点赞数量  打开详情页时，获取10条热门评论列表
			ans_str = commentService.getTenCommentByGood(Integer.valueOf(request.getParameter(Const.USER_ID)),Integer.valueOf(request.getParameter(Const.NOTE_ID)));
			break;

		case "01"://发评论
			Comment comment = new Comment();
			try {
				BeanUtils.populate(comment, request.getParameterMap());
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ans_str = commentService.addComment(comment);
			break;
		
		case "02"://按照时间顺序   一次获取20条评论列表
			Map<String, Object>temp = commentService.getTwentyCommentByTime(Integer.valueOf(request.getParameter(Const.USER_ID)),
					Integer.valueOf(request.getParameter(Const.NOTE_ID)),
					Integer.valueOf(request.getParameter(Const.START)));
			ans_str = new Gson().toJson(temp);
			break;
			
		case "03"://对评论点赞
			ans_str = commentService.addCommentGood(Integer.valueOf(request.getParameter(Const.COMMENT_ID)),
					Integer.valueOf(request.getParameter(Const.USER_ID)) ,Integer.valueOf(request.getParameter(Const.NOTE_ID)));
		    break;
		    
		case "04"://删除评论
			ans_str = commentService.deleteComment(Integer.valueOf(request.getParameter(Const.NOTE_ID)), Integer.valueOf(request.getParameter(Const.COMMENT_ID)));
			break;
		
		case "05"://打开详情页，得到10条热门评论 + 普通评论
			ans_str = commentService.getFirstComment(Integer.valueOf(request.getParameter(Const.USER_ID)) ,Integer.valueOf(request.getParameter(Const.NOTE_ID)));
			break;
		
		default:
			break;
		}
		
		PrintWriter writer = response.getWriter();
		writer.write(ans_str);
		writer.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
