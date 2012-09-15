package org.droidparts.test;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.test.persist.DBOpenHelper;

import android.content.Context;

public class DependencyProvider extends AbstractDependencyProvider {

	private DBOpenHelper dbOpenHelper;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public AbstractDBOpenHelper getDBOpenHelper() {
		return dbOpenHelper;
	}

}
