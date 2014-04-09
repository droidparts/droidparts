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
package org.droidparts.util.intent;

import static android.content.Intent.ACTION_DIAL;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SENDTO;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static org.droidparts.util.Strings.isNotEmpty;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentFactory {

	public static class PlayStore {

		public static Intent getAppIntent(Context ctx, String pkgName) {
			String uri = "market://details?id=" + pkgName;
			return new Intent(ACTION_VIEW, Uri.parse(uri));
		}

		public static Intent getPublisherIntent(Context ctx, String pubName) {
			String uri = "market://search?q=pub:" + pubName;
			return new Intent(ACTION_VIEW, Uri.parse(uri));
		}

		public static Intent getSearchIntent(Context ctx, String query) {
			String uri = "market://search?q=" + query;
			return new Intent(ACTION_VIEW, Uri.parse(uri));
		}

	}

	public static Intent getShareIntent(String subject, CharSequence body) {
		Intent intent = new Intent(ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(EXTRA_SUBJECT, subject);
		intent.putExtra(EXTRA_TEXT, body);
		return intent;
	}

	public static Intent getSendEmailIntent(String mailTo, String mailCC,
			String subject, CharSequence body, File attachment) {
		Intent intent = new Intent(ACTION_SENDTO);
		// intent.setType("text/plain");
		intent.setType("message/rfc822");
		if (mailTo == null) {
			mailTo = "";
		}
		intent.setData(Uri.parse("mailto:" + mailTo));
		if (isNotEmpty(mailCC)) {
			intent.putExtra(Intent.EXTRA_CC, new String[] { mailCC });
		}
		if (isNotEmpty(subject)) {
			intent.putExtra(EXTRA_SUBJECT, subject);
		}
		if (isNotEmpty(body)) {
			intent.putExtra(EXTRA_TEXT, body);
		}
		if (attachment != null) {
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
		}
		return intent;
	}

	public static Intent getSendSMSIntent(String msg) {
		Intent intent = new Intent(ACTION_VIEW);
		intent.setType("vnd.android-dir/mms-sms");
		intent.putExtra("sms_body", msg);
		return intent;
	}

	public static Intent getOpenUrlIntent(String webAddress) {
		Intent intent = new Intent(ACTION_VIEW, Uri.parse(webAddress));
		return intent;
	}

	public static Intent getDialIntent(String phoneNumber) {
		Intent intent = new Intent(ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		return intent;
	}

}
