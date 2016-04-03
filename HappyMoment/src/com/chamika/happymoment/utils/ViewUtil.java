package com.chamika.happymoment.utils;

import com.chamika.happymoment.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.widget.ImageView;

public class ViewUtil {

	public static void setupTransparentActionbar(ActionBar actionBar) {
		int transparentColor = Color.parseColor("#00000000");
		actionBar.setBackgroundDrawable(new ColorDrawable(transparentColor));
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(transparentColor));
	}

	public static void loadMainImage(ImageView view, String url) {
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new SimpleBitmapDisplayer()).build();

		ImageLoader.getInstance().displayImage(url, view, options);
	}

	public static void loadPhotoViewerImage(ImageView view, String url) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher)
				.cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.NONE)
				.bitmapConfig(Bitmap.Config.ARGB_8888).displayer(new SimpleBitmapDisplayer()).build();

		ImageLoader.getInstance().displayImage(url, view, options);
	}

}
