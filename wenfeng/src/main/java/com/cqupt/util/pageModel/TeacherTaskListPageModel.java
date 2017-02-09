package com.cqupt.util.pageModel;

import java.util.HashMap;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.model.Task;
import com.cqupt.util.XMLParser;

public class TeacherTaskListPageModel extends PageModel<Task>{
	
	private String userID;
	
	/**
	 * 1表示进行中的作业 2表示已截止的作业
	 */
	private int type = 1;
	
	/**
	 * 0 筛选全部 1 筛选已评分 2 筛选未评分
	 */
	private int selection = 0;
	
	/**
	 * 采用默认的起始页和每页显示的item数量
	 */
	public TeacherTaskListPageModel(String userID){
		this.userID = userID;
	}
	
	/**
	 * 自定义起始页和每页显示的item数量
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
