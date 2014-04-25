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
package org.droidparts.gram;

import org.droidparts.AbstractDependencyProvider;
import org.droidparts.gram.misc.DialogFactory;
import org.droidparts.gram.persist.DBOpenHelper;
import org.droidparts.gram.persist.ImageEntityManager;
import org.droidparts.gram.persist.PrefsManager;
import org.droidparts.net.image.ImageFetcher;
import org.droidparts.persist.sql.AbstractDBOpenHelper;

import android.content.Context;

public class DependencyProvider extends AbstractDependencyProvider {

	private final DBOpenHelper dbOpenHelper;
	private PrefsManager prefsManager;
	private ImageEntityManager imageManager;
	private ImageFetcher imageFetcher;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public AbstractDBOpenHelper getDBOpenHelper() {
		return dbOpenHelper;
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

	public ImageFetcher getImageFetcher(Context ctx) {
		if (imageFetcher == null) {
			imageFetcher = new ImageFetcher(ctx);
			imageFetcher.clearCacheOlderThan(48);
		}
		return imageFetcher;
	}

	public DialogFactory getDialogFactory(Context ctx) {
		return new DialogFactory(ctx);
	}

}
