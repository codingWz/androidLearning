package com.cqupt.util;

import android.content.Intent;
import android.net.Uri;


/**
 * 该类用于调用系统应用
 */
public class IntentUtils {
	
	public static Intent getViewIntent(Uri uri,String mime){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mime);
		return intent;
	}
	
	public static Intent getViewIntent(String uri,String mime){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(uri), mime);
		return intent;
	}
	
	public static Intent getCameraIntent(){
		return new Intent("android.media.action.IMAGE_CAPTURE");  
	}

}
