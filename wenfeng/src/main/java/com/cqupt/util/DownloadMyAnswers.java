package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.MyChoice;
import com.cqupt.model.Test;
import com.cqupt.net.WebService;

/**
 * 从web端下载已经做过的题目答案
 */

public class DownloadMyAnswers {
	
	public static final int CONNECTION_TIME_OUT = 0;
	public static final int DOWNLOAD_SUCCESS = 1;
	
	private DBManager db;
	
	private ArrayList<Test> testList;//存储题库列表
	private ArrayList<HashMap<String,String>> questionList;
	private int taskID;
	private String userID;
	
	public DownloadMyAnswers(int taskID,DBManager db,String userID){
		
		this.taskID = taskID;
		this.userID = userID;
		this.db = db;
		
	}
	
	public int download(){
		testList = db.getTestListByTaskID(taskID);//数据库获取题库列表
		for(Test test:testList){
			questionList = db.getQuestions(test.getTestID());
			for(int i = 0;i < questionList.size();i++){
				HashMap<String,String> p = new HashMap<String,String>();
				p.put("taskID", String.valueOf(taskID));
				p.put("testItemID", questionList.get(i).get("itemID"));
				p.put("userID",userID);
				p.put("testID", String.valueOf(test.getTestID()));
				WebService web = new WebService();
				SoapObject result = web.CallWebService("downloadMyChoice",p);
				//连接服务器超时
				if(result == null){
					return CONNECTION_TIME_OUT;
				}
				HashMap<String,String> r = XMLParser.parseAnswers(result);
				//更新本地数据库
				//web端未作答的不存入本地数据库
				if(r != null){
					MyChoice myChoice = new MyChoice();
					myChoice.setAnswer(r.get("answer"));
					myChoice.setTestItemID(Integer.parseInt(questionList.get(i).get("itemID")));
					myChoice.setUserID(userID);
					myChoice.setSubmitTime(r.get("submitTime"));
					myChoice.setTaskID(taskID);
					db.addMyChoice(myChoice);
				}
			}
		}
		
		return DOWNLOAD_SUCCESS;
	}

}
