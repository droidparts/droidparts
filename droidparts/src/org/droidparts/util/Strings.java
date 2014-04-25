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
package org.droidparts.util;

import static org.droidparts.contract.Constants.UTF8;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

public class Strings {

	public static boolean isNotEmpty(CharSequence str) {
		return !isEmpty(str);
	}

	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	public static <T> String join(Collection<T> coll, String separator) {
		return join(coll, separator, null);
	}

	public static String join(Object[] arr, String separator) {
		return join(arr, separator, null);
	}

	public static <T> String join(Collection<T> coll, String separator,
			String terminator) {
		return join(coll.toArray(new Object[coll.size()]), separator,
				terminator);
	}

	public static String join(Object[] arr, String separator, String terminator) {
		StringBuilder sb = new StringBuilder(arr.length * 2);
		for (int i = 0; i < arr.length; i++) {
			sb.append(arr[i]);
			if (i < arr.length - 1) {
				sb.append(separator);
			} else if (terminator != null && arr.length > 0) {
				sb.append(terminator);
			}
		}
		return sb.toString();
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("failed to encode", e);
		}
	}

	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("failed to decode", e);
		}
	}

	public static final String SHA1 = "SHA-1";
	public static final String MD5 = "MD5";

	public static String getMD5(String str) {
		try {
			return getHash(str, MD5);
		} catch (Exception e) {
			L.w(e);
			return null;
		}
	}

	public static String getSHA1(String str) {
		try {
			return getHash(str, SHA1);
		} catch (Exception e) {
			L.w(e);
			return null;
		}
	}

	public static String getHash(String str, String algorithm)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] bytes = str.getBytes(UTF8);
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] digest = md.digest(bytes);
		BigInteger bigInt = new BigInteger(1, digest);
		String hash = bigInt.toString(16);
		while (hash.length() < 32) {
			hash = "0" + hash;
		}
		return hash;
	}

}
