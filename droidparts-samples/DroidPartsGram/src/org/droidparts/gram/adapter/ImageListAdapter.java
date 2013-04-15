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
package org.droidparts.gram.adapter;

import static org.droidparts.util.Strings.join;

import org.droidparts.adapter.cursor.EntityCursorAdapter;
import org.droidparts.adapter.tag.IconText2Tag;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.gram.R;
import org.droidparts.gram.model.Image;
import org.droidparts.net.image.ImageFetcher;
import org.droidparts.persist.sql.stmt.Select;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ImageListAdapter extends EntityCursorAdapter<Image> implements
		OnScrollListener {

	@InjectDependency
	private ImageFetcher imageFetcher;

	private Drawable placeholderDrawable;

	public ImageListAdapter(Context ctx, Select<Image> select) {
		super(Image.class, ctx, select);
	}

	@Override
	public final View newView(Context ctx, Cursor cursor, ViewGroup parent) {
		View view = layoutInflater.inflate(R.layout.list_row_image, null);
		IconText2Tag tag = new IconText2Tag(view);
		view.setTag(tag);
		if (placeholderDrawable == null) {
			placeholderDrawable = tag.icon.getDrawable();
		}
		return view;
	}

	@Override
	public void bindView(Context ctx, View view, Image item) {
		entityManager.fillForeignKeys(item);
		IconText2Tag tag = (IconText2Tag) view.getTag();
		tag.text1.setText(item.captionText);
		tag.text2.setText(buildDescription(item));
		tag.icon.setImageDrawable(placeholderDrawable);
		imageFetcher.attachImage(tag.icon, item.thumbnailUrl);
	}

	private Spanned buildDescription(Image img) {
		StringBuilder sb = new StringBuilder();
		sb.append("<b>").append(img.filter.name).append("</b>");
		sb.append(" ");
		sb.append(join(img.tags, ", ", null));
		return Html.fromHtml(sb.toString());
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:
			imageFetcher.pause();
			break;
		default:
			imageFetcher.resume(true);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// n/a
	}

}
