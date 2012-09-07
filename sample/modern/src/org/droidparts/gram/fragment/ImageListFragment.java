package org.droidparts.gram.fragment;

import org.droidparts.annotation.inject.InjectParentActivity;
import org.droidparts.fragment.AlterableContent;
import org.droidparts.fragment.ListFragment;
import org.droidparts.gram.adapter.ImageListAdapter;

import android.view.View;
import android.widget.ListView;

public class ImageListFragment extends ListFragment implements
		AlterableContent<ImageListAdapter> {

	public static interface Listener {

		void onShowImageDetail(int position);

	}

	@InjectParentActivity
	private Listener listener;

	@Override
	public void setContent(ImageListAdapter adapter) {
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.onShowImageDetail(position);
	}

}
