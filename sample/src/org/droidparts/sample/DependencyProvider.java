package org.droidparts.sample;

import org.droidparts.inject.AbstractDependencyProvider;
import org.droidparts.sample.db.DBOpenHelper;
import org.droidparts.sample.json.EntrySerializer;
import org.droidparts.util.ui.DialogFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DependencyProvider extends AbstractDependencyProvider {

	private final DBOpenHelper dbOpenHelper;
	private final EntrySerializer entrySerializer;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
		entrySerializer = new EntrySerializer();
	}

	@Override
	public SQLiteDatabase getDB() {
		return dbOpenHelper.getWritableDatabase();
	}

	public EntrySerializer getEntrySerializer() {
		// singleton
		return entrySerializer;
	}

	public DialogFactory getDialogFactory(Context ctx) {
		// new instance each time injected
		return new DialogFactory(ctx);
	}

}
