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
package org.droidparts.task;

import org.droidparts.inject.Injector;
import org.droidparts.task.listener.AsyncTaskProgressListener;
import org.droidparts.task.listener.AsyncTaskResultListener;
import org.droidparts.util.L;

import android.content.Context;
import android.util.Pair;

public abstract class AsyncTask<Params, Progress, Result> extends
		android.os.AsyncTask<Params, Progress, Pair<Exception, Result>> {

	protected final Context ctx;
	protected final AsyncTaskProgressListener progressListener;
	protected final AsyncTaskResultListener<Result> resultListener;

	public AsyncTask(Context ctx) {
		this(ctx, null, null);
	}

	public AsyncTask(Context ctx, AsyncTaskProgressListener progressListener,
			AsyncTaskResultListener<Result> resultListener) {
		Injector.get().inject(ctx, this);
		this.ctx = ctx;
		this.progressListener = progressListener;
		this.resultListener = resultListener;
	}

	@Override
	protected void onPreExecute() {
		if (progressListener != null) {
			progressListener.show();
		}
	}

	@Override
	protected final Pair<Exception, Result> doInBackground(Params... params) {
		Result res = null;
		Exception ex = null;
		try {
			long start = System.currentTimeMillis();
			res = executeInBackground(params);
			long end = System.currentTimeMillis();
			L.d(String.format("Executed %s in %d ms.", getClass()
					.getSimpleName(), (end - start)));
		} catch (Exception e) {
			ex = e;
		}
		return new Pair<Exception, Result>(ex, res);
	}

	@Override
	protected void onCancelled() {
		if (progressListener != null) {
			progressListener.dismiss();
		}
	}

	@Override
	protected final void onPostExecute(Pair<Exception, Result> result) {
		if (progressListener != null) {
			progressListener.dismiss();
		}
		if (result.first != null) {
			onFailurePostExecute(result.first);
		} else {
			onSuccessPostExecute(result.second);
		}
	}

	public abstract Result executeInBackground(Params... params)
			throws Exception;

	protected void onSuccessPostExecute(Result result) {
		if (resultListener != null) {
			resultListener.onAsyncTaskSuccess(result);
		}
	}

	protected void onFailurePostExecute(Exception exception) {
		if (resultListener != null) {
			resultListener.onAsyncTaskFailure(exception);
		}
	}

}
