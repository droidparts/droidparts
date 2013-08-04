DroidParts
----------
a carefully crafted Android framework that includes:
* *DI* - injection of Views, Fragments, Services, etc.
* *ORM* - efficient persistence utilizing Cursors & fluent API.
* *EventBus* for posting event notifications.
* Simple *JSON* (de)serialization capable of handling nested objects.
* Improved *AsyncTask* & *IntentService* with Exceptions & result reporting support.
* *L*ogger that figures out tag itself & logs any object.
* *RESTClient* for GETting, PUTting, POSTing, DELETing & InputStream-getting,
  also speaks JSON.
* *ImageFetcher* to asynchronously attach images to ImageViews, with caching,
  cross-fade & transformation support.
* Numerous *Utils*.
* *Fragments* support: native on 3.0+ and either
  pure [SupportLibrary][1] or [ActionBarSherlock][2]-backed on 2.2+.

Documentation
-------------
available at http://droidparts.org.

Download
--------
[the latest JAR][3], get from Maven:
```xml
<dependency>
  <groupId>org.droidparts</groupId>
  <artifactId>droidparts</artifactId>
  <version>${version.from.jar.above}</version>
</dependency>
```
or use as a plain old Android library project.

 [1]: http://developer.android.com/tools/extras/support-library.html
 [2]: https://github.com/JakeWharton/ActionBarSherlock
 [3]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.droidparts&a=droidparts&v=LATEST