/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class IOUtils2 extends IOUtils {

	public static void dumpDBToCacheDir(Context ctx, SQLiteDatabase db) {
		String dbFilePath = db.getPath();
		String dbFileName = dbFilePath.substring(dbFilePath.lastIndexOf('/', dbFilePath.length()));
		File fileTo = new File(ctx.getExternalCacheDir(), dbFileName);
		try {
			IOUtils2.copy(new File(dbFilePath), fileTo);
			L.i("Copied DB file to '%s'.", fileTo.getAbsolutePath());
		} catch (IOException e) {
			L.w(e);
		}
	}

}
