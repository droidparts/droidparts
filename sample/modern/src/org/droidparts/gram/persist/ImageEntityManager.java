package org.droidparts.gram.persist;

import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.persist.sql.EntityManager;

import android.content.Context;

public class ImageEntityManager extends EntityManager<Image> {

	public ImageEntityManager(Context ctx) {
		super(ctx, Image.class);
	}

	@Override
	public boolean create(Image item) {
		setFilterId(item.filter);
		return super.create(item);
	}

	@Override
	public boolean update(Image item) {
		setFilterId(item.filter);
		return super.update(item);
	}

	private void setFilterId(Filter filter) {
		if (filter.id < 1) {
			new FilterEntityManager(ctx).readOrCreateForName(filter);
		}
	}

}
