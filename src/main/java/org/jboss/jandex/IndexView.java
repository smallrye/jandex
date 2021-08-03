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
    public Collection<ClassInfo> getKnownClasses();

    /**
     * Gets the class (or interface, or annotation) that was scanned during the
     * indexing phase.
     *
     * @param className the name of the class
     * @return information about the class or null if it is not known
     */
    public ClassInfo getClassByName(DotName className);

    /**
     * Gets all known direct subclasses of the specified class name. A known direct
     * subclass is one which was found during the scanning process; however, this is
     * often not the complete universe of subclasses, since typically indexes are
     * constructed per jar. It is expected that several indexes will need to be searched
     * when analyzing a jar that is a part of a complex multi-module/classloader
     * environment (like an EE application server).
     * <p>
     * Note that this will only pick up direct subclasses of the class. It will not
     * pick up subclasses of subclasses.
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className);

    /**
     * Returns all known (including non-direct) sub classes of the given class.
     * I.e., returns all known classes that are assignable to the given class.
     *
     * @param className The class
     *
     * @return All known subclasses
     */
    public Collection<ClassInfo> getAllKnownSubclasses(final DotName className);

    /**
     * Gets all known direct implementors of the specified interface name. A known
     * direct implementor is one which was found during the scanning process; however,
     * this is often not the complete universe of implementors, since typically indexes
     * are constructed per jar. It is expected that several indexes will need to
     * be searched when analyzing a jar that is a part of a complex
     * multi-module/classloader environment (like an EE application server).
     * <p>
     * The list of implementors may also include other methodParameters, in order to get a complete
     * list of all classes that are assignable to a given interface it is necessary to
     * recursively call {@link #getKnownDirectImplementors(DotName)} for every implementing
     * interface found.
     *
     * @param className the super class of the desired subclasses
     * @return a non-null list of all known subclasses of className
     */
    public Collection<ClassInfo> getKnownDirectImplementors(DotName className);

    /**
     * Returns all known classes that implement the given interface, directly and indirectly.
     * This will all return classes that implement sub methodParameters of the interface, and
     * sub-classes of classes that implement the interface. (In short, it will
     * return every class that is assignable to the interface that is found in the index)
     * <p>
     * This will only return classes, not methodParameters.
     *
     * @param interfaceName The interface
     * @return All known implementors of the interface
     */
    public Collection<ClassInfo> getAllKnownImplementors(final DotName interfaceName);

    /**
     * Obtains a list of instances for the specified annotation.
     * This is done using an O(1) lookup. Valid instance targets include
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return a non-null list of annotation instances
     */
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName);

    /**
     * Obtains a list of instances for the specified annotation. If the specified annotation is repeatable (JLS 9.6), the result also contains all values from
     * all instances of the container annotation. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation instance.
     *
     * @throws IllegalArgumentException If the the defining annotation class is not found
     * @param annotationName the name of the repeatable annotation
     * @param index the index containing the annotation class
     * @return a non-null list of annotation instances
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent an annotation type
     */
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index);

    /**
     * Gets all known modules by this index (those which were scanned).
     *
     * @return a collection of known modules
     */
    public Collection<ModuleInfo> getKnownModules();

    /**
     * Gets the module that was scanned during the indexing phase.
     *
     * @param moduleName the name of the module
     * @return information about the module or null if it is not known
     */
    public ModuleInfo getModuleByName(DotName moduleName);

    /**
     * Obtains a list of classes that use the specified class. In other words, a list of classes that include
     * a reference to the specified class in their constant pool.
     *
     * @param className the name of the class to look for
     * @return a non-null list of classes that use the specified class
     */
    public Collection<ClassInfo> getKnownUsers(DotName className);
}
