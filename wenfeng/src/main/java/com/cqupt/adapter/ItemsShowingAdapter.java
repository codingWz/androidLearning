package com.cqupt.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cqupt.R;
import com.cqupt.model.ItemsShowingModel;
import com.cqupt.util.FileUtil;
import com.cqupt.view.MyAudioPlayView;
import com.cqupt.view.TagView;

import java.util.ArrayList;
import java.util.List;

public class ItemsShowingAdapter extends ISimpleAdapter<ItemsShowingModel>{
	
	public static final String TAG_URLS = "urls"; 
	public static final String TAG_POSITION = "position"; 
	
	public ItemsShowingAdapter(Context context,List<ItemsShowingModel> data){
		
		mContext = context;
		mData = data;
		
		if(mData == null){
			mData = new ArrayList<ItemsShowingModel>();
		}
		
	}
	
	@Override
	public View getView(int position){
		
		ItemsShowingModel item = mData.get(position);
		String url = item.getUrl();
		
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		int type = FileUtil.getFileType(fileName);
		TagView tagView = new TagView(mContext);
		
		if(type == FileUtil.TYPE_AUDIO){
			
			MyAudioPlayView view = new MyAudioPlayView(mContext);
			tagView.setTarget(view, !item.isFileReaded());
			
		}else{
			ImageView imageView = new ImageView(mContext);
			
			if(type == FileUtil.TYPE_VIDEO){
				imageView.setImageResource(R.drawable.music);
			}else if(type == FileUtil.TYPE_DOCUMENT){
				imageView.setImageResource(R.drawable.document);
			}else{
				Glide.with(mContext)
				.load(url)
				.placeholder(R.drawable.default_image_background)
				.crossFade()
				.centerCrop()
				.into(imageView);
			}
			
			tagView.setTarget(imageView,!item.isFileReaded());
		}
		
		tagView.setTag(position);
		
		return tagView;
		
	}
	
}
