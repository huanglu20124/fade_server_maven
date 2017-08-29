package com.hl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
	private IOUtil(){
		
	}
	
	public static void inToOut(InputStream in, OutputStream out) throws IOException{
		byte[]bytes = new byte[1024];
		int i = 0;
		while((i =in.read(bytes)) != -1){
			out.write(bytes, 0, i);
		}
	}
	
	public static void close(InputStream in, OutputStream out){
		if(in != null){
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				in = null;
			}
		}
		
		if(out != null){
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				out = null;
			}
		}
	}
}
