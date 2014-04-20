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
package org.droidparts.adapter.cursor;

import org.droidparts.contract.AlterableContent;
import org.droidparts.model.Entity;
import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.AbstractSelect;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

public abstract class EntityCursorAdapter<EntityType extends Entity> extends
		CursorAdapter implements AlterableContent<AbstractSelect<EntityType>> {

	protected final EntityManager<EntityType> entityManager;

	public EntityCursorAdapter(Context ctx, Class<EntityType> entityCls) {
		this(ctx, entityCls, null);
	}

	public EntityCursorAdapter(Context ctx, Class<EntityType> entityCls,
			AbstractSelect<EntityType> select) {
		this(ctx, new EntityManager<EntityType>(entityCls, ctx), select);
	}

	public EntityCursorAdapter(Context ctx,
			EntityManager<EntityType> entityManager,
			AbstractSelect<EntityType> select) {
		super(ctx, (select != null) ? select.execute() : null);
		this.entityManager = entityManager;
	}

	@Override
	public void setContent(AbstractSelect<EntityType> select) {
		changeCursor((select != null) ? select.execute() : null);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		bindView(context, view, entityManager.readRow(cursor));
	}

	public abstract void bindView(Context context, View view, EntityType item);

	public boolean create(EntityType item) {
		boolean success = entityManager.create(item);
		return requeryOnSuccess(success);
	}

	public EntityType read(int position) {
		long id = getItemId(position);
		EntityType item = entityManager.read(id);
		return item;
	}

	public boolean update(EntityType item) {
		boolean success = entityManager.update(item);
		return requeryOnSuccess(success);
	}

	public boolean delete(int position) {
		long id = getItemId(position);
		boolean success = entityManager.delete(id);
		return requeryOnSuccess(success);
	}

	private boolean requeryOnSuccess(boolean success) {
		if (success) {
			requeryData();
		}
		return success;
	}

}
