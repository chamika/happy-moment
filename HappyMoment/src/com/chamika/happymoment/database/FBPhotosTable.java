package com.chamika.happymoment.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chamika.happymoment.model.FBPhoto;
import com.chamika.happymoment.utils.AppLogger;

public class FBPhotosTable extends SQLiteOpenHelper {

	private static final String TAG = "FBPhotosTable";

	private static final String DATABASE_NAME = "photos.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "photos";

	// Database table
	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_PHOTO_ID = "photo_id";
	public static final String COLUMN_CREATED_TIME = "created_time";
	public static final String COLUMN_THUMB = "url_thumb";
	public static final String COLUMN_ALBUM_ID = "album_id";
	public static final String COLUMN_URL_LARGE = "url_large";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_COMMENTS = "comments";
	public static final String COLUMN_LIKES = "likes";

	public static final String COLUMN_FOREIGN_PINNED = "pinned";

	private static final String SELECTION_IN = COLUMN_ID + " BETWEEN ? AND ? ";
	private static final String ORDER_BY_ID = COLUMN_ID + " ASC";

	public FBPhotosTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		// Database creation SQL statement
		// String DATABASE_CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " +
		// tableName + "(" + COLUMN_ID
		// + " integer primary key autoincrement, " + COLUMN_BODY + " text , " +
		// COLUMN_MESSAGE_ID
		// + " text not null," + COLUMN_AUTHOR + " text not null," + COLUMN_TIME
		// + " text not null" + ");";

		String DATABASE_CREATE_QUERY = getTableCreateQuery();

