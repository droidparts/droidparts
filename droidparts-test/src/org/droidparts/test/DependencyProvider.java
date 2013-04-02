package org.droidparts.test;

import org.droidparts.AbstractDependencyProvider;
import org.droidparts.test.persist.DBOpenHelper;

import android.content.Context;

public class DependencyProvider extends AbstractDependencyProvider {

	private DBOpenHelper dbOpenHelper;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public DBOpenHelper getDBOpenHelper() {
		return dbOpenHelper;
	}

}
