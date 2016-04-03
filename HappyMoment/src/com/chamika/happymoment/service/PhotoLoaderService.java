package com.chamika.happymoment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import com.chamika.happymoment.database.FBAlbumsTable;
import com.chamika.happymoment.database.FBPhotosTable;
import com.chamika.happymoment.database.HiddenTable;
import com.chamika.happymoment.database.HitsTable;
import com.chamika.happymoment.database.PinnedTable;
import com.chamika.happymoment.database.TaggedDownloadsTable;
import com.chamika.happymoment.model.FBAlbum;
import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.model.TaggedDownload;
import com.chamika.happymoment.utils.AppLogger;
import com.chamika.happymoment.utils.CommonUtils;
import com.chamika.happymoment.utils.DBUtil;
import com.chamika.happymoment.utils.FBUtil;
import com.facebook.Response;
import com.google.android.gms.drive.internal.SetDrivePreferencesRequest;

public class PhotoLoaderService extends Service {

	private static final String TAG = PhotoLoaderService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);

		initTables();

		Thread t = new Thread() {
			public void run() {
				loadAlbums();
				loadRemainingAlbums();
				loadTaggedPhotos();

			};
		};
		t.start();

		return Service.START_STICKY;
	}

	private void initTables() {
		new FBPhotosTable(getApplicationContext()).createTableIfNotExist();
		new FBAlbumsTable(getApplicationContext()).createTableIfNotExist();
		new HitsTable(getApplicationContext()).createTableIfNotExist();
		new HiddenTable(getApplicationContext()).createTableIfNotExist();
		new TaggedDownloadsTable(getApplicationContext()).createTableIfNotExist();
		new PinnedTable(getApplicationContext()).createTableIfNotExist();
	}

	private void loadFirstPhotos() {
		final Thread t = new Thread() {
			public void run() {
				final Response response = FBUtil.getRequest(FBUtil.TAGGED_PHOTOS_API_CALL, FBUtil.PHOTOS_FIELDS);
				// Response response = FBUtil.getRequest("/me/photos");

				handleTaggedPhotos(response);

				AppLogger.log(TAG, "response: " + response.getRawResponse());

			};
		};
		t.start();

	}

	private void loadTaggedPhotos() {

		TaggedDownloadsTable table = new TaggedDownloadsTable(getApplicationContext());
		ArrayList<TaggedDownload> remaining = table.getRemaining();

		if (remaining != null) {
			for (TaggedDownload download : remaining) {
				int count = 0;

				AppLogger.log(TAG, "Downloading " + download.toString());

				Map<String, String> params = FBUtil.getTaggedPhotoParams(download);
				boolean failed = false;
				String afterId = null;
				do {
					if (afterId != null) {
						params.put(FBUtil.PARAM_AFTER, afterId);
					}
					if (!CommonUtils.isInternetConnected(getApplicationContext())) {
						failed = true;
						break;
					}

					final Response response = FBUtil.getRequest(FBUtil.TAGGED_PHOTOS_API_CALL, params);
					count += handleTaggedPhotos(response);
					afterId = FBUtil.getNext(response);
				} while (afterId != null);

				if (download.getUntil() != 0 && !failed) {
					table.setDownloaded(download.getId(), true);
				}

				AppLogger.log(TAG, "Downloading  Finished size:" + count + " Dowload:" + download.toString());
			}
		}

	}

	private int handleTaggedPhotos(final Response response) {
		if (response != null && response.getError() == null) {
			try {
				final JSONObject json = response.getGraphObject().getInnerJSONObject();
				final JSONArray photosJson = json.getJSONArray("data");

				final ArrayList<FBPhoto> fbPhotos = new ArrayList<FBPhoto>();

				for (int i = 0; i < photosJson.length(); i++) {
					final JSONObject photoJson = photosJson.getJSONObject(i);
					final FBPhoto fbPhoto = FBUtil.convertTaggedPhotoJson(photoJson);

					if (fbPhoto != null) {
						fbPhotos.add(fbPhoto);
					}

				}

				if (fbPhotos.size() > 0) {
					final FBPhotosTable dbTable = new FBPhotosTable(getApplicationContext());
					dbTable.createTableIfNotExist();

					dbTable.addAll(fbPhotos);
					return fbPhotos.size();
				}

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	private void loadRemainingAlbums() {
		ArrayList<FBAlbum> albums = DBUtil.loadRemainingAlbumIds(getApplicationContext());

		if (albums != null) {
			for (FBAlbum album : albums) {
				String albumId = album.getAlbumId();
				int count = loadAlbumPhotos(albumId);
				if (count >= album.getCount() * 0.9) {
					FBAlbumsTable table = new FBAlbumsTable(getApplicationContext());
					table.setDownloaded(albumId, true);
				}
			}
		}
	}

	/**
	 * should be called in a background thread. Load all the photos of a given
	 * album
	 * 
	 * @param albumId
	 */
	private int loadAlbumPhotos(String albumId) {
		final Response firstResponse = FBUtil.getRequest(FBUtil.getAlbumPhotosUrl(albumId), FBUtil.PHOTOS_FIELDS);

		int count = 0;

		if (firstResponse != null && firstResponse.getError() == null) {
			try {
				count += handleAlbumPhotosResponse(albumId, firstResponse);

				String afterId = FBUtil.getNext(firstResponse);
				do {
					Map<String, String> params = new HashMap<String, String>();
					params.put(FBUtil.PARAM_FIELDS, FBUtil.PHOTOS_FIELDS);
					params.put(FBUtil.PARAM_AFTER, afterId);
					final Response response = FBUtil.getRequest(FBUtil.getAlbumPhotosUrl(albumId), params);
					count += handleAlbumPhotosResponse(albumId, response);
					afterId = FBUtil.getNext(response);
				} while (afterId != null);

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		AppLogger.log(TAG, "response: " + firstResponse.getRawResponse());

		return count;
	}

	private int handleAlbumPhotosResponse(String albumId, final Response firstResponse) throws JSONException {
		final JSONObject json = firstResponse.getGraphObject().getInnerJSONObject();
		final JSONArray photosJson = json.getJSONArray("data");

		final ArrayList<FBPhoto> fbPhotos = new ArrayList<FBPhoto>();

		for (int i = 0; i < photosJson.length(); i++) {
			final JSONObject photoJson = photosJson.getJSONObject(i);
			final FBPhoto fbPhoto = FBUtil.convertTaggedPhotoJson(photoJson);
			fbPhoto.setAlbumId(albumId);

			if (fbPhoto != null) {
				fbPhotos.add(fbPhoto);
			}

		}

		if (fbPhotos.size() > 0) {
			final FBPhotosTable dbTable = new FBPhotosTable(getApplicationContext());
			dbTable.createTableIfNotExist();

			dbTable.addAll(fbPhotos);
			AppLogger.log(TAG, "album:" + albumId + " feteched " + fbPhotos.size() + " photos");
			return fbPhotos.size();
		}
		return 0;
	}

	private void loadAlbums() {
		final Response firstResponse = FBUtil.getRequest(FBUtil.ALBUMS_API_CALL, FBUtil.ALBUMS_FIELDS);

		if (firstResponse != null && firstResponse.getError() == null) {
			handleAlbumRespnose(firstResponse);

			String afterId = FBUtil.getNext(firstResponse);
			do {
				Map<String, String> params = new HashMap<String, String>();
				params.put(FBUtil.PARAM_FIELDS, FBUtil.ALBUMS_FIELDS);
				params.put(FBUtil.PARAM_AFTER, afterId);
				final Response response = FBUtil.getRequest(FBUtil.ALBUMS_API_CALL, params);
				handleAlbumRespnose(response);
				afterId = FBUtil.getNext(response);
			} while (afterId != null);

		}

	}

	private void handleAlbumRespnose(final Response response) {
		try {
			final JSONObject json = response.getGraphObject().getInnerJSONObject();
			final JSONArray albumsJson = json.getJSONArray("data");

			final ArrayList<FBAlbum> albums = new ArrayList<FBAlbum>();

			for (int i = 0; i < albumsJson.length(); i++) {
				final JSONObject photoJson = albumsJson.getJSONObject(i);
				final FBAlbum fbAlbum = FBUtil.convertAlbumJson(photoJson);

				if (fbAlbum != null) {
					albums.add(fbAlbum);
				}

			}

			if (albums.size() > 0) {
				final FBAlbumsTable dbTable = new FBAlbumsTable(getApplicationContext());
				dbTable.createTableIfNotExist();
				dbTable.addAll(albums);
				AppLogger.log(TAG, "fetched " + albums.size() + "albums");
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
