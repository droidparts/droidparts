package org.droidparts.sample;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.sample.db.DBOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DependencyProvider extends AbstractDependencyProvider {

	private final DBOpenHelper dbOpenHelper;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public SQLiteDatabase getDB() {
		return dbOpenHelper.getWritableDatabase();
	}

}
