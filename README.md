# Jandex 

Jandex is a space efficient Java annotation indexer and offline reflection library.

## Features
 
 It supports the following capabilities:
 
 * Indexing all runtime visible Java annotations for a set of classes into a memory efficient representation.</li>
 * Indexing the class hierarchy and interface implementation of a set classes.</li>
 * Browsing and searching of declared methods on an indexed class.</li>
 * Browsing and searching of declared fields on an indexed class.</li>
 * Browsing of all generic type information on methods, fields, and classes.</li>
 * Browsing and searching annotations, including Java 8 type annotations</li>
 * Persisting an index into a custom storage efficient format.</li>
 * Quick-loading of the storage efficient format</li>
 * Compatibility with previous API and storage format versions</li>
 * Execution via an API, a command line tool, and ant</li>

## Browsing a Class

The following example demonstrates indexing a class and browsing its methods:</p>

```java
// Index java.util.Map
Indexer indexer = new Indexer();
// Normally a direct file is opened, but class-loader backed streams work as well.
InputStream stream = getClass().getClassLoader().getResourceAsStream("java/util/Map.class");
indexer.index(stream);
Index index = indexer.complete();
 
// Retrieve Map from the index and print its declared methods
ClassInfo clazz = index.getClassByName(DotName.createSimple("java.util.Map"));
 
for (MethodInfo method : clazz.methods()) {
  System.out.println(method);
}
```

## Creating a Persisted Index

The following example demonstrates indexing a class and persisting the index to disk. The resulting
file can later be loaded scanning Java classes:

``` java
// Index java.util.Map
Indexer indexer = new Indexer();
// Normally a direct file is opened, but class-loader backed streams work as well.
InputStream stream = getClass().getClassLoader().getResourceAsStream("java/util/Map.class");
indexer.index(stream);
Index index = indexer.complete();
 
FileOutputStream out = new FileOutputStream("/tmp/index.idx");
IndexWriter writer = new IndexWriter(out);
 
try {
  writer.write(index);
} finally {
  safeClose(out);
}
```

## Creating a Persisted Index Using the CLI

The following example demonstrates indexing hibernate core, followed by the entire Java
JDK using Jandex on the CLI:
 
 ```
$ java -jar target/jandex-2.0.0.Alpha1.jar hibernate-core-4.0.0.Final.jar
   Wrote /Users/jason/devel/jandex/hibernate-core-4.0.0.Final-jar.idx in 0.9020 seconds
         (2722 classes, 20 annotations, 1696 instances, 621565 bytes)
$ java -jar target/jandex-2.0.0.Alpha1.jar rt.jar
   Wrote /Users/jason/devel/jandex/rt-jar.idx in 4.2870 seconds
         (19831 classes, 41 annotations, 1699 instances, 4413790 bytes)
```
 The above summary output tells us that this version of hibernate has 2,722 classes, and those classes
 contained 1,696 annotation declarations, using 20 different annotation types. The resulting index
 is 606KB uncompressed, which is only 14% of the 4.1MB compressed jar size, or 4% of the uncompressed
 class file data. If the index is stored in the jar (using the -m option) it can be compressed an additional 47%,
 leading to a jar growth of only 8%</p>
 
 ## Loading a Persisted Index
 
 The following example demonstrates loading the index from the previous example and using that
 index to print all methods on <code>java.util.Map</code>:
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
 
 # Searching for an Annotation
 
The following example demonstrates indexing the Thread and String classes, and searching for methods that have been marked with @Deprecated:

```java
Indexer indexer = new Indexer();
InputStream stream = getClass().getClassLoader().getResourceAsStream("java/lang/Thread.class");
InputStream stream = getClass().getClassLoader().getResourceAsStream("java/lang/String.class");
indexer.index(stream);
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

The following example demonstrates indexing the Collections class and printing the resolved bound on the List<T> method parameter, which resolves to Comparable from the method type parameter.

The sort() method analyzed by the example is defined in source as:

```java
public static <T extends Comparable<? super T>> void sort(List<T> list)
```

The example code, which prints "Comparable<? super T>", followed by "T" is: 

```java
Indexer indexer = new Indexer();
InputStream stream = getClass().getClassLoader().getResourceAsStream("java/util/Collections.class");
indexer.index(stream);
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

Consider a complex nested generic structure which contains a @Label annotation

```java
Map<Integer, List<@Label("Name") String>> names
```

The following code will print "Name", the annotation value associated with the type:

```java
Indexer indexer = new Indexer();
InputStream stream = new FileInputStream("/tmp/Test.class");
indexer.index(stream);
stream = new FileInputStream("/tmp/Test$Label.class");
indexer.index(stream);
Index index = indexer.complete();
 
DotName test = DotName.createSimple("Test");
FieldInfo field = index.getClassByName(test).field("names");
System.out.println(
  field.type().asParameterizedType().arguments().get(1)
              .asParameterizedType().arguments().get(0)
              .annotations().get(0).value().asString()
);
```


##Searching for Type Annotations

A type annotation can also be located by searching for the annotation. The target for a found type annotation is represented as a TypeTarget. The TypeTarget provides a reference to the annotated type, as well as the enclosing target that contains the type. The target itself can be a method, a class, or a field. The usage on that target can be a number of places, including parameters, return types, type parameters, type arguments, class extends values, type bounds and receiver types. Subclasses of TypeTarget provide the necessary information to locate the starting point of the usage.

Since the particular type use can occur at any depth, the relevant branch of the type tree constrained by the above starting point must be traversed to understand the context of the use.

Consider a complex nested generic structure which contains a @Label annotation:

```java
Map<Integer, List<@Label("Name") String>> names
```

The following code locates a type annotation using a hardcoded path:

```java
Indexer indexer = new Indexer();
 
InputStream stream = new FileInputStream("/tmp/Test.class");
indexer.index(stream);
stream = new FileInputStream("/tmp/Test$Label.class");
indexer.index(stream);
 
Index index = indexer.complete();
List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple("Test$Label"));
for (AnnotationInstance annotation : annotations) {
if (annotation.target().kind() == AnnotationTarget.Kind.TYPE) {
    TypeTarget typeTarget = annotation.target().asType();
    System.out.println("Type usage is located within: " + typeTarget.enclosingTarget());
    System.out.println("Usage type: " + typeTarget.usage());
    System.out.println("Target type:"  + typeTarget.target());
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
   Target type:@Label(value = "Name") java.lang.String
   Equivalent? true
```
