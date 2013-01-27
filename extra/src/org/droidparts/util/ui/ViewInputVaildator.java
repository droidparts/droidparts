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

import android.view.View;
import android.widget.EditText;

public abstract class ViewInputVaildator {

	static class InputValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public final View view;
		public final String errorMessage;

		public InputValidationException(View view, String errMsg) {
			this.view = view;
			this.errorMessage = errMsg;
		}
	}

	public static String getText(EditText et, int minLen, String errMsg)
			throws InputValidationException {
		String txt = getTrimmed(et);
		if (txt.length() < minLen) {
			throwValidationException(et, errMsg);
		}
		return txt;
	}

	public static String getText(EditText et, Pattern pattern, String errMsg)
			throws InputValidationException {
		String txt = getTrimmed(et);
		if (!pattern.matcher(txt).matches()) {
			throwValidationException(et, errMsg);
		}
		return txt;
	}

	public static String getTrimmed(EditText et) {
		return et.getText().toString().trim();
	}

	private static void throwValidationException(View v, String errMsg)
			throws InputValidationException {
		v.requestFocus();
		throw new InputValidationException(v, errMsg);
	}

}
