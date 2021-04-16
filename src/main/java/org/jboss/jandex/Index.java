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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An index useful for quickly processing annotations. The index is read-only and supports
 * concurrent access. Also the index is optimized for memory efficiency by using componentized
 * DotName values.
 *
 * <p>It contains the following information:
 * <ol>
 * <li>All annotations and a collection of targets they refer to </li>
 * <li>All classes (including methodParameters) scanned during the indexing process (typical all classes in a jar)</li>
 * <li>All subclasses indexed by super class known to this index</li>
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
    final Map<DotName, List<ClassInfo>> implementors;
    final Map<DotName, ClassInfo> classes;
    final Map<DotName, ModuleInfo> modules;
    final Map<DotName, List<ClassInfo>> users;

    Index(Map<DotName, List<AnnotationInstance>> annotations, Map<DotName, List<ClassInfo>> subclasses,
          Map<DotName, List<ClassInfo>> implementors, Map<DotName, ClassInfo> classes, Map<DotName, ModuleInfo> modules,
          Map<DotName, List<ClassInfo>> users) {
        this.annotations = Collections.unmodifiableMap(annotations);
        this.classes = Collections.unmodifiableMap(classes);
        this.subclasses = Collections.unmodifiableMap(subclasses);
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
        return new Index(annotations, subclasses, implementors, classes, Collections.<DotName, ModuleInfo>emptyMap(), Collections.<DotName, List<ClassInfo>>emptyMap());
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
                               Map<DotName, List<ClassInfo>> implementors, Map<DotName, ClassInfo> classes,
                               Map<DotName, List<ClassInfo>> users) {
        return new Index(annotations, subclasses, implementors, classes, Collections.<DotName, ModuleInfo>emptyMap(), users);
    }

    /**
     * Constructs a "mock" Index using the passed classes.
     *
     * @param classes An Iterable collection of classes to include in the index
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
     * Constructs a "mock" Index using the passed classes.
     *
     * @param classes Classes to include in the index
     * @return the index
     */
    public static Index of(Class<?>... classes) throws IOException {
        return of(Arrays.asList(classes));
    }

    /**
     * Constructs a "mock" Index containing class files found in the passed directories.
     *
     * @param directories Directories containing class files to include in the index
     * @return the index
     * @throws IllegalArgumentException if any directories are null or are not a directory
     */
    public static Index of(File... directories) throws IOException {
        Indexer indexer = new Indexer();

        for (File directory : directories) {
            if (directory == null || !directory.isDirectory()) {
                throw new IllegalArgumentException("not a directory: " + directory);
            }

            File[] sources = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".class");
                }
            });

            for (File source : sources) {
                indexer.index(new FileInputStream(source));
            }
        }

        return indexer.complete();
    }

    /**
     * {@inheritDoc}
     */
    public List<AnnotationInstance> getAnnotations(DotName annotationName) {
        List<AnnotationInstance> list = annotations.get(annotationName);
        return list == null ? EMPTY_ANNOTATION_LIST: Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
        ClassInfo annotationClass = index.getClassByName(annotationName);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + annotationName);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.classAnnotation(REPEATABLE);
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
                instances.add(new AnnotationInstance(nestedInstance.name(), containingInstance.target(), nestedInstance.valueArray()));
            }
        }
        return instances;
    }


    /**
     * {@inheritDoc}
     */
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
    public List<ClassInfo> getKnownDirectImplementors(DotName className) {
        List<ClassInfo> list = implementors.get(className);
        return list == null ? EMPTY_CLASSINFO_LIST : Collections.unmodifiableList(list);
    }

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
     * Print all annotations known by this index to stdout.
     */
    public void printAnnotations()
    {
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

                for (int i =  0; i < values.size(); i ++) {
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
    public void printSubclasses()
    {
        System.out.println("Subclasses:");
        for (Map.Entry<DotName, List<ClassInfo>> entry : subclasses.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (ClassInfo clazz : entry.getValue())
                System.out.println("    " + clazz.name());
        }
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
}
