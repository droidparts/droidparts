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
package org.droidparts.util.intent;

import java.util.ArrayList;
import java.util.List;

import org.droidparts.util.L;
import org.droidparts.util.ui.AbstractDialogFactory;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

public class IntentHelper {

	public static void startChooserOrWarn(Context ctx, Intent intent) {
		startChooserOrWarn(ctx, intent, null);
	}

	public static void startChooserOrWarn(Context ctx, Intent intent,
			String title) {
		Intent choooserIntent = Intent.createChooser(intent, title);
		startActivityOrWarn(ctx, choooserIntent);
	}

	public static void startActivityOrWarn(Context ctx, Intent intent) {
		startActivityOrWarn(ctx, intent, AbstractDialogFactory.ERROR);
	}

	public static void startActivityOrWarn(Context ctx, Intent intent,
			String errorMessage) {
		try {
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			L.w(e);
			new AbstractDialogFactory(ctx).showToast(errorMessage);
		}
	}

	public static boolean gotHandlerForIntent(Context ctx, Intent intent) {
		return ctx.getPackageManager().resolveActivity(intent, 0) != null;
	}

	public static ActivityInfo[] getIntentHandlers(Context ctx, Intent intent) {
		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(
				intent, 0);
		ArrayList<ActivityInfo> activities = new ArrayList<ActivityInfo>();
		if (list != null) {
			for (ResolveInfo ri : list) {
				activities.add(ri.activityInfo);
			}
		}
		return activities.toArray(new ActivityInfo[activities.size()]);
	}

}
