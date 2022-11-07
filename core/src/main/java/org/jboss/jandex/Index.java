/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.jboss.jandex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * An index useful for quickly processing annotations. The index is read-only and supports
 * concurrent access. Also, the index is optimized for memory efficiency by using componentized
 * {@code DotName} values.
 *
 * <p>
 * It contains the following information:
 * <ol>
 * <li>All annotations and a collection of targets they refer to</li>
 * <li>All classes (including interfaces) scanned during the indexing process (typically all classes in a jar)</li>
 * <li>All modules scanned during the indexing process (typically one module in a jar)</li>
 * <li>All subclasses for each class known to this index</li>
 * <li>All subinterfaces for each interface known to this index</li>
 * <li>All implementors for each interface known to this index</li>
 * <li>All users of each class known to this index</li>
 * </ol>
 *
 * @author Jason T. Greene
 *
 */
public final class Index implements IndexView {
    private static final List<AnnotationInstance> EMPTY_ANNOTATION_LIST = Collections.emptyList();
    private static final List<ClassInfo> EMPTY_CLASSINFO_LIST = Collections.emptyList();

    static final DotName REPEATABLE = DotName.createSimple("java.lang.annotation.Repeatable");

    final Map<DotName, List<AnnotationInstance>> annotations;
    final Map<DotName, List<ClassInfo>> subclasses;
    final Map<DotName, List<ClassInfo>> subinterfaces;
    final Map<DotName, List<ClassInfo>> implementors;
    final Map<DotName, ClassInfo> classes;
    final Map<DotName, ModuleInfo> modules;
    final Map<DotName, List<ClassInfo>> users;

    // populated lazily
    volatile Map<DotName, Collection<ClassInfo>> classesInPackage;
    volatile Map<DotName, Set<DotName>> subpackages;

    Index(Map<DotName, List<AnnotationInstance>> annotations, Map<DotName, List<ClassInfo>> subclasses,
            Map<DotName, List<ClassInfo>> subinterfaces, Map<DotName, List<ClassInfo>> implementors,
            Map<DotName, ClassInfo> classes, Map<DotName, ModuleInfo> modules, Map<DotName, List<ClassInfo>> users) {
        this.annotations = Collections.unmodifiableMap(annotations);
        this.classes = Collections.unmodifiableMap(classes);
        this.subclasses = Collections.unmodifiableMap(subclasses);
        this.subinterfaces = Collections.unmodifiableMap(subinterfaces);
        this.implementors = Collections.unmodifiableMap(implementors);
        this.modules = Collections.unmodifiableMap(modules);
        this.users = Collections.unmodifiableMap(users);
    }

    /**
     * Constructs a "mock" Index using the passed values. All passed values MUST NOT BE MODIFIED AFTER THIS CALL.
     * Otherwise the resulting object would not conform to the contract outlined above. Also, to conform to the
     * memory efficiency contract this method should be passed componentized DotNames, which all share common root
     * instances. Of course for testing code this doesn't really matter.
     *
     * @param annotations A map to lookup annotation instances by class name
     * @param subclasses A map to lookup subclasses by super class name
     * @param implementors A map to lookup implementing classes by interface name
     * @param classes A map to lookup classes by class name
     * @return the index
     */
    public static Index create(Map<DotName, List<AnnotationInstance>> annotations, Map<DotName, List<ClassInfo>> subclasses,
            Map<DotName, List<ClassInfo>> implementors, Map<DotName, ClassInfo> classes) {
        return new Index(annotations, subclasses, Collections.emptyMap(), implementors, classes, Collections.emptyMap(),
                Collections.emptyMap());
    }

    /**
     * Constructs a "mock" Index using the passed values. All passed values MUST NOT BE MODIFIED AFTER THIS CALL.
     * Otherwise the resulting object would not conform to the contract outlined above. Also, to conform to the
     * memory efficiency contract this method should be passed componentized DotNames, which all share common root
     * instances. Of course for testing code this doesn't really matter.
     *
     * @param annotations A map to lookup annotation instances by class name
     * @param subclasses A map to lookup subclasses by super class name
     * @param implementors A map to lookup implementing classes by interface name
     * @param classes A map to lookup classes by class name
     * @param users A map to lookup class users
     * @return the index
     */
    public static Index create(Map<DotName, List<AnnotationInstance>> annotations, Map<DotName, List<ClassInfo>> subclasses,
            Map<DotName, List<ClassInfo>> implementors, Map<DotName, ClassInfo> classes, Map<DotName, List<ClassInfo>> users) {
        return new Index(annotations, subclasses, Collections.emptyMap(), implementors, classes, Collections.emptyMap(), users);
    }

