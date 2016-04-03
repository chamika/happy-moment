package com.chamika.happymoment.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.model.TaggedDownload;
import com.chamika.happymoment.utils.AppLogger;
import com.google.android.gms.ads.doubleclick.c;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;

public class TaggedDownloadsTable extends SQLiteOpenHelper {

	private static final String TAG = "TaggedDownloadsTable";

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	// Database table
	public static final String TABLE_NAME = "tagged_downloads";
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_SINCE = "since";
	public static final String COLUMN_UNTIL = "until";
	public static final String COLUMN_DOWNLOADED = "downloaded";

	public TaggedDownloadsTable(Context context) {
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
				+ COLUMN_SINCE + " text unique, " 
				+ COLUMN_UNTIL + " text unique, " 
				+ COLUMN_DOWNLOADED + " integer " 
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

			insertInitialValues();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}

	}

	public void closeReadableDatabase() {
		this.getReadableDatabase().close();
	}

	public void addAll(ArrayList<TaggedDownload> list) {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			if (db != null && list != null) {
				ContentValues values = new ContentValues();
				db.beginTransaction();

				for (TaggedDownload download : list) {
					// values.put(COLUMN_ID, startIndex++);
					values.put(COLUMN_SINCE, download.getSince());
					values.put(COLUMN_UNTIL, download.getUntil());
					values.put(COLUMN_DOWNLOADED, download.isDownloaded());

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

	public ArrayList<TaggedDownload> getRemaining() {
		try {
			SQLiteDatabase db = getReadableDatabase();
			String sql = "select * from `tagged_downloads` where `tagged_downloads`.`downloaded` = 0 order by `tagged_downloads`.`since`";
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor != null) {
				ArrayList<TaggedDownload> list = new ArrayList<TaggedDownload>();

				if (cursor.getCount() > 0) {
					cursor.moveToFirst();

					int indexId = cursor.getColumnIndex(COLUMN_ID);
					int indexSince = cursor.getColumnIndex(COLUMN_SINCE);
					int indexUntil = cursor.getColumnIndex(COLUMN_UNTIL);

					do {
						TaggedDownload download = new TaggedDownload();

						download.setId(cursor.getString(indexId));
						download.setSince(cursor.getLong(indexSince));
						download.setUntil(cursor.getLong(indexUntil));

						list.add(download);
					} while (cursor.moveToNext());

					return list;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean setDownloaded(String id, boolean value) {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			if (db != null) {
				ContentValues values = new ContentValues();
				db.beginTransaction();

				values.put(COLUMN_DOWNLOADED, value);

				db.updateWithOnConflict(TABLE_NAME, values, COLUMN_ID + "= ? ", new String[] { id },
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

	public void insertInitialValues() {
		ArrayList<TaggedDownload> list = new ArrayList<TaggedDownload>();
		list.add(new TaggedDownload("1", 0, 1293840000L, false));// <-2011
		list.add(new TaggedDownload("2", 1262304000L, 1325376000L, false));// 2011-2012
		list.add(new TaggedDownload("3", 1325376000L, 1356998400L, false));// 2012-2013
		list.add(new TaggedDownload("4", 1356998400L, 1388534400L, false));// 2013-2014
		list.add(new TaggedDownload("5", 1388534400L, 1420070400L, false));// 2014-2015
		list.add(new TaggedDownload("6", 1420070400L, 0L, false));// 2015->

		addAll(list);
	}
}
