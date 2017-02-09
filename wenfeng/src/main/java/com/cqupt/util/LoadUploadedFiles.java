package com.cqupt.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.ksoap2.serialization.SoapObject;

import android.os.Environment;

import com.cqupt.db.DBManager;
import com.cqupt.model.Attachment;
import com.cqupt.model.UploadFiles;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.FileExploreActivity.MyFile;

/**
*该类用于加载用户从本地和web上传的附件(优先加载本地上传的文件，免于下载)
*/

public class LoadUploadedFiles {
	
	public static void loadFiles(ArrayList<File> files,DBManager db,int taskID
			,String userID,int userType){
		
		ArrayList<UploadFiles> uploadFiles = db.getUploadFile(userID,taskID);//本地上传的附件
		
		//web端上传的附件
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		
		//学生用户获得web端上传过的附件直接从本地数据库取得
		if(userType == 3){
			attachments = db.getAttachment(taskID,userID);
		}else{//教师用户获得已经上传的附件直接从网络获取
			WebService web = new WebService();
			//获得任务附件列表
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("id", String.valueOf(taskID));
			map.put("type",String.valueOf(2));
			map.put("userID","");
			
			SoapObject result = web.CallWebService("getAttachment", map);
			
			if(result != null){
				attachments = XMLParser.parseAttachment(result);
			}

		}
		
		Iterator<Attachment> ait = attachments.iterator();
		Iterator<UploadFiles> fit = uploadFiles.iterator();
		
		//优先加载本地数据库已有记录,这样可以直接打开附件而不用先下载
		while(fit.hasNext()){
			String fileNewName = fit.next().getFileNewName();
			while(ait.hasNext()){
				String fileName = ait.next().getNewName();
				if(fileNewName.equals(fileName)){
					ait.remove();
					ait = attachments.iterator();
					break;
				}
			}
		}
		
		for(UploadFiles f : uploadFiles){
			MyFile mFile = new MyFile(f.getFilePath());
			mFile.setNewName(f.getFileNewName());
			files.add(mFile);	
		}
		
		for(Attachment a : attachments){
			MyFile mFile = new MyFile(Environment.getExternalStorageDirectory()
					.getPath() + "/wenfeng/" + userID+ "/download/" + a.getOriginName());
			mFile.setNewName(a.getNewName());
			files.add(mFile);
		}
		
	}

}
