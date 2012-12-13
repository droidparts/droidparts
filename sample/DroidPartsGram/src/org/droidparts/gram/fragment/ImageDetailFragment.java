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
package org.droidparts.gram.fragment;

import static org.droidparts.util.Strings.join;

import org.droidparts.annotation.inject.InjectBundleExtra;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.fragment.sherlock.DialogFragment;
import org.droidparts.gram.R;
import org.droidparts.gram.model.Image;
import org.droidparts.gram.persist.PrefsManager;
import org.droidparts.util.ImageAttacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDetailFragment extends DialogFragment {

	private static final String EXTRA_IMAGE = "img";

	public static ImageDetailFragment newInstance(Image img) {
		Bundle b = new Bundle();
		b.putSerializable(EXTRA_IMAGE, img);
		ImageDetailFragment f = new ImageDetailFragment();
		f.setArguments(b);
		return f;
	}

	@InjectDependency
	private PrefsManager prefsManager;

	@InjectBundleExtra(EXTRA_IMAGE)
	private Image img;

	@InjectView(R.id.view_img)
	private ImageView imgView;
	@InjectView(R.id.view_filter)
	private TextView filterView;
	@InjectView(R.id.view_tags)
	private TextView tagsView;

	@Override
	public View onCreateView(Bundle savedInstanceState,
			LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.dialog_image, null);
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		getDialog().setTitle(img.captionText);
		new ImageAttacher(getActivity()).attachImage(imgView, img.imageUrl);
		if (prefsManager.isShowDetailFilter()) {
			filterView.setText(img.filter.name);
		}
		if (prefsManager.isShowDetailTags()) {
			tagsView.setText(join(img.tags, ", ", null));
		}
	}
}
