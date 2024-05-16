package org.jboss.jandex;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An annotation transformation.
 *
 * @see #priority()
 * @see #supports(AnnotationTarget.Kind)
 * @see #apply(TransformationContext)
 * @see #builder()
 * @see #forClasses()
 * @see #forFields()
 * @see #forMethods()
 * @see #forMethodParameters()
 * @see #forRecordComponents()
 * @since 3.2.0
 */
public interface AnnotationTransformation {
    /**
     * The default {@link #priority()} value: 1000.
     */
    int DEFAULT_PRIORITY_VALUE = 1000;

    /**
     * Returns the priority of this annotation transformation. Annotation transformations
     * are applied in descending order of priority values (that is, transformation with
     * higher priority value is executed sooner than transformation with smaller priority
     * value).
     * <p>
     * By default, the priority is {@link #DEFAULT_PRIORITY_VALUE}.
     *
     * @return the priority of this annotation transformation
     */
    default int priority() {
        return DEFAULT_PRIORITY_VALUE;
    }

    /**
     * Returns whether this annotation transformation supports given {@link AnnotationTarget.Kind kind}
     * of declarations. A transformation is only {@linkplain #apply(TransformationContext) applied}
     * if it supports the correct kind of declarations.
     * <p>
     * By default, the transformation supports all declaration kinds.
     *
     * @param kind the kind of declaration, never {@code null}
     * @return whether this annotation transformation should apply
     */
    default boolean supports(AnnotationTarget.Kind kind) {
        return true;
    }

    /**
     * Implements the actual annotation transformation.
     *
     * @param context the {@linkplain TransformationContext transformation context}, never {@code null}
     */
    void apply(TransformationContext context);

    /**
     * Returns whether this annotation transformation requires the annotation overlay to be
     * in the {@linkplain AnnotationOverlay.Builder#compatibleMode() compatible mode}.
     * When this method returns {@code true} and the annotation overlay is not set to be
     * in the compatible mode, an exception is thrown during construction of the overlay.
     * <p>
     * This method returns {@code false} by default and should be overridden sparingly.
     *
     * @return whether this transformation requires the annotation overlay to be in the compatible mode
     */
    default boolean requiresCompatibleMode() {
        return false;
    }

    /**
     * A transformation context. Passed as a singular parameter to {@link #apply(TransformationContext)}.
     *
     * @see #declaration()
     * @see #annotations()
     * @see #hasAnnotation(Class)
     * @see #hasAnnotation(DotName)
     * @see #hasAnnotation(Predicate)
     * @see #add(Class)
     * @see #add(AnnotationInstance)
     * @see #addAll(AnnotationInstance...)
     * @see #addAll(Collection)
     * @see #remove(Predicate)
     * @see #removeAll()
     */
    interface TransformationContext {
        /**
         * Returns the declaration that is being transformed.
         *
         * @return the declaration that is being transformed
         */
        Declaration declaration();

        /**
         * Returns the collection of annotations present on the declaration that is being transformed.
         * Reflects all changes done by this annotation transformation and all annotation transformations
         * executed prior to this one.
         * <p>
         * Changes made directly to this collection and changes made through the other
         * {@code TransformationContext} methods are interchangeable.
         *
         * @return the collection of annotations present on the declaration that is being transformed
         */
        Collection<AnnotationInstance> annotations();

        /**
         * Returns whether the {@linkplain #annotations() current set of annotations} contains
         * an annotation of given {@code annotationClass}.
         *
         * @param annotationClass the annotation class, must not be {@code null}
         * @return whether the current set of annotations contains an annotation of given class
         */
        boolean hasAnnotation(Class<? extends Annotation> annotationClass);

        /**
         * Returns whether the {@linkplain #annotations() current set of annotations} contains
         * an annotation whose class has given {@code annotationName}.
         *
         * @param annotationName name of the annotation class, must not be {@code null}
         * @return whether the current set of annotations contains an annotation of given class
         */
        boolean hasAnnotation(DotName annotationName);

        /**
         * Returns whether the {@linkplain #annotations() current set of annotations} contains
         * an annotation that matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return whether the current set of annotations contains an annotation of given class
         */
        boolean hasAnnotation(Predicate<AnnotationInstance> predicate);

        /**
         * Adds an annotation of given {@code annotationClass} to
         * the {@linkplain #annotations() current set of annotations}.
         * <p>
         * The annotation type must have no members.
         *
         * @param annotationClass the class of annotation to add, must not be {@code null}
         */
        void add(Class<? extends Annotation> annotationClass);

        /**
         * Adds the {@code annotation} to the {@linkplain #annotations() current set of annotations}.
         *
         * @param annotation the annotation to add, must not be {@code null}
         */
        void add(AnnotationInstance annotation);

