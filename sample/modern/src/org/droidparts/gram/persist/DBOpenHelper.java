package org.droidparts.gram.persist;

import org.droidparts.gram.contract.DB;
import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper {

	public DBOpenHelper(Context ctx) {
		super(ctx, DB.FILE, DB.VERSION);
	}

	@Override
	protected Class<? extends Entity>[] getModelClasses() {
		@SuppressWarnings("unchecked")
		Class<? extends Entity>[] arr = new Class[] { Filter.class, Image.class };
		return arr;
	}

	@Override
	protected void onCreateExtra(SQLiteDatabase db) {
		createIndex(db, DB.Table.IMAGES, false, DB.Column.CAPTION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropAll(db, true, true);
		onCreate(db);
	}

}
