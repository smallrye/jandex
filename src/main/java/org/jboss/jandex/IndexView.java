/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
     * <p/>
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
     * <p/>
     * The list of implementors may also include other interfaces, in order to get a complete
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
     * This will all return classes that implement sub interfaces of the interface, and
     * sub-classes of classes that implement the interface. (In short, it will
     * return every class that is assignable to the interface that is found in the index)
     * <p/>
     * This will only return classes, not interfaces.
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
}
