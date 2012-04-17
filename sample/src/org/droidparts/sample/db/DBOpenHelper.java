package org.droidparts.sample.db;

import org.droidparts.model.Entity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends
		org.droidparts.manager.sql.AbstractDBOpenHelper {

	private static final String DB_FILE = "droidparts_sample.sqlite";
	private static final int DB_VER = 1;

	public DBOpenHelper(Context ctx) {
		super(ctx, DB_FILE, DB_VER);
	}

	@Override
	protected Class<? extends Entity>[] getModelClasses() {
		@SuppressWarnings("unchecked")
		Class<? extends Entity>[] arr = new Class[] { Entry.class };
		return arr;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropAllTables(db);
		onCreate(db);
	}

}
