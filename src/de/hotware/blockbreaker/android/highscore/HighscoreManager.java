package de.hotware.blockbreaker.android.highscore;

import de.hotware.blockbreaker.model.gamehandler.IHighscoreManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighscoreManager extends SQLiteOpenHelper implements IHighscoreManager {

	private static final String DATABASE_NAME = "highscores.db";
	private static final int DATABASE_VERSION = 2;
	
	private static final String CREATE_NAMES_TABLE_SQL = "create table names ( " +
			"id integer primary key autoincrement, " +
			"name text unique not null);";
	
	private static final String CREATE_TIME_ATTACK_SCORES_TABLE_SQL = 
			 "create table time_attack_scores( " +
			 "id integer primary key autoincrement, " +
			 "name_fk integer, " +
			 "number_of_wins integer not null, " +
			 "number_of_losses integer not null, " +
			 "score integer not null, " +
			 "foreign key(name_fk) references names(id));";
	
	private static final String QUERY_TIME_ATTACK_ORDERED_SQL = "select tas.number_of_wins, tas.number_of, " +
			"tas.score, n.name " +
			"from time_attack_scores tas " +
			"left outer join names n on tas.name_fk = n.id " +
			"order by tas.score desc;";
	
	private static final String INSERT_TIME_ATTACK_SQL = "insert into time_attack_scores " +
			"(name_fk, number_of_wins, number_of_losses, score) values ((select id from names " +
			"where name = '%s'), %d, %d, %d);"; 

	public HighscoreManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase pDb) {
		pDb.execSQL(CREATE_NAMES_TABLE_SQL);
		pDb.execSQL(CREATE_TIME_ATTACK_SCORES_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase pDb, int pOldVersion, int pNewVersion) {
		pDb.execSQL("drop table if exists time_attack_scores");
		pDb.execSQL("drop table if exists names");
		this.onCreate(pDb);
	}

	/**
	 * inserts a new score for the TimeAtttackGameHandler into the database
	 * @param pName
	 * @param pNumberOfWins
	 * @param pNumberOfLosses
	 * @param pScore
	 */
	public void createTimeAttackEntry(String pName,
			int pNumberOfWins,
			int pNumberOfLosses,
			int pScore) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = String.format(INSERT_TIME_ATTACK_SQL, pName, pNumberOfWins, pNumberOfLosses, pScore);
		db.execSQL(sql);
		db.close();
	}
	
	/**
	 * ensures that the given name is existent in the database
	 * and if not creates an DB entry for the name in the names
	 * table
	 */
	public void ensureNameExistsInDB(String pName) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("insert or ignore into names (name) values ('" + pName + "');");
		db.close();
	}
	
	/**
	 * gets all the score entries ordered by the score in an descending
	 * order
	 * @return
	 */
	public Cursor getTimeAttackOrdered() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor ret =  db.rawQuery(QUERY_TIME_ATTACK_ORDERED_SQL, null);
		db.close();
		return ret;
	}
	
}
