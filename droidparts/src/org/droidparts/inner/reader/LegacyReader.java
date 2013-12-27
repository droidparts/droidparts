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
