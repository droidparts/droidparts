package org.droidparts.gram.persist;

import java.util.Arrays;
import java.util.HashSet;

import org.droidparts.gram.R;
import org.droidparts.persist.AbstractPrefsManager;
import org.droidparts.preference.MultiSelectListPreference;

import android.content.Context;

public class PrefsManager extends AbstractPrefsManager {

	private static final int VERSION = 1;

	public PrefsManager(Context ctx) {
		super(ctx, VERSION);
	}

	public boolean isShowDetailFilter() {
		return getShowDetailVaules().contains(ctx.getString(R.string.pref_filter));
	}

	public boolean isShowDetailTags() {
		return getShowDetailVaules().contains(ctx.getString(R.string.pref_tags));
	}

	private HashSet<String> getShowDetailVaules() {
		String str = getString(R.string.pref_show_detail,
				R.string.pref_show_detail);
		String[] arr = MultiSelectListPreference
				.fromPersistedPreferenceValue(str);
		return new HashSet<String>(Arrays.asList(arr));
	}

}
