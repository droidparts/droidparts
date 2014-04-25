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
package org.droidparts.inner.reader;

import org.droidparts.util.L;

import android.app.Activity;
import android.os.Bundle;

public class LegacyReader {

	public static boolean isSupportAvaliable() {
		return (supportFragmentsReader != null);
	}

	static {
		try {
			supportFragmentsReader = (ISupportFragmentsReader) Class.forName(
					"org.droidparts.inner.reader.SupportFragmentsReader")
					.newInstance();
		} catch (Exception e) {
			L.i("Legacy package not available.");
		}
	}

	private static ISupportFragmentsReader supportFragmentsReader;

	//

	static boolean isSupportObject(Object obj) {
		return supportFragmentsReader.isSupportObject(obj);
	}

	static Activity getParentActivity(Object fragmentObj) {
		return supportFragmentsReader.getParentActivity(fragmentObj);
	}

	static Object getFragment(Object fragmentActivityObj, int fragmentId,
			String valName) {
		return supportFragmentsReader.getFragment(fragmentActivityObj,
				fragmentId, valName);
	}

	static Bundle getFragmentArguments(Object fragmentObj) {
		return supportFragmentsReader.getFragmentArguments(fragmentObj);
	}

	static interface ISupportFragmentsReader {

		boolean isSupportObject(Object obj);

		Activity getParentActivity(Object fragmentObj);

		Object getFragment(Object fragmentActivityObj, int fragmentId,
				String valName);

		Bundle getFragmentArguments(Object fragmentObj);

	}

}
