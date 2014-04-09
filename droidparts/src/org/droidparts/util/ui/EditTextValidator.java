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
package org.droidparts.util.ui;

import java.util.regex.Pattern;

import android.widget.EditText;

public class EditTextValidator {

	// Typical handling:
	// editText.setError(errMsg);
	// editText.requestFocus();
	//
	// Empty popup fix:
	// <item
	// name="android:textColorPrimaryInverse">@android:color/primary_text_light</item>

	public static String getText(EditText editText, int minLen,
			int errMsgResId, Object... errMsgFormatArgs)
			throws ValidationException {
		return getText(editText, minLen,
				editText.getContext().getString(errMsgResId, errMsgFormatArgs));

	}

	public static String getText(EditText editText, Pattern pattern,
			int errMsgResId, Object... errMsgFormatArgs)
			throws ValidationException {
		return getText(editText, pattern,
				editText.getContext().getString(errMsgResId, errMsgFormatArgs));
	}

	public static String getText(EditText editText, int minLen, String errMsg)
			throws ValidationException {
		String txt = getTrimmedText(editText);
		if (txt.length() < minLen) {
			throw new ValidationException(editText, errMsg);
		}
		return txt;
	}

	public static String getText(EditText editText, Pattern pattern,
			String errMsg) throws ValidationException {
		String txt = getTrimmedText(editText);
		if (!pattern.matcher(txt).matches()) {
			throw new ValidationException(editText, errMsg);
		}
		return txt;
	}

	public static String getTrimmedText(EditText et) {
		return et.getText().toString().trim();
	}

	public static class ValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public final EditText editText;
		public final String errorMessage;

		public ValidationException(EditText et, String errMsg) {
			super(errMsg);
			this.editText = et;
			this.errorMessage = errMsg;
		}

	}

}