        /**
         * Adds all {@code annotations} to the {@linkplain #annotations() current set of annotations}.
         *
         * @param annotations the annotations to add, must not be {@code null}
         */
        void addAll(AnnotationInstance... annotations);

        /**
         * Adds all {@code annotations} to the {@linkplain #annotations() current set of annotations}.
         *
         * @param annotations the annotations to add, must not be {@code null}
         */
        void addAll(Collection<AnnotationInstance> annotations);

        /**
         * Removes annotations that match given {@code predicate} from
         * the {@linkplain #annotations() current set of annotations}.
         *
         * @param predicate the annotation predicate, must not be {@code null}
         */
        void remove(Predicate<AnnotationInstance> predicate);

        /**
         * Removes all annotations from {@linkplain #annotations() current set of annotations}.
         */
        void removeAll();
    }

    // ---

    /**
     * Returns a builder for annotation transformation of arbitrary declarations.
     *
     * @return a builder for annotation transformation of arbitrary declarations
     */
    static DeclarationBuilder builder() {
        return new DeclarationBuilder();
    }

    /**
     * Returns a builder for annotation transformation of classes.
     *
     * @return a builder for annotation transformation of classes
     */
    static ClassBuilder forClasses() {
        return new ClassBuilder();
    }

    /**
     * Returns a builder for annotation transformation of fields.
     *
     * @return a builder for annotation transformation of fields
     */
    static FieldBuilder forFields() {
        return new FieldBuilder();
    }

    /**
     * Returns a builder for annotation transformation of methods.
     *
     * @return a builder for annotation transformation of methods
     */
    static MethodBuilder forMethods() {
        return new MethodBuilder();
    }

    /**
     * Returns a builder for annotation transformation of method parameters.
     *
     * @return a builder for annotation transformation of method parameters
     */
    static MethodParameterBuilder forMethodParameters() {
        return new MethodParameterBuilder();
    }

    /**
     * Returns a builder for annotation transformation of record components.
     *
     * @return a builder for annotation transformation of record components
     */
    static RecordComponentBuilder forRecordComponents() {
        return new RecordComponentBuilder();
    }

    /**
     * Abstract class for {@linkplain AnnotationTransformation annotation transformation} builders.
     *
     * @see #priority(int)
     * @see #whenAnyMatch(Class...)
     * @see #whenAnyMatch(DotName...)
     * @see #whenAnyMatch(List)
     * @see #whenAnyMatch(Predicate)
     * @see #whenAllMatch(Class...)
     * @see #whenAllMatch(DotName...)
     * @see #whenAllMatch(List)
     * @see #whenAllMatch(Predicate)
     * @see #whenNoneMatch(Class...)
     * @see #whenNoneMatch(DotName...)
     * @see #whenNoneMatch(List)
     * @see #whenNoneMatch(Predicate)
     * @see #when(Predicate)
     * @see DeclarationBuilder
     * @see ClassBuilder
     * @see FieldBuilder
     * @see MethodBuilder
     * @see MethodParameterBuilder
     * @see RecordComponentBuilder
     * @param <THIS> type of this builder
     */
    abstract class Builder<THIS extends Builder<THIS>> {
        private final AnnotationTarget.Kind kind;

        private int priority;
        private Predicate<TransformationContext> predicate;

        Builder(AnnotationTarget.Kind kind) {
            this.kind = kind;
            this.priority = DEFAULT_PRIORITY_VALUE;
        }

        /**
         * Sets the priority of the built annotation transformation.
         * By default, the priority is {@link #DEFAULT_PRIORITY_VALUE}.
         *
         * @param priority the priority
         * @return this builder
         */
        public final THIS priority(int priority) {
            this.priority = priority;
            return self();
        }

        @SafeVarargs
        private static Predicate<AnnotationInstance> annotationPredicate(Class<? extends Annotation>... classes) {
            Objects.requireNonNull(classes);
            return annotation -> {
                String annotationName = annotation.name().toString();
                for (Class<? extends Annotation> clazz : classes) {
                    if (annotationName.equals(clazz.getName())) {
                        return true;
                    }
                }
                return false;
            };
        }

        private static Predicate<AnnotationInstance> annotationPredicate(DotName... classes) {
            Objects.requireNonNull(classes);
            return annotation -> {
                DotName annotationName = annotation.name();
                for (DotName clazz : classes) {
                    if (annotationName.equals(clazz)) {
                        return true;
                    }
                }
                return false;
            };
        }

