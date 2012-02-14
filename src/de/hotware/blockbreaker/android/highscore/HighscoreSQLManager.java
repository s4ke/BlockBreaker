package de.hotware.blockbreaker.android.highscore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighscoreSQLManager extends SQLiteOpenHelper {
	
	public static final String TABLE_HIGHSCORES = "highscores";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_SCORE = "score";

	private static final String DATABASE_NAME = "highscores.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_HIGHSCORES + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_NAME
			+ " text not null, " + COLUMN_SCORE + " int " +
			");";

	public HighscoreSQLManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase pDb) {
		pDb.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase pDb, int pOldVersion, int pNewVersion) {
		pDb.execSQL("DROP TABLE IF EXISTS" + TABLE_HIGHSCORES);
		this.onCreate(pDb);
	}

	public void removeEntry(long pId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_HIGHSCORES, COLUMN_ID+"=?", new String [] {String.valueOf(pId)});
		db.close();
	}
	
	public void createEntry(long pId, String name, int score) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_ID, pId);
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_SCORE, score);
		db.insert(TABLE_HIGHSCORES, COLUMN_ID, cv);
	}
	
	public int updateEntry(long pId, String name, int score) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, name);
		cv.put(COLUMN_SCORE, score);
		return db.update(TABLE_HIGHSCORES, cv, COLUMN_ID+"=?", new String []{String.valueOf(pId)}); 
	}
	
	public boolean idExists(long pId) {
		SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cur = db.rawQuery("SELECT "+ COLUMN_ID + " FROM "
	    		+ TABLE_HIGHSCORES + " WHERE "
	    		+ COLUMN_ID + "=" +  String.valueOf(pId), null);
		return cur.getCount() == 1;
	}
	
	public Cursor allEntries() {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery("SELECT * FROM " + TABLE_HIGHSCORES, null);
	}
	
}
