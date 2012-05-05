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
package org.droidparts.preference;

import java.util.Arrays;
import java.util.HashSet;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class MultipleChoiceListPreference extends ListPreference {

	public static String[] splitPreferenceValue(String val) {
		return val.split("\\" + SEP);
	}

	private static final String SEP = "|";

	private boolean[] checkedEntryIndexes;

	public MultipleChoiceListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		checkedEntryIndexes = new boolean[getEntries().length];
	}

	public MultipleChoiceListPreference(Context context) {
		super(context);
		checkedEntryIndexes = new boolean[getEntries().length];
	}

	@Override
	public void setEntries(CharSequence[] entries) {
		checkedEntryIndexes = new boolean[entries.length];
		super.setEntries(entries);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// restore checked state
		String val = getValue();
		if (val != null) {
			HashSet<String> checkedEntryVals = new HashSet<String>(
					Arrays.asList(splitPreferenceValue(val)));
			CharSequence[] entryVals = getEntryValues();
			for (int i = 0; i < entryVals.length; i++) {
				checkedEntryIndexes[i] = checkedEntryVals
						.contains(entryVals[i]);
			}
		}
		// set multiple choice items
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
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < entryVals.length; i++) {
				if (checkedEntryIndexes[i]) {
					sb.append(entryVals[i]);
					sb.append(SEP);
				}
			}
			String val = sb.toString();
			// strip last separator
			if (val.length() > 0) {
				val = val.substring(0, val.length() - SEP.length());
			}
			if (callChangeListener(val)) {
				setValue(val);
			}
		}
	}

}
