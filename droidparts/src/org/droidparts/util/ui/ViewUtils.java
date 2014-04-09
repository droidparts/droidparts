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

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ViewUtils {

	public static <T extends View> T findViewById(View view, int id) {
		@SuppressWarnings("unchecked")
		T v = (T) view.findViewById(id);
		return v;
	}

	public static <T extends View> T findViewById(Activity activity, int id) {
		@SuppressWarnings("unchecked")
		T v = (T) activity.findViewById(id);
		return v;
	}

	public static void setInvisible(boolean invisible, View... views) {
		for (View view : views) {
			view.setVisibility(invisible ? INVISIBLE : VISIBLE);
		}
	}

	public static void setGone(boolean gone, View... views) {
		for (View view : views) {
			view.setVisibility(gone ? GONE : VISIBLE);
		}
	}

	public static void setEnabled(boolean enabled, View... views) {
		for (View view : views) {
			view.setEnabled(enabled);
		}
	}

	public static void runOnLayoutDone(final View view, final Runnable runnable) {
		OnGlobalLayoutListener l = new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				runnable.run();
			}
		};
		view.getViewTreeObserver().addOnGlobalLayoutListener(l);
	}

	public static void putCursorAfterLastSymbol(EditText... editTexts) {
		for (EditText editText : editTexts) {
			editText.setSelection(editText.getText().length());
		}
	}

	public static void showKeyboard(final View view) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

			@Override
			public void run() {
				setKeyboardVisible(view, true);
			}
		}, 200);
	}

	public static void setKeyboardVisible(View view, boolean visible) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(INPUT_METHOD_SERVICE);
		if (visible) {
			imm.showSoftInput(view, 0);
		} else {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

}
