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
package org.droidparts.widget;

import static org.droidparts.util.Strings.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

// android:defaultValue="entryValue1|entryValue2"
public class MultiSelectListPreference extends ListPreference {

	public static String[] fromPersistedPreferenceValue(String val) {
		if (isEmpty(val)) {
			return new String[0];
		} else {
			return val.split("\\" + SEP);
		}
	}

	public static String toPersistedPreferenceValue(CharSequence... entryKeys) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < entryKeys.length; i++) {
			sb.append(entryKeys[i]);
			if (i < entryKeys.length - 1) {
				sb.append(SEP);
			}
		}
		return sb.toString();
	}

	public CharSequence[] getCheckedEntries() {
		CharSequence[] entries = getEntries();
		ArrayList<CharSequence> checkedEntries = new ArrayList<CharSequence>();
		for (int i = 0; i < entries.length; i++) {
			if (checkedEntryIndexes[i]) {
				checkedEntries.add(entries[i]);
			}
		}
		return checkedEntries.toArray(new String[checkedEntries.size()]);
	}

	// boring stuff

	private static final String SEP = "|";

	private boolean[] checkedEntryIndexes;

	public MultiSelectListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultiSelectListPreference(Context context) {
		super(context);
	}

	@Override
	public void setEntries(CharSequence[] entries) {
		super.setEntries(entries);
		updateCheckedEntryIndexes();
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		updateCheckedEntryIndexes();
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		updateCheckedEntryIndexes();
		builder.setMultiChoiceItems(getEntries(), checkedEntryIndexes,
				new OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						checkedEntryIndexes[which] = isChecked;
					}
				});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			CharSequence[] entryVals = getEntryValues();
			ArrayList<CharSequence> checkedVals = new ArrayList<CharSequence>();
			for (int i = 0; i < entryVals.length; i++) {
				if (checkedEntryIndexes[i]) {
					checkedVals.add(entryVals[i]);
				}
			}
			String val = toPersistedPreferenceValue(checkedVals
					.toArray(new CharSequence[checkedVals.size()]));
			if (callChangeListener(val)) {
				setValue(val);
			}
		}
	}

	private void updateCheckedEntryIndexes() {
		CharSequence[] entryVals = getEntryValues();
		checkedEntryIndexes = new boolean[entryVals.length];
		String val = getValue();
		if (val != null) {
			HashSet<String> checkedEntryVals = new HashSet<String>(
					Arrays.asList(fromPersistedPreferenceValue(val)));
			for (int i = 0; i < entryVals.length; i++) {
				checkedEntryIndexes[i] = checkedEntryVals
						.contains(entryVals[i]);
			}
		}
	}

}