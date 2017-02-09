
package com.cqupt.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.Attachment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.net.WebService;

/**
 * 该类主要用于学生更新单个任务，即同步web端任务
 */
public class SingleTaskUpdate {
	/**
	 *用于监听后台任务的处理进度
	 */
	 public static interface OnProcessChangeListener{
		 
		public void onProcessChange(int process);
	 }
	  
	 public static final int CONNECTION_FAIL = 0;//连接服务器超时
	 public static final int UPDATE_SUCCESS = 1;//更新成功
	 public static final int IMPORT_TASK = 2;//导入任务
	 public static final int DOWNLOAD_TEST = 3;//下载作业
	 public static final int IMPORT_TEST = 4;//导入作业
	 public static final int DOWNLOAD_ITEM = 5;//下载题干
	 public static final int IMPORT_ITEM = 6;//导入题干
	 public static final int DOWNLOAD_OPTION = 7;//下载选项
	 public static final int IMPORT_OPTION = 8;//导入选项
	 
	 private OnProcessChangeListener listener;
	 private DBManager db;
	 private WebService webService;
	 private int taskID;
	 private String userID;
	 
	 private  ArrayList<Test> testList = null;//web端test列表
	 private  ArrayList<Integer> testIDs = null;//本地test列表
	 private  ArrayList<Attachment> attachmentList = null;//web端附件列表
	 private  ArrayList<Attachment> attachments = null;//本地attachment列表
	 private  ArrayList<TestItem> testItemList = null;//web端item列表
	 private  ArrayList<Integer> testItemIDs = null;//本地item列表
	 private  ArrayList<TestItemOption> testItemOptionList = null;//web端option列表
	 private  ArrayList<Integer> testItemOptionIDs = null;//本地option列表
	 
	 public SingleTaskUpdate(DBManager db,OnProcessChangeListener listener,String userID){
		 
		 this.listener = listener;
		 this.db = db;
		 webService = new WebService();
		 this.userID = userID;
	 }
	 
	 public int updateTask(Task task){
		 
		 listener.onProcessChange(IMPORT_TASK);
		 db.addTask(task);
		 taskID = task.getTaskID();
		 
		 //获得任务附件列表
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(2));
		 map.put("userID","");
		 SoapObject result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web端附件列表
		 //db.addAttachment(attachmentList);
		 attachments = db.getAttachment(taskID);//本地附件列表
	     //删除本地和服务器不同的附件
	     if(attachmentList.size() == 0){
	    	 db.deleteAttachment(taskID);
		 }else{
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
          }
	     
	     
	     
