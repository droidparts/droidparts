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
package org.droidparts.sample;

import org.droidparts.AbstractDependencyProvider;
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
		entrySerializer = new EntrySerializer(ctx);
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
