/**
 * Copyright 2011 Alex Yanchenko
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
import android.content.Intent;
import android.net.Uri;

public class IntentFactory {

	public static Intent getShare(String subject, String body) {
		Intent intent = new Intent(ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(EXTRA_SUBJECT, subject);
		intent.putExtra(EXTRA_TEXT, body);
		return intent;
	}

	public static Intent getSendEmail(String mailTo, String subject, String body) {
		Intent intent = new Intent(ACTION_SENDTO);
		intent.setType("text/plain");
		if (mailTo == null) {
			mailTo = "";
		}
		intent.setData(Uri.parse("mailto:" + mailTo));
		if (isNotEmpty(subject)) {
			intent.putExtra(EXTRA_SUBJECT, subject);
		}
		if (isNotEmpty(body)) {
			intent.putExtra(EXTRA_TEXT, body);
		}
		return intent;
	}

	public static Intent getOpenUrl(String webAddress) {
		Intent intent = new Intent(ACTION_VIEW, Uri.parse(webAddress));
		return intent;
	}

	public static Intent getDial(String phoneNumber) {
		Intent intent = new Intent(ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + phoneNumber));
		return intent;
	}

}
