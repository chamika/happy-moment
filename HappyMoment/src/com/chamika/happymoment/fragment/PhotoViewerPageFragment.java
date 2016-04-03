package com.chamika.happymoment.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chamika.happymoment.R;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.utils.ViewUtil;

public class PhotoViewerPageFragment extends Fragment {

	private FBPhoto photo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photo_page, container, false);
		// TODO set values using photo;

		if (photo != null && photo.getImageUrl() != null) {
			ImageView imageView = (ImageView) rootView.findViewById(R.id.photoviewer_image_single);
			ViewUtil.loadPhotoViewerImage(imageView, photo.getImageUrl());
		}

		return rootView;
	}

	public FBPhoto getPhoto() {
		return photo;
	}

	public void setPhoto(FBPhoto photo) {
		this.photo = photo;
	}

	public void loadPhoto(FBPhoto photo) {
		this.photo = photo;
	}

}