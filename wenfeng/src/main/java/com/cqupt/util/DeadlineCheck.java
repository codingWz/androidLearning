
package com.cqupt.util;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DeadlineCheck {
	
	/**
	 * 判断作业是否已经截止
	 */
	public static boolean check(String endDate){
		boolean flag = false;
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = format.format(date);
		int re = endDate.compareTo(time);
		if(re < 0) flag = true;
		return flag;
	}

}
