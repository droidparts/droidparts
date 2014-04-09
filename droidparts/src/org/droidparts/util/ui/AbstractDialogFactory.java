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

import static android.widget.Toast.LENGTH_SHORT;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

public class AbstractDialogFactory {

	public static final String ERROR = "Error";

	private final Context ctx;
	private final LayoutInflater layoutInflater;

	public AbstractDialogFactory(Context ctx) {
		this.ctx = ctx;
		layoutInflater = LayoutInflater.from(ctx);
	}

	protected Context getContext() {
		return ctx;
	}

	protected LayoutInflater getLayoutInflater() {
		return layoutInflater;
	}

	public void showErrorToast() {
		showToast(ERROR);
	}

	public void showToast(int msgResId, Object... formatArgs) {
		showToast(ctx.getString(msgResId, formatArgs));
	}

	public void showToast(CharSequence msg) {
		Toast.makeText(ctx, msg, LENGTH_SHORT).show();
	}

}
