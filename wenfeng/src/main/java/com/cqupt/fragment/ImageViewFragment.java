package com.cqupt.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cqupt.R;

import uk.co.senab.photoview.PhotoView;

public class ImageViewFragment extends Fragment {
	
	private static final String TAG_URL = "url";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ImageView imageView = new PhotoView(getActivity());
		
		Glide.with(getActivity())
		     .load(getArguments().getString(TAG_URL))
		     .asBitmap()//���������� ����ʹ�Զ���view �ɹ���ʾͼƬ��������
		     .placeholder(R.drawable.default_image_background)
		     .into(imageView);
		
		return imageView;
	}
	
	public static ImageViewFragment getInstance(String url){
		
		ImageViewFragment fragment = new ImageViewFragment();
		Bundle bundle = new Bundle();
		bundle.putString(TAG_URL, url);
		fragment.setArguments(bundle);
		
		return fragment;
		
	}

}
