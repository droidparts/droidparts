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

import java.lang.reflect.Field;

import org.droidparts.util.L;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public abstract class SimpleIntentService extends IntentService {

	// in
	private static final String EXTRA_RESULT_RECEIVER = "_result_receiver";

	// out
	public static final String EXTRA_ACTION = "_action";
	public static final String EXTRA_EXCEPTION = "_exception";

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

	public void removePendingIntents() {
		if (mHandler != null) {
			mHandler.removeMessages(0);
		}
	}

	public SimpleIntentService(String name) {
		super(name);
		reflect();
	}

	private void reflect() {
		try {
			Field f = android.app.IntentService.class
					.getDeclaredField("mServiceHandler");
			f.setAccessible(true);
			mHandler = (Handler) f.get(this);
		} catch (Exception e) {
			L.w(e);
		}
	}

	private volatile Handler mHandler;

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
			data = execute(action, data);
			if (resultReceiver != null) {
				resultReceiver.send(RESULT_OK, data);
			}
		} catch (Exception e) {
			L.d(e);
			if (resultReceiver != null) {
				data.putSerializable(EXTRA_EXCEPTION, e);
				resultReceiver.send(RESULT_CANCELED, data);
			}
		}
	}

	protected abstract Bundle execute(String action, Bundle data)
			throws Exception;

}
