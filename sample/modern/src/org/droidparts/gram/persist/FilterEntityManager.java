package org.droidparts.gram.persist;

import static org.droidparts.contract.DB.Column.ID;
import static org.droidparts.gram.contract.DB.Column.NAME;

import org.droidparts.gram.model.Filter;
import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;

import android.content.Context;
import android.database.Cursor;

public class FilterEntityManager extends EntityManager<Filter> {

	public FilterEntityManager(Context ctx) {
		super(ctx, Filter.class);
	}

	public void readOrCreateForName(Filter filter) {
		Cursor cursor = select().columns(ID).where(NAME, Is.EQUAL, filter.name)
				.execute();
		try {
			if (cursor.moveToFirst()) {
				filter.id = cursor.getLong(0);
			} else {
				create(filter);
			}
		} finally {
			cursor.close();
		}
	}
}