    /**
     * Constructs a "mock" Index using the passed values. All passed values MUST NOT BE MODIFIED AFTER THIS CALL.
     * Otherwise the resulting object would not conform to the contract outlined above. Also, to conform to the
     * memory efficiency contract this method should be passed componentized DotNames, which all share common root
     * instances. Of course for testing code this doesn't really matter.
     *
     * @param annotations A map to lookup annotation instances by class name
     * @param subclasses A map to lookup subclasses by super class name
     * @param subinterfaces A map to lookup subinterfaces by super interface name
     * @param implementors A map to lookup implementing classes by interface name
     * @param classes A map to lookup classes by class name
     * @param users A map to lookup class users
     * @return the index
     */
    public static Index create(Map<DotName, List<AnnotationInstance>> annotations, Map<DotName, List<ClassInfo>> subclasses,
            Map<DotName, List<ClassInfo>> subinterfaces, Map<DotName, List<ClassInfo>> implementors,
            Map<DotName, ClassInfo> classes, Map<DotName, List<ClassInfo>> users) {
        return new Index(annotations, subclasses, subinterfaces, implementors, classes, Collections.emptyMap(), users);
    }

    /**
     * Constructs an Index of the passed classes.
     *
     * @param classes Classes to index
     * @return the index
     */
    public static Index of(Iterable<Class<?>> classes) throws IOException {
        Indexer indexer = new Indexer();

        for (Class<?> clazz : classes) {
            indexer.indexClass(clazz);
        }

        return indexer.complete();
    }

    /**
     * Constructs an Index of the passed classes.
     *
     * @param classes Classes to index
     * @return the index
     */
    public static Index of(Class<?>... classes) throws IOException {
        return of(Arrays.asList(classes));
    }

    /**
     * Constructs an Index of the passed files and directories. Files may be class files or JAR files.
     * Directories are scanned for class files, but <i>not</i> recursively.
     *
     * @param files class files, JAR files or directories containing class files to index
     * @return the index
     * @throws IllegalArgumentException if any passed {@code File} is null or not a class file, JAR file or directory
     */
    public static Index of(File... files) throws IOException {
        Indexer indexer = new Indexer();

        for (File file : files) {
            if (file == null) {
                throw new IllegalArgumentException("File must not be null");
            } else if (file.isDirectory()) {
                File[] classFiles = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().endsWith(".class");
                    }
                });

