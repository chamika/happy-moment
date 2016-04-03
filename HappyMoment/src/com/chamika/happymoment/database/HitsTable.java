package com.chamika.happymoment.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chamika.happymoment.utils.AppLogger;

public class HitsTable extends SQLiteOpenHelper {

	private static final String TAG = "HitsTable";

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	// Database table
	public static final String TABLE_NAME = "hits";
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_PHOTO_ID = "photo_id";
	public static final String COLUMN_HITS = "hits";

	public HitsTable(Context context) {
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
				+ COLUMN_PHOTO_ID + " text unique," 
				+ COLUMN_HITS + " INTEGER " 
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

	public boolean hit(String photoId) {
		return hit(photoId, 1);
	}

	/**
	 * increase hit count by given value
	 * 
	 * @param photoId
	 * @param hitCount
	 *            value to be added to the hits
	 */
	public boolean hit(String photoId, int hitCount) {
		SQLiteDatabase db = this.getWritableDatabase();

		StringBuilder updateQueryBuilder = new StringBuilder();
		updateQueryBuilder.append("UPDATE `hits` SET `hits` = `hits` + 1 WHERE `photo_id` = ?");

		try {

			ContentValues cv = new ContentValues();
			cv.put(COLUMN_PHOTO_ID, photoId);
			cv.put(COLUMN_HITS, 0);

			db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
			db.execSQL(updateQueryBuilder.toString(), new String[] { photoId });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
