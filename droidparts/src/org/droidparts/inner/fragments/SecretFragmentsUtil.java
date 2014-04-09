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
package org.droidparts.inner.fragments;

import static org.droidparts.util.ResourceUtils.dpToPx;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

public class SecretFragmentsUtil {

	public static View fragmentActivityBuildLoadingIndicator(Context ctx) {
		boolean large = (ctx.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_NORMAL;
		boolean fresh = Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
		int px = (large && fresh) ? 64 : 56;
		FrameLayout fl = new FrameLayout(ctx);
		fl.setMinimumWidth(dpToPx(ctx, px));
		ProgressBar pb = new ProgressBar(ctx);
		px = dpToPx(ctx, 32);
		fl.addView(pb, new LayoutParams(px, px, Gravity.CENTER));
		return fl;
	}

	protected static final int CONTENT_VIEW_ID = 140584;

	public static void singleFragmentActivitySetContentView(Activity activity) {
		FrameLayout fl = new FrameLayout(activity);
		fl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		fl.setId(CONTENT_VIEW_ID);
		activity.setContentView(fl);
	}

}
