package org.droidparts.test.manager;

import org.droidparts.manager.sql.AbstractDBOpenHelper2;
import org.droidparts.model.Entity;
import org.droidparts.test.model.Phone;
import org.droidparts.test.model.TwoStrings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper2 {

	public DBOpenHelper(Context ctx) {
		super(ctx, null, 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends Entity>[] getModelClasses() {
		return new Class[] { Phone.class, TwoStrings.class };
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropAll(db, true, true);
	}

}
