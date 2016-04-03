package com.chamika.happymoment.activity;

import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chamika.happymoment.AppConfig;
import com.chamika.happymoment.R;
import com.chamika.happymoment.database.HitsTable;
import com.chamika.happymoment.database.PinnedTable;
import com.chamika.happymoment.fragment.PhotoViewerPageFragment;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.utils.AppLogger;
import com.chamika.happymoment.utils.CommonUtils;
import com.chamika.happymoment.utils.DBUtil;
import com.chamika.happymoment.utils.ViewUtil;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;

public class PhotoViewerActivity extends ActionBarActivity {

	public static final String INTENT_EXTRA_INDEX = "index";
	public static final String INTENT_EXTRA_PHOTO_LIST = "list";
	public static final String INTENT_EXTRA_SIMILAR = "similar";
	public static final String INTENT_EXTRA_DIFFERENT = "different";

	private ArrayList<FBPhoto> photoList;
	private int currentIndex;
	private boolean similar;
	private boolean different;// true if different button is enabled

	private ViewPager pager;
	private PhotoViewerPagerAdapter adapter;
	private MenuItem menuPin;
	private MenuItem menuUnPin;

	private Activity activity;
	private UiLifecycleHelper uiHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, null);
		uiHelper.onCreate(savedInstanceState);

		activity = this;

		ActionBar actionBar = getSupportActionBar();
		ViewUtil.setupTransparentActionbar(actionBar);
		Drawable actionbarBg = getResources().getDrawable(R.drawable.photoviewer_actionbar_gradient);
		actionBar.setBackgroundDrawable(actionbarBg);
		actionBar.setStackedBackgroundDrawable(actionbarBg);
		actionBar.setTitle("");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_photoviewer);
		boolean hasExtras = handleExtras(getIntent().getExtras());

		if (hasExtras) {
			initUI();
		} else {
			Toast.makeText(this, "No photos found to show", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.photoviewer, menu);

		menuPin = menu.findItem(R.id.action_pin);
		menuUnPin = menu.findItem(R.id.action_unpin);
		updatedPinState();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		FBPhoto photo = getSelectedPhoto();
		switch (id) {
		case android.R.id.home:
			finishActivity();
		case R.id.action_pin:
			if (photo != null) {
				setPhotoPinned(photo.getPhotoId(), true);
			}
			break;
		case R.id.action_unpin:
			if (photo != null) {
				setPhotoPinned(photo.getPhotoId(), false);
			}
			break;
		case R.id.action_view_fb:
			if (photo != null) {
				CommonUtils.startUrlApp(activity, photo.getLink());
			}

			break;
		case R.id.action_wallpaper:
			if (photo != null) {
				setAsWallpaper(photo.getImageUrl());
			}
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				AppLogger.log("Activity", String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				AppLogger.log("Activity", "Success!");
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void initUI() {
		pager = (ViewPager) findViewById(R.id.photviewer_pager);

		if (photoList != null) {
			if (currentIndex < 0 || currentIndex >= photoList.size()) {
				currentIndex = 0;
			}

			updateAdapter(photoList);

			pager.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					updatedPinState();
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
		}

		ViewGroup buttonParent = (ViewGroup) findViewById(R.id.photoviewer_layout_container);
		Button buttonShare = (Button) buttonParent.findViewById(R.id.photoviewer_button_share);
		Button buttonPeople = (Button) buttonParent.findViewById(R.id.photoviewer_button_people);
		Button buttonSimilar = (Button) buttonParent.findViewById(R.id.photoviewer_button_similar);
		Button buttonDifferent = (Button) buttonParent.findViewById(R.id.photoviewer_button_different);

		buttonShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FBPhoto fbPhoto = getSelectedPhoto();
				if (fbPhoto != null) {
					shareLink(fbPhoto.getLink(), fbPhoto.getImageUrl());
				}
			}
		});

		if (similar) {
			buttonSimilar.setVisibility(View.GONE);
		} else {
			buttonSimilar.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FBPhoto selectedPhoto = getSelectedPhoto();
					if (selectedPhoto != null) {
						showSimilarPhotos(selectedPhoto);
					}
				}
			});
		}

		if (different) {
			buttonDifferent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new RankedPhotoLoader().execute();
				}
			});
		} else {
			buttonDifferent.setVisibility(View.GONE);
		}

		buttonPeople.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startContactActivity();
			}
		});

		if (similar) {
			getSupportActionBar().setTitle("Similar Photos");
		}

	}

	private void updateAdapter(ArrayList<FBPhoto> list) {
		adapter = new PhotoViewerPagerAdapter(getSupportFragmentManager(), list);
		pager.setAdapter(adapter);
	}

	/**
	 * read extras and set variables
	 * 
	 * @return true if valid extras are found and set
	 */
	private boolean handleExtras(Bundle bundle) {
		boolean result = true;

		if (bundle == null) {
			return false;
		}
		if (bundle.containsKey(INTENT_EXTRA_PHOTO_LIST)) {
			photoList = (ArrayList<FBPhoto>) bundle.getSerializable(INTENT_EXTRA_PHOTO_LIST);
		} else {
			result = false;
		}

		if (result && bundle.containsKey(INTENT_EXTRA_INDEX)) {
			currentIndex = bundle.getInt(INTENT_EXTRA_INDEX);
		}

		if (bundle.containsKey(INTENT_EXTRA_SIMILAR)) {
			similar = bundle.getBoolean(INTENT_EXTRA_SIMILAR);
		}

		if (bundle.containsKey(INTENT_EXTRA_DIFFERENT)) {
			different = bundle.getBoolean(INTENT_EXTRA_DIFFERENT);
		}

		return result;
	}

	private class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter {
		private ArrayList<FBPhoto> list;

		public PhotoViewerPagerAdapter(FragmentManager fm, ArrayList<FBPhoto> photoList) {
			super(fm);
			this.list = photoList;
		}

		@Override
		public Fragment getItem(int position) {
			FBPhoto photo = list.get(position);
			if (photo != null) {
				PhotoViewerPageFragment fragment = new PhotoViewerPageFragment();
				fragment.setPhoto(photo);
				return fragment;
			} else {
				return new PhotoViewerPageFragment();
			}
		}

		public FBPhoto getPhoto(int position) {
			if (list != null && list.size() > 0) {
				return list.get(position);
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}
	}

	private void shareLink(String link, String picture) {
		if (link != null) {
			try {
				ShareDialogBuilder dialogBuilder = new FacebookDialog.ShareDialogBuilder(this);
				dialogBuilder.setLink(link);
				dialogBuilder.setApplicationName(AppConfig.FACEBOOK_APP_NAME);
				dialogBuilder.setCaption(AppConfig.APP_POST_CAPTION);
				if (picture != null) {
					dialogBuilder.setPicture(picture);
				}
				FacebookDialog shareDialog = dialogBuilder.build();
				uiHelper.trackPendingDialogCall(shareDialog.present());
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, "Facebook application not found to share the photo", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void updatedPinState() {
		FBPhoto selectedPhoto = getSelectedPhoto();
		if (selectedPhoto != null) {
			boolean isPinned = selectedPhoto.isPinned();

			menuPin.setVisible(!isPinned);
			menuUnPin.setVisible(isPinned);
		}
	}

	private FBPhoto getSelectedPhoto() {
		int index = pager.getCurrentItem();
		FBPhoto photo = adapter.getPhoto(index);

		return photo;
	}

	private void setAsWallpaper(final String photoLink) {
		final WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL(photoLink);
					Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					myWallpaperManager.setBitmap(image);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		t.start();
	}

	private void setPhotoPinned(final String photoId, final boolean pin) {
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				boolean result = false;
				PinnedTable table = new PinnedTable(getApplicationContext());
				if (pin) {
					result = table.pinPhoto(photoId);
				} else {
					result = table.removeFromPinned(photoId);
				}
				return result;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (result) {
					FBPhoto photo = getSelectedPhoto();
					photo.setPinned(pin);

					updatedPinState();
				}
			}

		};

		task.execute();

	}

	private void showSimilarPhotos(FBPhoto photo) {
		AsyncTask<FBPhoto, Void, ArrayList<FBPhoto>> task = new AsyncTask<FBPhoto, Void, ArrayList<FBPhoto>>() {

			@Override
			protected ArrayList<FBPhoto> doInBackground(FBPhoto... params) {
				if (params.length == 1) {
					FBPhoto photo = params[0];

					ArrayList<FBPhoto> similarPhotos = DBUtil.getSimilarPhotos(getApplicationContext(), photo);

					return similarPhotos;

				}
				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<FBPhoto> result) {
				super.onPostExecute(result);
				if (result != null && result.size() > 0 && activity != null) {
					CommonUtils.startPhotoViewerActivity(activity, 0, result, true, false);
				} else {
					Toast.makeText(getApplicationContext(), "No similar photos found", Toast.LENGTH_LONG).show();
				}
			}

		};

		task.execute(photo);
	}

	private void hitPhoto(final String photoId) {
		Thread t = new Thread() {
			public void run() {
				HitsTable hitsTable = new HitsTable(getApplicationContext());
				hitsTable.hit(photoId);
			};
		};
		t.start();
	}

	private class RankedPhotoLoader extends AsyncTask<Void, Integer, ArrayList<FBPhoto>> {

		@Override
		protected ArrayList<FBPhoto> doInBackground(Void... params) {
			ArrayList<FBPhoto> rankedPhotos = DBUtil.getRankedPhotos(getApplicationContext());
			return rankedPhotos;
		}

		@Override
		protected void onPostExecute(ArrayList<FBPhoto> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				ArrayList<FBPhoto> list = new ArrayList<FBPhoto>();
				FBPhoto fbPhoto = result.get(0);
				list.add(fbPhoto);
				hitPhoto(fbPhoto.getPhotoId());
				updateAdapter(list);
			} else {
				Toast.makeText(getApplicationContext(), "Unable to load different photo", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void startContactActivity() {
		FBPhoto photo = getSelectedPhoto();
		Intent intent = new Intent(this, ContactActivity.class);
		intent.putExtra(ContactActivity.INTENT_EXTRA_PHOTO, photo);
		startActivity(intent);
	}

	private void finishActivity() {
		finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
