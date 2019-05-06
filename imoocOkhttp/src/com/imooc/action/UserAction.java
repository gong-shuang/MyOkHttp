package com.imooc.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

public class UserAction extends ActionSupport {
	
	private String name ;
	private String password ;
	
	public File mPhoto; // 这个名字和Android中的body中的名字一样；
	public String mPhotoFileName;
	
	public String login()
	{
		System.out.println(name + "," + password);
		
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter write;
		try {
			write = response.getWriter();
			write.write(" login sucess!");
			write.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String postString() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		ServletInputStream is = request.getInputStream();
		
		StringBuffer sb = new StringBuffer();
		int len = 0;
		byte[] buf = new byte[1024];
		
		while((len=is.read(buf)) != -1) {
			sb.append(new String(buf,0,len));
		}
		
		System.out.println(sb.toString());
		
		return null;
	}
	
	public String postFile() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		ServletInputStream is = request.getInputStream();
		
		String dir = ServletActionContext.getServletContext().getRealPath("files");
		System.out.println("dir:" + dir);
		
		File file = new File(dir, "imooc1.jpg");
		
		
		FileOutputStream fos = new FileOutputStream(file);
		int len = 0;
		byte[] buf = new byte[1024];
		
		while((len=is.read(buf)) != -1) {
			fos.write(buf,0,len);
		}
		
		fos.flush();
		fos.close();
		
		System.out.println("file_length :" + file.length());
		
		
		return null;
	}
	
	public String uploadInfo() {
		
		System.out.println(name + "," + password);
		
		
		if(mPhoto == null) {
			System.out.println(mPhotoFileName +" is null .");
		}
		
		System.out.println("mPhotoFileName :" + mPhotoFileName);
		
		String dir = ServletActionContext.getServletContext().getRealPath("files");
		System.out.println("dir:" + dir);
		File file = new File(dir, mPhotoFileName);
		
		try {
			FileUtils.copyFile(mPhoto, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
