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
package org.droidparts.gram.service;

import java.util.ArrayList;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.bus.EventBus;
import org.droidparts.concurrent.service.IntentService;
import org.droidparts.gram.contract.Instagram;
import org.droidparts.gram.model.Image;
import org.droidparts.gram.persist.ImageEntityManager;
import org.droidparts.gram.persist.ImageSerializer;
import org.droidparts.net.http.RESTClient2;
import org.droidparts.net.http.worker.OkHttpWorker;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ImageIntentService extends IntentService {

	private static final String ACTION_REFRESH = "refresh";

	public static Intent getUpdatePicsIntent(Context ctx,
			ResultReceiver resultReceiver) {
		return getIntent(ctx, ImageIntentService.class, ACTION_REFRESH,
				resultReceiver);
	}

	private final Uri refreshUri;
	private RESTClient2 restClient;
	private ImageSerializer imageSerializer;

	@InjectDependency
	private ImageEntityManager imageEntityManager;

	public ImageIntentService() {
		super(ImageIntentService.class.getSimpleName());
		refreshUri = Uri
				.parse(Instagram.Url.POPULAR_MEDIA)
				.buildUpon()
				.appendQueryParameter(Instagram.Param.CLIENT_ID,
						Instagram.CLIENT_ID).build();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		restClient = new RESTClient2(this, new OkHttpWorker(this));
		imageSerializer = new ImageSerializer(this);
	}

	@Override
	protected Bundle onExecute(String action, Bundle data) throws Exception {
		if (ACTION_REFRESH.equals(action)) {
			JSONObject obj = restClient.getJSONObject(refreshUri.toString());
			JSONArray arr = obj.getJSONArray("data");
			ArrayList<Image> list = imageSerializer.deserialize(arr);
			imageEntityManager.delete().execute();
			imageEntityManager.create(list);
			EventBus.postEvent("REFRESH_COMPLETE", list);
			return data;
		} else {
			throw new UnsupportedOperationException(action);
		}
	}
}
