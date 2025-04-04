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

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
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
        ArrayList<IndexView> list = new ArrayList<IndexView>();
        for (CompositeIndex index : indexes) {
            list.addAll(index.indexes);
        }
        return create(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
        List<AnnotationInstance> allInstances = new ArrayList<AnnotationInstance>();
        for (IndexView i : indexes) {
            allInstances.addAll(i.getAnnotationsWithRepeatable(annotationName, index));
        }
        return Collections.unmodifiableList(allInstances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
    public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        Set<ClassInfo> allKnown = new HashSet<>();
        for (IndexView index : indexes) {
            Collection<ClassInfo> list = index.getKnownDirectSubinterfaces(interfaceName);
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableSet(allKnown);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        Set<ClassInfo> result = new HashSet<>();

        Queue<DotName> worklist = new ArrayDeque<>();
        Set<DotName> alreadyProcessed = new HashSet<>();

        worklist.add(interfaceName);
        while (!worklist.isEmpty()) {
            DotName iface = worklist.remove();
            if (!alreadyProcessed.add(iface)) {
                continue;
            }

            for (IndexView index : indexes) {
                for (ClassInfo directSubinterface : index.getKnownDirectSubinterfaces(iface)) {
                    result.add(directSubinterface);
                    worklist.add(directSubinterface.name());
                }
            }
        }

        return result;
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementations(DotName interfaceName) {
        Set<ClassInfo> allKnown = new HashSet<>();
        for (IndexView index : indexes) {
            Collection<ClassInfo> list = index.getKnownDirectImplementations(interfaceName);
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableSet(allKnown);
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementations(DotName interfaceName) {
        // no difference here
        return getAllKnownImplementors(interfaceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(final DotName interfaceName) {
        final Set<ClassInfo> allKnown = new HashSet<ClassInfo>();
        for (IndexView index : indexes) {
            final Collection<ClassInfo> list = index.getKnownDirectImplementors(interfaceName);
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableSet(allKnown);
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
     * {@inheritDoc}
     */
    @Override
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
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleInfo getModuleByName(final DotName moduleName) {
        for (IndexView index : indexes) {
            final ModuleInfo info = index.getModuleByName(moduleName);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ModuleInfo> getKnownModules() {
        final List<ModuleInfo> allKnown = new ArrayList<ModuleInfo>();
        for (IndexView index : indexes) {
            final Collection<ModuleInfo> list = index.getKnownModules();
            if (list != null) {
                allKnown.addAll(list);
            }
        }
        return Collections.unmodifiableCollection(allKnown);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getKnownUsers(DotName className) {
        final List<ClassInfo> users = new ArrayList<ClassInfo>();
        Set<DotName> processedClasses = new HashSet<DotName>();
        for (IndexView index : indexes) {
            final Collection<ClassInfo> set = index.getKnownUsers(className);
            if (set != null) {
                for (ClassInfo classInfo : set) {
                    if (processedClasses.add(classInfo.name())) {
                        users.add(classInfo);
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> alreadySeen = new HashSet<>();
        for (IndexView index : indexes) {
            for (ClassInfo clazz : index.getClassesInPackage(packageName)) {
                if (alreadySeen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        Set<DotName> result = new HashSet<>();
        for (IndexView index : indexes) {
            result.addAll(index.getSubpackages(packageName));
        }
        return Collections.unmodifiableSet(result);
    }
}
