package com.cqupt.util;

import java.io.File;

public class MIMEUtils {
	
	private static final String[][] MIME_MapTable={
		    //{��׺����    MIME����}
		    {".amr",    "audio/*"},
		    {".3gp",    "video/3gpp"},
		    {".apk",    "application/vnd.android.package-archive"},
		    {".asf",    "video/x-ms-asf"},
		    {".avi",    "video/x-msvideo"},
		    {".bmp",      "image/bmp"},			   
		    {".doc",    "application/msword"},
		    {".docx",    "application/msword"},
		    {".xls",    "application/msword"},
		    {".xlsx",    "application/msword"},
		    {".exe",    "application/octet-stream"},
		    {".gif",    "image/gif"},
		    {".htm",    "text/html"},
		    {".html",    "text/html"},
		    {".jar",    "application/java-archive"},
		    {".jpeg",    "image/jpeg"},
		    {".jpg",    "image/jpeg"},
		    {".js",        "application/x-javascript"},
		    {".m3u",    "audio/x-mpegurl"},
		    {".m4a",    "audio/mp4a-latm"},
		    {".m4b",    "audio/mp4a-latm"},
		    {".m4p",    "audio/mp4a-latm"},
		    {".m4u",    "video/vnd.mpegurl"},
		    {".m4v",    "video/x-m4v"},    
		    {".mov",    "video/quicktime"},		    
		    {".mp3",    "audio/x-mpeg"},
		    {".mp4",    "video/mp4"},
		    {".mpc",    "application/vnd.mpohun.certificate"},        
		    {".mpe",    "video/mpeg"},    
		    {".mpeg",    "video/mpeg"},    
		    {".mpg",    "video/mpeg"},    
		    {".mpg4",    "video/mp4"},    
		    {".mpga",    "audio/mpeg"},
		    {".msg",    "application/vnd.ms-outlook"},
		    {".ogg",    "audio/ogg"},
		    {".pdf",    "application/pdf"},
		    {".png",    "image/png"},
		    {".pps",    "application/vnd.ms-powerpoint"},
		    {".ppt",    "application/vnd.ms-powerpoint"},
		    {".prop",    "text/plain"},
		    {".rar",    "application/x-rar-compressed"},
		    {".rc",        "text/plain"},
		    {".rmvb",    "audio/x-pn-realaudio"},
		    {".rtf",    "application/rtf"},
		    {".sh",        "text/plain"},
		    {".tar",    "application/x-tar"},    
		    {".tgz",    "application/x-compressed"}, 
		    {".txt",    "text/plain"},
		    {".wav",    "audio/x-wav"},
		    {".wma",    "audio/x-ms-wma"},
		    {".wmv",    "audio/x-ms-wmv"},
		    {".wps",    "application/vnd.ms-works"},
		    {".xml",    "text/plain"},
		    {".z",        "application/x-compress"},
		    {".zip",    "application/zip"},
		    {".flv",    "flv-application/octet-stream"},
		    {"",        "*/*"}    
		};
	
	/**
	 * �����ļ���׺����ö�Ӧ��MIME���͡�
	 * @param file
	 */
	public static String getMIMEType(File file)
	{
	    String type="*/*";
	    String fName=file.getName();
	    //��ȡ��׺��ǰ�ķָ���"."��fName�е�λ�á�
	    int dotIndex = fName.lastIndexOf(".");
	    if(dotIndex < 0){
	        return type;
	    }
	    /* ��ȡ�ļ��ĺ�׺�� */
	    String end=fName.substring(dotIndex,fName.length());
	    if(end=="")return type;
	    //��MIME���ļ����͵�ƥ������ҵ���Ӧ��MIME���͡�
	    for(int i=0;i<MIME_MapTable.length;i++){
	        if(end.equalsIgnoreCase(MIME_MapTable[i][0]))
	            type = MIME_MapTable[i][1];
	    }
	    return type;
	}
	
	public static String getMIMEType(String fName)
	{
	    String type="*/*";
	    //��ȡ��׺��ǰ�ķָ���"."��fName�е�λ�á�
	    int dotIndex = fName.lastIndexOf(".");
	    if(dotIndex < 0){
	        return type;
	    }
	    /* ��ȡ�ļ��ĺ�׺�� */
	    String end=fName.substring(dotIndex,fName.length());
	    if(end=="")return type;
	    //��MIME���ļ����͵�ƥ������ҵ���Ӧ��MIME���͡�
	    for(int i=0;i<MIME_MapTable.length;i++){
	        if(end.equalsIgnoreCase(MIME_MapTable[i][0]))
	            type = MIME_MapTable[i][1];
	    }
	    return type;
	}

}