package de.hotware.blockbreaker.android.highscore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighscoreSQLManager extends SQLiteOpenHelper {
	
	public static final String TABLE_HIGHSCORES = "highscores";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_SEED = "seed";
	public static final String COLUMN_TURNS = "turns";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_SCORE = "score";

	private static final String DATABASE_NAME = "highscores.db";
	private static final int DATABASE_VERSION = 1;
	
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_HIGHSCORES + "( " 
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_SEED + " integer, "
			+ COLUMN_TURNS + " integer "
			+ COLUMN_NAME	+ " text not null, " 
			+ COLUMN_SCORE + " integer "
			+ ");";

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
	
	public void createEntry(long pSeed, int pTurns, String pName, int pScore) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_SEED, pSeed);
		cv.put(COLUMN_TURNS, pTurns);
		cv.put(COLUMN_NAME, pName);
		cv.put(COLUMN_SCORE, pScore);
		db.insert(TABLE_HIGHSCORES, COLUMN_ID, cv);
	}
	
	public int updateEntry(long pSeed, int pTurns, String pName, int pScore) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_NAME, pName);
		cv.put(COLUMN_SCORE, pScore);
		return db.update(TABLE_HIGHSCORES, cv, COLUMN_SEED + "=?" + "AND" + COLUMN_TURNS + "=?", new String []{String.valueOf(pSeed), String.valueOf(pTurns)}); 
	}
	
	public boolean highScoreExists(long pSeed, int pTurns) {
		SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cur = db.rawQuery("SELECT "+ COLUMN_ID + " FROM "
	    		+ TABLE_HIGHSCORES + " WHERE "
	    		+ COLUMN_SEED + "=" +  String.valueOf(pSeed)
	    		+ COLUMN_TURNS + "=" + String.valueOf(pTurns), null);
		return cur.getCount() == 1;
	}
	
	public Cursor allEntries() {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery("SELECT * FROM " + TABLE_HIGHSCORES, null);
	}
	
}
