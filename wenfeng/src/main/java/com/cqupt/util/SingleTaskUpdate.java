
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
 * ������Ҫ����ѧ�����µ������񣬼�ͬ��web������
 */
public class SingleTaskUpdate {
	/**
	 *���ڼ�����̨����Ĵ������
	 */
	 public static interface OnProcessChangeListener{
		 
		public void onProcessChange(int process);
	 }
	  
	 public static final int CONNECTION_FAIL = 0;//���ӷ�������ʱ
	 public static final int UPDATE_SUCCESS = 1;//���³ɹ�
	 public static final int IMPORT_TASK = 2;//��������
	 public static final int DOWNLOAD_TEST = 3;//������ҵ
	 public static final int IMPORT_TEST = 4;//������ҵ
	 public static final int DOWNLOAD_ITEM = 5;//�������
	 public static final int IMPORT_ITEM = 6;//�������
	 public static final int DOWNLOAD_OPTION = 7;//����ѡ��
	 public static final int IMPORT_OPTION = 8;//����ѡ��
	 
	 private OnProcessChangeListener listener;
	 private DBManager db;
	 private WebService webService;
	 private int taskID;
	 private String userID;
	 
	 private  ArrayList<Test> testList = null;//web��test�б�
	 private  ArrayList<Integer> testIDs = null;//����test�б�
	 private  ArrayList<Attachment> attachmentList = null;//web�˸����б�
	 private  ArrayList<Attachment> attachments = null;//����attachment�б�
	 private  ArrayList<TestItem> testItemList = null;//web��item�б�
	 private  ArrayList<Integer> testItemIDs = null;//����item�б�
	 private  ArrayList<TestItemOption> testItemOptionList = null;//web��option�б�
	 private  ArrayList<Integer> testItemOptionIDs = null;//����option�б�
	 
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
		 
		 //������񸽼��б�
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(2));
		 map.put("userID","");
		 SoapObject result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web�˸����б�
		 //db.addAttachment(attachmentList);
		 attachments = db.getAttachment(taskID);//���ظ����б�
	     //ɾ�����غͷ�������ͬ�ĸ���
	     if(attachmentList.size() == 0){
	    	 db.deleteAttachment(taskID);
		 }else{
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
          }
	     
	     
	     
	     //�����web���Ѿ��ϴ��ĸ����б�
		 map = new HashMap<String,String>();
		 map.put("id", String.valueOf(task.getTaskID()));
		 map.put("type",String.valueOf(3));
		 map.put("userID", userID);
		 result = webService.CallWebService("getAttachment",map);
		 
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 
		 attachmentList = XMLParser.parseAttachment(result);//web�˸����б�
		
		 
		 for(Attachment a : attachmentList){
			 a.setUserID(userID);
		 }
		 
		 if(attachmentList.size() == 0){
			 db.deleteAttachment(userID,taskID);
			 db.deleteUploadFiles(userID, taskID);
		 }else{
			 //db.addAttachment(attachmentList);
			 attachments = db.getAttachment(taskID,userID);//���ظ����б�
			 
		     //ɾ�����غͷ�������ͬ�ĸ���	
			 attachments.removeAll(attachmentList);
			 
			 for(Attachment a : attachments){
				 db.deleteAttachment(taskID, userID,a.getNewName());
				 db.deleteUploadFiles(a.getNewName());
			 }
			 
			 db.addAttachment(attachmentList);
		 } 
		 
		 //���·�������
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
		 //�����Ŀ�б�
		 listener.onProcessChange(DOWNLOAD_TEST);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("taskID", String.valueOf(taskID));
		 SoapObject result = webService.CallWebService("getTestList", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testList = XMLParser.parseTestList(result);//web��test�б�
		 testIDs = db.getTestListIDs(taskID);//����test�б�
		 
		 if(testList != null){
			 listener.onProcessChange(IMPORT_TEST);
			 
			 //��̨������Ŀ�е�ͼƬ
			 new Thread(new Runnable(){
				 public void run(){
					 DownloadMultiMedia.downloadInnerTextImage(testList);
				 }
			 }).start();
			 
			 db.addTestLibrary(testList);
			 for(Test test:testList){
				 
				 //�����ҵ�����б�
				 map = new HashMap<String,String>();
				 map.put("id", String.valueOf(test.getTestID()));
				 map.put("type",String.valueOf(1));
				 map.put("userID", "");
				 result = webService.CallWebService("getAttachment",map);
				 
				 if(result == null){
					 return CONNECTION_FAIL;
				 }
				 
				 attachmentList = XMLParser.parseAttachment(result);//web�˸����б�
				 //db.addAttachment(attachmentList);
				 attachments = db.getAttachment(test.getTestID());//���ظ����б�
				 
			     //ɾ�����غͷ�������ͬ����ҵ����
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
				 
				 //�����ҵ��Ŀ��Ӧ��ϵ��
				 db.addTaskTestArrange(taskID, test.getTestID());
				 
				 //�������
				 if(updateItem(test.getTestID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 }
		 }
		 
		 //ɾ�����غͷ�������ͬ��������ҵ���ź���ҵ����Ӧ�Ĵ�(ʼ�ձ����������һ��)
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
		 //����������
         listener.onProcessChange(DOWNLOAD_ITEM);
		 HashMap<String,String> map = new HashMap<String,String>();
		 map.put("testID", String.valueOf(testID));
		 SoapObject result = webService.CallWebService("getTestItem", map);
		 if(result == null){
			 return CONNECTION_FAIL;
		 }
		 testItemList = XMLParser.parseTestItem(result);//web��item�б�
		 testItemIDs = db.getItemListIDs(testID);//����item�б�
		 
		 if(testItemList != null){
			 listener.onProcessChange(IMPORT_ITEM);
			 db.addTestItem(testItemList);//������
			 for(TestItem items:testItemList){
				 testItemIDs.remove((Object)items.getTestItemID());
				 if(updateOption(items.getTestItemID()) == CONNECTION_FAIL){
					 return CONNECTION_FAIL;
				 }
			 } 
		 }
		 //ɾ�����غͷ�������ͬ��testItem(ʼ�ձ����������һ��)
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
		 testItemOptionList = XMLParser.parseTestItemOption(result);//web��option�б�
		 testItemOptionIDs = db.getOptionListIDs(itemID);//����Option�б�
		 if(testItemOptionList != null){
			 listener.onProcessChange(IMPORT_OPTION);
			 db.addTestOption(testItemOptionList);
			 for(TestItemOption options : testItemOptionList){
				 testItemOptionIDs.remove((Object)options.getTestItemOptionID());
			 }
		 }
		 //ɾ�����غͷ�������ͬ��testItemOption(ʼ�ձ����������һ��)
		 for(int id : testItemOptionIDs){
			 db.deleteOption(itemID,id);
		 }
		 return UPDATE_SUCCESS;
	 }
}