        /**
         * Adds a predicate that tests whether any of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        @SafeVarargs
        public final THIS whenAnyMatch(Class<? extends Annotation>... classes) {
            Objects.requireNonNull(classes);
            return whenAnyMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether any of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAnyMatch(DotName... classes) {
            Objects.requireNonNull(classes);
            return whenAnyMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether any of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAnyMatch(List<DotName> classes) {
            Objects.requireNonNull(classes);
            return whenAnyMatch(classes.toArray(new DotName[0]));
        }

        /**
         * Adds a predicate that tests whether any of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * matches the given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAnyMatch(Predicate<AnnotationInstance> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> {
                Collection<AnnotationInstance> annotations = ctx.annotations();
                for (AnnotationInstance annotation : annotations) {
                    if (predicate.test(annotation)) {
                        return true;
                    }
                }
                return false;
            });
        }

        /**
         * Adds a predicate that tests whether all of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * are of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        @SafeVarargs
        public final THIS whenAllMatch(Class<? extends Annotation>... classes) {
            Objects.requireNonNull(classes);
            return whenAllMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether all of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * are of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAllMatch(DotName... classes) {
            Objects.requireNonNull(classes);
            return whenAllMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether all of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * are of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAllMatch(List<DotName> classes) {
            Objects.requireNonNull(classes);
            return whenAllMatch(classes.toArray(new DotName[0]));
        }

        /**
         * Adds a predicate that tests whether all of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * match the given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenAllMatch(Predicate<AnnotationInstance> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> {
                Collection<AnnotationInstance> annotations = ctx.annotations();
                for (AnnotationInstance annotation : annotations) {
                    if (!predicate.test(annotation)) {
                        return false;
                    }
                }
                return true;
            });
        }

        /**
         * Adds a predicate that tests whether none of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        @SafeVarargs
        public final THIS whenNoneMatch(Class<? extends Annotation>... classes) {
            Objects.requireNonNull(classes);
            return whenNoneMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether none of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenNoneMatch(DotName... classes) {
            Objects.requireNonNull(classes);
            return whenNoneMatch(annotationPredicate(classes));
        }

        /**
         * Adds a predicate that tests whether none of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * is of given {@code classes}.
         *
         * @param classes the annotation classes, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenNoneMatch(List<DotName> classes) {
            Objects.requireNonNull(classes);
            return whenNoneMatch(classes.toArray(new DotName[0]));
        }

        /**
         * Adds a predicate that tests whether none of
         * the {@linkplain TransformationContext#annotations() current set of annotations}
         * matches the given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public final THIS whenNoneMatch(Predicate<AnnotationInstance> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> {
                Collection<AnnotationInstance> annotations = ctx.annotations();
                for (AnnotationInstance annotation : annotations) {
                    if (predicate.test(annotation)) {
                        return false;
                    }
                }
                return true;
            });
        }

        /**
         * Adds a predicate to the list of predicates that will be tested before applying the transformation.
         * If some of the predicates returns {@code false}, the transformation is not applied. In other words,
         * the predicates are combined using logical <em>and</em> (conjunction).
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         */
        public THIS when(Predicate<TransformationContext> predicate) {
            Objects.requireNonNull(predicate);
            if (this.predicate == null) {
                this.predicate = predicate;
            } else {
                this.predicate = this.predicate.and(predicate);
            }
            return self();
        }

        /**
         * Builds an annotation transformation based on the given {@code transformation} function.
         *
         * @param transformation the transformation function, must not be {@code null}
         * @return the built annotation transformation, never {@code null}
         */
        public AnnotationTransformation transform(Consumer<TransformationContext> transformation) {
            Objects.requireNonNull(transformation);

            return new AnnotationTransformation() {
                @Override
                public int priority() {
                    return priority;
                }

                @Override
                public boolean supports(AnnotationTarget.Kind kind) {
                    return Builder.this.kind == null || Builder.this.kind == kind;
                }

                @Override
                public void apply(TransformationContext context) {
                    if (predicate == null || predicate.test(context)) {
                        transformation.accept(context);
                    }
                }
            };
        }

