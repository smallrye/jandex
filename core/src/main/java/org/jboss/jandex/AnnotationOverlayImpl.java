package org.jboss.jandex;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jboss.jandex.AnnotationTransformation.TransformationContext;

class AnnotationOverlayImpl implements AnnotationOverlay {
    final IndexView index;
    final boolean compatibleMode;
    final boolean runtimeAnnotationsOnly;
    final boolean inheritedAnnotations;
    final List<AnnotationTransformation> transformations;
    final Map<EquivalenceKey, Collection<AnnotationInstance>> overlay = new ConcurrentHashMap<>();

    AnnotationOverlayImpl(IndexView index, boolean compatibleMode, boolean runtimeAnnotationsOnly, boolean inheritedAnnotations,
            Collection<AnnotationTransformation> annotationTransformations) {
        this.index = index;
        this.compatibleMode = compatibleMode;
        this.runtimeAnnotationsOnly = runtimeAnnotationsOnly;
        this.inheritedAnnotations = inheritedAnnotations;
        if (!compatibleMode) {
            for (AnnotationTransformation transformation : annotationTransformations) {
                if (transformation.requiresCompatibleMode()) {
                    throw new IllegalStateException("Compatible mode required by " + transformation);
                }
            }
        }
        List<AnnotationTransformation> transformations = new ArrayList<>(annotationTransformations);
        transformations.sort(new Comparator<AnnotationTransformation>() {
            @Override
            public int compare(AnnotationTransformation o1, AnnotationTransformation o2) {
                return Integer.compare(o2.priority(), o1.priority());
            }
        });
        this.transformations = transformations;
    }

    @Override
    public final IndexView index() {
        return index;
    }

