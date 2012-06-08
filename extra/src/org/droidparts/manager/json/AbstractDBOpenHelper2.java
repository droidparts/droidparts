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
package org.droidparts.manager.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.droidparts.manager.sql.AbstractDBOpenHelper;

import android.content.Context;

public abstract class AbstractDBOpenHelper2 extends AbstractDBOpenHelper {

	private final String DB_PATH;

	public AbstractDBOpenHelper2(Context ctx, String name, int version) {
		super(ctx, name, version);
		DB_PATH = (name != null) ? "/data/data/" + ctx.getPackageName()
				+ "/databases/" + name : null;
	}

	public void copyDBTo(File fileOrDirTo) throws Exception {
		if (DB_PATH == null) {
			throw new IllegalStateException("Copy in-memory db? No way!");
		}
		// TODO improve code
		File fileFrom = new File(DB_PATH);
		File fileTo = fileOrDirTo.isDirectory() ? new File(fileOrDirTo,
				DB_PATH.substring(DB_PATH.lastIndexOf('/', DB_PATH.length())))
				: fileOrDirTo;
		if (fileFrom.exists()) {
			FileChannel src = new FileInputStream(fileFrom).getChannel();
			FileChannel dst = new FileOutputStream(fileTo).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
		}
	}

}
