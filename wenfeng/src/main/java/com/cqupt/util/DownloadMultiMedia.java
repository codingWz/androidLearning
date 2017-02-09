package com.cqupt.util;

import android.os.Environment;

import com.cqupt.model.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类用于下载多媒体文件
 */
public class DownloadMultiMedia {
	
	private static String fileSavePath = Environment.getExternalStorageDirectory()
			+ "/wenfeng/cache";
	
	//下载图文混排中的图片
	public static void downloadInnerTextImage(List<Test> testList){
		
		for(Test test : testList){
			List<String> srcList = getSrc(test.getTestContent());
			
			for(String src : srcList){
				String fileName = src.substring(src.lastIndexOf("/") + 1);
				
				//文件不存在才进行下载
				if(!new File(fileSavePath + "/" + fileName).exists()){
					FileDownloadHttp.download("http://202.202.43.245" + src
							, fileName, fileSavePath);
				}			
			}
	    }
	
    }
	
	private static List<String> getSrc(String string){
		
		List<String> srcList = new ArrayList<String>();
		List<String> imgTagList = new ArrayList<String>();
		
		Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
		Matcher m = p.matcher(string);
		
		//取得所有的img标签
		while(m.find()){
			imgTagList.add(m.group());
		}
		
		//取得scr内容
		for(String s : imgTagList){
			srcList.add(s.substring(s.indexOf("src=") + 5
					,s.indexOf("\"",s.indexOf("src=") + 5)));
		}
		
		return srcList;
		
	}
	
}
