package com.hl.test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hl.domain.User;

public class GsonTest {
	static Gson gson = new Gson();
	
	public static void main(String[] args) {
//		Map<String, String>map = new HashMap<>();
//		map.put("1", "huang");
//		map.put("2", "lusdf");
//		String str = gson.toJson(map);
//		System.out.println(str);
//		
//		Map<String, String>map2 = (Map<String, String>) jsonToMap(str);
//		System.out.println(map2.get("2"));

	
//		User user = new User();
//		user.setNickname("黄路");
//		String str = gson.toJson(user);
//		User user2 = gson.fromJson(str, User.class);
//		System.out.println(user2.getNickname());
		
		List<String>list = new ArrayList<>();
		list.add("img1");
		list.add("img2");
		list.add("img3");
		Map<String,Object>map1 = new HashMap<>();
		map1.put("1", "帖子数据1");
		map1.put("2", "帖子数据2");
		map1.put("帖子图片", list);
		
		List<Map<String, Object>>list2 = new ArrayList<>();
		list2.add(map1);
		Gson gson = new Gson();
		String ans_str = gson.toJson(list2);
		System.out.println(ans_str);
		
		List<Map<String, Object>> list3 = gson.fromJson(ans_str,
				new TypeToken<List<Map<String, Object>>>(){}.getType());
		
		List<String>list4 = (List<String>) list3.get(0).get("帖子图片");
		for(int i = 0 ; i < list4.size(); i++){
			System.out.println(list4.get(i));
		}
		
		
		
	}
	
    public static Map<?,?> jsonToMap(String jsonStr){
    	Gson gson = new Gson();
        Map<?,?> objMap=null;
        if(gson!=null){
            Type type=new com.google.gson.reflect.TypeToken<Map<?,?>>(){}.getType();
            objMap=gson.fromJson(jsonStr, type);
        }
        return objMap;
    }
    
    public static Object  getJsonValue(String jsonStr,String key){
        Object rulsObj=null;
        Map<?,?> rulsMap=jsonToMap(jsonStr);
        if(rulsMap!=null&&rulsMap.size()>0){
            rulsObj=rulsMap.get(key);
        }
        return rulsObj;
    }
    
    /**
     * 将json转换成bean对象
     * @param jsonStr
     * @return
     */
    public static Object  jsonToBean(String jsonStr,Class<?> cl){

        Object obj=null;
        if(gson!=null){
            obj=gson.fromJson(jsonStr, cl);
        }
        return obj;
    }
    

}
