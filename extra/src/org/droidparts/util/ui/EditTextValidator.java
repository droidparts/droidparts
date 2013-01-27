/**
 * Copyright 2013 Alex Yanchenko
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

public abstract class EditTextValidator {

	static class InputValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public final EditText editText;
		public final String errorMessage;

		public InputValidationException(EditText et, String errMsg) {
			this.editText = et;
			this.errorMessage = errMsg;
		}

		// editText.setError(errMsg);
		// editText.requestFocus();
	}

	public static String getText(EditText editText, int minLen, String errMsg)
			throws InputValidationException {
		String txt = getTrimmed(editText);
		if (txt.length() < minLen) {
			throw new InputValidationException(editText, errMsg);
		}
		return txt;
	}

	public static String getText(EditText editText, Pattern pattern, String errMsg)
			throws InputValidationException {
		String txt = getTrimmed(editText);
		if (!pattern.matcher(txt).matches()) {
			throw new InputValidationException(editText, errMsg);
		}
		return txt;
	}

	public static String getTrimmed(EditText et) {
		return et.getText().toString().trim();
	}
}
