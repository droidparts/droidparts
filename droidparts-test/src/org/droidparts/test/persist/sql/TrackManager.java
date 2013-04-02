package org.droidparts.test.persist.sql;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.test.model.Track;

import android.content.Context;

public class TrackManager extends EntityManager<Track> {

	public TrackManager(Context ctx) {
		super(Track.class, ctx);
	}

}
