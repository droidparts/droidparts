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
package org.droidparts.util;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import java.lang.reflect.Method;

import android.view.View;
import android.widget.EditText;

public class ViewUtils {

	public static void setVisible(View view, boolean visible) {
		view.setVisibility(visible ? VISIBLE : GONE);
	}

	public static void disableOverscroll(View view) {
		Class<?> viewCls = view.getClass();
		try {
			Method m = viewCls.getMethod("setOverScrollMode",
					new Class[] { int.class });
			int OVER_SCROLL_NEVER = (Integer) viewCls.getField(
					"OVER_SCROLL_NEVER").get(view);
			m.invoke(view, OVER_SCROLL_NEVER);
		} catch (Exception e) {
			L.e(e);
		}
	}

	public static void putCursorAfterLastSymbol(EditText editText) {
		editText.setSelection(editText.getText().length());
	}
}
