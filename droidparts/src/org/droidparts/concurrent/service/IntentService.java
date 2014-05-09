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
package org.droidparts.concurrent.service;

import java.lang.reflect.Field;

import org.droidparts.Injector;
import org.droidparts.util.L;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public abstract class IntentService extends android.app.IntentService {

	// in
	private static final String EXTRA_RESULT_RECEIVER = "__result_receiver__";

	// out
	public static final int RESULT_SUCCESS = Activity.RESULT_OK;
	public static final int RESULT_FAILURE = Activity.RESULT_CANCELED;
	//
	public static final String EXTRA_ACTION = "__action__";
	public static final String EXTRA_EXCEPTION = "__exception__";

	public static final Intent getIntent(Context ctx,
			Class<? extends IntentService> cls, String action) {
		Intent intent = new Intent(ctx, cls);
		intent.setAction(action);
		return intent;
	}

	public static final Intent getIntent(Context ctx,
			Class<? extends IntentService> cls, String action,
			ResultReceiver resultReceiver) {
		Intent intent = getIntent(ctx, cls, action);
		intent.putExtra(EXTRA_RESULT_RECEIVER, resultReceiver);
		return intent;
	}

	public IntentService(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Injector.inject(this);
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
		data.putString(EXTRA_ACTION, action);
		try {
			data = onExecute(action, data);
			if (resultReceiver != null) {
				resultReceiver.send(RESULT_SUCCESS, data);
			}
		} catch (Exception e) {
			L.d(e);
			if (resultReceiver != null) {
				data.putSerializable(EXTRA_EXCEPTION, e);
				resultReceiver.send(RESULT_FAILURE, data);
			}
		}
	}

	public void removePendingIntents() {
		Handler handler = getHandler();
		if (handler != null) {
			handler.removeMessages(0);
		}
	}

	protected abstract Bundle onExecute(String action, Bundle data)
			throws Exception;

	//

	private Handler getHandler() {
		Handler handler = null;
		try {
			if (mServiceHandlerField == null) {
				mServiceHandlerField = android.app.IntentService.class
						.getDeclaredField("mServiceHandler");
				mServiceHandlerField.setAccessible(true);
			}
			handler = (Handler) mServiceHandlerField.get(this);
		} catch (Exception e) {
			L.w(e);
		}
		return handler;
	}

	private static Field mServiceHandlerField;

}
