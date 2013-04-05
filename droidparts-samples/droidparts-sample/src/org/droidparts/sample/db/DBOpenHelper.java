package org.droidparts.sample.db;

import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper {

	private static final String DB_FILE = "droidparts_sample.sqlite";
	private static final int DB_VER = 1;

	public DBOpenHelper(Context ctx) {
		super(ctx, DB_FILE, DB_VER);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateTables(SQLiteDatabase db) {
		createTables(db, Entity.class);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}

}
