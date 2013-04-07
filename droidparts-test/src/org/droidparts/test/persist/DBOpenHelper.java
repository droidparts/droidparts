package org.droidparts.test.persist;

import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.model.Tag;
import org.droidparts.test.model.Track;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOpenHelper extends AbstractDBOpenHelper implements DB {

	public DBOpenHelper(Context ctx) {
		super(ctx, null, 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateTables(SQLiteDatabase db) {
		createTables(db, Primitives.class, Tag.class, Album.class, Track.class,
				AlbumToTag.class);
		createIndex(db, Table.ALBUM_TO_TAG, true, Column.ALBUM_ID,
				Column.TAG_ID);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}

}
