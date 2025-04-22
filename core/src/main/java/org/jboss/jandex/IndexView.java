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

import java.util.Collection;
import java.util.Set;

/**
 * The basic contract for accessing Jandex indexed information.
 *
 * @author Jason T. Greene
 * @author Steve Ebersole
 */
public interface IndexView {
    /**
     * Returns an immutable empty index; that is, an index that doesn't contain any class.
     * All methods return either an empty collection, or {@code null}.
     *
     * @since 3.2.0
     */
    static IndexView empty() {
        return EmptyIndex.INSTANCE;
    }

    /**
     * Gets all known classes by this index (those which were scanned).
     *
     * @return immutable collection of known classes, never {@code null}
     */
    Collection<ClassInfo> getKnownClasses();

    /**
     * Returns the class (or enum, record, interface, annotation) with given name.
     * Returns {@code null} if class with given name is not present in the index.
     *
     * @param className the name of the class
     * @return information about the class or {@code null} if it is not known
     */
    ClassInfo getClassByName(DotName className);

    /**
     * Returns the class (or enum, record, interface, annotation) with given name.
     * Returns {@code null} if class with given name is not present in the index.
     *
     * @param className the name of the class
     * @return information about the class or {@code null} if it is not known
     */
    default ClassInfo getClassByName(String className) {
        return getClassByName(DotName.createSimple(className));
    }

