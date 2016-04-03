package com.chamika.happymoment.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chamika.happymoment.utils.AppLogger;

public class PinnedTable extends SQLiteOpenHelper {

	private static final String TAG = "PinnedTable";

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	// Database table
	public static final String TABLE_NAME = "pinned";
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_PHOTO_ID = "photo_id";

	public PinnedTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String DATABASE_CREATE_QUERY = getTableCreateQuery();
		database.execSQL(DATABASE_CREATE_QUERY);

	}

	private String getTableCreateQuery() {
		//	@formatter:off
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" 
				+ COLUMN_ID + " integer primary key, " 
				+ COLUMN_PHOTO_ID + " text unique" 
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

	public boolean pinPhoto(String photoId) {
		SQLiteDatabase db = this.getWritableDatabase();

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("INSERT OR IGNORE INTO `pinned` (`photo_id`) values(?)");

		try {
			db.execSQL(queryBuilder.toString(), new String[] { photoId });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean removeFromPinned(String photoId) {
		SQLiteDatabase db = this.getWritableDatabase();

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("DELETE FROM `pinned` WHERE `photo_id` = ?");

		try {
			db.execSQL(queryBuilder.toString(), new String[] { photoId });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void closeReadableDatabase() {
		this.getReadableDatabase().close();
	}

}
