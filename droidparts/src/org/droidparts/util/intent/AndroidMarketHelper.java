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
package org.droidparts.util.intent;

import static android.content.Intent.ACTION_VIEW;

import org.droidparts.util.L;
import org.droidparts.util.ui.AbstractDialogFactory;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AndroidMarketHelper {

	public static void showDetail(Context ctx, String pkgName) {
		launchMarket(ctx, "market://details?id=" + pkgName);
	}

	public static void searchPublisher(Context ctx, String pubName) {
		launchMarket(ctx, "market://search?q=pub:" + pubName);
	}

	public static void search(Context ctx, String query) {
		launchMarket(ctx, "market://search?q=" + query);
	}

	private static void launchMarket(Context ctx, String query) {
		try {
			Intent intent = new Intent(ACTION_VIEW, Uri.parse(query));
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			L.w(e);
			new AbstractDialogFactory(ctx).showErrorToast();
		}
	}

}
