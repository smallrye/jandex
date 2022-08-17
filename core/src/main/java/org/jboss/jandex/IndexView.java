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
     * Gets all known classes by this index (those which were scanned).
     *
     * @return a collection of known classes
     */
    Collection<ClassInfo> getKnownClasses();

    /**
     * Gets the class (or interface, or annotation) that was scanned during the
     * indexing phase.
     *
     * @param className the name of the class
     * @return information about the class or null if it is not known
     */
    ClassInfo getClassByName(DotName className);

    /**
     * Gets the class (or interface, or annotation) that was scanned during the
     * indexing phase.
     *
     * @param className the name of the class
     * @return information about the class or null if it is not known
     */
    default ClassInfo getClassByName(String className) {
        return getClassByName(DotName.createSimple(className));
    }

    /**
     * Gets the class (or interface, or annotation) that was scanned during the
     * indexing phase.
     *
     * @param clazz the class
     * @return information about the class or null if it is not known
     */
    default ClassInfo getClassByName(Class<?> clazz) {
        return getClassByName(DotName.createSimple(clazz.getName()));
    }

    /**
     * Gets all known direct subclasses of the specified class. A known direct
     * subclass is one which was found during the scanning process; however, this is
     * often not the complete universe of subclasses, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subclasses of the class. It will not
     * pick up subclasses of subclasses.
     * 
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    Collection<ClassInfo> getKnownDirectSubclasses(DotName className);

    /**
     * Gets all known direct subclasses of the specified class. A known direct
     * subclass is one which was found during the scanning process; however, this is
     * often not the complete universe of subclasses, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subclasses of the class. It will not
     * pick up subclasses of subclasses.
     *
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    default Collection<ClassInfo> getKnownDirectSubclasses(String className) {
        return getKnownDirectSubclasses(DotName.createSimple(className));
    }

    /**
     * Gets all known direct subclasses of the specified class. A known direct
     * subclass is one which was found during the scanning process; however, this is
     * often not the complete universe of subclasses, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subclasses of the class. It will not
     * pick up subclasses of subclasses.
     *
     * @param clazz the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    default Collection<ClassInfo> getKnownDirectSubclasses(Class<?> clazz) {
        return getKnownDirectSubclasses(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns all known (including non-direct) subclasses of the given class.
     * I.e., returns all known classes that are assignable to the given class.
     *
     * @param className The class
     *
     * @return All known subclasses
     */
    Collection<ClassInfo> getAllKnownSubclasses(final DotName className);

    /**
     * Returns all known (including non-direct) subclasses of the given class.
     * I.e., returns all known classes that are assignable to the given class.
     *
     * @param className The class
     *
     * @return All known subclasses
     */
    default Collection<ClassInfo> getAllKnownSubclasses(final String className) {
        return getAllKnownSubclasses(DotName.createSimple(className));
    }

    /**
     * Returns all known (including non-direct) subclasses of the given class.
     * I.e., returns all known classes that are assignable to the given class.
     *
     * @param clazz The class
     *
     * @return All known subclasses
     */
    default Collection<ClassInfo> getAllKnownSubclasses(final Class<?> clazz) {
        return getAllKnownSubclasses(DotName.createSimple(clazz.getName()));
    }

    /**
     * Gets all known direct subinterfaces of the specified interface. A known direct
     * subinterface is one which was found during the scanning process; however, this is
     * often not the complete universe of subinterfaces, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subinterfaces of the interface. It will not
     * pick up subinterfaces of subinterfaces.
     *
     * @param interfaceName the super interface of the desired subinterfaces
     * @return a non-null list of all known subinterfaces of interfaceName
     * @since 3.0
     */
    Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName);

    /**
     * Gets all known direct subinterfaces of the specified interface. A known direct
     * subinterface is one which was found during the scanning process; however, this is
     * often not the complete universe of subinterfaces, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subinterfaces of the interface. It will not
     * pick up subinterfaces of subinterfaces.
     *
     * @param interfaceName the super interface of the desired subinterfaces
     * @return a non-null list of all known subinterfaces of interfaceName
     * @since 3.0
     */
    default Collection<ClassInfo> getKnownDirectSubinterfaces(String interfaceName) {
        return getKnownDirectSubinterfaces(DotName.createSimple(interfaceName));
    }

    /**
     * Gets all known direct subinterfaces of the specified interface. A known direct
     * subinterface is one which was found during the scanning process; however, this is
     * often not the complete universe of subinterfaces, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subinterfaces of the interface. It will not
     * pick up subinterfaces of subinterfaces.
     *
     * @param iface the super interface of the desired subinterfaces
     * @return a non-null list of all known subinterfaces of iface
     * @since 3.0
     */
    default Collection<ClassInfo> getKnownDirectSubinterfaces(Class<?> iface) {
        return getKnownDirectSubinterfaces(DotName.createSimple(iface.getName()));
    }

    /**
     * Returns all known interfaces that extend the given interface, directly and indirectly.
     * I.e., returns every interface in the index that is assignable to the given interface.
     *
     * @param interfaceName The interace
     * @return all known subinterfaces
     * @since 3.0
     */
    Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName);

    /**
     * Returns all known interfaces that extend the given interface, directly and indirectly.
     * I.e., returns every interface in the index that is assignable to the given interface.
     *
     * @param interfaceName The interace
     * @return all known subinterfaces
     * @since 3.0
     */
    default Collection<ClassInfo> getAllKnownSubinterfaces(String interfaceName) {
        return getAllKnownSubinterfaces(DotName.createSimple(interfaceName));
    }

    /**
     * Returns all known interfaces that extend the given interface, directly and indirectly.
     * I.e., returns every interface in the index that is assignable to the given interface.
     *
     * @param iface The interace
     * @return all known subinterfaces
     * @since 3.0
     */
    default Collection<ClassInfo> getAllKnownSubinterfaces(Class<?> iface) {
        return getAllKnownSubinterfaces(DotName.createSimple(iface.getName()));
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
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    Collection<ClassInfo> getKnownDirectImplementors(DotName className);

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
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    default Collection<ClassInfo> getKnownDirectImplementors(String className) {
        return getKnownDirectImplementors(DotName.createSimple(className));
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
     * @param clazz the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    default Collection<ClassInfo> getKnownDirectImplementors(Class<?> clazz) {
        return getKnownDirectImplementors(DotName.createSimple(clazz.getName()));
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
     */
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
     */
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
     */
    default Collection<ClassInfo> getAllKnownImplementors(final Class<?> interfaceClass) {
        return getAllKnownImplementors(DotName.createSimple(interfaceClass.getName()));
    }

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return a non-null list of annotation instances
     */
    Collection<AnnotationInstance> getAnnotations(DotName annotationName);

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return a non-null list of annotation instances
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
     * @return a non-null list of annotation instances
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
     * @return a non-null list of annotation instances
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
     * @return a non-null list of annotation instances
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
     * @return a non-null list of annotation instances
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent
     *         an annotation type
     */
    default Collection<AnnotationInstance> getAnnotationsWithRepeatable(Class<?> annotationType, IndexView index) {
        return getAnnotationsWithRepeatable(DotName.createSimple(annotationType.getName()), index);
    }

    /**
     * Gets all known modules by this index (those which were scanned).
     *
     * @return a collection of known modules
     */
    Collection<ModuleInfo> getKnownModules();

    /**
     * Gets the module that was scanned during the indexing phase.
     *
     * @param moduleName the name of the module
     * @return information about the module or null if it is not known
     */
    ModuleInfo getModuleByName(DotName moduleName);

    /**
     * Gets the module that was scanned during the indexing phase.
     *
     * @param moduleName the name of the module
     * @return information about the module or null if it is not known
     */
    default ModuleInfo getModuleByName(String moduleName) {
        return getModuleByName(DotName.createSimple(moduleName));
    }

    /**
     * Obtains a list of classes that use the specified class. In other words, a list of classes that include
     * a reference to the specified class in their constant pool.
     *
     * @param className the name of the class to look for
     * @return a non-null list of classes that use the specified class
     */
    Collection<ClassInfo> getKnownUsers(DotName className);

    /**
     * Obtains a list of classes that use the specified class. In other words, a list of classes that include
     * a reference to the specified class in their constant pool.
     *
     * @param className the name of the class to look for
     * @return a non-null list of classes that use the specified class
     */
    default Collection<ClassInfo> getKnownUsers(String className) {
        return getKnownUsers(DotName.createSimple(className));
    }

    /**
     * Obtains a list of classes that use the specified class. In other words, a list of classes that include
     * a reference to the specified class in their constant pool.
     *
     * @param clazz the class to look for
     * @return a non-null list of classes that use the specified class
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
