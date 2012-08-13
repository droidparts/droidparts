package org.droidparts.test;

import org.droidparts.inject.AbstractDependencyProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DependencyProvider extends AbstractDependencyProvider {

	public DependencyProvider(Context ctx) {
		super(ctx);
	}

	@Override
	public SQLiteDatabase getDB() {
		return null;
	}

}
