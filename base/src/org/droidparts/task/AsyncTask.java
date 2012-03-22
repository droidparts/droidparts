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
import org.droidparts.model.Tuple;
import org.droidparts.task.listener.AsyncTaskProgressListener;
import org.droidparts.task.listener.AsyncTaskResultListener;

import android.content.Context;

public abstract class AsyncTask<Params, Progress, Result> extends
		android.os.AsyncTask<Params, Progress, Tuple<Exception, Result>> {

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
	protected final Tuple<Exception, Result> doInBackground(Params... params) {
		Result res = null;
		Exception ex = null;
		try {
			res = executeInBackground(params);
		} catch (Exception e) {
			ex = e;
		}
		return new Tuple<Exception, Result>(ex, res);
	}

	@Override
	protected void onCancelled() {
		if (progressListener != null) {
			progressListener.dismiss();
		}
	}

	@Override
	protected final void onPostExecute(Tuple<Exception, Result> result) {
		if (progressListener != null) {
			progressListener.dismiss();
		}
		if (result.k != null) {
			onFailurePostExecute(result.k);
		} else {
			onSuccessPostExecute(result.v);
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

	public Context getContext() {
		return ctx;
	}

}
