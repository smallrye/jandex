# Jandex 

Jandex is a space efficient Java annotation indexer and offline reflection library.

## Features
 
It supports the following capabilities:
 
* Indexing all runtime visible Java annotations for a set of classes into a memory efficient representation
* Indexing the class hierarchy and interface implementation of a set classes
* Browsing and searching of declared methods on an indexed class
* Browsing and searching of declared fields on an indexed class
* Browsing of all generic type information on methods, fields, and classes
* Browsing and searching annotations, including Java 8 type annotations
* Persisting an index into a custom storage efficient format
* Quick-loading of the storage efficient format
* Compatibility with previous API and storage format versions
* Execution via an API, a command line tool, Ant, and a Maven plugin

## Downloading Jandex

Jandex artifacts can be [downloaded from Maven Central](https://search.maven.org/search?q=g:io.smallrye%20a:jandex).

## API Docs

The extensive API docs for Jandex [are available here](http://wildfly.github.io/jandex).

## Reporting issues

Issues can be reported in [GitHub Issues](https://github.com/smallrye/jandex/issues).
 
## Getting Help

Post to the [SmallRye Google group](https://groups.google.com/g/smallrye) or the [Quarkus Zulip chat](https://quarkusio.zulipchat.com/).

## Creating a Persisted Index Using the CLI

The following example demonstrates indexing hibernate core, followed by the entire Java
JDK 8 using Jandex on the CLI:
 
```shell
$ java -jar jandex-2.4.2.Final.jar hibernate-core-6.0.0.Final.jar 
   Wrote hibernate-core-6.0.0.Final-jar.idx in 0.9170 seconds
         (5746 classes, 50 annotations, 2995 instances, 61729 class usages, 1737428 bytes)
$ java -jar jandex-2.4.2.Final.jar rt.jar 
   Wrote rt-jar.idx in 1.7310 seconds
         (20226 classes, 57 annotations, 2476 instances, 246298 class usages, 5890787 bytes)
```

The above summary output tells us that this version of Hibernate ORM has 5,746 classes, and those classes contained 2,995 annotation declarations, using 50 different annotation types.
The resulting index is 1.7 MB uncompressed, which is only 19% of the 9.0MB compressed jar size, or 4% of the uncompressed class file data.
If the index is stored in the jar (using the -m option) it can be compressed an additional 43%, leading to a jar growth of only 11%.
 
## Using the Ant task to index your project

The following Ant task can be used with either the `maven-antrun-plugin` or an Ant build to build an index for your project:

```xml
<taskdef name="jandex" classname="org.jboss.jandex.JandexAntTask"/>
<jandex run="@{jandex}">
    <fileset dir="${location.to.index.dir}"/>
</jandex>
```

## Using the Maven plugin to index your project

### Basic Usage

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.smallrye</groupId>
            <artifactId>jandex-maven-plugin</artifactId>
            <version>3.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>make-index</id>
                    <goals>
                        <goal>jandex</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

The `jandex` goal is bound to the `process-classes` phase by default.

This configuration will index all `.class` files in your `target/classes` directory, and write the index to `target/classes/META-INF/jandex.idx`.

### Advanced Usage

If you need to process more than one directory of classes, you can specify multiple `fileSets` like this:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.smallrye</groupId>
            <artifactId>jandex-maven-plugin</artifactId>
            <version>3.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>make-index</id>
                    <goals>
                        <goal>jandex</goal>
                    </goals>
                    <configuration>
                        <fileSets>
                            <fileSet>
                                <directory>${project.build.directory}/generated-classes/foo</directory>
                            </fileSet>
                            <fileSet>
                                <directory>${project.build.directory}/generated-classes/bar</directory>
                            </fileSet>
                        </fileSets>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

To turn _off_ processing of `target/classes`, add the following configuration:

```xml
<processDefaultFileSet>false</processDefaultFileSet>
```

For any `fileSet`, you can also fine-tune the classes that are processed using the following options:

```xml
<fileSet>
    <directory>${project.build.directory}/somedir</directory>
    <!-- included globs -->
    <includes>
        <include>**/indexed/*.class</include>
        <include>**/idxd/*.class</include>
    </includes>
    <!-- excluded globs -->
    <excludes>
        <exclude>**/*NotIndexed.class</exclude>
    </excludes>
    <!-- normally true, this excludes things like the CVS/ and .svn/ directories, log files, etc. -->
    <useDefaultExcludes>false</useDefaultExcludes>
</fileSet>
```
         
A `fileSet` may specify a _dependency_ instead of a directory.
That dependency must exist among the set of dependencies of the Maven project being built.
A `groupId` and `artifactId` are mandatory, a `classifier` is optional:

```xml
<fileSet>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>my-project</artifactId>
    </dependency>
    <includes>
        <include>com/example/my/project/api/**/*.class</include>
    </includes>
    <excludes>
        <exclude>com/example/**/_private/*.class</exclude>
    </excludes>
</fileSet>
```

## Adding the Jandex API to your Maven project

Just add the following to your POM:

```xml
<dependency>
    <groupId>io.smallrye</groupId>
    <artifactId>jandex</artifactId>
    <version>${version.jandex}</version>
</dependency>
```

## Browsing a Class

The following example demonstrates indexing a class and browsing its methods:

```java
// Index java.util.Map
Indexer indexer = new Indexer();
indexer.indexClass(Map.class);
Index index = indexer.complete();
 
// Retrieve Map from the index and print its declared methods
ClassInfo clazz = index.getClassByName(DotName.createSimple("java.util.Map"));
 
for (MethodInfo method : clazz.methods()) {
  System.out.println(method);
}
```

## Creating a Persisted Index

The following example demonstrates indexing a class and persisting the index to disk.
The resulting file can later be loaded and used.

```java
// Index java.util.Map
Indexer indexer = new Indexer();
indexer.indexClass(Map.class);
Index index = indexer.complete();
 
FileOutputStream out = new FileOutputStream("/tmp/index.idx");
IndexWriter writer = new IndexWriter(out);
 
try {
  writer.write(index);
} finally {
  safeClose(out);
}
```

## Loading a Persisted Index
 
The following example demonstrates loading the index from the previous example and using that index to print all methods on `java.util.Map`:

```java
FileInputStream input = new FileInputStream("/tmp/index.idx");
IndexReader reader = new IndexReader(input);
try {
  index = reader.read();
} finally {
  safeClose(input);
}
 
// Retrieve Map from the index and print its declared methods
ClassInfo clazz = index.getClassByName(DotName.createSimple("java.util.Map"));
 
for (MethodInfo method : clazz.methods()) {
  System.out.println(method);
}
```
 
## Searching for an Annotation
 
The following example demonstrates indexing the `Thread` and `String` classes, and searching for methods that have been marked with `@Deprecated`:

```java
Indexer indexer = new Indexer();
indexer.indexClass(Thread.class);
indexer.indexClass(String.class);

Index index = indexer.complete();
DotName deprecated = DotName.createSimple("java.lang.Deprecated");
List<AnnotationInstance> annotations = index.getAnnotations(deprecated);
 
for (AnnotationInstance annotation : annotations) {
  switch (annotation.target().kind()) {
    case METHOD:
      System.out.println(annotation.target());
      break;
  }
}
```

## Analyzing Generics

The following example demonstrates indexing the `Collections` class and printing the resolved bound on the `List<T>` method parameter, which resolves to `Comparable` from the method type parameter.

The `sort()` method analyzed by the example is defined in source as:

```java
public static <T extends Comparable<? super T>> void sort(List<T> list)
```

The example code, which prints `Comparable<? super T>` followed by `T`, is: 

```java
Indexer indexer = new Indexer();
indexer.indexClass(Collections.class);
Index index = indexer.complete();
 
// Find method
ClassInfo clazz = index.getClassByName(DotName.createSimple("java.util.Collections"));
Type listType = Type.create(DotName.createSimple("java.util.List"), Type.Kind.CLASS);
MethodInfo sort = clazz.method("sort", listType);
 
Type t =
  sort.parameters().get(0).asParameterizedType() // List<T extends Comparable<? super T>>
      .arguments().get(0)                        // T extends Comparable<? super T>
      .asTypeVariable().bounds().get(0);         // Comparable<? super T>
 
System.out.println(t);
System.out.println(t.asWildcardType().superBound()); // T
```
 
## Browsing Type Annotations

Consider a complex nested generic structure which contains a `@Label` annotation:

```java
Map<Integer, List<@Label("Name") String>> names
```

The following code will print `Name`, the annotation value associated with the type:

```java
Indexer indexer = new Indexer();
indexer.indexClass(Test.class);
indexer.indexClass(Test.Label.class);
Index index = indexer.complete();
 
DotName test = DotName.createSimple("Test");
FieldInfo field = index.getClassByName(test).field("names");
System.out.println(
  field.type().asParameterizedType().arguments().get(1)
              .asParameterizedType().arguments().get(0)
              .annotations().get(0).value().asString()
);
```

## Searching for Type Annotations

A type annotation can also be located by searching for the annotation. The target for a found type annotation is represented as a `TypeTarget`.
The `TypeTarget` provides a reference to the annotated type, as well as the enclosing target that contains the type.
The target itself can be a method, a class, or a field.
The usage on that target can be a number of places, including parameters, return types, type parameters, type arguments, class extends values, type bounds and receiver types.
Subclasses of `TypeTarget` provide the necessary information to locate the starting point of the usage.

Since the particular type use can occur at any depth, the relevant branch of the type tree constrained by the above starting point must be traversed to understand the context of the use.

Consider a complex nested generic structure which contains a `@Label` annotation:

```java
Map<Integer, List<@Label("Name") String>> names
```

The following code locates a type annotation using a hardcoded path:

```java
Indexer indexer = new Indexer();
indexer.indexClass(Test.class);
indexer.indexClass(Test.Label.class);
Index index = indexer.complete();

DotName label DotName.createSimple("Test$Label");
List<AnnotationInstance> annotations = index.getAnnotations(label);
for (AnnotationInstance annotation : annotations) {
if (annotation.target().kind() == AnnotationTarget.Kind.TYPE) {
    TypeTarget typeTarget = annotation.target().asType();
    System.out.println("Type usage is located within: " + typeTarget.enclosingTarget());
    System.out.println("Usage type: " + typeTarget.usage());
    System.out.println("Target type: "  + typeTarget.target());
    System.out.println("Equivalent? " + (typeTarget.enclosingTarget().asField().type()
                                           .asParameterizedType().arguments().get(1)
                                           .asParameterizedType().arguments().get(0)
                                           == typeTarget.target()));
}
```
The above code prints the following output:

```
   Type usage is located within: java.util.Map<java.lang.Integer,
                                 java.util.List<@Label(value = "Name") java.lang.String>>
                                 Test.names
   Usage type: EMPTY
   Target type: @Label(value = "Name") java.lang.String
   Equivalent? true
```

## Compatibility Promise

Jandex uses an `X.Y.Z` versioning scheme.
In the following text, we call `X` a _major_ version, `Y` a _minor_ version, and `Z` a _micro_ version.

### API

Jandex may break backward compatibility for _callers_ of the Jandex API in major versions.
If you only call Jandex API, updating to a newer minor or micro version is safe.

Jandex may break backward compatibility for users that _extend_ Jandex classes or _implement_ Jandex interfaces in minor or major versions.
If you extend Jandex classes or implement Jandex interfaces, updating to a newer micro version is safe.

### Persistent Index Format

The persistent index format is versioned.
Jandex is backward compatible when it comes to reading the persistent index, but not forward compatible.
In other words, newer Jandex can read older index, but older Jandex can't read newer index.

Jandex may introduce a new persistent index format version in minor or major versions.
If you distribute a Jandex index as part of your artifacts, updating to a newer micro version is safe.
Updating to a newer minor or major version may require consumers of the Jandex index to also update.