        @SuppressWarnings("unchecked")
        THIS self() {
            return (THIS) this;
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for arbitrary declarations.
     *
     * @see #whenDeclaration(Predicate)
     * @see Builder
     */
    class DeclarationBuilder extends Builder<DeclarationBuilder> {
        DeclarationBuilder() {
            super(null);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current declaration}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public DeclarationBuilder whenDeclaration(Predicate<Declaration> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration()));
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for classes.
     *
     * @see #whenClass(Class)
     * @see #whenClass(DotName)
     * @see #whenClass(Predicate)
     * @see Builder
     */
    class ClassBuilder extends Builder<ClassBuilder> {
        ClassBuilder() {
            super(AnnotationTarget.Kind.CLASS);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current class}
         * is the given {@code clazz}.
         *
         * @param clazz the class, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public ClassBuilder whenClass(Class<?> clazz) {
            Objects.requireNonNull(clazz);
            return whenClass(DotName.createSimple(clazz));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current class}
         * has given {@code name}.
         *
         * @param name the class name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public ClassBuilder whenClass(DotName name) {
            Objects.requireNonNull(name);
            return whenClass(clazz -> clazz.name().equals(name));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current class}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public ClassBuilder whenClass(Predicate<ClassInfo> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration().asClass()));
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for fields.
     *
     * @see #whenField(Class, String)
     * @see #whenField(DotName, String)
     * @see #whenField(Predicate)
     * @see Builder
     */
    class FieldBuilder extends Builder<FieldBuilder> {
        FieldBuilder() {
            super(AnnotationTarget.Kind.FIELD);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current field}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class, must not be {@code null}
         * @param name the field name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public FieldBuilder whenField(Class<?> clazz, String name) {
            Objects.requireNonNull(clazz);
            return whenField(DotName.createSimple(clazz), name);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current field}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class name, must not be {@code null}
         * @param name the field name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public FieldBuilder whenField(DotName clazz, String name) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(name);
            return whenField(field -> field.name().equals(name) && field.declaringClass().name().equals(clazz));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current field}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public FieldBuilder whenField(Predicate<FieldInfo> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration().asField()));
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for methods.
     *
     * @see #whenMethod(Class, String)
     * @see #whenMethod(DotName, String)
     * @see #whenMethod(Predicate)
     * @see Builder
     */
    class MethodBuilder extends Builder<MethodBuilder> {
        MethodBuilder() {
            super(AnnotationTarget.Kind.METHOD);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class, must not be {@code null}
         * @param name the method name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodBuilder whenMethod(Class<?> clazz, String name) {
            Objects.requireNonNull(clazz);
            return whenMethod(DotName.createSimple(clazz), name);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class name, must not be {@code null}
         * @param name the method name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodBuilder whenMethod(DotName clazz, String name) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(name);
            return whenMethod(method -> method.name().equals(name) && method.declaringClass().name().equals(clazz));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodBuilder whenMethod(Predicate<MethodInfo> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration().asMethod()));
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for method parameters.
     *
     * @see #whenMethodParameter(Class, String)
     * @see #whenMethodParameter(DotName, String)
     * @see #whenMethodParameter(Predicate)
     * @see Builder
     */
    class MethodParameterBuilder extends Builder<MethodParameterBuilder> {
        MethodParameterBuilder() {
            super(AnnotationTarget.Kind.METHOD_PARAMETER);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method parameter}
         * belongs to a method with given {@code name} declared on given {@code clazz}.
         *
         * @param clazz the class, must not be {@code null}
         * @param name the method name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodParameterBuilder whenMethodParameter(Class<?> clazz, String name) {
            Objects.requireNonNull(clazz);
            return whenMethodParameter(DotName.createSimple(clazz), name);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method parameter}
         * belongs to a method with given {@code name} declared on given {@code clazz}.
         *
         * @param clazz the class name, must not be {@code null}
         * @param name the method name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodParameterBuilder whenMethodParameter(DotName clazz, String name) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(name);
            return whenMethodParameter(param -> param.method().name().equals(name)
                    && param.method().declaringClass().name().equals(clazz));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current method parameter}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public MethodParameterBuilder whenMethodParameter(Predicate<MethodParameterInfo> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration().asMethodParameter()));
        }
    }

    /**
     * A builder of {@linkplain AnnotationTransformation annotation transformations} for record components.
     *
     * @see #whenRecordComponent(Class, String)
     * @see #whenRecordComponent(DotName, String)
     * @see #whenRecordComponent(Predicate)
     * @see Builder
     */
    class RecordComponentBuilder extends Builder<RecordComponentBuilder> {
        RecordComponentBuilder() {
            super(AnnotationTarget.Kind.RECORD_COMPONENT);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current record component}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class, must not be {@code null}
         * @param name the record component name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public RecordComponentBuilder whenRecordComponent(Class<?> clazz, String name) {
            Objects.requireNonNull(clazz);
            return whenRecordComponent(DotName.createSimple(clazz), name);
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current record component}
         * has given {@code name} and is declared on given {@code clazz}.
         *
         * @param clazz the class name, must not be {@code null}
         * @param name the record component name, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public RecordComponentBuilder whenRecordComponent(DotName clazz, String name) {
            Objects.requireNonNull(clazz);
            Objects.requireNonNull(name);
            return whenRecordComponent(component -> component.name().equals(name)
                    && component.declaringClass().name().equals(clazz));
        }

        /**
         * Adds a predicate that tests whether
         * the {@linkplain TransformationContext#declaration() current record component}
         * matches given {@code predicate}.
         *
         * @param predicate the predicate, must not be {@code null}
         * @return this builder
         * @see #when(Predicate)
         */
        public RecordComponentBuilder whenRecordComponent(Predicate<RecordComponentInfo> predicate) {
            Objects.requireNonNull(predicate);
            return when(ctx -> predicate.test(ctx.declaration().asRecordComponent()));
        }
    }
}
