/**
 * Copyright 2014 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.gram.persist;

import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.persist.sql.EntityManager;

import android.content.Context;

public class ImageEntityManager extends EntityManager<Image> {

	private final FilterEntityManager filterEntityManager;

	public ImageEntityManager(Context ctx) {
		super(Image.class, ctx);
		filterEntityManager = new FilterEntityManager(ctx);
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
			filterEntityManager.setIdOrCreateForName(filter);
		}
	}

}
