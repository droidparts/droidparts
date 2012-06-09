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

import static android.content.Intent.ACTION_VIEW;

import org.droidparts.R;
import org.droidparts.util.L;
import org.droidparts.util.ui.DialogFactory;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AndroidMarketHelper {

	private final Context ctx;

	public AndroidMarketHelper(Context ctx) {
		this.ctx = ctx;
	}

	public void showPackage(String pkgName) {
		launchMarket("market://details?id=" + pkgName);
	}

	public void searchPublisher(String pubName) {
		launchMarket("market://search?q=pub:" + pubName);
	}

	public void search(String query) {
		launchMarket("market://search?q=" + query);
	}

	private void launchMarket(String query) {
		try {
			Intent intent = new Intent(ACTION_VIEW, Uri.parse(query));
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			L.e(e);
			new DialogFactory(ctx).showToast(R.string.error_no_android_market);
		}
	}

}
