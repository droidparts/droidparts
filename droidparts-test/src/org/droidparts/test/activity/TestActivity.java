package org.droidparts.test.activity;

import org.droidparts.activity.legacy.Activity;
import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.test.R;

import android.widget.TextView;

public class TestActivity extends Activity {

	@InjectResource(R.string.test_string)
	public String testString;

	@InjectView(id = R.id.view_text)
	public TextView textView;

	@Override
	public void onPreInject() {
		setContentView(R.layout.activity_test);
	}

}
