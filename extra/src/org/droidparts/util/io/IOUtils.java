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

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.droidparts.util.L;

public class IOUtils {

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("failed to encode");
		}
	}

	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("failed to decode");
		}
	}

	public static void silentlyClose(Closeable... closeables) {
		for (Closeable cl : closeables) {
			try {
				cl.close();
			} catch (Exception e) {
				L.d(e);
			}
		}
	}

	public static String readAndCloseInputStream(InputStream is)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, UTF8),
					BUFFER_SIZE);
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		} finally {
			silentlyClose(br);
		}
	}

	public static ArrayList<File> getFileList(File dir, String fileExtension) {
		final ArrayList<File> files = new ArrayList<File>();
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				if (fileExtension == null) {
					files.add(file);
				} else {
					if (file.getName().endsWith(fileExtension)) {
						files.add(file);
					}
				}
			} else {
				files.addAll(getFileList(file, fileExtension));
			}
		}
		return files;
	}

	public static void copy(File fileFrom, File fileTo) throws IOException {
		FileChannel src = null;
		FileChannel dst = null;
		try {
			src = new FileInputStream(fileFrom).getChannel();
			dst = new FileOutputStream(fileTo).getChannel();
			dst.transferFrom(src, 0, src.size());
		} finally {
			silentlyClose(src, dst);
		}

	}

}