	     //获得在web端已经上传的附件列表
		 map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(3));
		 map.put("userID", userID);
		 result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web端附件列表
		
		 
		 for(Attachment a : attachmentList){
			 a.setUserID(userID);
		 }
		 
		 if(attachmentList.size() == 0){
			 db.deleteAttachment(userID,taskID);
			 db.deleteUploadFiles(userID, taskID);
		 }else{
			 //db.addAttachment(attachmentList);
			 attachments = db.getAttachment(taskID,userID);//本地附件列表
			 
		     //删除本地和服务器不同的附件	
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, userID,a.getNewName());
				 db.deleteUploadFiles(a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
		 } 
		 
		 //更新反馈内容
		 map = new HashMap<String, String>();
		 map.put("taskID", String.valueOf(taskID));
		 map.put("userID", userID);
		 
		 result = webService.CallWebService("getFeedback", map);
		 
		 if(result != null){
			 FeedBack feedback = XMLParser.parseFeedBack(result);
			 feedback.setTaskID(taskID);
			 db.addFeedBack(userID, feedback);
		 }

		 return updateTest(taskID);
	 }
	 
	 private int updateTest(int taskID){
		 //获得题目列表
		 listener.onProcessChange(DOWNLOAD_TEST);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("taskID", String.valueOf(taskID));
		 SoapObject result = webService.CallWebService("getTestList", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testList = XMLParser.parseTestList(result);//web端test列表
		 testIDs = db.getTestListIDs(taskID);//本地test列表
		 
		 if(testList != null){
			 listener.onProcessChange(IMPORT_TEST);
			 
			 //后台下载题目中的图片
			 new Thread(new Runnable(){
				 public void run(){
					 DownloadMultiMedia.downloadInnerTextImage(testList);
				 }
			 }).start();
			 
			 db.addTestLibrary(testList);
			 for(Test test:testList){
				 
				 //获得作业附件列表
				 map = new HashMap<String,String>();
				 map.put("id", String.valueOf(test.getTestID()));
				 map.put("type",String.valueOf(1));
				 map.put("userID", "");
				 result = webService.CallWebService("getAttachment",map);
				 
				 if(result == null){
					 return CONNECTION_FAIL;
				 }
				 
				 attachmentList = XMLParser.parseAttachment(result);//web端附件列表
				 //db.addAttachment(attachmentList);
				 attachments = db.getAttachment(test.getTestID());//本地附件列表
				 
			     //删除本地和服务器不同的作业附件
			     if(attachmentList.size() == 0){
			    	 db.deleteAttachment(test.getTestID());
				 }else{
					 attachments.removeAll(attachmentList);
					 
					 for(Attachment a : attachments){
						 db.deleteAttachment(test.getTestID(), a.getNewName());
					 }
					 
					 db.addAttachment(attachmentList);
		          }
			     
				 testIDs.remove((Object)test.getTestID());
				 
				 //添加作业题目对应关系表
				 db.addTaskTestArrange(taskID, test.getTestID());
				 
				 //更新题干
				 if(updateItem(test.getTestID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 }
		 }
		 
		 //删除本地和服务器不同的任务作业安排和作业所对应的答案(始终保持与服务器一致)
		 for(int id : testIDs){
			 db.deleteTaskTestArrange(taskID,id);
			 ArrayList<Integer> itemIDs = db.getItemListIDs(id);
			 for(int itemID : itemIDs){
				 db.deleteMyChoice(userID,taskID, itemID);
			 }
		 }
		 return UPDATE_SUCCESS;
	 }
	 
	 private int updateItem(int testID){
		 //获得题干详情
         listener.onProcessChange(DOWNLOAD_ITEM);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("testID", String.valueOf(testID));
		 SoapObject result = webService.CallWebService("getTestItem", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testItemList = XMLParser.parseTestItem(result);//web端item列表
		 testItemIDs = db.getItemListIDs(testID);//本地item列表
		 
		 if(testItemList != null){
			 listener.onProcessChange(IMPORT_ITEM);
			 db.addTestItem(testItemList);//添加题干
			 for(TestItem items:testItemList){
				 testItemIDs.remove((Object)items.getTestItemID());
				 if(updateOption(items.getTestItemID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 } 
		 }
		 //删除本地和服务器不同的testItem(始终保持与服务器一致)
		 for(int id : testItemIDs){
			 db.deleteItem(testID,id);
			 db.deleteMyChoice(userID,taskID,id);
		 }
		 return UPDATE_SUCCESS;
	 }
	 
	 private int updateOption(int itemID){
		 listener.onProcessChange(DOWNLOAD_OPTION);
		 HashMap<String,String> map = new HashMap<String,String>() ;
		 map.put("testItemID", String.valueOf(itemID));
		 SoapObject result = webService.CallWebService("getTestItemOption", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testItemOptionList = XMLParser.parseTestItemOption(result);//web端option列表
		 testItemOptionIDs = db.getOptionListIDs(itemID);//本地Option列表
		 if(testItemOptionList != null){
			 listener.onProcessChange(IMPORT_OPTION);
			 db.addTestOption(testItemOptionList);
			 for(TestItemOption options : testItemOptionList){
				 testItemOptionIDs.remove((Object)options.getTestItemOptionID());
			 }
		 }
		 //删除本地和服务器不同的testItemOption(始终保持与服务器一致)
		 for(int id : testItemOptionIDs){
			 db.deleteOption(itemID,id);
		 }
		 return UPDATE_SUCCESS;
	 }
}
