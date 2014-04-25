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
package org.droidparts.gram.persist;

import java.util.Arrays;
import java.util.HashSet;

import org.droidparts.gram.R;
import org.droidparts.persist.AbstractPrefsManager;
import org.droidparts.widget.MultiSelectListPreference;

import android.content.Context;

public class PrefsManager extends AbstractPrefsManager {

	private static final int VERSION = 1;

	public PrefsManager(Context ctx) {
		super(ctx, VERSION);
	}

	public boolean isShowDetailFilter() {
		return getShowDetailVaules().contains(
				getContext().getString(R.string.pref_filter));
	}

	public boolean isShowDetailTags() {
		return getShowDetailVaules().contains(
				getContext().getString(R.string.pref_tags));
	}

	private HashSet<String> getShowDetailVaules() {
		String str = readString(R.string.pref_show_detail,
				R.string.pref_show_detail);
		String[] arr = MultiSelectListPreference
				.fromPersistedPreferenceValue(str);
		return new HashSet<String>(Arrays.asList(arr));
	}

}
