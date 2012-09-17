package org.droidparts.test.persist;

import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper2;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper2 {

	public DBOpenHelper(Context ctx) {
		super(ctx, null, 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends Entity>[] getEntityClasses() {
		return new Class[] { Album.class, Track.class };
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
	}

}
