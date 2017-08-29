package com.hl.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hl.service.NoteService;
import com.hl.service.UserService;
import com.hl.util.Const;
import com.hl.util.IOUtil;

/**
 * Servlet implementation class UpLoadServlet
 */
public class UpLoadServlet extends HttpServlet {
       
    /**
	 * 
	 */
	private static final long serialVersionUID = 1516645242589147984L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public UpLoadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		
		//创建工厂
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(100*1024); //设定内存缓冲区的大小
		File file2 = new File("WEB-INF/temp");
		if(!file2.exists()) file2.mkdirs();
		factory.setRepository(file2);//设置临时文件夹（文件大小大于内存缓冲区）
		
		//创建核心上传类
		FileUpload fileUpload = new FileUpload(factory);
		//--检查是否是正确的文件上传表单
		if(!fileUpload.isMultipartContent(request)){
			throw new RuntimeException("请用正确的表单进行上传!");
		}
		fileUpload.setHeaderEncoding("utf-8");//解决上传文件名的乱码
		
		
		//本地测试用
		String ip_here = "http://localhost:8080/fade";
		String ip_yun = "https://sysufade.cn/fade";
		
		
		//解析request
		try {
			String imageType = ""; //为head或note
			
			Integer user_id = 0;
			Integer note_id = 0;
			//保存帖子图片的数组
			List<String>note_image_list = new ArrayList<>();
			//图片的尺寸
			String image_size_list_str = null;
			
			List<FileItem>list = fileUpload.parseRequest(request);
			for(FileItem fileItem : list){
				if(fileItem.isFormField()){
					String name = fileItem.getFieldName();
					String value = fileItem.getString("utf-8");
					System.out.println("name: " + name + " value: " + value);
					if(name.equals("imageType")) imageType = value;
					if(name.equals(Const.USER_ID)) user_id = Integer.valueOf(value);
					if(name.equals(Const.NOTE_ID)) note_id = Integer.valueOf(value);
					if(name.endsWith("image_size_list")) image_size_list_str = value;
					
				}else{
					String imagePath = "";
					String path2 = "";
					String fileName_ = fileItem.getName();
					System.out.println("fileItem.getName()=="+fileName_);
					int i = fileName_.lastIndexOf("\\");
					String fileName = fileName_.substring(i+1);
					String imageName = "";
					String uuid = UUID.randomUUID().toString(); //生成随机数
					
					
					if(imageType.equals("head")){
						//到时候部署时换这个地址
						imagePath = request.getSession().getServletContext().getRealPath("image/head");
						//imagePath = "E:/apache-tomcat-9.0.0.M22/wtpwebapps/Image/head"; //存在Image工程里
						path2 = "/image/head/";
						imageName = uuid.substring(30)+"_"+fileName;
					}else{
						imagePath = request.getSession().getServletContext().getRealPath("image/note");
						//imagePath = "E:/apache-tomcat-9.0.0.M22/wtpwebapps/Image/note";
						path2 = "/image/note/";
						imageName = uuid.substring(30)+"_"+fileName;
						note_image_list.add(ip_yun +path2+imageName);
					}
					File file = new File(imagePath);
				     if(!file.exists()){
				        file.mkdirs();
				      }
					
					//得到流，并写入
					InputStream inputStream = fileItem.getInputStream();
					OutputStream outputStream = new FileOutputStream(new File(imagePath,imageName));
					
					IOUtil.inToOut(inputStream, outputStream);
					IOUtil.close(inputStream, outputStream);
					
					//删除临时文件
					fileItem.delete();
					//UserService service = BasicFactory.getFactory().getInstance(UserService.class);
					ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
					UserService userService = (UserService) applicationContext.getBean("userService");
					//保存用户头像
					if(imageType.equals("head")){
						String str_response = userService.saveHeadImageUrl(ip_yun +path2 + imageName,user_id);
						response.getWriter().write(str_response);
					}				
				}
			}
			
			//保存帖子图片
			if(imageType.equals("note")){
				System.out.println(note_image_list);
				//得到图片尺寸数组
				String[]image_size_list = image_size_list_str.split(",");
				//NoteService noteService = BasicFactory.getFactory().getInstance(NoteService.class);
				ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
				NoteService noteService= (NoteService) applicationContext.getBean("noteService");
				String str_response = noteService.saveNoteImageUrl(note_image_list,image_size_list,note_id);
				response.getWriter().write(str_response);
				response.getWriter().flush();
			}	
			
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