    @Override
    public final boolean hasAnnotation(Declaration declaration, DotName name) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            throw new UnsupportedOperationException();
        }

        Collection<AnnotationInstance> annotations = getAnnotationsFor(declaration);
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(name)) {
                return true;
            }
        }

        if (inheritedAnnotations && declaration.kind() == AnnotationTarget.Kind.CLASS
                && declaration.asClass().superName() != null) {
            ClassInfo clazz = index.getClassByName(declaration.asClass().superName());
            while (clazz != null && !DotName.OBJECT_NAME.equals(clazz.name())) {
                for (AnnotationInstance annotation : getAnnotationsFor(clazz)) {
                    ClassInfo annotationClass = index.getClassByName(annotation.name());
                    if (annotationClass != null
                            && annotationClass.hasDeclaredAnnotation(DotName.INHERITED_NAME)
                            && annotation.name().equals(name)) {
                        return true;
                    }
                }
                clazz = index.getClassByName(clazz.superName());
            }
        }

        return false;
    }

    @Override
    public final boolean hasAnyAnnotation(Declaration declaration, Set<DotName> names) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            throw new UnsupportedOperationException();
        }

        Collection<AnnotationInstance> annotations = getAnnotationsFor(declaration);
        for (AnnotationInstance annotation : annotations) {
            for (DotName name : names) {
                if (annotation.name().equals(name)) {
                    return true;
                }
            }
        }

        if (inheritedAnnotations && declaration.kind() == AnnotationTarget.Kind.CLASS
                && declaration.asClass().superName() != null) {
            ClassInfo clazz = index.getClassByName(declaration.asClass().superName());
            while (clazz != null && !DotName.OBJECT_NAME.equals(clazz.name())) {
                for (AnnotationInstance annotation : getAnnotationsFor(clazz)) {
                    ClassInfo annotationClass = index.getClassByName(annotation.name());
                    if (annotationClass != null && annotationClass.hasDeclaredAnnotation(DotName.INHERITED_NAME)) {
                        for (DotName name : names) {
                            if (annotation.name().equals(name)) {
                                return true;
                            }
                        }
                    }
                }
                clazz = index.getClassByName(clazz.superName());
            }
        }

        return false;
    }

    @Override
    public final AnnotationInstance annotation(Declaration declaration, DotName name) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            throw new UnsupportedOperationException();
        }

        Collection<AnnotationInstance> annotations = getAnnotationsFor(declaration);
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(name)) {
                return annotation;
            }
        }

        if (inheritedAnnotations && declaration.kind() == AnnotationTarget.Kind.CLASS
                && declaration.asClass().superName() != null) {
            ClassInfo clazz = index.getClassByName(declaration.asClass().superName());
            while (clazz != null && !DotName.OBJECT_NAME.equals(clazz.name())) {
                for (AnnotationInstance annotation : getAnnotationsFor(clazz)) {
                    ClassInfo annotationClass = index.getClassByName(annotation.name());
                    if (annotationClass != null
                            && annotationClass.hasDeclaredAnnotation(DotName.INHERITED_NAME)
                            && annotation.name().equals(name)) {
                        return annotation;
                    }
                }
                clazz = index.getClassByName(clazz.superName());
            }
        }

        return null;
    }

    @Override
    public final Collection<AnnotationInstance> annotationsWithRepeatable(Declaration declaration, DotName name) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            throw new UnsupportedOperationException();
        }

        DotName containerName = null;
        {
            ClassInfo annotationClass = index.getClassByName(name);
            if (annotationClass != null) {
                AnnotationInstance repeatable = annotationClass.declaredAnnotation(DotName.REPEATABLE_NAME);
                if (repeatable != null) {
                    containerName = repeatable.value().asClass().name();
                }
            }
        }

        List<AnnotationInstance> result = new ArrayList<>();
        for (AnnotationInstance annotation : getAnnotationsFor(declaration)) {
            if (annotation.name().equals(name)) {
                result.add(annotation);
            } else if (annotation.name().equals(containerName)) {
                AnnotationInstance[] nestedAnnotations = annotation.value().asNestedArray();
                for (AnnotationInstance nestedAnnotation : nestedAnnotations) {
                    result.add(AnnotationInstance.create(nestedAnnotation, annotation.target()));
                }
            }
        }

        if (inheritedAnnotations && declaration.kind() == AnnotationTarget.Kind.CLASS
                && declaration.asClass().superName() != null) {
            ClassInfo clazz = index.getClassByName(declaration.asClass().superName());
            while (result.isEmpty() && clazz != null && !DotName.OBJECT_NAME.equals(clazz.name())) {
                for (AnnotationInstance annotation : getAnnotationsFor(clazz)) {
                    ClassInfo annotationClass = index.getClassByName(annotation.name());
                    if (annotationClass != null && annotationClass.hasDeclaredAnnotation(DotName.INHERITED_NAME)) {
                        if (annotation.name().equals(name)) {
                            result.add(annotation);
                        } else if (annotation.name().equals(containerName)) {
                            AnnotationInstance[] nestedAnnotations = annotation.value().asNestedArray();
                            for (AnnotationInstance nestedAnnotation : nestedAnnotations) {
                                result.add(AnnotationInstance.create(nestedAnnotation, annotation.target()));
                            }
                        }
                    }
                }
                clazz = index.getClassByName(clazz.superName());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public final Collection<AnnotationInstance> annotations(Declaration declaration) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            throw new UnsupportedOperationException();
        }

        Collection<AnnotationInstance> result = getAnnotationsFor(declaration);

        if (inheritedAnnotations && declaration.kind() == AnnotationTarget.Kind.CLASS
                && declaration.asClass().superName() != null) {
            result = new ArrayList<>(result);
            ClassInfo clazz = index.getClassByName(declaration.asClass().superName());
            while (clazz != null && !DotName.OBJECT_NAME.equals(clazz.name())) {
                for (AnnotationInstance annotation : getAnnotationsFor(clazz)) {
                    ClassInfo annotationClass = index.getClassByName(annotation.name());
                    if (annotationClass != null && annotationClass.hasDeclaredAnnotation(DotName.INHERITED_NAME)) {
                        boolean noMatchingAnnotationPresent = true;
                        for (AnnotationInstance it : result) {
                            if (it.name().equals(annotation.name())) {
                                noMatchingAnnotationPresent = false;
                                break;
                            }
                        }
                        if (noMatchingAnnotationPresent) {
                            result.add(annotation);
                        }
                    }
                }
                clazz = index.getClassByName(clazz.superName());
            }
            result = Collections.unmodifiableCollection(result);
        }

        return result;
    }

    Collection<AnnotationInstance> getAnnotationsFor(Declaration declaration) {
        EquivalenceKey key = EquivalenceKey.of(declaration);
        return overlay.computeIfAbsent(key, new Function<EquivalenceKey, Collection<AnnotationInstance>>() {
            @Override
            public Collection<AnnotationInstance> apply(EquivalenceKey ignored) {
                Collection<AnnotationInstance> original = getOriginalAnnotations(declaration);
                TransformationContextImpl transformationContext = new TransformationContextImpl(declaration, original);
                for (AnnotationTransformation transformation : transformations) {
                    if (transformation.supports(declaration.kind())) {
                        transformation.apply(transformationContext);
                    }
                }

                if (transformationContext.modified()) {
                    Collection<AnnotationInstance> result = transformationContext.annotations;
                    if (result.isEmpty()) {
                        return Collections.emptyList();
                    } else if (result.size() == 1) {
                        return Collections.singleton(result.iterator().next());
                    } else {
                        return Collections.unmodifiableCollection(result);
                    }
                }
                return original;
            }
        });
    }

    // always returns an unmodifiable collection
    final Collection<AnnotationInstance> getOriginalAnnotations(Declaration declaration) {
        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD) {
            List<AnnotationInstance> annotations = declaration.asMethod().annotations();
            if (annotations.isEmpty()) {
                return Collections.emptyList();
            }

            List<AnnotationInstance> result = new ArrayList<>(annotations.size());
            for (AnnotationInstance annotation : annotations) {
                if (annotation.target() != null
                        && (annotation.target().kind() == AnnotationTarget.Kind.METHOD
                                || annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER)
                        && (!runtimeAnnotationsOnly || annotation.runtimeVisible())) {
                    result.add(annotation);
                }
            }
            return Collections.unmodifiableList(result);
        }

        Collection<AnnotationInstance> annotations = declaration.declaredAnnotations();

        if (!runtimeAnnotationsOnly) {
            return annotations;
        }

        List<AnnotationInstance> result = new ArrayList<>(annotations.size());
        for (AnnotationInstance annotation : annotations) {
            if (annotation.runtimeVisible()) {
                result.add(annotation);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static final class TransformationContextImpl implements TransformationContext {
        private final Declaration declaration;
        private final Collection<AnnotationInstance> originalAnnotations;
        private Set<AnnotationInstance> annotations;

        TransformationContextImpl(Declaration declaration, Collection<AnnotationInstance> annotations) {
            this.declaration = declaration;
            this.originalAnnotations = annotations;
            this.annotations = null;
        }

        boolean modified() {
            return annotations != null;
        }

        private void initializeIfNecessary() {
            if (annotations == null) {
                annotations = new HashSet<>(originalAnnotations);
            }
        }

        @Override
        public Declaration declaration() {
            return declaration;
        }

        @Override
        public Collection<AnnotationInstance> annotations() {
            initializeIfNecessary();
            return annotations;
        }

        @Override
        public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
            Objects.requireNonNull(annotationClass);
            return hasAnnotation(DotName.createSimple(annotationClass));
        }

        @Override
        public boolean hasAnnotation(DotName annotationName) {
            Objects.requireNonNull(annotationName);
            for (AnnotationInstance annotation : modified() ? annotations : originalAnnotations) {
                if (annotation.name().equals(annotationName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasAnnotation(Predicate<AnnotationInstance> predicate) {
            Objects.requireNonNull(predicate);
            for (AnnotationInstance annotation : modified() ? annotations : originalAnnotations) {
                if (predicate.test(annotation)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void add(Class<? extends Annotation> annotationClass) {
            initializeIfNecessary();

            Objects.requireNonNull(annotationClass);
            annotations.add(AnnotationInstance.builder(annotationClass).buildWithTarget(declaration));
        }

        @Override
        public void add(AnnotationInstance annotation) {
            initializeIfNecessary();

            Objects.requireNonNull(annotation);
            if (annotation.target() == null) {
                annotation = AnnotationInstance.create(annotation, declaration);
            }
            annotations.add(annotation);
        }

        @Override
        public void addAll(AnnotationInstance... annotations) {
            initializeIfNecessary();

            Objects.requireNonNull(annotations);
            for (int i = 0; i < annotations.length; i++) {
                if (annotations[i].target() == null) {
                    annotations[i] = AnnotationInstance.create(annotations[i], declaration);
                }
            }
            Collections.addAll(this.annotations, annotations);
        }

        @Override
        public void addAll(Collection<AnnotationInstance> annotations) {
            initializeIfNecessary();

            Objects.requireNonNull(annotations);
            boolean hasNullTarget = false;
            for (AnnotationInstance annotation : annotations) {
                if (annotation.target() == null) {
                    hasNullTarget = true;
                    break;
                }
            }
            if (hasNullTarget) {
                List<AnnotationInstance> fixed = new ArrayList<>();
                for (AnnotationInstance annotation : annotations) {
                    if (annotation.target() == null) {
                        fixed.add(AnnotationInstance.create(annotation, declaration));
                    } else {
                        fixed.add(annotation);
                    }
                }
                annotations = fixed;
            }
            this.annotations.addAll(annotations);
        }

        @Override
        public void remove(Predicate<AnnotationInstance> predicate) {
            initializeIfNecessary();

            Objects.requireNonNull(predicate);
            annotations.removeIf(predicate);
        }

        @Override
        public void removeAll() {
            // skipping `initializeIfNecessary()` here, because that would do useless work
            if (modified()) {
                annotations.clear();
            } else {
                annotations = new HashSet<>();
            }
        }
    }
}
