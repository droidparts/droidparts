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

import org.droidparts.Injector;
import org.droidparts.annotation.inject.InjectSystemService;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;

public abstract class CursorAdapter extends android.widget.CursorAdapter {

	private final Context ctx;

	@InjectSystemService
	private LayoutInflater layoutInflater;

	public CursorAdapter(Context ctx, Cursor cursor) {
		super(ctx, cursor);
		this.ctx = ctx.getApplicationContext();
		Injector.inject(ctx, this);
	}

	protected Context getContext() {
		return ctx;
	}

	protected LayoutInflater getLayoutInflater() {
		return layoutInflater;
	}

	public void requeryData() {
		Cursor c = getCursor();
		if (c != null) {
			c.requery();
		}
	}

}
