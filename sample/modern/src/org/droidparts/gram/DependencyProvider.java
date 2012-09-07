package org.droidparts.gram;

import org.droidparts.gram.persist.DBOpenHelper;
import org.droidparts.gram.persist.ImageEntityManager;
import org.droidparts.gram.persist.PrefsManager;
import org.droidparts.inject.AbstractDependencyProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DependencyProvider extends AbstractDependencyProvider {

	private final DBOpenHelper dbOpenHelper;
	private PrefsManager prefsManager;
	private ImageEntityManager imageManager;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public SQLiteDatabase getDB() {
		return dbOpenHelper.getWritableDatabase();
	}

	public PrefsManager getPrefsManager(Context ctx) {
		if (prefsManager == null) {
			prefsManager = new PrefsManager(ctx);
		}
		return prefsManager;
	}

	public ImageEntityManager getImageEntityManager(Context ctx) {
		if (imageManager == null) {
			imageManager = new ImageEntityManager(ctx);
		}
		return imageManager;
	}

}
