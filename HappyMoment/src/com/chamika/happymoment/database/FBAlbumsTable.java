package com.chamika.happymoment.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chamika.happymoment.model.FBAlbum;
import com.chamika.happymoment.utils.AppLogger;

public class FBAlbumsTable extends SQLiteOpenHelper {

	private static final String TAG = "FBPhotosTable";

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_NAME = "albums";

	// Database table
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_ALBUM_ID = "album_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_CREATED_TIME = "created_time";
	public static final String COLUMN_UPDATED_TIME = "updated_time";
	public static final String COLUMN_COMMENTS = "comments";
	public static final String COLUMN_LIKES = "likes";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_COUNT = "count";
	public static final String COLUMN_DOWNLOADED = "downloaded";// 0=false

	public FBAlbumsTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE_QUERY = getTableCreateQuery();
		database.execSQL(DATABASE_CREATE_QUERY);
	}

	private String getTableCreateQuery() {
		// @formatter:off
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" 
				+ COLUMN_ID + " integer primary key, "
				+ COLUMN_ALBUM_ID + " text unique," 
				+ COLUMN_CREATED_TIME + " INTEGER, " 
				+ COLUMN_UPDATED_TIME + " INTEGER, " 
				+ COLUMN_NAME + " text," 
				+ COLUMN_COMMENTS + " INTEGER , " 
				+ COLUMN_LIKES + " INTEGER, "
				+ COLUMN_COUNT + " INTEGER, " 
				+ COLUMN_LINK + " text, " 
				+ COLUMN_DOWNLOADED + " INTEGER" 
				+ ");";
		// @formatter:on

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		AppLogger.log(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

	public void createTableIfNotExist() {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			db.execSQL(getTableCreateQuery());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}

	}

	public void closeReadableDatabase() {
		this.getReadableDatabase().close();
	}

	public Cursor getRemainingAlbums() {
		try {
			SQLiteDatabase db = getReadableDatabase();
			String sql = "select `albums`.`album_id`,`albums`.`count` from `albums` where `albums`.`downloaded` = 0 order by `albums`.`created_time` ASC";
			Cursor cursor = db.rawQuery(sql, null);
			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean setDownloaded(String albumId, boolean value) {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			if (db != null) {
				ContentValues values = new ContentValues();
				db.beginTransaction();

				values.put(COLUMN_DOWNLOADED, value);

				db.updateWithOnConflict(TABLE_NAME, values, COLUMN_ALBUM_ID + "= ? ", new String[] { albumId },
						SQLiteDatabase.CONFLICT_IGNORE);
				values.clear();

				db.setTransactionSuccessful();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.inTransaction()) {
				db.endTransaction();
				db.close();
			}
		}

		return false;
	}

	public void addAll(ArrayList<FBAlbum> list) {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			if (db != null && list != null) {
				ContentValues values = new ContentValues();
				db.beginTransaction();

				for (FBAlbum album : list) {
					values.put(COLUMN_ALBUM_ID, album.getAlbumId());
					values.put(COLUMN_COMMENTS, album.getComments());
					if (album.getCreatedTime() != null) {
						values.put(COLUMN_CREATED_TIME, album.getCreatedTime().getTime());
					}
					if (album.getCreatedTime() != null) {
						values.put(COLUMN_UPDATED_TIME, album.getUpdatedTime().getTime());
					}
					values.put(COLUMN_LIKES, album.getLikes());
					values.put(COLUMN_NAME, album.getName());
					values.put(COLUMN_LINK, album.getLink());
					values.put(COLUMN_COUNT, album.getCount());
					values.put(COLUMN_DOWNLOADED, album.isDownloaded());

					db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
					values.clear();
				}

				db.setTransactionSuccessful();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.inTransaction()) {
				db.endTransaction();
				db.close();
			}
		}
	}
}
