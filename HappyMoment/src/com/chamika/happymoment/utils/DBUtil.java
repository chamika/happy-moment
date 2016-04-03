package com.chamika.happymoment.utils;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;

import com.chamika.happymoment.database.FBAlbumsTable;
import com.chamika.happymoment.database.FBPhotosTable;
import com.chamika.happymoment.model.FBAlbum;
import com.chamika.happymoment.model.FBPhoto;

public class DBUtil {

	private static final String TAG = DBUtil.class.getSimpleName();

	public static ArrayList<FBPhoto> getRankedPhotos(Context applicationContext) {
		final FBPhotosTable dbTable = new FBPhotosTable(applicationContext);
		dbTable.createTableIfNotExist();
		// Cursor cursor = dbTable.getRankedPhotos();
		Cursor cursor = dbTable.getRankedPhotos(1, 1, 20);

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		} else {
			ArrayList<FBPhoto> photos = createPhotos(cursor);
			return photos;
		}
	}

	private static ArrayList<FBPhoto> createPhotos(Cursor cursor) {
		ArrayList<FBPhoto> photos = new ArrayList<FBPhoto>();
		int indexPhotoId = cursor.getColumnIndex(FBPhotosTable.COLUMN_PHOTO_ID);
		int indexComments = cursor.getColumnIndex(FBPhotosTable.COLUMN_COMMENTS);
		int indexTime = cursor.getColumnIndex(FBPhotosTable.COLUMN_CREATED_TIME);
		int indexImage = cursor.getColumnIndex(FBPhotosTable.COLUMN_URL_LARGE);
		int indexLikes = cursor.getColumnIndex(FBPhotosTable.COLUMN_LIKES);
		int indexLink = cursor.getColumnIndex(FBPhotosTable.COLUMN_LINK);
		int indexLocation = cursor.getColumnIndex(FBPhotosTable.COLUMN_LOCATION);
		int indexThumbnail = cursor.getColumnIndex(FBPhotosTable.COLUMN_THUMB);
		int indexAlbumId = cursor.getColumnIndex(FBPhotosTable.COLUMN_ALBUM_ID);
		int indexPinned = cursor.getColumnIndex(FBPhotosTable.COLUMN_FOREIGN_PINNED);

		do {
			FBPhoto photo = new FBPhoto();

			photo.setPhotoId(cursor.getString(indexPhotoId));
			photo.setComments(cursor.getInt(indexComments));
			photo.setImageUrl(cursor.getString(indexImage));
			photo.setLikes(cursor.getInt(indexLikes));
			photo.setLink(cursor.getString(indexLink));
			photo.setLocation(cursor.getString(indexLocation));
			photo.setThumbnailUrl(cursor.getString(indexThumbnail));
			try {
				photo.setAlbumId(cursor.getString(indexAlbumId));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Long.parseLong(cursor.getString(indexTime)));
				photo.setCreatedTime(cal.getTime());
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String pinnedStr = cursor.getString(indexPinned);
				if (pinnedStr != null && pinnedStr.length() > 0) {
					photo.setPinned(true);
				} else {
					photo.setPinned(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				photo.setPinned(false);
			}

			// String authorId = cursor.getString(1);
			// String body = cursor.getString(2);

			photos.add(photo);

		} while (cursor.moveToNext());

		cursor.close();

		return photos;
	}

	public static ArrayList<FBPhoto> getSimilarPhotos(Context applicationContext, FBPhoto source) {

		if (source == null || source.getAlbumId() == null) {
			return null;
		} else {

			final FBPhotosTable dbTable = new FBPhotosTable(applicationContext);
			dbTable.createTableIfNotExist();
			// Cursor cursor = dbTable.getRankedPhotos();
			Cursor cursor = dbTable.getAlbumPhotos(source.getAlbumId(), source.getPhotoId());

			if (cursor == null || cursor.getCount() == 0) {
				return null;
			} else {
				ArrayList<FBPhoto> photos = createPhotos(cursor);
				return photos;
			}
		}
	}

	public static ArrayList<FBPhoto> getHiddenPhotos(Context applicationContext) {

		final FBPhotosTable dbTable = new FBPhotosTable(applicationContext);
		// Cursor cursor = dbTable.getRankedPhotos();
		Cursor cursor = dbTable.getHiddenPhotos();

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		} else {
			ArrayList<FBPhoto> photos = createPhotos(cursor);
			return photos;
		}
	}
	
	public static ArrayList<FBPhoto> getPinnedPhotos(Context applicationContext) {
		
		final FBPhotosTable dbTable = new FBPhotosTable(applicationContext);
		// Cursor cursor = dbTable.getRankedPhotos();
		Cursor cursor = dbTable.getPinnedPhotos();
		
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		} else {
			ArrayList<FBPhoto> photos = createPhotos(cursor);
			return photos;
		}
	}

	public static long getPhotosCount(Context context) {
		final FBPhotosTable dbTable = new FBPhotosTable(context);
		dbTable.createTableIfNotExist();
		return dbTable.getRowCount();
	}

	/**
	 * loads all the photos of each albums. should be called after loadAlbums()
	 * is called. return album ids to download
	 */
	public static ArrayList<FBAlbum> loadRemainingAlbumIds(Context context) {
		FBAlbumsTable table = new FBAlbumsTable(context);
		Cursor cursor = table.getRemainingAlbums();

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		} else {
			ArrayList<FBAlbum> ids = new ArrayList<FBAlbum>();

			cursor.moveToFirst();

			int indexAlbumId = cursor.getColumnIndex(FBAlbumsTable.COLUMN_ALBUM_ID);
			int indexCount = cursor.getColumnIndex(FBAlbumsTable.COLUMN_COUNT);

			do {
				try {
					FBAlbum album = new FBAlbum();
					album.setAlbumId(cursor.getString(indexAlbumId));
					album.setCount(cursor.getInt(indexCount));
					ids.add(album);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} while (cursor.moveToNext());

			return ids;

		}
	}
}
