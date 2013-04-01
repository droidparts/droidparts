DroidParts
----------
a carefully crafted Android framework that includes:
* *DI* - dependency injection for Views, resources, etc.
* *SQLite* object-relational mapping.
* *JSON* (de)serialization.
* Improved *AsyncTasks* with Exceptions, progress & result reporting.
* Better *logger* (log any object without a tag).
* *RESTClient* for GETting, PUTting, POSTing, DELETing & InputStream-getting,
also speaks JSON.
* *ImageFetcher* to asynchronously attach images to ImageViews, with
caching, cross-fade & transformation support.
* Numerous *Utils*.
* Support for *Fragments*: native, support-v4 and [ActionBarSherlock][1]-backed.

Documentation
-------------
available at http://droidparts.org.

Download
--------
[the latest JAR][2], get from Maven:
```xml
<dependency>
  <groupId>org.droidparts</groupId>
  <artifactId>droidparts</artifactId>
  <version>${version.from.jar.above}</version>
</dependency>
```
or use as a plain old Android library project.

 [1]: https://github.com/JakeWharton/ActionBarSherlock
 [2]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.droidparts&a=droidparts&v=LATEST