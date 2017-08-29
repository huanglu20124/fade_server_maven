package com.hl.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DaoUtil {
	
	private static ComboPooledDataSource dataSource = new ComboPooledDataSource();

    private DaoUtil(){
    	
    }
    
	public static ComboPooledDataSource getDataSource() {
		return dataSource;
	}
	
	public static Connection getConnection(){
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	//释放连接回连接池
    public static void close(Connection conn,PreparedStatement pst,ResultSet rs){  
           if(rs!=null){  
               try {  
                   rs.close();  
               } catch (SQLException e) {  
            	   e.printStackTrace();
               }  
           }  
           if(pst!=null){  
               try {  
                   pst.close();  
               } catch (SQLException e) {  
            	   e.printStackTrace();    
               }  
           }  
     
           if(conn!=null){  
               try {  
                   conn.close();  
               } catch (SQLException e) {  
            	   e.printStackTrace();

               }  
           }  
       }  

	
	
}
