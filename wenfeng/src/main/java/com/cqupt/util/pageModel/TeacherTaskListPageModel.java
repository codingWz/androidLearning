package com.cqupt.util.pageModel;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.Task;
import com.cqupt.util.XMLParser;

public class TeacherTaskListPageModel extends PageModel<Task>{
	
	private String userID;
	
	/**
	 * 1��ʾ�����е���ҵ 2��ʾ�ѽ�ֹ����ҵ
	 */
	private int type = 1;
	
	/**
	 * 0 ɸѡȫ�� 1 ɸѡ������ 2 ɸѡδ����
	 */
	private int selection = 0;
	
	/**
	 * ����Ĭ�ϵ���ʼҳ��ÿҳ��ʾ��item����
	 */
	public TeacherTaskListPageModel(String userID){
		this.userID = userID;
	}
	
	/**
	 * �Զ�����ʼҳ��ÿҳ��ʾ��item����
	 */
	public TeacherTaskListPageModel(String userID,int currentPage,int pageSize){
		this.userID = userID;
		this.currentPage = currentPage;
		this.lineSize = pageSize;
	}
	

	@Override
	public List<Task> getDataList() {
		  	
    	HashMap<String,String> p = new HashMap<String, String>();
    	p.put("userID",userID);
    	p.put("type", String.valueOf(type));
    	p.put("currentPage",String.valueOf(currentPage));
    	p.put("lineSize", String.valueOf(lineSize));
    	p.put("selection", String.valueOf(selection));
    	
    	SoapObject result = web.CallWebService("getTeacherExerciseList", p);
    	
    	if(result == null){
    		
    		return null;
    	}
    	
    	return XMLParser.parseTeacherTaskList(result);
    	 	
	}

	@Override
	protected int getDataCount() {
		 	
    	HashMap<String,String> p = new HashMap<String, String>();
    	p.put("userID", userID);
    	p.put("type",String.valueOf(type));
    	p.put("selection", String.valueOf(selection));
    	
    	SoapObject result = web.CallWebService("getTeacherTaskCount",p);
    	
    	if(result == null){
    		return CONNECTION_FAIL;
    	}
    	
    	return XMLParser.parseInt(result);
    	
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void setSelection(int selection){
		this.selection = selection;
	}

}
