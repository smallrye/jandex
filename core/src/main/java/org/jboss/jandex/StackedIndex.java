package org.jboss.jandex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * A stack of {@linkplain IndexView indexes} with overlay semantics. Overlaying is done on class
 * granularity. That is, if a class is present in multiple indexes on the stack, only the top-most
 * occurrence is considered; the other variants of the class present below on the stack are ignored.
 *
 * @since 3.2.0
 */
public final class StackedIndex implements IndexView {
    // note that the top-most index comes first (reverse order compared to the `create()` methods)
    private final IndexView[] stack;

    private StackedIndex(IndexView[] stack) {
        this.stack = stack;
    }

    /**
     * Creates a stacked index from given {@code indexes}. The first element of the list is the bottom-most index
     * on the stack, while the last element of the list is the top-most index on the stack.
     *
     * @param indexes indexes in stack order; must not be {@code null} and must not contain {@code null} elements
     * @return stacked index, never {@code null}
     */
    public static StackedIndex create(List<IndexView> indexes) {
        return create(indexes.toArray(new IndexView[0]));
    }

    /**
     * Creates a stacked index from given {@code indexes}. The first element of the array is the bottom-most index
     * on the stack, while the last element of the array is the top-most index on the stack.
     *
     * @param indexes indexes in stack order; must not be {@code null} and must not contain {@code null} elements
     * @return stacked index, never {@code null}
     */
    public static StackedIndex create(IndexView... indexes) {
        Objects.requireNonNull(indexes);

        List<IndexView> stack = new ArrayList<>();
        for (int i = indexes.length - 1; i >= 0; i--) {
            IndexView index = indexes[i];
            Objects.requireNonNull(index);
            if (index instanceof StackedIndex) {
                Collections.addAll(stack, ((StackedIndex) index).stack);
            } else {
                stack.add(index);
            }
        }
        return new StackedIndex(stack.toArray(new IndexView[0]));
    }

    /**
     * Creates a new stacked index where the given {@code index} is on top of the stack
     * and the rest of the stack is equivalent to this stacked index.
     *
     * @param index the index to become a new top of the stack
     * @return a new stacked index that results from pushing {@code index} on top of this stacked index
     */
    public StackedIndex pushIndex(IndexView index) {
        IndexView[] newStack;
        if (index instanceof StackedIndex) {
            IndexView[] indexes = ((StackedIndex) index).stack;
            newStack = new IndexView[indexes.length + stack.length];
            System.arraycopy(indexes, 0, newStack, 0, indexes.length);
            System.arraycopy(stack, 0, newStack, indexes.length, stack.length);
        } else {
            newStack = new IndexView[1 + stack.length];
            newStack[0] = index;
            System.arraycopy(stack, 0, newStack, 1, stack.length);
        }
        return new StackedIndex(newStack);
    }

    // ---

