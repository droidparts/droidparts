/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.concurrent.task;

import android.content.Context;
import android.util.Pair;

import org.droidparts.Injector;
import org.droidparts.util.L;

public abstract class AsyncTask<Params, Progress, Result>
		extends android.os.AsyncTask<Params, Progress, Pair<Exception, Result>> {

	private final Context ctx;
	private final ResultListener<Result> resultListener;

	public AsyncTask(Context ctx) {
		this(ctx, null);
	}

	public AsyncTask(Context ctx, ResultListener<Result> resultListener) {
		Injector.inject(ctx, this);
		this.ctx = ctx.getApplicationContext();
		this.resultListener = resultListener;
	}

	protected Context getContext() {
		return ctx;
	}

	@Override
	protected final Pair<Exception, Result> doInBackground(Params... params) {
		Result res = null;
		Exception ex = null;
		try {
			long start = System.currentTimeMillis();
			res = onExecute(params);
			L.i("Executed %s in %d ms.", getClass().getSimpleName(), (System.currentTimeMillis() - start));
		} catch (Exception e) {
			L.d(e);
			ex = e;
		}
		return new Pair<Exception, Result>(ex, res);
	}

	@Override
	protected final void onPostExecute(Pair<Exception, Result> result) {
		if (result.first != null) {
			onPostExecuteFailure(result.first);
			if (resultListener != null) {
				resultListener.onFailure(result.first);
			}
		} else {
			onPostExecuteSuccess(result.second);
			if (resultListener != null) {
				resultListener.onSuccess(result.second);
			}
		}
	}

	protected abstract Result onExecute(Params... params) throws Exception;

	protected void onPostExecuteSuccess(Result result) {
	}

	protected void onPostExecuteFailure(Exception exception) {
	}

}
