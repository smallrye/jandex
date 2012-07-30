/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Composite annotation index. Represents an aggregation of multiple <code>Index</code> instances.
 * An example application is a Java EE deployment, which can contain multiple nested jars, each with
 * their own index.
 *
 * @author John Bailey
 * @author Stuart Douglas
 * @author Jason T Greene
 */
public class CompositeIndex implements IndexView {
    final Collection<IndexView> indexes;

    private CompositeIndex(final Collection<IndexView> indexes) {
        this.indexes = indexes;
    }

    public static CompositeIndex create(Collection<IndexView> indexes) {
        return new CompositeIndex(indexes);
    }

    public static CompositeIndex create(final IndexView... indexes) {
        return new CompositeIndex(Arrays.asList(indexes));
    }

    public static CompositeIndex createMerged(final CompositeIndex... indexes) {
        ArrayList<IndexView> list =  new ArrayList<IndexView>();
        for (CompositeIndex index : indexes) {
            list.addAll(index.indexes);
        }
        return create(list);
    }

    /**
     * {@inheritDoc}
     */
    public List<AnnotationInstance> getAnnotations(final DotName annotationName) {
        final List<AnnotationInstance> allInstances = new ArrayList<AnnotationInstance>();
        for (IndexView index : indexes) {
            final Collection<AnnotationInstance> list = index.getAnnotations(annotationName);
            if (list != null) {
                allInstances.addAll(list);
            }
        }
        return Collections.unmodifiableList(allInstances);
    }

    /**
     * {@inheritDoc}
     */
    public Set<ClassInfo> getKnownDirectSubclasses(final DotName className) {
        final Set<ClassInfo> allKnown = new HashSet<ClassInfo>();
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownDirectSubclasses(className);
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableSet(allKnown);
    }

    /**
     * {@inheritDoc}
     */
    public Set<ClassInfo> getAllKnownSubclasses(final DotName className) {
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
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownDirectSubclasses(name);
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
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ClassInfo> getKnownDirectImplementors(final DotName className) {
        final Set<ClassInfo> allKnown = new HashSet<ClassInfo>();
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownDirectImplementors(className);
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableSet(allKnown);
    }

    /**
     * {@inheritDoc}
     */
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
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownDirectImplementors(name);
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
    }

    /**
     * @see {@link Index#getClassByName(org.jboss.jandex.DotName)}
     */
    public ClassInfo getClassByName(final DotName className) {
        for (IndexView index : indexes) {
            final ClassInfo info = index.getClassByName(className);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<ClassInfo> getKnownClasses() {
        final List<ClassInfo> allKnown = new ArrayList<ClassInfo>();
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownClasses();
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableCollection(allKnown);
    }
}

