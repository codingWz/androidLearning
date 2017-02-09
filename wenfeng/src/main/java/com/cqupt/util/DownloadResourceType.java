package com.cqupt.util;

import org.ksoap2.serialization.SoapObject;

import com.cqupt.db.DBManager;
import com.cqupt.model.TestSubType;
import com.cqupt.model.TestType;
import com.cqupt.net.WebService;

/**
 * 
 * @author Administrator
 * 下载TestTopType 和 TestType
 *
 */
public class DownloadResourceType {
	
	public static boolean download(DBManager db){
		
		WebService web = new WebService();
		
		SoapObject result = web.CallWebService("getTestType", null);
		
		if(result == null){
			return false;
		}
		
		//添加testTopType
		for(TestType tp : XMLParser.parseTestType(result)){
			db.addTestTopType(tp);
		}
		
		//添加testType
		result = web.CallWebService("getTestSubTypeForLocal", null);
		
		if(result == null){
			return false;
		}
		
		for(TestSubType tst : XMLParser.parseTestSubType(result)){
			db.addTestType(tst);
		} 
		
		return true;
		
	}

}
