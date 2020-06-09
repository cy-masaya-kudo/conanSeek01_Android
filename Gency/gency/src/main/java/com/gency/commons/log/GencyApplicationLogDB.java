package com.gency.commons.log;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <h3>ログをDBに保存する</h3>
 */
public class GencyApplicationLogDB {
    static final int DATABASE_VERSION           = 1;
    static final String DATABASE_NAME           = "lib_applog.db";
    static final String TABLE_APPLOG            = "applog";
    public static final String COLUMN__ID       = "_id";
    public static final String COLUMN_TIMESTAMP = "applog_timestamp";
    public static final String COLUMN_TAG       = "applog_tag";
    public static final String COLUMN_MESSAGE   = "applog_message";

    private static final String[] TABLE_TRANSACTIONS_COLUMNS = {
    	COLUMN__ID, 
    	COLUMN_TIMESTAMP, 
    	COLUMN_MESSAGE,
    	COLUMN_TAG, 
    };

    SQLiteDatabase mDb;
    private DatabaseHelper mDatabaseHelper;
	static Context mContext;

    /**
     * @param context Android context
     */
    public GencyApplicationLogDB(Context context) {
    	mContext = context;
        mDatabaseHelper = new DatabaseHelper(context);
        mDb = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * DBのクローズ
     */
    public void close() {
        mDatabaseHelper.close();
    }

    /**
     * ログの保存
     * @param appLog 保存したいデータ
     */
    public void insert(GencyApplicationLog appLog) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, appLog.getTimestamp());
    	values.put(COLUMN_TAG, appLog.getTag());
    	values.put(COLUMN_MESSAGE, appLog.getMessage());
        mDb.replace(TABLE_APPLOG, null /* nullColumnHack */, values);
    }

    /**
     * ログの取得
     * @return DBへ保存されているログ
     */
    public Cursor queryTransactions() {
        return mDb.query(TABLE_APPLOG, TABLE_TRANSACTIONS_COLUMNS, null,
                null, null, null, null);
    }

    /**
     * productidを指定してログを取得
     * @param productId 取得したいログのproductid
     * @return 指定されたproductidのログ
     */
    public Cursor queryTransactions(String productId) {
        return mDb.query(TABLE_APPLOG, TABLE_TRANSACTIONS_COLUMNS, COLUMN_TIMESTAMP + " = ?",
                new String[] {productId}, null, null, null);
    }
//
//    public Cursor queryTransactions(String productId, PurchaseState state) {
//        return mDb.query(TABLE_APPLOG, TABLE_TRANSACTIONS_COLUMNS, COLUMN_TIMESTAMP + " = ? AND " + COLUMN_MESSAGE + " = ?",
//                new String[] {productId, String.valueOf(state.ordinal())}, null, null, null);
//    }

    protected static final GencyApplicationLog createTransaction(Cursor cursor) {
    	final GencyApplicationLog appLog = new GencyApplicationLog(
    			cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
    			cursor.getString(cursor.getColumnIndex(COLUMN_TAG)), 
    			cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE))
			);
    	return appLog;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTransactionsTable(db);
        }

        private void createTransactionsTable(SQLiteDatabase db) {
            db.execSQL(
            		"CREATE TABLE " + TABLE_APPLOG + 
            		"(" +
	            		COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            		COLUMN_TIMESTAMP + " INTEGER, " +
	            		COLUMN_TAG + " TEXT, " +
	            		COLUMN_MESSAGE + " TEXT " +
    				")");
        }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

    /**
     * 保存している全てのログを取得
     * @return DBへ保存されている全てのログ
     */
	public List<GencyApplicationLog> getAllLogs() {
		List<GencyApplicationLog> resultArray = new ArrayList<GencyApplicationLog>();
		GencyApplicationLog tmpMap = null;
		Cursor cursor = queryTransactions();
		if (cursor.moveToFirst()) {  
			do {  
				tmpMap = createTransaction(cursor);
				resultArray.add(tmpMap);
			} while (cursor.moveToNext());  
		}  
		cursor.close(); 
		
		return resultArray;
	}
}
