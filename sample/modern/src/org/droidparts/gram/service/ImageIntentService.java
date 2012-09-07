package org.droidparts.gram.service;

import java.util.ArrayList;

import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.gram.contract.Instagram;
import org.droidparts.gram.model.Image;
import org.droidparts.gram.persist.ImageEntityManager;
import org.droidparts.gram.persist.ImageSerializer;
import org.droidparts.http.RESTClient2;
import org.droidparts.service.SimpleIntentService;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ImageIntentService extends SimpleIntentService {

	private static final String ACTION_REFRESH = "refresh";

	public static Intent getUpdatePicsIntent(Context ctx,
			ResultReceiver resultReceiver) {
		return getIntent(ctx, ImageIntentService.class, ACTION_REFRESH,
				resultReceiver);
	}

	private final Uri refreshUri;
	private RESTClient2 restClient;

	@InjectDependency
	private ImageEntityManager imageEntityManager;

	public ImageIntentService() {
		super(ImageIntentService.class.getName());
		refreshUri = Uri
				.parse(Instagram.Url.POPULAR_MEDIA)
				.buildUpon()
				.appendQueryParameter(Instagram.Param.CLIENT_ID,
						Instagram.CLIENT_ID).build();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		restClient = new RESTClient2(this, Instagram.USER_AGENT);
	}

	@Override
	protected Bundle execute(String action, Bundle data) throws Exception {
		if (ACTION_REFRESH.equals(action)) {
			JSONObject obj = restClient.getJSONObject(refreshUri.toString());
			JSONArray arr = obj.getJSONArray("data");
			ArrayList<Image> list = new ImageSerializer().deserialize(arr);
			imageEntityManager.delete().execute();
			imageEntityManager.create(list);
			return data;
		} else {
			throw new IllegalArgumentException("Unsupported action: " + action);
		}
	}
}
