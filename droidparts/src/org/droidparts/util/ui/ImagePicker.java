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
package org.droidparts.util.ui;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.pm.PackageManager.FEATURE_CAMERA;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

import java.io.File;
import java.util.ArrayList;

import org.droidparts.adapter.widget.ArrayAdapter;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class ImagePicker {

	public interface Listener {

		void didPickImage(Bitmap bm);

	}

	private final Activity activity;
	private final Listener listener;

	public ImagePicker(Activity activity, Listener listener) {
		this.activity = activity;
		this.listener = listener;
	}

	public void showDialog(boolean deleteOptionAvailable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		ProfilePictureAdapter adapter = new ProfilePictureAdapter(
				deleteOptionAvailable);
		builder.setAdapter(adapter, adapter);
		builder.setNeutralButton(getLocalization().cancel, null);
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	public boolean handleActivityResult(int requestCode, int resultCode,
			Intent data) {
		boolean handled = false;
		Bitmap bm = null;
		switch (requestCode) {
		case RC_TAKE_A_PICTURE:
			handled = true;
			if (resultCode == RESULT_OK) {
				File tmpFile = getTmpFile();
				bm = readFromUri(activity, Uri.fromFile(tmpFile));
				tmpFile.delete();
			}
			break;
		case RC_CHOOSE_FROM_LIBRARY:
			handled = true;
			if (resultCode == RESULT_OK) {
				bm = readFromUri(activity, data.getData());
			}
			break;
		}
		if (bm != null) {
			listener.didPickImage(bm);
		}
		return handled;
	}

	public static class Localization {
		public String takeAPicture;
		public String chooseFromLibrary;
		public String delete;
		public String cancel;
	}

	protected Localization getLocalization() {
		Localization loc = new Localization();
		loc.takeAPicture = "Take a Picture";
		loc.chooseFromLibrary = "Choose from Library";
		loc.delete = "Delete";
		loc.cancel = "Cancel";
		return loc;
	}

	protected File getTmpFile() {
		return new File(Environment.getExternalStorageDirectory(), "tmp.pic");
	}

	private static final int RC_TAKE_A_PICTURE = 1405;
	private static final int RC_CHOOSE_FROM_LIBRARY = 8405;

	private static Bitmap readFromUri(Context ctx, Uri uri) {
		Bitmap bm = null;
		try {
			bm = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(),
					uri);
		} catch (Exception e) {
			L.w(e);
		}
		return bm;
	}

	private class ProfilePictureAdapter extends ArrayAdapter<String> implements
			DialogInterface.OnClickListener {

		private final boolean cameraAvailable;

		public ProfilePictureAdapter(boolean deleteAvailable) {
			super(activity, new ArrayList<String>());
			cameraAvailable = activity.getPackageManager().hasSystemFeature(
					FEATURE_CAMERA);
			Localization loc = getLocalization();
			ArrayList<String> options = new ArrayList<String>();
			if (cameraAvailable) {
				options.add(loc.takeAPicture);
			}
			options.add(loc.chooseFromLibrary);
			if (deleteAvailable) {
				options.add(loc.delete);
			}
			setContent(options);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = null;
			int requestCode = -1;
			switch (which) {
			case 0:
				intent = new Intent(ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(getTmpFile()));
				requestCode = RC_TAKE_A_PICTURE;
				break;
			case 1:
				intent = new Intent(ACTION_GET_CONTENT);
				intent.setType("image/*");
				requestCode = RC_CHOOSE_FROM_LIBRARY;
				break;
			case 2:
				listener.didPickImage(null);
				break;
			}
			if (intent != null) {
				try {
					activity.startActivityForResult(intent, requestCode);
				} catch (Exception e) {
					L.w(e);
					new AbstractDialogFactory(activity).showErrorToast();
				}
			}
		}

	}

}
