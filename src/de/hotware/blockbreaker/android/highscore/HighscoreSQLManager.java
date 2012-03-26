package de.hotware.blockbreaker.android.highscore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighscoreSQLManager extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "highscores.db";
	private static final int DATABASE_VERSION = 1;
	
	
	private static final String CREATE_NAMES_TABLE = "create table names ( " +
			"id integer primary key autoincrement, " +
			"name text not null);";
	
	private static final String CREATE_TIME_ATTACK_SCORES_TABLE = "create table time_attack_scores( " +
			 "id integer primary key autoincrement, " +
			 "name_fk integer, " +
			 "number_of_wins integer not null, " +
			 "number_of_losses integer not null, " +
			 "score integer not null, " +
			 "foreign key(name_fk) references names(id));";
	
	private static final String TIME_ATTACK_SQL = "select tas.number_of_wins, tas.number_of, tas.score, n.name " +
			"from time_attack_scores tas " +
			"left outer join names n on tas.name_fk = n.id " +
			"order by tas.score desc;";

	public HighscoreSQLManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase pDb) {
		pDb.execSQL(CREATE_NAMES_TABLE);
		pDb.execSQL(CREATE_TIME_ATTACK_SCORES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase pDb, int pOldVersion, int pNewVersion) {
		pDb.execSQL("drop table if exists time_attack_scores");
		pDb.execSQL("drop table if exists names");
		this.onCreate(pDb);
	}
	
	public void createTimeAttackEntry(String pName,
			int pNumberOfWins,
			int pNumberOfLosses,
			int pScore) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
	}
	
	public Cursor getTimeAttackOrdered() {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery(TIME_ATTACK_SQL, null);
	}
	
}
