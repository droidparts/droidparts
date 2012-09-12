package org.droidparts.test.persist.sql;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.test.model.Album;

import android.content.Context;

public class AlbumManager extends EntityManager<Album> {

	public AlbumManager(Context ctx) {
		super(ctx, Album.class);
	}

}
