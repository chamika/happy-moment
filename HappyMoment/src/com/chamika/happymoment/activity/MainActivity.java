package com.chamika.happymoment.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.chamika.happymoment.R;
import com.chamika.happymoment.database.HitsTable;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.model.StateMainActivity;
import com.chamika.happymoment.model.User;
import com.chamika.happymoment.service.PhotoLoaderService;
import com.chamika.happymoment.utils.AppLogger;
import com.chamika.happymoment.utils.CommonUtils;
import com.chamika.happymoment.utils.DBUtil;
import com.chamika.happymoment.utils.FBUtil;
import com.chamika.happymoment.utils.ViewUtil;
import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int ACTIVITY_REQUEST_CODE_FB_AUTH = 100;

	private Session.StatusCallback statusCallback;

	private ImageView imageMain;
	private ImageView imageProfile;
	private ImageView imageCover;
	private TextView textName;

	private ArrayList<FBPhoto> rankedPhotos;
	private int currentIndex = 0;
	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);

		ViewUtil.setupTransparentActionbar(getSupportActionBar());

		setContentView(R.layout.activity_main);

		activity = this;

		statusCallback = new SessionStatusCallback();

		initUI();

		if (validateFBLogin()) {
			proceed();
		} else {
			startFBLogin();
		}

		// try {
		// PackageInfo info =
		// getPackageManager().getPackageInfo("com.chamika.happymoment",
		// PackageManager.GET_SIGNATURES);
		// for (Signature signature : info.signatures) {
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(signature.toByteArray());
		// Log.d(TAG, Base64.encodeToString(md.digest(), Base64.DEFAULT));
		// }
		// } catch (NameNotFoundException e) {
		//
		// } catch (NoSuchAlgorithmException e) {
		//
		// }
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		StateMainActivity activityState = new StateMainActivity();
		activityState.setCurrentIndex(currentIndex);
		activityState.setRankedPhotos(rankedPhotos);
		return activityState;
	}

	private void startLoaderService() {
		Intent dataService = new Intent(getApplicationContext(), PhotoLoaderService.class);
		startService(dataService);
	}

	private void initUI() {
		getOverflowMenu();

		imageMain = (ImageView) findViewById(R.id.main_image);
		imageMain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// showNextImage();
				startPhotoViewerActivity();
			}
		});

		imageProfile = (ImageView) findViewById(R.id.main_image_profile);
		imageCover = (ImageView) findViewById(R.id.main_image_cover);

		imageCover.post(new Runnable() {

			@Override
			public void run() {
				RelativeLayout.LayoutParams layoutParams = (LayoutParams) imageProfile.getLayoutParams();
				int margin = (int) (getResources().getDimension(R.dimen.profile_photo_size) * 2 / 3);
				layoutParams.topMargin = imageCover.getHeight() - margin;

			}
		});

		textName = (TextView) findViewById(R.id.main_text_name);
	}

	private void proceed() {
		new UserLoader().execute();
		startLoaderService();
		startLoadingFirstPhoto();

		if (!CommonUtils.isInternetConnected(getApplicationContext())) {
			new AlertDialog.Builder(this).setTitle("Connection Error").setMessage("Please connect to the internet.")
					.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create().show();

		}
	}

	private void startLoadingFirstPhoto() {
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				ArrayList<FBPhoto> rankedPhotos = DBUtil.getRankedPhotos(getApplicationContext());
				if (rankedPhotos != null && rankedPhotos.size() > 0) {
					timer.cancel();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							new RankedPhotoLoader().execute();
						}
					});
				} else {
					AppLogger.log(TAG, "No photos in db");
				}
			}
		};

		timer.scheduleAtFixedRate(task, 0, 1000);
	}

	private void getOverflowMenu() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showNextImage() {
		if (rankedPhotos != null) {
			currentIndex = (currentIndex + 1) % rankedPhotos.size();
			ViewUtil.loadMainImage(imageMain, rankedPhotos.get(currentIndex).getImageUrl());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_logout:
			startFBLogin();
			break;
		case R.id.action_pinned:
			showPinnedPhotos();
			break;
		// case R.id.action_hidden:
		// showHiddenPhotos();
		// break;
		case R.id.action_about:
			startActivity(new Intent(getApplicationContext(), AboutActivity.class));
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTIVITY_REQUEST_CODE_FB_AUTH) {
			if (resultCode == Activity.RESULT_OK) {
				initUI();
				proceed();
			} else {
				if (!validateFBLogin()) {
					finish();
				}
			}
		}
	}

	/**
	 * validates if the user logged to fb using the app
	 * 
	 * @return
	 */
	private boolean validateFBLogin() {
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			return true;
		} else {
			Session sessionNew = Session.openActiveSession(this, false, statusCallback);

			if (sessionNew != null && sessionNew.isOpened()) {
				return true;
			}
		}

		return false;
	}

	private void startFBLogin() {
		startActivityForResult(new Intent(this, FBAuthActivity.class), ACTIVITY_REQUEST_CODE_FB_AUTH);
	}

	private void startPhotoViewerActivity() {
		ArrayList<FBPhoto> selectedPhotos = new ArrayList<FBPhoto>();
		if (rankedPhotos != null && rankedPhotos.size() > currentIndex) {
			selectedPhotos.add(rankedPhotos.get(currentIndex));
			CommonUtils.startPhotoViewerActivity(this, 0, selectedPhotos, false, true);
		} else {
			Toast.makeText(getApplicationContext(), "No photos found", Toast.LENGTH_LONG).show();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			AppLogger.log(TAG, "session update");
		}

	}

	private class RankedPhotoLoader extends AsyncTask<Void, Integer, ArrayList<FBPhoto>> {

		@Override
		protected ArrayList<FBPhoto> doInBackground(Void... params) {
			ArrayList<FBPhoto> rankedPhotos = DBUtil.getRankedPhotos(getApplicationContext());

			if (rankedPhotos != null && rankedPhotos.size() > 0) {
				String photoId1 = rankedPhotos.get(0).getPhotoId();

				HitsTable hitsTable = new HitsTable(getApplicationContext());
				hitsTable.hit(photoId1);
			}

			return rankedPhotos;
		}

		@Override
		protected void onPostExecute(ArrayList<FBPhoto> result) {
			super.onPostExecute(result);
			if (result != null) {
				rankedPhotos = new ArrayList<FBPhoto>(result);
				currentIndex = -1;
				showNextImage();
			} else {
				// startLoaderService();
			}
		}

	}

	private class CoverPhotoLoader extends AsyncTask<Void, Integer, String> {

		@Override
		protected String doInBackground(Void... params) {
			return FBUtil.getCoverUrl();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				ViewUtil.loadMainImage(imageCover, result);
			}
		}

	}

	private class ProfilePicLoader extends AsyncTask<Void, Integer, String> {

		@Override
		protected String doInBackground(Void... params) {
			return FBUtil.getProfilePicUrl();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				ViewUtil.loadMainImage(imageProfile, result);
			}
		}

	}

	private class UserLoader extends AsyncTask<Void, Integer, User> {

		@Override
		protected User doInBackground(Void... params) {
			return FBUtil.getUser();
		}

		@Override
		protected void onPostExecute(User result) {
			super.onPostExecute(result);
			if (result != null) {
				ViewUtil.loadMainImage(imageProfile, result.getProfileUrl());
				ViewUtil.loadMainImage(imageCover, result.getCoverUrl());
				textName.setText(result.getName());
			}
		}

	}

	private void showHiddenPhotos() {
		AsyncTask<Void, Void, ArrayList<FBPhoto>> task = new AsyncTask<Void, Void, ArrayList<FBPhoto>>() {

			@Override
			protected ArrayList<FBPhoto> doInBackground(Void... params) {
				ArrayList<FBPhoto> photos = DBUtil.getHiddenPhotos(getApplicationContext());
				return photos;
			}

			@Override
			protected void onPostExecute(ArrayList<FBPhoto> result) {
				super.onPostExecute(result);
				if (result != null && result.size() > 0 && activity != null) {
					CommonUtils.startPhotoViewerActivity(activity, 0, result, false, false);
				} else {
					Toast.makeText(getApplicationContext(), "No hidden photos found", Toast.LENGTH_LONG).show();
				}
			}

		};

		task.execute();
	}

	private void showPinnedPhotos() {
		AsyncTask<Void, Void, ArrayList<FBPhoto>> task = new AsyncTask<Void, Void, ArrayList<FBPhoto>>() {

			@Override
			protected ArrayList<FBPhoto> doInBackground(Void... params) {
				ArrayList<FBPhoto> photos = DBUtil.getPinnedPhotos(getApplicationContext());
				return photos;
			}

			@Override
			protected void onPostExecute(ArrayList<FBPhoto> result) {
				super.onPostExecute(result);
				if (result != null && result.size() > 0 && activity != null) {
					CommonUtils.startPhotoViewerActivity(activity, 0, result, false, false);
				} else {
					Toast.makeText(getApplicationContext(), "No pinned photos found", Toast.LENGTH_LONG).show();
				}
			}

		};

		task.execute();
	}

}
