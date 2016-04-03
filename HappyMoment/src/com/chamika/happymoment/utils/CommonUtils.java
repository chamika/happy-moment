package com.chamika.happymoment.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import com.chamika.happymoment.activity.PhotoViewerActivity;
import com.chamika.happymoment.model.FBPhoto;

public class CommonUtils {

	public static void startPhotoViewerActivity(Activity activity, int index, ArrayList<FBPhoto> photos,
			boolean isSimilar, boolean enableDiffent) {
		Intent intent = new Intent(activity, PhotoViewerActivity.class);
		intent.putExtra(PhotoViewerActivity.INTENT_EXTRA_INDEX, index);
		intent.putExtra(PhotoViewerActivity.INTENT_EXTRA_PHOTO_LIST, photos);
		intent.putExtra(PhotoViewerActivity.INTENT_EXTRA_SIMILAR, isSimilar);
		intent.putExtra(PhotoViewerActivity.INTENT_EXTRA_DIFFERENT, enableDiffent);
		activity.startActivity(intent);
	}

	public static void startUrlApp(Activity activity, String link) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			activity.startActivity(browserIntent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(activity.getApplicationContext(), "Unable to open link", Toast.LENGTH_LONG).show();
		}
	}

	public static boolean isInternetConnected(Context applicationContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;

	}
}
