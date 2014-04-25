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
package org.droidparts.concurrent.thread;

import static org.droidparts.util.Strings.isNotEmpty;
import android.os.Process;

public class BackgroundThread extends Thread {

	public BackgroundThread(String name) {
		initName(name);
	}

	public BackgroundThread(Runnable r, String name) {
		super(r);
		initName(name);
	}

	private void initName(String name) {
		if (isNotEmpty(name)) {
			setName(name + "-" + getId());
		}
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		super.run();
	}

}