    /**
     * Returns the class (or enum, record, interface, annotation) with given name ({@code clazz.getName()}).
     * Returns {@code null} if class with given name is not present in the index.
     *
     * @param clazz the class
     * @return information about the class or {@code null} if it is not known
     */
    default ClassInfo getClassByName(Class<?> clazz) {
        return getClassByName(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns all known direct subclasses of the given class. Indirect subclasses
     * are <em>not</em> returned.
     * <p>
     * Note that interfaces are considered direct subclasses of {@code java.lang.Object}.
     *
     * @param className the class
     * @return an immutable collection of known direct subclasses of given class, never {@code null}
     */
    Collection<ClassInfo> getKnownDirectSubclasses(DotName className);

    /**
     * Returns all known direct subclasses of the given class. Indirect subclasses
     * are <em>not</em> returned.
     * <p>
     * Note that interfaces are considered direct subclasses of {@code java.lang.Object}.
     *
     * @param className the class
     * @return an immutable collection of known direct subclasses of given class, never {@code null}
     */
    default Collection<ClassInfo> getKnownDirectSubclasses(String className) {
        return getKnownDirectSubclasses(DotName.createSimple(className));
    }

    /**
     * Returns all known direct subclasses of the given class. Indirect subclasses
     * are <em>not</em> returned.
     * <p>
     * Note that interfaces are considered direct subclasses of {@code java.lang.Object}.
     *
     * @param clazz the class
     * @return an immutable collection of known direct subclasses of given class, never {@code null}
     */
    default Collection<ClassInfo> getKnownDirectSubclasses(Class<?> clazz) {
        return getKnownDirectSubclasses(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns all known subclasses of the given class, direct and indirect.
     * In other words, all classes that are assignable to the given class.
     *
     * @param className the class
     * @return immutable collection of all known subclasses of given class, never {@code null}
     */
    Collection<ClassInfo> getAllKnownSubclasses(final DotName className);

    /**
     * Returns all known subclasses of the given class, direct and indirect.
     * In other words, all classes that are assignable to the given class.
     *
     * @param className the class
     * @return immutable collection of all known subclasses of given class, never {@code null}
     */
    default Collection<ClassInfo> getAllKnownSubclasses(final String className) {
        return getAllKnownSubclasses(DotName.createSimple(className));
    }

    /**
     * Returns all known subclasses of the given class, direct and indirect.
     * In other words, all classes that are assignable to the given class.
     *
     * @param clazz the class
     * @return immutable collection of all known subclasses of given class, never {@code null}
     */
    default Collection<ClassInfo> getAllKnownSubclasses(final Class<?> clazz) {
        return getAllKnownSubclasses(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns all known direct subinterfaces of the given interface. Indirect subinterfaces
     * are <em>not</em> returned.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known subinterfaces of given interface, never {@code null}
     * @since 3.0
     */
    Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName);

    /**
     * Returns all known direct subinterfaces of the given interface. Indirect subinterfaces
     * are <em>not</em> returned.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known subinterfaces of given interface, never {@code null}
     * @since 3.0
     */
    default Collection<ClassInfo> getKnownDirectSubinterfaces(String interfaceName) {
        return getKnownDirectSubinterfaces(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known direct subinterfaces of the given interface. Indirect subinterfaces
     * are <em>not</em> returned.
     *
     * @param interfaceClass the interface
     * @return immutable collection of all known subinterfaces of given interface, never {@code null}
     * @since 3.0
     */
    default Collection<ClassInfo> getKnownDirectSubinterfaces(Class<?> interfaceClass) {
        return getKnownDirectSubinterfaces(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Returns all known subinterfaces of the given interface, direct and indirect.
     * In other words, all interfaces that are assignable to the given interface.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known subinterfaces of given interface, never {@code null}
     * @since 3.0
     */
    Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName);

    /**
     * Returns all known interfaces that extend the given interface, directly and indirectly.
     * In other words, all interfaces in the index that are assignable to the given interface.
     *
     * @param interfaceName The interface
     * @return all known subinterfaces
     * @since 3.0
     */
    default Collection<ClassInfo> getAllKnownSubinterfaces(String interfaceName) {
        return getAllKnownSubinterfaces(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known subinterfaces of the given interface, direct and indirect.
     * In other words, all interfaces that are assignable to the given interface.
     *
     * @param interfaceClass the interface
     * @return immutable collection of all known subinterfaces of given interface, never {@code null}
     * @since 3.0
     */
    default Collection<ClassInfo> getAllKnownSubinterfaces(Class<?> interfaceClass) {
        return getAllKnownSubinterfaces(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Returns all known classes that directly implement the given interface. Classes that
     * do not directly implement the given interface but do directly implement subinterfaces
     * are <em>not</em> returned. Subclasses of classes that directly implement the given
     * interface are <em>not</em> returned either.
     * <p>
     * Note that unlike {@link #getKnownDirectImplementors(DotName)}, this method
     * does <em>NOT</em> return direct subinterfaces of the given interface, which
     * is typically what you expect when you call this method.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known direct implementations of the interface, never {@code null}
     */
    Collection<ClassInfo> getKnownDirectImplementations(DotName interfaceName);

    /**
     * Returns all known classes that directly implement the given interface. Classes that
     * do not directly implement the given interface but do directly implement subinterfaces
     * are <em>not</em> returned. Subclasses of classes that directly implement the given
     * interface are <em>not</em> returned either.
     * <p>
     * Note that unlike {@link #getKnownDirectImplementors(DotName)}, this method
     * does <em>NOT</em> return direct subinterfaces of the given interface, which
     * is typically what you expect when you call this method.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known direct implementations of the interface, never {@code null}
     */
    default Collection<ClassInfo> getKnownDirectImplementations(String interfaceName) {
        return getKnownDirectImplementations(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known classes that directly implement the given interface. Classes that
     * do not directly implement the given interface but do directly implement subinterfaces
     * are <em>not</em> returned. Subclasses of classes that directly implement the given
     * interface are <em>not</em> returned either.
     * <p>
     * Note that unlike {@link #getKnownDirectImplementors(DotName)}, this method
     * does <em>NOT</em> return direct subinterfaces of the given interface, which
     * is typically what you expect when you call this method.
     *
     * @param interfaceClass the interface
     * @return immutable collection of all known direct implementations of the interface, never {@code null}
     */
    default Collection<ClassInfo> getKnownDirectImplementations(Class<?> interfaceClass) {
        return getKnownDirectImplementations(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * That is, all classes that implement the interface and its subinterfaces, as well as
     * all their subclasses. In other words, all classes that are assignable to the interface.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known implementations of the interface, never {@code null}
     */
    Collection<ClassInfo> getAllKnownImplementations(DotName interfaceName);

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * That is, all classes that implement the interface and its subinterfaces, as well as
     * all their subclasses. In other words, all classes that are assignable to the interface.
     *
     * @param interfaceName the interface
     * @return immutable collection of all known implementations of the interface, never {@code null}
     */
    default Collection<ClassInfo> getAllKnownImplementations(String interfaceName) {
        return getAllKnownImplementations(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * That is, all classes that implement the interface and its subinterfaces, as well as
     * all their subclasses. In other words, all classes that are assignable to the interface.
     *
     * @param interfaceClass the interface
     * @return immutable collection of all known implementations of the interface, never {@code null}
     */
    default Collection<ClassInfo> getAllKnownImplementations(Class<?> interfaceClass) {
        return getAllKnownImplementations(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Gets all known direct implementors of the specified interface. A known
     * direct implementor is one which was found during the scanning process; however,
     * this is often not the complete universe of implementors, since typically indexes
     * are constructed per jar. It is expected that several indexes will need to
     * be searched when analyzing a jar that is a part of a complex
     * multi-module/classloader environment (like an EE application server).
     * <p>
     * The list of implementors also includes direct subinterfaces. This is inconsistent
     * with {@link #getAllKnownImplementors(DotName)}, which doesn't return subinterfaces.
     * <p>
     * Note that this will only pick up classes that directly implement given interface.
     * It will not pick up classes implementing subinterfaces.
     *
     * @param interfaceName The interface
     * @return All known direct implementors of the interface
     * @deprecated use {@link #getKnownDirectImplementations(DotName)}
     */
    @Deprecated
    Collection<ClassInfo> getKnownDirectImplementors(DotName interfaceName);

    /**
     * Gets all known direct implementors of the specified interface name. A known
     * direct implementor is one which was found during the scanning process; however,
     * this is often not the complete universe of implementors, since typically indexes
     * are constructed per jar. It is expected that several indexes will need to
     * be searched when analyzing a jar that is a part of a complex
     * multi-module/classloader environment (like an EE application server).
     * <p>
     * The list of implementors also includes direct subinterfaces. This is inconsistent
     * with {@link #getAllKnownImplementors(String)}, which doesn't return subinterfaces.
     * <p>
     * Note that this will only pick up classes that directly implement given interface.
     * It will not pick up classes implementing subinterfaces.
     *
     * @param interfaceName The interface
     * @return All known direct implementors of the interface
     * @deprecated use {@link #getKnownDirectImplementations(String)}
     */
    @Deprecated
    default Collection<ClassInfo> getKnownDirectImplementors(String interfaceName) {
        return getKnownDirectImplementors(DotName.createSimple(interfaceName));
    }

    /**
     * Gets all known direct implementors of the specified interface name. A known
     * direct implementor is one which was found during the scanning process; however,
     * this is often not the complete universe of implementors, since typically indexes
     * are constructed per jar. It is expected that several indexes will need to
     * be searched when analyzing a jar that is a part of a complex
     * multi-module/classloader environment (like an EE application server).
     * <p>
     * The list of implementors also includes direct subinterfaces. This is inconsistent
     * with {@link #getAllKnownImplementors(Class)}, which doesn't return subinterfaces.
     * <p>
     * Note that this will only pick up classes that directly implement given interface.
     * It will not pick up classes implementing subinterfaces.
     *
     * @param interfaceClass The interface
     * @return All known direct implementors of the interface
     * @deprecated use {@link #getKnownDirectImplementations(Class)}
     */
    @Deprecated
    default Collection<ClassInfo> getKnownDirectImplementors(Class<?> interfaceClass) {
        return getKnownDirectImplementors(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * This will return all classes that implement the interface and its subinterfaces,
     * as well as subclasses of classes that implement the interface and its subinterfaces.
     * (In short, it will return every class in the index that is assignable to the interface.)
     * <p>
     * Note that this method only returns classes. Unlike {@link #getKnownDirectImplementors(DotName)},
     * this method does not return subinterfaces of given interface.
     *
     * @param interfaceName The interface
     * @return All known implementors of the interface
     * @deprecated use {@link #getAllKnownImplementations(DotName)}
     */
    @Deprecated
    Collection<ClassInfo> getAllKnownImplementors(final DotName interfaceName);

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * This will return all classes that implement the interface and its subinterfaces,
     * as well as subclasses of classes that implement the interface and its subinterfaces.
     * (In short, it will return every class in the index that is assignable to the interface.)
     * <p>
     * Note that this method only returns classes. Unlike {@link #getKnownDirectImplementors(String)},
     * this method does not return subinterfaces of given interface.
     *
     * @param interfaceName The interface
     * @return All known implementors of the interface
     * @deprecated use {@link #getAllKnownImplementations(String)}
     */
    @Deprecated
    default Collection<ClassInfo> getAllKnownImplementors(final String interfaceName) {
        return getAllKnownImplementors(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * This will return all classes that implement the interface and its subinterfaces,
     * as well as subclasses of classes that implement the interface and its subinterfaces.
     * (In short, it will return every class in the index that is assignable to the interface.)
     * <p>
     * Note that this method only returns classes. Unlike {@link #getKnownDirectImplementors(Class)},
     * this method does not return subinterfaces of given interface.
     *
     * @param interfaceClass The interface
     * @return All known implementors of the interface
     * @deprecated use {@link #getAllKnownImplementations(Class)}
     */
    @Deprecated
    default Collection<ClassInfo> getAllKnownImplementors(final Class<?> interfaceClass) {
        return getAllKnownImplementors(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return immutable collection of annotation instances, never {@code null}
     */
    Collection<AnnotationInstance> getAnnotations(DotName annotationName);

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return immutable collection of annotation instances, never {@code null}
     */
    default Collection<AnnotationInstance> getAnnotations(String annotationName) {
        return getAnnotations(DotName.createSimple(annotationName));
    }

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationType the type of the annotation to look for
     * @return immutable collection of annotation instances, never {@code null}
     */
    default Collection<AnnotationInstance> getAnnotations(Class<?> annotationType) {
        return getAnnotations(DotName.createSimple(annotationType.getName()));
    }

    /**
     * Obtains a list of instances for the specified annotation. If the specified annotation is repeatable (JLS 9.6), the result
     * also contains all values from all instances of the container annotation. In this case, the
     * {@link AnnotationInstance#target()} returns the target of the container annotation instance.
     *
     * @param annotationName the name of the repeatable annotation
     * @param index the index containing the annotation class
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent
     *         an annotation type
     */
    Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index);

    /**
     * Obtains a list of instances for the specified annotation. If the specified annotation is repeatable (JLS 9.6), the result
     * also contains all values from all instances of the container annotation. In this case, the
     * {@link AnnotationInstance#target()} returns the target of the container annotation instance.
     *
     * @param annotationName the name of the repeatable annotation
     * @param index the index containing the annotation class
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent
     *         an annotation type
     */
    default Collection<AnnotationInstance> getAnnotationsWithRepeatable(String annotationName, IndexView index) {
        return getAnnotationsWithRepeatable(DotName.createSimple(annotationName), index);
    }

    /**
     * Obtains a list of instances for the specified annotation. If the specified annotation is repeatable (JLS 9.6), the result
     * also contains all values from all instances of the container annotation. In this case, the
     * {@link AnnotationInstance#target()} returns the target of the container annotation instance.
     *
     * @param annotationType the name of the repeatable annotation
     * @param index the index containing the annotation class
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent
     *         an annotation type
     */
    default Collection<AnnotationInstance> getAnnotationsWithRepeatable(Class<?> annotationType, IndexView index) {
        return getAnnotationsWithRepeatable(DotName.createSimple(annotationType.getName()), index);
    }

    /**
     * Gets all known modules by this index (those which were scanned).
     *
     * @return immutable collection of known modules, never {@code null}
     */
    Collection<ModuleInfo> getKnownModules();

    /**
     * Gets the module that was scanned during the indexing phase.
     *
     * @param moduleName the name of the module
     * @return information about the module or {@code null} if it is not known
     */
    ModuleInfo getModuleByName(DotName moduleName);

    /**
     * Gets the module that was scanned during the indexing phase.
     *
     * @param moduleName the name of the module
     * @return information about the module or {@code null} if it is not known
     */
    default ModuleInfo getModuleByName(String moduleName) {
        return getModuleByName(DotName.createSimple(moduleName));
    }

    /**
     * Returns a list of classes in this index that use the specified class. For one class
     * to <em>use</em> another class, the other class has to:
     * <ul>
     * <li>occur in the signature of the class (that is, in the superclass type,
     * in the superinterface types, in the type parameters, or in the list of
     * permitted subclasses), or</li>
     * <li>occur in the signature of any of the class's methods (that is, in the return type,
     * in the parameter types, in the exception types, or in the type parameters), or</li>
     * <li>occur in the type of any of the class's fields or record components, or</li>
     * <li>occur in the list of class references in the constant pool, as described
     * by the JLS and JVMS.</li>
     * </ul>
     *
     * @param className the name of the class to look for
     * @return immutable collection of classes that use the specified class, never {@code null}
     */
    Collection<ClassInfo> getKnownUsers(DotName className);

    /**
     * Returns a list of classes in this index that use the specified class. For one class
     * to <em>use</em> another class, the other class has to:
     * <ul>
     * <li>occur in the signature of the class (that is, in the superclass type,
     * in the superinterface types, in the type parameters, or in the list of
     * permitted subclasses), or</li>
     * <li>occur in the signature of any of the class's methods (that is, in the return type,
     * in the parameter types, in the exception types, or in the type parameters), or</li>
     * <li>occur in the type of any of the class's fields or record components, or</li>
     * <li>occur in the list of class references in the constant pool, as described
     * by the JLS and JVMS.</li>
     * </ul>
     *
     * @param className the name of the class to look for
     * @return immutable collection of classes that use the specified class, never {@code null}
     */
    default Collection<ClassInfo> getKnownUsers(String className) {
        return getKnownUsers(DotName.createSimple(className));
    }

    /**
     * Returns a list of classes in this index that use the specified class. For one class
     * to <em>use</em> another class, the other class has to:
     * <ul>
     * <li>occur in the signature of the class (that is, in the superclass type,
     * in the superinterface types, in the type parameters, or in the list of
     * permitted subclasses), or</li>
     * <li>occur in the signature of any of the class's methods (that is, in the return type,
     * in the parameter types, in the exception types, or in the type parameters), or</li>
     * <li>occur in the type of any of the class's fields or record components, or</li>
     * <li>occur in the list of class references in the constant pool of the class,
     * as described by the JLS and JVMS.</li>
     * </ul>
     *
     * @param clazz the class to look for
     * @return immutable collection of classes that use the specified class, never {@code null}
     */
    default Collection<ClassInfo> getKnownUsers(Class<?> clazz) {
        return getKnownUsers(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns all {@linkplain ClassInfo classes} known to this index that are present in given package.
     * Classes present in subpackages of given package are not returned. Classes present in the unnamed
     * package may be looked up using {@code null} as the package name. If this index does not contain
     * any class in given package, returns an empty collection.
     * <p>
     * In the default {@link Index} implementation, this information is not stored in the index initially.
     * Instead, an index of classes by package name is constructed on demand (on the first invocation
     * of this method).
     *
     * @param packageName package name in the common, dot-separated form (e.g. {@code com.example.foobar});
     *        {@code null} means the unnamed package
     * @return immutable collection of classes present in given package, never {@code null}
     * @since 3.0
     */
    Collection<ClassInfo> getClassesInPackage(DotName packageName);

    /**
     * Returns all {@linkplain ClassInfo classes} known to this index that are present in given package.
     * Classes present in subpackages of given package are not returned. Classes present in the unnamed
     * package may be looked up using {@code null} as the package name. If this index does not contain
     * any class in given package, returns an empty collection.
     * <p>
     * In the default {@link Index} implementation, this information is not stored in the index initially.
     * Instead, an index of classes by package name is constructed on demand (on the first invocation
     * of this method).
     *
     * @param packageName package name in the common, dot-separated form (e.g. {@code com.example.foobar});
     *        {@code null} means the unnamed package
     * @return immutable collection of classes present in given package, never {@code null}
     * @since 3.0
     */
    default Collection<ClassInfo> getClassesInPackage(String packageName) {
        return getClassesInPackage(DotName.createSimple(packageName));
    }

    /**
     * Returns a set of packages known to this index that are direct subpackages of given package.
     * Indirect subpackages of given package (subpackages of subpackages) are not returned.
     * If this index does not contain any class in a direct or indirect subpackage of given package,
     * returns an empty collection.
     * <p>
     * Given that the unnamed package may not contain subpackages, passing {@code null} as the package
     * name is permitted, but always results in an empty set.
     * <p>
     * In the default {@link Index} implementation, this information is not stored in the index initially.
     * Instead, an index of packages is constructed on demand (on the first invocation of this method).
     *
     * @param packageName package name in the common, dot-separated form (e.g. {@code com.example.foobar});
     *        {@code null} means the unnamed package
     * @return immutable set of subpackages of given package, never {@code null}
     * @since 3.0
     */
    Set<DotName> getSubpackages(DotName packageName);

    /**
     * Returns a set of packages known to this index that are direct subpackages of given package.
     * Indirect subpackages of given package (subpackages of subpackages) are not returned.
     * If this index does not contain any class in a direct or indirect subpackage of given package,
     * returns an empty collection.
     * <p>
     * Given that the unnamed package may not contain subpackages, passing {@code null} as the package
     * name is permitted, but always results in an empty set.
     * <p>
     * In the default {@link Index} implementation, this information is not stored in the index initially.
     * Instead, an index of packages is constructed on demand (on the first invocation of this method).
     *
     * @param packageName package name in the common, dot-separated form (e.g. {@code com.example.foobar});
     *        {@code null} means the unnamed package
     * @return immutable set of subpackages of given package, never {@code null}
     * @since 3.0
     */
    default Set<DotName> getSubpackages(String packageName) {
        return getSubpackages(DotName.createSimple(packageName));
    }
}
