package org.droidparts.gram.activity;

import static org.droidparts.util.Strings.join;

import org.droidparts.activity.PreferenceFragmentActivity;
import org.droidparts.gram.R;
import org.droidparts.preference.MultiSelectListPreference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends PreferenceFragmentActivity implements
		OnPreferenceChangeListener {

	public static Intent getIntent(Context ctx) {
		return new Intent(ctx, SettingsActivity.class);
	}

	private MultiSelectListPreference showDetailPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.preferences);
		//
		showDetailPref = (MultiSelectListPreference) findPreference(getString(R.string.pref_show_detail));
		showDetailPref.setOnPreferenceChangeListener(this);
		onPreferenceChange(showDetailPref, null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if (pref == showDetailPref) {
			pref.setSummary(join(showDetailPref.getCheckedEntries(), ", ", "."));
		}
		return true;
	}
}
