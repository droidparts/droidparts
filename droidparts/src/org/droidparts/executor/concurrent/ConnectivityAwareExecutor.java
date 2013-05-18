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
package org.droidparts.executor.concurrent;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;

import org.droidparts.util.L;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityAwareExecutor extends BackgroundExecutor {

	private final Context ctx;
	private final int slowMobileThreads, fastMobileThreads, wifiThreads;

	private final ConnectivityManager connectivityManager;

	public ConnectivityAwareExecutor(Context ctx) {
		this(ctx, 1, 2, 4);
	}

	public ConnectivityAwareExecutor(Context ctx, int slowMobileThreads,
			int fastMobileThreads, int wifiThreads) {
		super(1);
		this.ctx = ctx.getApplicationContext();
		this.slowMobileThreads = slowMobileThreads;
		this.fastMobileThreads = fastMobileThreads;
		this.wifiThreads = wifiThreads;
		//
		connectivityManager = (ConnectivityManager) ctx
				.getSystemService(CONNECTIVITY_SERVICE);
		//
		ctx.registerReceiver(connectivityReceiver, new IntentFilter(
				CONNECTIVITY_ACTION));
	}

	@Override
	protected void terminated() {
		super.terminated();
		ctx.unregisterReceiver(connectivityReceiver);
	}

	private void detemineNetworTypeAndUpdatePoolSize() {
		try {
			NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
			int threadCount;
			switch (netInfo.getType()) {
			case TYPE_MOBILE:
				// slower than 3g
				if (netInfo.getSubtype() < 3) {
					threadCount = slowMobileThreads;
				} else {
					threadCount = fastMobileThreads;
				}
				break;
			case TYPE_WIMAX:
				threadCount = fastMobileThreads;
				break;
			case TYPE_WIFI:
				threadCount = wifiThreads;
				break;
			default:
				threadCount = 1;
			}
			L.i("Pool size: %d.", threadCount);
			setCorePoolSize(threadCount);
			setMaximumPoolSize(threadCount);
		} catch (SecurityException e) {
			L.e("'android.permission.ACCESS_NETWORK_STATE' required.");
		} catch (Exception e) {
			L.e(e);
		}

	}

	private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			detemineNetworTypeAndUpdatePoolSize();
		}
	};

}
