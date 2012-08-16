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
package org.droidparts.util.intent;

import org.droidparts.manager.AbstractDialogFactory;
import org.droidparts.util.L;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class IntentHelper {

	private Context ctx;

	public IntentHelper(Context ctx) {
		this.ctx = ctx;
	}

	public void startChooserOrWarn(Intent intent) {
		startChooserOrWarn(intent, null);
	}

	public void startChooserOrWarn(Intent intent, String title) {
		Intent choooserIntent = Intent.createChooser(intent, title);
		startOrWarn(choooserIntent);
	}

	public void startOrWarn(Intent intent) {
		try {
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			L.e(e);
			new AbstractDialogFactory(ctx).showErrorToast();
		}
	}

}