		database.execSQL(DATABASE_CREATE_QUERY);

	}

	private String getTableCreateQuery() {
		//	@formatter:off
		return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" 
				+ COLUMN_ID + " integer primary key, " 
				+ COLUMN_PHOTO_ID + " text unique," 
				+ COLUMN_CREATED_TIME + " INTEGER, " 
				+ COLUMN_THUMB + " text," 
				+ COLUMN_ALBUM_ID + " text," 
				+ COLUMN_URL_LARGE + " text," 
				+ COLUMN_LINK + " text," 
				+ COLUMN_LOCATION + " text," 
				+ COLUMN_COMMENTS + " INTEGER , " 
				+ COLUMN_LIKES + " INTEGER " 
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

	public Cursor getRankedPhotos(int commentsFactor, int likesFactor, int limit) {
		SQLiteDatabase db = this.getReadableDatabase();
		StringBuilder queryBuilder = new StringBuilder();
		String tableName = TABLE_NAME;
		queryBuilder.append("SELECT `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`comments`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`created_time`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_large`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`likes`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`album_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`link`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`location`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_thumb`, (");
		queryBuilder.append(commentsFactor);
		queryBuilder.append("* `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`comments` + ");
		queryBuilder.append(likesFactor);
		queryBuilder.append(" * `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`likes`) `score` ,");
		queryBuilder.append("`pinned`.`photo_id` `");
		queryBuilder.append(COLUMN_FOREIGN_PINNED);
		queryBuilder.append("`");
		queryBuilder.append("FROM `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ");
		queryBuilder.append("LEFT JOIN `hidden` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hidden`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `hits` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hits`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `pinned` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`pinned`.`photo_id` ");
		queryBuilder.append("WHERE `hidden`.`photo_id` IS NULL ");
		queryBuilder.append("ORDER BY `hits`.`hits` ASC, `score` DESC ");
		queryBuilder.append("LIMIT ");
		queryBuilder.append(limit);

		try {
			Cursor cursor = db.rawQuery(queryBuilder.toString(), null);

			if (cursor != null) {
				cursor.moveToFirst();
			}

			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cursor getAlbumPhotos(String albumId, String photoId) {
		SQLiteDatabase db = this.getReadableDatabase();
		StringBuilder queryBuilder = new StringBuilder();
		String tableName = TABLE_NAME;
		queryBuilder.append("SELECT `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`comments`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`created_time`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_large`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`likes`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`album_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`link`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`location`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_thumb`, ");
		queryBuilder.append("`pinned`.`photo_id` `");
		queryBuilder.append(COLUMN_FOREIGN_PINNED);
		queryBuilder.append("`");
		queryBuilder.append("FROM `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ");
		queryBuilder.append("LEFT JOIN `hidden` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hidden`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `hits` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hits`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `pinned` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`pinned`.`photo_id` ");
		queryBuilder
				.append("WHERE `hidden`.`photo_id` IS NULL AND `photos`.`album_id` = ? AND `photos`.`photo_id` IS NOT ? ");
		queryBuilder.append("ORDER BY `hits`.`hits` ASC ");

		try {
			Cursor cursor = db.rawQuery(queryBuilder.toString(), new String[] { albumId, photoId });

			if (cursor != null) {
				cursor.moveToFirst();
			}

			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cursor getHiddenPhotos() {
		SQLiteDatabase db = this.getReadableDatabase();
		StringBuilder queryBuilder = new StringBuilder();
		String tableName = TABLE_NAME;
		queryBuilder.append("SELECT `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`comments`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`created_time`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_large`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`likes`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`album_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`link`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`location`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_thumb`, ");
		queryBuilder.append("`pinned`.`photo_id` `");
		queryBuilder.append(COLUMN_FOREIGN_PINNED);
		queryBuilder.append("`");
		queryBuilder.append("FROM `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ");
		queryBuilder.append("LEFT JOIN `hidden` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hidden`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `hits` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hits`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `pinned` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`pinned`.`photo_id` ");
		queryBuilder.append("WHERE `hidden`.`photo_id` IS NOT NULL ");
		queryBuilder.append("ORDER BY `hidden`.`_id` ASC ");

		try {
			Cursor cursor = db.rawQuery(queryBuilder.toString(), null);

			if (cursor != null) {
				cursor.moveToFirst();
			}

			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Cursor getPinnedPhotos() {
		SQLiteDatabase db = this.getReadableDatabase();
		StringBuilder queryBuilder = new StringBuilder();
		String tableName = TABLE_NAME;
		queryBuilder.append("SELECT `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`comments`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`created_time`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_large`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`likes`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`album_id`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`link`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`location`, `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`url_thumb`, ");
		queryBuilder.append("`pinned`.`photo_id` `");
		queryBuilder.append(COLUMN_FOREIGN_PINNED);
		queryBuilder.append("`");
		queryBuilder.append("FROM `");
		queryBuilder.append(tableName);
		queryBuilder.append("` ");
		queryBuilder.append("LEFT JOIN `hidden` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hidden`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `hits` ");
		queryBuilder.append("ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`hits`.`photo_id` ");
		queryBuilder.append("LEFT JOIN `pinned` ON `");
		queryBuilder.append(tableName);
		queryBuilder.append("`.`photo_id`=`pinned`.`photo_id` ");
		queryBuilder.append("WHERE `pinned`.`photo_id` IS NOT NULL ");
		queryBuilder.append("ORDER BY `pinned`.`_id` ASC ");
		
		try {
			Cursor cursor = db.rawQuery(queryBuilder.toString(), null);
			
			if (cursor != null) {
				cursor.moveToFirst();
			}
			
			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void closeReadableDatabase() {
		this.getReadableDatabase().close();
	}

	public long getRowCount() {
		Cursor cursor = null;
		try {
			SQLiteDatabase db = getReadableDatabase();
			cursor = db.rawQuery("select count(`_id`) from `" + TABLE_NAME + "`", null);
			if (cursor != null) {
				cursor.moveToFirst();
			}

			return Long.parseLong(cursor.getString(0));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	public void addAll(ArrayList<FBPhoto> list) {
		SQLiteDatabase db = null;

		try {
			db = this.getWritableDatabase();
			if (db != null && list != null) {
				ContentValues values = new ContentValues();
				db.beginTransaction();

				for (FBPhoto photo : list) {
					// values.put(COLUMN_ID, startIndex++);
					values.put(COLUMN_PHOTO_ID, photo.getPhotoId());
					values.put(COLUMN_COMMENTS, photo.getComments());
					if (photo.getCreatedTime() != null) {
						values.put(COLUMN_CREATED_TIME, photo.getCreatedTime().getTime());
					}
					values.put(COLUMN_LIKES, photo.getLikes());
					values.put(COLUMN_LINK, photo.getLink());
					values.put(COLUMN_LOCATION, photo.getLocation());
					values.put(COLUMN_THUMB, photo.getThumbnailUrl());
					values.put(COLUMN_URL_LARGE, photo.getImageUrl());
					values.put(COLUMN_ALBUM_ID, photo.getAlbumId());

					db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
	// public void addAll(List<FBMessage> messages, long startIndex) {
	// SQLiteDatabase db = null;
	//
	// try {
	// db = this.getWritableDatabase();
	// if (db != null && messages != null) {
	// ContentValues values = new ContentValues();
	// db.beginTransaction();
	//
	// for (FBMessage msg : messages) {
	// values.put(COLUMN_ID, startIndex++);
	// values.put(COLUMN_PHOTO_ID, msg.getBody());
	// values.put(COLUMN_AUTHOR, msg.getAuthor());
	// values.put(COLUMN_CREATED_TIME, msg.getMessageId());
	// values.put(COLUMN_THUMB, msg.getFormattedTime());
	//
	// db.insertWithOnConflict(tableName, null, values,
	// SQLiteDatabase.CONFLICT_REPLACE);
	// values.clear();
	// }
	//
	// db.setTransactionSuccessful();
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (db != null && db.inTransaction()) {
	// db.endTransaction();
	// db.close();
	// }
	// }
	//
	// }
}
