package org.droidparts.sample;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.persist.sql.AbstractDBOpenHelper;
import org.droidparts.sample.db.DBOpenHelper;
import org.droidparts.sample.json.EntrySerializer;
import org.droidparts.util.ui.AbstractDialogFactory;

import android.content.Context;

public class DependencyProvider extends AbstractDependencyProvider {

	private final DBOpenHelper dbOpenHelper;
	private final EntrySerializer entrySerializer;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
		entrySerializer = new EntrySerializer();
	}

	@Override
	public AbstractDBOpenHelper getDBOpenHelper() {
		return dbOpenHelper;
	}

	public EntrySerializer getEntrySerializer() {
		// singleton
		return entrySerializer;
	}

	public AbstractDialogFactory getDialogFactory(Context ctx) {
		// new instance each time injected
		return new AbstractDialogFactory(ctx);
	}

}
