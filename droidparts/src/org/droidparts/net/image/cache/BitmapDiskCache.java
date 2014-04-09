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
package org.droidparts.net.image.cache;

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.getFileList;
import static org.droidparts.util.IOUtils.readToByteArray;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.droidparts.inner.BitmapFactoryUtils;
import org.droidparts.util.L;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Pair;

public class BitmapDiskCache {

	private static final String DEFAULT_DIR = "img";

	private static BitmapDiskCache instance;

	public static BitmapDiskCache getDefaultInstance(Context ctx) {
		if (instance == null) {
			File cacheDir = new File(ctx.getCacheDir(), DEFAULT_DIR);
			instance = new BitmapDiskCache(cacheDir);
		}
		return instance;
	}

	private final File cacheDir;

	public BitmapDiskCache(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean put(String key, Bitmap bm,
			Pair<CompressFormat, Integer> cacheFormat) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bm.compress(cacheFormat.first, cacheFormat.second, baos);
			return put(key, baos.toByteArray());
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			silentlyClose(baos);
		}
	}

	public boolean put(String key, byte[] bmArr) {
		File file = getCachedFile(key);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			bos.write(bmArr);
			return true;
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			silentlyClose(bos);
		}
	}

	public Pair<Bitmap, BitmapFactory.Options> get(String key, int reqWidth,
			int reqHeight, Bitmap.Config config, Bitmap inBitmap) {
		Pair<Bitmap, BitmapFactory.Options> bmData = null;
		File file = getCachedFile(key);
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				byte[] data = readToByteArray(fis);
				bmData = BitmapFactoryUtils.decodeScaled(data, reqWidth,
						reqHeight, config, inBitmap);
				file.setLastModified(System.currentTimeMillis());
			} catch (Exception e) {
				L.w(e);
			} finally {
				silentlyClose(fis);
			}
		}
		L.v("DiskCache " + ((bmData == null) ? "miss" : "hit") + " for '%s'.",
				key);
		return bmData;
	}

	public void purgeFilesAccessedBefore(long timestamp) {
		for (File f : getFileList(cacheDir)) {
			if (f.lastModified() < timestamp) {
				f.delete();
			}
		}
	}

	private File getCachedFile(String key) {
		return new File(cacheDir, String.valueOf(key.hashCode()));
	}

}