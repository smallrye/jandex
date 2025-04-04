package org.jboss.jandex;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Immutable empty index; that is, an index that doesn't contain any class. All methods return either
 * an empty collection, or {@code null}.
 *
 * @since 3.2.0
 */
public final class EmptyIndex implements IndexView {
    public static final EmptyIndex INSTANCE = new EmptyIndex();

    private EmptyIndex() {
    }

    @Override
    public Collection<ClassInfo> getKnownClasses() {
        return Collections.emptySet();
    }

    @Override
    public ClassInfo getClassByName(DotName className) {
        return null;
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementations(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementations(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        return Collections.emptySet();
    }

    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ModuleInfo> getKnownModules() {
        return Collections.emptySet();
    }

    @Override
    public ModuleInfo getModuleByName(DotName moduleName) {
        return null;
    }

    @Override
    public Collection<ClassInfo> getKnownUsers(DotName className) {
        return Collections.emptySet();
    }

    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        return Collections.emptySet();
    }

    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        return Collections.emptySet();
    }
}