    @Override
    public Collection<ClassInfo> getKnownClasses() {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownClasses()) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public ClassInfo getClassByName(DotName className) {
        for (IndexView index : stack) {
            ClassInfo result = index.getClassByName(className);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownDirectSubclasses(className)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
        List<ClassInfo> result = new ArrayList<>();

        Queue<DotName> worklist = new ArrayDeque<>();
        Set<DotName> seen = new HashSet<>();

        worklist.add(className);
        while (!worklist.isEmpty()) {
            DotName cls = worklist.remove();
            for (IndexView index : stack) {
                for (ClassInfo directSubclass : index.getKnownDirectSubclasses(cls)) {
                    worklist.add(directSubclass.name());
                    if (seen.add(directSubclass.name())) {
                        result.add(directSubclass);
                    }
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownDirectSubinterfaces(interfaceName)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
        List<ClassInfo> result = new ArrayList<>();

        Queue<DotName> worklist = new ArrayDeque<>();
        Set<DotName> seen = new HashSet<>();

        worklist.add(interfaceName);
        while (!worklist.isEmpty()) {
            DotName iface = worklist.remove();
            for (IndexView index : stack) {
                for (ClassInfo directSubinterface : index.getKnownDirectSubinterfaces(iface)) {
                    worklist.add(directSubinterface.name());
                    if (seen.add(directSubinterface.name())) {
                        result.add(directSubinterface);
                    }
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementations(DotName interfaceName) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownDirectImplementations(interfaceName)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementations(DotName interfaceName) {
        // no difference here
        return getAllKnownImplementors(interfaceName);
    }

    @Override
    public Collection<ClassInfo> getKnownDirectImplementors(DotName interfaceName) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownDirectImplementors(interfaceName)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
        List<ClassInfo> result = new ArrayList<>();

        Queue<DotName> worklist = new ArrayDeque<>();
        Set<DotName> seen = new HashSet<>();

        worklist.add(interfaceName);
        while (!worklist.isEmpty()) {
            DotName iface = worklist.remove();
            for (IndexView index : stack) {
                for (ClassInfo directImplementor : index.getKnownDirectImplementors(iface)) {
                    if (directImplementor.isInterface()) {
                        worklist.add(directImplementor.name());
                    } else if (seen.add(directImplementor.name())) {
                        result.add(directImplementor);
                    }
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
        List<AnnotationInstance> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        Set<DotName> seenInThisIteration = new HashSet<>();
        for (IndexView idx : stack) {
            for (AnnotationInstance annotation : idx.getAnnotations(annotationName)) {
                DotName inClass = nameOfDeclaringClass(annotation.target());
                if (inClass == null) {
                    continue;
                }
                if (!seen.contains(inClass)) {
                    result.add(annotation);
                    seenInThisIteration.add(inClass);
                }
            }
            seen.addAll(seenInThisIteration);
            seenInThisIteration.clear();
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
        List<AnnotationInstance> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        Set<DotName> seenInThisIteration = new HashSet<>();
        for (IndexView idx : stack) {
            for (AnnotationInstance annotation : idx.getAnnotationsWithRepeatable(annotationName, index)) {
                DotName inClass = nameOfDeclaringClass(annotation.target());
                if (inClass == null) {
                    continue;
                }
                if (!seen.contains(inClass)) {
                    result.add(annotation);
                    seenInThisIteration.add(inClass);
                }
            }
            seen.addAll(seenInThisIteration);
            seenInThisIteration.clear();
        }
        return Collections.unmodifiableList(result);
    }

    private static DotName nameOfDeclaringClass(AnnotationTarget target) {
        if (target == null) {
            return null;
        } else if (target.kind() == AnnotationTarget.Kind.CLASS) {
            return target.asClass().name();
        } else if (target.kind() == AnnotationTarget.Kind.METHOD) {
            return target.asMethod().declaringClass().name();
        } else if (target.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            return target.asMethodParameter().method().declaringClass().name();
        } else if (target.kind() == AnnotationTarget.Kind.FIELD) {
            return target.asField().declaringClass().name();
        } else if (target.kind() == AnnotationTarget.Kind.RECORD_COMPONENT) {
            return target.asRecordComponent().declaringClass().name();
        } else if (target.kind() == AnnotationTarget.Kind.TYPE) {
            return nameOfDeclaringClass(target.asType().enclosingTarget());
        } else {
            throw new IllegalArgumentException("Unknown annotation target: " + target);
        }
    }

    @Override
    public Collection<ModuleInfo> getKnownModules() {
        List<ModuleInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ModuleInfo module : idx.getKnownModules()) {
                if (seen.add(module.name())) {
                    result.add(module);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public ModuleInfo getModuleByName(DotName moduleName) {
        for (IndexView idx : stack) {
            ModuleInfo result = idx.getModuleByName(moduleName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Collection<ClassInfo> getKnownUsers(DotName className) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getKnownUsers(className)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
        List<ClassInfo> result = new ArrayList<>();
        Set<DotName> seen = new HashSet<>();
        for (IndexView idx : stack) {
            for (ClassInfo clazz : idx.getClassesInPackage(packageName)) {
                if (seen.add(clazz.name())) {
                    result.add(clazz);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Set<DotName> getSubpackages(DotName packageName) {
        Set<DotName> result = new HashSet<>();
        for (IndexView idx : stack) {
            result.addAll(idx.getSubpackages(packageName));
        }
        return Collections.unmodifiableSet(result);
    }
}
