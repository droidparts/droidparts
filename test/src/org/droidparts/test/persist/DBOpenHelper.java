package org.droidparts.test.persist;

import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper2;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.model.Tag;
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
		return new Class[] { Primitives.class, Tag.class, Album.class,
				Track.class, AlbumToTag.class };
	}

	@Override
	protected void onCreateExtra(SQLiteDatabase db) {
		createIndex(db, "AlbumToTag", true, "album_id", "tag_id");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}

}
