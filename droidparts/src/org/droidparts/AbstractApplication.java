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
package org.droidparts;

import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.ReflectionUtils;
import org.droidparts.inner.converter.Converter;

public abstract class AbstractApplication extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Injector.setUp(this);
		Injector.inject(this, this);
		// http://code.google.com/p/android/issues/detail?id=20915
		ReflectionUtils.classForName("android.os.AsyncTask");
	}

	@Override
	public void onTerminate() {
		// XXX doesn't get called
		Injector.tearDown();
	}

	public final void registerConverters(Converter<?>... converters) {
		for (Converter<?> converter : converters) {
			ConverterRegistry.registerConverter(converter);
		}
	}

}
