package org.jboss.jandex;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

final class MutableAnnotationOverlayImpl extends AnnotationOverlayImpl implements MutableAnnotationOverlay {
    private volatile boolean frozen;

    MutableAnnotationOverlayImpl(IndexView index, boolean compatibleMode, boolean runtimeAnnotationsOnly,
            boolean inheritedAnnotations) {
        super(index, compatibleMode, runtimeAnnotationsOnly, inheritedAnnotations, false, Collections.emptyList());
    }

    @Override
    Set<AnnotationInstance> getAnnotationsFor(Declaration declaration) {
        EquivalenceKey key = EquivalenceKey.of(declaration);
        Set<AnnotationInstance> annotations = overlay.get(key);
        if (annotations == null) {
            annotations = getOriginalAnnotations(declaration);
            overlay.put(key, annotations);
        }
        return annotations;
    }

    @Override
    public void addAnnotation(Declaration declaration, AnnotationInstance annotation) {
        if (frozen) {
            throw new IllegalStateException("Mutable annotation overlay is already frozen");
        }

        if (annotation.target() == null) {
            annotation = AnnotationInstance.create(annotation, declaration);
        }

        getAnnotationsFor(declaration).add(annotation);
        transformations.add(addTransformation(declaration, annotation));
    }

    private AnnotationTransformation addTransformation(Declaration declaration, AnnotationInstance annotation) {
        AnnotationTarget.Kind declarationKind;
        EquivalenceKey key;

        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            // the `annotation` has correct `target`, see `addAnnotation()` above,
            // so we don't need to do anything else
            declarationKind = AnnotationTarget.Kind.METHOD;
            key = EquivalenceKey.of(declaration.asMethodParameter().method());
        } else {
            declarationKind = declaration.kind();
            key = EquivalenceKey.of(declaration);
        }

        return new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == declarationKind;
            }

            @Override
            public void apply(TransformationContext context) {
                if (key.equals(EquivalenceKey.of(context.declaration()))) {
                    context.add(annotation);
                }
            }

            @Override
            public boolean requiresCompatibleMode() {
                return compatibleMode;
            }
        };
    }

    @Override
    public void removeAnnotations(Declaration declaration, Predicate<AnnotationInstance> predicate) {
        if (frozen) {
            throw new IllegalStateException("Mutable annotation overlay is already frozen");
        }

        getAnnotationsFor(declaration).removeIf(predicate);
        transformations.add(removeTransformation(declaration, predicate));
    }

    private AnnotationTransformation removeTransformation(Declaration declaration, Predicate<AnnotationInstance> predicate) {
        AnnotationTarget.Kind declarationKind;
        EquivalenceKey key;
        Predicate<AnnotationInstance> finalPredicate;

        if (compatibleMode && declaration.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            declarationKind = AnnotationTarget.Kind.METHOD;
            key = EquivalenceKey.of(declaration.asMethodParameter().method());
            int position = declaration.asMethodParameter().position();
            finalPredicate = new Predicate<AnnotationInstance>() {
                @Override
                public boolean test(AnnotationInstance annotation) {
                    return annotation.target() != null
                            && annotation.target().kind() == AnnotationTarget.Kind.METHOD_PARAMETER
                            && annotation.target().asMethodParameter().position() == position
                            && predicate.test(annotation);
                }
            };
        } else {
            declarationKind = declaration.kind();
            key = EquivalenceKey.of(declaration);
            finalPredicate = predicate;
        }

        return new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == declarationKind;
            }

            @Override
            public void apply(TransformationContext context) {
                if (key.equals(EquivalenceKey.of(context.declaration()))) {
                    context.remove(finalPredicate);
                }
            }

            @Override
            public boolean requiresCompatibleMode() {
                return compatibleMode;
            }
        };
    }

    @Override
    public List<AnnotationTransformation> freeze() {
        frozen = true;
        return Collections.unmodifiableList(transformations);
    }
}
