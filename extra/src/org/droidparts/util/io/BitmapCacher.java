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
package org.droidparts.util.io;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import org.droidparts.util.L;
import org.droidparts.util.crypto.HashCalc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCacher {

	private final File cacheDir;

	public BitmapCacher(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean saveToCache(String name, Bitmap bm) {
		File file = getCachedFile(name);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			bm.compress(PNG, 100, bos);
			return true;
		} catch (Exception e) {
			L.e(e);
			return false;
		} finally {
			silentlyClose(bos);
		}
	}

	public Bitmap readFromCache(String name) {
		File file = getCachedFile(name);
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file),
						BUFFER_SIZE);
				Bitmap bm = BitmapFactory.decodeStream(bis);
				// only after successful restore
				file.setLastModified(System.currentTimeMillis());
				return bm;
			} catch (Exception e) {
				L.e(e);
				return null;
			} finally {
				silentlyClose(bis);
			}
		} else {
			L.i("Cache miss: " + file.getAbsolutePath());
			return null;
		}
	}

	public void purgeCache(int targetSizeMB) {
		TreeMap<Long, File> map = new TreeMap<Long, File>(reverseComparator);
		final long targetSize = targetSizeMB * 1024 * 1024;
		long size = 0;
		for (File f : IOUtils.getFileList(cacheDir, null)) {
			map.put(f.lastModified(), f);
			size += f.length();
		}
		L.d("Cache size " + (size / 1024 / 1024) + "MB.");
		Iterator<Long> it = map.keySet().iterator();
		while ((size > targetSize) && it.hasNext()) {
			File f = map.get(it.next());
			size -= f.length();
			f.delete();
		}
	}

	private File getCachedFile(String name) {
		return new File(cacheDir, HashCalc.getMD5(name));
	}

	private static final Comparator<Long> reverseComparator = new Comparator<Long>() {

		@Override
		public int compare(Long lhs, Long rhs) {
			return (int) (lhs - rhs);
		}
	};

}