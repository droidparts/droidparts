DroidParts
----------
a carefully crafted Android framework that includes:
* *DI* - injection of Views, Fragments, Services, etc.
* *ORM* - efficient persistence utilizing Cursors & fluent API.
* *EventBus* for posting event notifications.
* Simple *JSON* (de)serialization capable of handling nested objects.
* Improved *AsyncTask* & *IntentService* with Exceptions & result reporting support.
* *Logger* that figures out tag itself & logs any object.
* *RESTClient* for GETting, PUTting, POSTing, DELETing & InputStream-getting,
  also speaks JSON.
* *ImageFetcher* to asynchronously attach images to ImageViews, with caching,
  cross-fade & transformation support.
* Numerous *Utils*.

Documentation
-------------
available at http://droidparts.org.

Download
--------
[the latest JAR][1], get from Maven:
```xml
<dependency>
  <groupId>org.droidparts</groupId>
  <artifactId>droidparts</artifactId>
  <version>${version.from.jar.above}</version>
</dependency>
```
or Gradle:
```groovy
dependencies {
   compile 'org.droidparts:droidparts:${version.from.jar.above}'
}
```
or use as a plain old Android library project.

 [1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=org.droidparts&a=droidparts&v=LATEST
