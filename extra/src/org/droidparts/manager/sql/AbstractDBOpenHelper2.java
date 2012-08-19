/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.manager.sql;

import java.io.File;

import org.droidparts.util.L;
import org.droidparts.util.io.IOUtils;

import android.content.Context;

public abstract class AbstractDBOpenHelper2 extends AbstractDBOpenHelper {

	private final String dbFilePath;

	public AbstractDBOpenHelper2(Context ctx, String name, int version) {
		super(ctx, name, version);
		dbFilePath = (name != null) ? "/data/data/" + ctx.getPackageName()
				+ "/databases/" + name : null;
	}

	public void copyDBTo(File fileOrDirTo) throws Exception {
		if (dbFilePath == null) {
			throw new IllegalStateException("Copy in-memory db? No way!");
		}
		File fileFrom = new File(dbFilePath);
		if (fileFrom.exists()) {
			File fileTo;
			if (fileOrDirTo.isDirectory()) {
				String dbFileName = dbFilePath.substring(dbFilePath
						.lastIndexOf('/', dbFilePath.length()));
				fileTo = new File(fileOrDirTo, dbFileName);
			} else {
				fileTo = fileOrDirTo;
			}
			if (fileTo.exists()) {
				fileTo.delete();
			}
			IOUtils.copy(fileFrom, fileTo);
		} else {
			L.e("No DB file at " + dbFilePath);
		}
	}

}
