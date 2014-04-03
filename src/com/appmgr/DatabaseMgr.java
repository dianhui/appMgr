package com.appmgr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/*package*/ class DatabaseMgr {
	private static final String TAG = "DatabaseMgr";
	private DatabaseHelper mDbHelper = null;
	
	public interface WhiteListColumn {
		public static final String FIELD_ID = "_id";
		public static final String FIELD_PACKAGE_NAME = "package_name";
	}
	
	public interface HibernateListColumn {
		public static final String FIELD_ID = "_id";
		public static final String FIELD_PACKAGE_NAME = "package_name";
	}
	
	public DatabaseMgr(Context ctx) {
		mDbHelper = new DatabaseHelper(ctx);
	}
	
	public void closeDbHelper() {
		Log.d(TAG, "Close opened database.");
		mDbHelper.getWritableDatabase().close();
	}
	
	/*package*/ long insertAppToWhiteList(String packName) {
		if (TextUtils.isEmpty(packName)) {
			Log.d(TAG, "Input package name is empty.");
	        return -1;
        }
		
		ContentValues values = new ContentValues();
		values.put(WhiteListColumn.FIELD_PACKAGE_NAME, packName);
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.insert(TABLE_WHITE_LIST, WhiteListColumn.FIELD_PACKAGE_NAME, values);
	}

	/*package*/ int deleteAppFromWhiteList(String packName) {
		if (TextUtils.isEmpty(packName)) {
			Log.d(TAG, "Input package name is empty.");
	        return 0;
        }
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String where = String.format("%s='%s'",
		        WhiteListColumn.FIELD_PACKAGE_NAME, packName);
		return db.delete(TABLE_WHITE_LIST, where, null);
	}
	
	/* package */Cursor queryWhiteList(String[] projection, String selection,
	        String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String orderBy = TextUtils.isEmpty(sortOrder) ? "_id DESC" : sortOrder;
		return db.query(TABLE_WHITE_LIST, projection, selection, selectionArgs, null,
		        null, orderBy);
	}
	
	/*package*/ long insertAppToHibernateList(String packName) {
		if (TextUtils.isEmpty(packName)) {
			Log.d(TAG, "Input package name is empty.");
	        return -1;
        }
		
		ContentValues values = new ContentValues();
		values.put(HibernateListColumn.FIELD_PACKAGE_NAME, packName);
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.insert(TABLE_HIBERNATE_LIST, HibernateListColumn.FIELD_PACKAGE_NAME, values);
	}
	
	/*package*/ int deleteAppFromHibernateList(String packName) {
		if (TextUtils.isEmpty(packName)) {
			Log.d(TAG, "Input package name is empty.");
	        return 0;
        }
		
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		String where = String.format("%s='%s'",
				HibernateListColumn.FIELD_PACKAGE_NAME, packName);
		return db.delete(TABLE_HIBERNATE_LIST, where, null);
	}
	
	/* package */Cursor queryHibernateList(String[] projection, String selection,
	        String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String orderBy = TextUtils.isEmpty(sortOrder) ? "_id DESC" : sortOrder;
		return db.query(TABLE_HIBERNATE_LIST, projection, selection, selectionArgs, null,
		        null, orderBy);
	}
	
	private static final int DATA_BASE_VERSION = 1;
	private static final String DATA_BASE_NAME = "killapp.db";
	private static final String TABLE_WHITE_LIST = "white_list";
	private static final String TABLE_HIBERNATE_LIST = "hibernate_list";
	/*package*/class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
	        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        }

		@Override
        public void onCreate(SQLiteDatabase db) {
        	String sqlCreateWhiteListTable = "create table if not exists " + TABLE_WHITE_LIST
			        + " (" + WhiteListColumn.FIELD_ID
			        + " integer primary key autoincrement, "
			        + WhiteListColumn.FIELD_PACKAGE_NAME + " text UNIQUE"
			        + "); ";
        	db.execSQL(sqlCreateWhiteListTable);
        	
        	String sqlCreateHibernateListTable = "create table if not exists " + TABLE_HIBERNATE_LIST
			        + " (" + HibernateListColumn.FIELD_ID
			        + " integer primary key autoincrement, "
			        + HibernateListColumn.FIELD_PACKAGE_NAME + " text UNIQUE"
			        + "); ";
        	db.execSQL(sqlCreateHibernateListTable);
        }

		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, String.format("upgrade database:  ? -> ?"
		            , oldVersion, newVersion));
			
			String sqlDropMsgTable = "drop table if exists " + TABLE_WHITE_LIST;
		    db.execSQL(sqlDropMsgTable);
        }
	}
}