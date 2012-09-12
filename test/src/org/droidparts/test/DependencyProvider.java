package org.droidparts.test;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.test.persist.DBOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DependencyProvider extends AbstractDependencyProvider {

	private DBOpenHelper dbOpenHelper;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public SQLiteDatabase getDB() {
		return dbOpenHelper.getWritableDatabase();
	}

}
