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
package org.droidparts.service;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public abstract class SimpleIntentService extends IntentService {

	public static final String EXTRA_EXCEPTION = "exception";

	private static final String EXTRA_RESULT_RECEIVER = "result_receiver";

	protected final static Intent getIntent(Context ctx,
			Class<? extends SimpleIntentService> cls, String action) {
		Intent intent = new Intent(ctx, cls);
		intent.setAction(action);
		return intent;
	}

	protected final static Intent getIntent(Context ctx,
			Class<? extends SimpleIntentService> cls, String action,
			ResultReceiver resultReceiver) {
		Intent intent = new Intent(ctx, cls);
		intent.setAction(action);
		intent.putExtra(EXTRA_RESULT_RECEIVER, resultReceiver);
		return intent;
	}

	public SimpleIntentService(String name) {
		super(name);
	}

	@Override
	protected final void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		Bundle data = intent.getExtras();
		if (data == null) {
			data = new Bundle();
		}
		ResultReceiver resultReceiver = data
				.getParcelable(EXTRA_RESULT_RECEIVER);
		try {
			data = execute(action, data);
			if (resultReceiver != null) {
				resultReceiver.send(RESULT_OK, data);
			}
		} catch (Exception e) {
			if (resultReceiver != null) {
				data.putSerializable(EXTRA_EXCEPTION, e);
				resultReceiver.send(RESULT_CANCELED, data);
			}
		}
	}

	protected abstract Bundle execute(String action, Bundle data)
			throws Exception;

}