                for (File classFile : classFiles) {
                    try (InputStream in = new FileInputStream(classFile)) {
                        indexer.index(in);
                    }
                }
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                try (InputStream in = new FileInputStream(file)) {
                    indexer.index(in);
                }
            } else if (file.isFile() && file.getName().endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(file)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            try (InputStream in = jarFile.getInputStream(entry)) {
                                indexer.index(in);
                            }
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Not a class file, JAR file or directory: " + file);
            }
        }

        return indexer.complete();
    }

    /**
     * Creates a temporary {@link Indexer}, indexes given {@code clazz}, and returns
     * the corresponding {@link ClassInfo}.
     *
     * @param clazz the class to index, must not be {@code null}
     * @return the corresponding {@link ClassInfo}
     */
    public static ClassInfo singleClass(Class<?> clazz) throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexClass(clazz);
        Index index = indexer.complete();
        return index.getKnownClasses().iterator().next();
    }

    /**
     * Creates a temporary {@link Indexer}, indexes given {@code classData}, and returns
     * the corresponding {@link ClassInfo}.
     *
     * @param classData the class bytecode to index, must not be {@code null}
     * @return the corresponding {@link ClassInfo}
     */
    public static ClassInfo singleClass(byte[] classData) throws IOException {
        return Index.singleClass(new ByteArrayInputStream(classData));
    }

    /**
     * Creates a temporary {@link Indexer}, indexes given {@code classData}, and returns
     * the corresponding {@link ClassInfo}. Closing the input stream is the caller's
     * responsibility.
     *
     * @param classData the class bytecode to index, must not be {@code null}
     * @return the corresponding {@link ClassInfo}
     */
    public static ClassInfo singleClass(InputStream classData) throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(classData);
        Index index = indexer.complete();
        return index.getKnownClasses().iterator().next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AnnotationInstance> getAnnotations(DotName annotationName) {
        List<AnnotationInstance> list = annotations.get(annotationName);
        return list == null ? EMPTY_ANNOTATION_LIST : Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
        ClassInfo annotationClass = index.getClassByName(annotationName);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + annotationName);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(REPEATABLE);
        if (repeatable == null) {
            // Not a repeatable annotation
            return getAnnotations(annotationName);
        }
        Type containing = repeatable.value().asClass();
        return getRepeatableAnnotations(annotationName, containing.name());
    }

    private Collection<AnnotationInstance> getRepeatableAnnotations(DotName annotationName, DotName containingAnnotationName) {
        List<AnnotationInstance> instances = new ArrayList<AnnotationInstance>();
        instances.addAll(getAnnotations(annotationName));
        for (AnnotationInstance containingInstance : getAnnotations(containingAnnotationName)) {
            for (AnnotationInstance nestedInstance : containingInstance.value().asNestedArray()) {
                // We need to set the target of the containing instance
                instances.add(AnnotationInstance.create(nestedInstance, containingInstance.target()));
            }
        }
        return instances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClassInfo> getKnownDirectSubclasses(DotName className) {
        List<ClassInfo> list = subclasses.get(className);
        return list == null ? EMPTY_CLASSINFO_LIST : Collections.unmodifiableList(list);
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        final Set<ClassInfo> allKnown = new HashSet<ClassInfo>();
        final Set<DotName> processedClasses = new HashSet<DotName>();
        getAllKnownSubClasses(className, allKnown, processedClasses);
        return allKnown;
    }

    private void getAllKnownSubClasses(DotName className, Set<ClassInfo> allKnown, Set<DotName> processedClasses) {
        final Set<DotName> subClassesToProcess = new HashSet<DotName>();
        subClassesToProcess.add(className);
        while (!subClassesToProcess.isEmpty()) {
            final Iterator<DotName> toProcess = subClassesToProcess.iterator();
            DotName name = toProcess.next();
            toProcess.remove();
            processedClasses.add(name);
            getAllKnownSubClasses(name, allKnown, subClassesToProcess, processedClasses);
        }
    }

    private void getAllKnownSubClasses(DotName name, Set<ClassInfo> allKnown, Set<DotName> subClassesToProcess,
            Set<DotName> processedClasses) {
        final List<ClassInfo> list = getKnownDirectSubclasses(name);
        if (list != null) {
            for (final ClassInfo clazz : list) {
                final DotName className = clazz.name();
                if (!processedClasses.contains(className)) {
                    allKnown.add(clazz);
                    subClassesToProcess.add(className);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        List<ClassInfo> list = subinterfaces.get(interfaceName);
        return list == null ? EMPTY_CLASSINFO_LIST : Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        Set<ClassInfo> result = new HashSet<>();

        Queue<DotName> workQueue = new ArrayDeque<>();
        Set<DotName> alreadyProcessed = new HashSet<>();

        workQueue.add(interfaceName);
        while (!workQueue.isEmpty()) {
            DotName iface = workQueue.remove();
            if (!alreadyProcessed.add(iface)) {
                continue;
            }

            for (ClassInfo directSubinterface : getKnownDirectSubinterfaces(iface)) {
                result.add(directSubinterface);
                workQueue.add(directSubinterface.name());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClassInfo> getKnownDirectImplementors(DotName className) {
        List<ClassInfo> list = implementors.get(className);
        return list == null ? EMPTY_CLASSINFO_LIST : Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ClassInfo> getAllKnownImplementors(final DotName interfaceName) {
        final Set<ClassInfo> allKnown = new HashSet<ClassInfo>();
        final Set<DotName> subInterfacesToProcess = new HashSet<DotName>();
        final Set<DotName> processedClasses = new HashSet<DotName>();
        subInterfacesToProcess.add(interfaceName);
        while (!subInterfacesToProcess.isEmpty()) {
            final Iterator<DotName> toProcess = subInterfacesToProcess.iterator();
            DotName name = toProcess.next();
            toProcess.remove();
            processedClasses.add(name);
            getKnownImplementors(name, allKnown, subInterfacesToProcess, processedClasses);
        }
        return allKnown;
    }

    private void getKnownImplementors(DotName name, Set<ClassInfo> allKnown, Set<DotName> subInterfacesToProcess,
            Set<DotName> processedClasses) {
        final List<ClassInfo> list = getKnownDirectImplementors(name);
        if (list != null) {
            for (final ClassInfo clazz : list) {
                final DotName className = clazz.name();
                if (!processedClasses.contains(className)) {
                    if (Modifier.isInterface(clazz.flags())) {
                        subInterfacesToProcess.add(className);
                    } else {
                        if (!allKnown.contains(clazz)) {
                            allKnown.add(clazz);
                            processedClasses.add(className);
                            getAllKnownSubClasses(className, allKnown, processedClasses);
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassInfo getClassByName(DotName className) {
        return classes.get(className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return classes.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ModuleInfo> getKnownModules() {
        return modules.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleInfo getModuleByName(DotName moduleName) {
        return modules.get(moduleName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClassInfo> getKnownUsers(DotName className) {
        List<ClassInfo> ret = users.get(className);
        if (ret == null) {
            return EMPTY_CLASSINFO_LIST;
        }
        return Collections.unmodifiableList(ret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        if (classesInPackage == null) {
            synchronized (this) {
                if (classesInPackage == null) {
                    Map<DotName, Collection<ClassInfo>> map = new HashMap<>();
                    for (ClassInfo clazz : classes.values()) {
                        DotName pkg = clazz.name().packagePrefixName();
                        map.computeIfAbsent(pkg, ignored -> new ArrayList<>()).add(clazz);
                    }
                    classesInPackage = Collections.unmodifiableMap(map);
                }
            }
        }

        Collection<ClassInfo> result = classesInPackage.get(packageName);
        return result != null ? Collections.unmodifiableCollection(result) : EMPTY_CLASSINFO_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        if (subpackages == null) {
            synchronized (this) {
                if (subpackages == null) {
                    Map<DotName, Set<DotName>> map = new HashMap<>();
                    for (ClassInfo clazz : classes.values()) {
                        DotName pkg = clazz.name().packagePrefixName();
                        while (pkg != null) {
                            DotName superPkg = pkg.packagePrefixName();
                            if (superPkg != null) {
                                map.computeIfAbsent(superPkg, ignored -> new HashSet<>()).add(pkg);
                            }
                            pkg = superPkg;
                        }
                    }
                    subpackages = Collections.unmodifiableMap(map);
                }
            }
        }

        Set<DotName> result = subpackages.get(packageName);
        return result != null ? Collections.unmodifiableSet(result) : Collections.emptySet();
    }

    // ---

    /**
     * Print all annotations known by this index to stdout.
     */
    public void printAnnotations() {
        System.out.println("Annotations:");
        for (Map.Entry<DotName, List<AnnotationInstance>> e : annotations.entrySet()) {
            System.out.println(e.getKey() + ":");
            for (AnnotationInstance instance : e.getValue()) {
                AnnotationTarget target = instance.target();

                if (target instanceof ClassInfo) {
                    System.out.println("    Class: " + target);
                } else if (target instanceof FieldInfo) {
                    System.out.println("    Field: " + target);
                } else if (target instanceof MethodInfo) {
                    System.out.println("    Method: " + target);
                } else if (target instanceof MethodParameterInfo) {
                    System.out.println("    Parameter: " + target);
                }

                List<AnnotationValue> values = instance.values();
                if (values.size() < 1)
                    continue;

                StringBuilder builder = new StringBuilder("        (");

                for (int i = 0; i < values.size(); i++) {
                    builder.append(values.get(i));
                    if (i < values.size() - 1)
                        builder.append(", ");
                }
                builder.append(')');
                System.out.println(builder.toString());
            }
        }
    }

    /**
     * Print all classes that have known subclasses, and all their subclasses
     */
    public void printSubclasses() {
        System.out.println("Subclasses:");
        for (Map.Entry<DotName, List<ClassInfo>> entry : subclasses.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (ClassInfo clazz : entry.getValue())
                System.out.println("    " + clazz.name());
        }
    }
}
