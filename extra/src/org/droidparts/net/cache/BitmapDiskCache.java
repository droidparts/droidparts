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
package org.droidparts.net.cache;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.getFileList;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import org.droidparts.util.L;
import org.droidparts.util.crypto.HashCalc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapDiskCache {

	private final File cacheDir;

	public BitmapDiskCache(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean put(String key, Bitmap bm) {
		File file = getCachedFile(key);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			bm.compress(PNG, 100, bos);
			return true;
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			silentlyClose(bos);
		}
	}

	public Bitmap get(String key) {
		Bitmap bm = null;
		File file = getCachedFile(key);
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file),
						BUFFER_SIZE);
				bm = BitmapFactory.decodeStream(bis);
				// only after successful restore
				file.setLastModified(System.currentTimeMillis());
			} catch (Exception e) {
				L.w(e);
			} finally {
				silentlyClose(bis);
			}
		}
		if (bm == null) {
			L.i("Cache miss for " + key);
		}
		return bm;
	}

	public void purgeFilesAccessedBefore(long timestamp) {
		for (File f : getFileList(cacheDir)) {
			if (timestamp <= 0 || f.lastModified() < timestamp) {
				f.delete();
			}
		}
	}

	public void trimToSize(int sizeMb) {
		TreeMap<Long, ArrayList<File>> map = new TreeMap<Long, ArrayList<File>>(
				reverseComparator);
		final long targetSize = sizeMb * 1024 * 1024;
		long size = 0;
		for (File file : getFileList(cacheDir)) {
			Long modified = file.lastModified();
			ArrayList<File> files = map.get(modified);
			if (files == null) {
				files = new ArrayList<File>();
				map.put(modified, files);
			}
			files.add(file);
			size += file.length();
		}
		L.i(String.format("Cache size: %.2f MB.", (float) size / 1024 / 1024));
		Iterator<Long> it = map.keySet().iterator();
		while ((size > targetSize) && it.hasNext()) {
			for (File file : map.get(it.next())) {
				size -= file.length();
				file.delete();
			}
		}
	}

	private File getCachedFile(String key) {
		return new File(cacheDir, HashCalc.getMD5(key));
	}

	private static final Comparator<Long> reverseComparator = new Comparator<Long>() {

		@Override
		public int compare(Long lhs, Long rhs) {
			return (int) (lhs - rhs);
		}
	};

}