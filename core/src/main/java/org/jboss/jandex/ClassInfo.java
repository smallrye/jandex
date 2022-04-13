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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a class entry in an index. A ClassInfo is only a partial view of a
 * Java class, it is not intended as a complete replacement for Java reflection.
 * Only the methods and fields which are references by an annotation are stored.
 *
 * <p>
 * Global information including the parent class, implemented interfaces, and
 * access flags are also provided since this information is often necessary.
 *
 * <p>
 * Note that a parent class and interface may exist outside of the scope of the
 * index (e.g. classes in a different jar) so the references are stored as names
 * instead of direct references. It is expected that multiple indexes may need
 * to be queried to assemble a full hierarchy in a complex multi-jar environment
 * (e.g. an application server).
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class ClassInfo implements AnnotationTarget {

    private static final int MODULE = 0x8000;
    private static final int MAX_POSITIONS = 256;
    private static final byte[] EMPTY_POSITIONS = new byte[0];

    private final DotName name;
    private Map<DotName, List<AnnotationInstance>> annotations;

    // Not final to allow lazy initialization, immutable once published
    private short flags;
    private Type[] interfaceTypes;
    private Type superClassType;
    private Type[] typeParameters;
    private MethodInternal[] methods;
    private FieldInternal[] fields;
    private RecordComponentInternal[] recordComponents;
    private byte[] methodPositions = EMPTY_POSITIONS;
    private byte[] fieldPositions = EMPTY_POSITIONS;
    private byte[] recordComponentPositions = EMPTY_POSITIONS;
    private boolean hasNoArgsConstructor;
    private NestingInfo nestingInfo;

    /** Describes the form of nesting used by a class */
    public enum NestingType {
        /** A standard class declared within its own source unit */
        TOP_LEVEL,

        /** A named class enclosed by another class */
        INNER,

        /** A named class enclosed within a code block */
        LOCAL,

        /** An unnamed class enclosed within a code block */
        ANONYMOUS
    }

    private static final class NestingInfo {
        private DotName enclosingClass;
        private String simpleName;
        private EnclosingMethodInfo enclosingMethod;

        // non-null if the class is in fact a module descriptor
        // this field would naturally belong to ClassInfo, but is present here
        // to make the ClassInfo object smaller in the most common case:
        // non-nested class that is not a module descriptor
        private ModuleInfo module;
    }

    /**
     * Provides information on the enclosing method or constructor for a local or anonymous class,
     * if available.
     */
    public static final class EnclosingMethodInfo {
        private String name;
        private Type returnType;
        private Type[] parameters;
        private DotName enclosingClass;

        /**
         * The name of the method or constructor
         *
         * @return the name of the method or constructor
         */
        public String name() {
            return name;
        }

        /**
         * Returns the return type of the method.
         *
         * @return the return type
         */
        public Type returnType() {
            return returnType;
        }

        /**
         * Returns the list of parameters declared by this method or constructor.
         * This may be empty, but never null.
         *
         * @return the list of parameters.
         */
        public List<Type> parameters() {
            return Collections.unmodifiableList(Arrays.asList(parameters));
        }

        Type[] parametersArray() {
            return parameters;
        }

        /**
         * Returns the class name which declares this method or constructor.
         *
         * @return the name of the class which declared this method or constructor
         */
        public DotName enclosingClass() {
            return enclosingClass;
        }

        EnclosingMethodInfo(String name, Type returnType, Type[] parameters, DotName enclosingClass) {
            this.name = name;
            this.returnType = returnType;
            this.parameters = parameters;
            this.enclosingClass = enclosingClass;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(returnType).append(' ').append(enclosingClass).append('.').append(name).append('(');
            for (int i = 0; i < parameters.length; i++) {
                builder.append(parameters[i]);
                if (i + 1 < parameters.length)
                    builder.append(", ");
            }
            builder.append(')');
            return builder.toString();
        }

    }

    ClassInfo(DotName name, Type superClassType, short flags, Type[] interfaceTypes) {
        this(name, superClassType, flags, interfaceTypes, false);
    }

    ClassInfo(DotName name, Type superClassType, short flags, Type[] interfaceTypes, boolean hasNoArgsConstructor) {
        this.name = name;
        this.superClassType = superClassType;
        this.flags = flags;
        this.interfaceTypes = interfaceTypes.length == 0 ? Type.EMPTY_ARRAY : interfaceTypes;
        this.hasNoArgsConstructor = hasNoArgsConstructor;
        this.typeParameters = Type.EMPTY_ARRAY;
        this.methods = MethodInternal.EMPTY_ARRAY;
        this.fields = FieldInternal.EMPTY_ARRAY;
    }

    /**
     * Constructs a "mock" ClassInfo using the passed values. All passed values MUST NOT BE MODIFIED AFTER THIS CALL.
     * Otherwise the resulting object would not conform to the contract outlined above.
     *
     * @param name the name of this class
     * @param superName the name of the parent class
     * @param flags the class attributes
     * @param interfaces the interfaces this class implements
     * @param annotations the annotations on this class
     * @param hasNoArgsConstructor whether this class has a no arg constructor
     * @return a new mock class representation
     */
    @Deprecated
    public static ClassInfo create(DotName name, DotName superName, short flags, DotName[] interfaces,
            Map<DotName, List<AnnotationInstance>> annotations, boolean hasNoArgsConstructor) {
        Type[] interfaceTypes = new Type[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceTypes[i] = new ClassType(interfaces[i]);
        }

        ClassType superClassType = superName == null ? null : new ClassType(superName);
        ClassInfo clazz = new ClassInfo(name, superClassType, flags, interfaceTypes, hasNoArgsConstructor);
        clazz.setAnnotations(annotations);
        return clazz;
    }

    @Override
    public final Kind kind() {
        return Kind.CLASS;
    }

    public String toString() {
        return name.toString();
    }

    /**
     * Returns the name of the class
     *
     * @return the name of the class
     */
    public final DotName name() {
        return name;
    }

    /**
     * Returns the access flags for this class. The standard {@link java.lang.reflect.Modifier}
     * can be used to decode the value.
     *
     * @return the access flags
     */
    public final short flags() {
        return flags;
    }

    /**
     *
     * @return {@code true} if this class is a synthetic class
     */
    public final boolean isSynthetic() {
        return Modifiers.isSynthetic(flags);
    }

    /**
     *
     * @return {@code true} if this class was declared as an enum
     */
    public final boolean isEnum() {
        return (flags & Modifiers.ENUM) != 0 && DotName.ENUM_NAME.equals(superName());
    }

    /**
     *
     * @return {@code true} if this class object represents an annotation type
     */
    public final boolean isAnnotation() {
        return (flags & Modifiers.ANNOTATION) != 0;
    }

    /**
     * @return {@code true} if this class was declared as a record
     */
    public final boolean isRecord() {
        // there's no flag for record classes, but extending java.lang.Record
        // is prohibited, so this should be fine
        return DotName.RECORD_NAME.equals(superName());
    }

    /**
     * @return {@code true} if this class object represents a Java module descriptor
     */
    public final boolean isModule() {
        return (flags & MODULE) != 0;
    }

    /**
     * Returns the name of the super class declared by the extends clause of this class. This
     * information is also available from the {@link #superClassType} method. For all classes,
     * with the one exception of <code>java.lang.Object</code>, which is the one class in the
     * Java language without a super-type, this method will always return a non-null value.
     *
     * @return the name of the super class of this class, or null if this class is <code>java.lang.Object</code>
     */
    public final DotName superName() {
        return superClassType == null ? null : superClassType.name();
    }

    /**
     * Returns an array of interface names implemented by this class. Every call to this method
     * performs a defensive copy, so {@link #interfaceNames()} should be used instead.
     *
     * @return an array of interface names implemented by this class
     */
    @Deprecated
    public final DotName[] interfaces() {
        DotName[] interfaces = new DotName[interfaceTypes.length];
        for (int i = 0; i < interfaceTypes.length; i++) {
            interfaces[i] = interfaceTypes[i].name();
        }
        return interfaces;
    }

    /**
     * Returns whether an annotation instance with given name is declared on this class, any of its members,
     * or any type within the signature of the class or its members.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public final boolean hasAnnotation(DotName name) {
        return annotations.containsKey(name) && !annotations.get(name).isEmpty();
    }

    /**
     * Returns the annotation instance with given name declared on this class, any of its members, or any type
     * within the signature of the class or its members. The {@code target()} method of the returned annotation
     * instance may be used to determine the exact location of the annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java; gutter: false;">
     *     {@literal @}MyClassAnnotation
     *     public class Foo&lt;{@literal @}MyTypeAnnotation T&gt; {
     *         {@literal @}MyFieldAnnotation
     *         public String foo;
     *
     *         public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     *
     *         {@literal @}MyMethodAnnotation
     *         public void foo() {...}
     *
     *         public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     *         public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     *         public &lt;{@literal @}MyTypeAnnotation T&gt; void foo(T t) {...}
     *     }
     * </pre>
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotations(DotName)
     */
    @Override
    public final AnnotationInstance annotation(DotName name) {
        if (annotations.containsKey(name) && !annotations.get(name).isEmpty()) {
            return annotations.get(name).get(0);
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this class, any of its members, or any type
     * within the signature of the class or its members. The {@code target()} method of the returned annotation
     * instances may be used to determine the exact location of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java; gutter: false;">
     *     {@literal @}MyClassAnnotation
     *     public class Foo&lt;{@literal @}MyTypeAnnotation T&gt; {
     *         {@literal @}MyFieldAnnotation
     *         public String foo;
     *
     *         public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     *
     *         {@literal @}MyMethodAnnotation
     *         public void foo() {...}
     *
     *         public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     *         public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     *         public &lt;{@literal @}MyTypeAnnotation T&gt; void foo(T t) {...}
     *     }
     * </pre>
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> annotations(DotName name) {
        if (annotations.containsKey(name)) {
            return Collections.unmodifiableList(annotations.get(name));
        }
        return Collections.emptyList();
    }

    /**
     * Returns the annotation instances with given name declared on this class, any of its members, or any type
     * within the signature of the class or its members. The {@code target()} method of the returned annotation
     * instances may be used to determine the exact location of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations(DotName)
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>(annotations(name));
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            for (AnnotationInstance container : annotations(containingType.name())) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this class, any of its members, or any type
     * within the signature of the class or its members. The {@code target()} method of the returned annotation
     * instances may be used to determine the exact location of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java; gutter: false;">
     *     {@literal @}MyClassAnnotation
     *     public class Foo&lt;{@literal @}MyTypeAnnotation T&gt; {
     *         {@literal @}MyFieldAnnotation
     *         public String foo;
     *
     *         public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     *
     *         {@literal @}MyMethodAnnotation
     *         public void foo() {...}
     *
     *         public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     *         public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     *         public &lt;{@literal @}MyTypeAnnotation T&gt; void foo(T t) {...}
     *     }
     * </pre>
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     */
    @Override
    public final List<AnnotationInstance> annotations() {
        List<AnnotationInstance> result = new ArrayList<>();
        for (List<AnnotationInstance> list : annotations.values()) {
            for (AnnotationInstance instance : list) {
                result.add(instance);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns whether an annotation instance with given name is declared on this class.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on the class members
     * and types within the signature of the class and its members.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    @Override
    public final boolean hasDeclaredAnnotation(DotName name) {
        return declaredAnnotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this class.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on the class members
     * and types within the signature of the class and its members.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public final AnnotationInstance declaredAnnotation(DotName name) {
        List<AnnotationInstance> instances = annotations.get(name);
        if (instances != null) {
            for (AnnotationInstance instance : instances) {
                if (instance.target().kind() == Kind.CLASS) {
                    return instance;
                }
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this class.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on the class members and types within the signature of the class and its members.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    @Override
    public final List<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>();
        AnnotationInstance declaredInstance = declaredAnnotation(name);
        if (declaredInstance != null) {
            instances.add(declaredInstance);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            AnnotationInstance container = declaredAnnotation(containingType.name());
            if (container != null) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this class.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations the class members
     * and types within the signature of the class and its members.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> declaredAnnotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (List<AnnotationInstance> list : annotations.values()) {
            for (AnnotationInstance instance : list) {
                if (instance.target().kind() == Kind.CLASS) {
                    instances.add(instance);
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns a map indexed by annotation name, with a value list of annotation instances.
     * The annotation instances in this map correspond to both annotations on the class,
     * and every nested element of the class (fields, types, methods, etc).
     * <p>
     * The target of the annotation instance can be used to determine the location of
     * the annotation usage.
     *
     * @return immutable map of annotations specified on this class and its elements, never {@code null}
     */
    public final Map<DotName, List<AnnotationInstance>> annotationsMap() {
        return Collections.unmodifiableMap(annotations);
    }

    final void setAnnotations(Map<DotName, List<AnnotationInstance>> annotations) {
        this.annotations = annotations;
    }

    /**
     * Returns a list of all annotations directly declared on this class.
     *
     * @deprecated use {@link #declaredAnnotations()}
     * @return immutable list of annotations declared on this class
     */
    @Deprecated
    public final Collection<AnnotationInstance> classAnnotations() {
        return declaredAnnotations();
    }

    /**
     * Returns the annotation with the specified name directly declared on this class.
     *
     * @deprecated use {@link #declaredAnnotation(DotName)}
     * @param name the annotation name to look for
     * @return the declared annotation or null if not found
     */
    @Deprecated
    public final AnnotationInstance classAnnotation(DotName name) {
        return declaredAnnotation(name);
    }

    /**
     * Retrieves annotation instances declared on this class, by the name of the annotation.
     *
     * If the specified annotation is repeatable (JLS 9.6), then attempt to result contains the values from the containing
     * annotation.
     *
     * @deprecated use {@link #declaredAnnotationsWithRepeatable(DotName, IndexView)}
     * @param name the name of the annotation
     * @param index the index used to obtain the annotation class
     * @return immutable list of annotation instances declared on this class, or an empty list if none
     */
    @Deprecated
    public final List<AnnotationInstance> classAnnotationsWithRepeatable(DotName name, IndexView index) {
        return declaredAnnotationsWithRepeatable(name, index);
    }

    /**
     * Returns a list of all methods declared in this class. This includes constructors
     * and static initializer blocks which have the special JVM assigned names of "&lt;init&gt;"
     * and "&lt;clinit&gt;", respectively. It does not, however, include inherited methods.
     * These must be discovered by traversing the class hierarchy.
     *
     * <p>
     * This list may be empty, but never null.
     * </p>
     *
     * @return the list of methods declared in this class
     */
    public final List<MethodInfo> methods() {
        return new MethodInfoGenerator(this, methods, EMPTY_POSITIONS);
    }

    public final List<MethodInfo> unsortedMethods() {
        return new MethodInfoGenerator(this, methods, methodPositions);
    }

    /**
     * Returns a list of all constructors declared in this class (which the JVM names "&lt;init&gt;").
     * It does not include inherited methods.
     * These must be discovered by traversing the class hierarchy.
     *
     * <p>
     * This list may never be null.
     * </p>
     *
     * @return the list of constructors declared in this class
     */
    public final List<MethodInfo> constructors() {
        List<MethodInfo> constructors = new ArrayList<MethodInfo>(1);
        for (MethodInfo method : methods()) {
            if ("<init>".equals(method.name())) {
                constructors.add(method);
            }
        }
        return constructors;
    }

    final MethodInternal[] methodArray() {
        return methods;
    }

    final byte[] methodPositionArray() {
        return methodPositions;
    }

    /**
     * Retrieves a method based on its signature, which includes a method name and an argument list.
     * The argument list is compared based on the underlying raw type of the type arguments. As an example,
     * a generic type parameter "T" is equivalent to <code>java.lang.Object</code>, since the raw form
     * of a type parameter is its upper bound.
     *
     * <p>
     * Eligible methods include constructors and static initializer blocks which have the special JVM
     * assigned names of "&lt;init&gt;" and "&lt;clinit&gt;", respectively. This does not, however, include
     * inherited methods. These must be discovered by traversing the class hierarchy.
     * </p>
     *
     * @param name the name of the method to find
     * @param parameters the type parameters of the method
     * @return the located method or null if not found
     */
    public final MethodInfo method(String name, Type... parameters) {
        MethodInternal key = new MethodInternal(Utils.toUTF8(name), MethodInternal.EMPTY_PARAMETER_NAMES, parameters, null,
                (short) 0);
        int i = Arrays.binarySearch(methods, key, MethodInternal.NAME_AND_PARAMETER_COMPONENT_COMPARATOR);
        return i >= 0 ? new MethodInfo(this, methods[i]) : null;
    }

    /**
     * Retrieves the "first" occurrence of a method by the given name. Note that the order of methods
     * is not defined, and may change in the future. Therefore, this method should not be used when
     * overloading is possible. It's merely intended to provide a handy shortcut for throw away or test
     * code.
     *
     * @param name the name of the method
     * @return the first discovered method matching this name, or null if no match is found
     */
    public final MethodInfo firstMethod(String name) {
        MethodInternal key = new MethodInternal(Utils.toUTF8(name), MethodInternal.EMPTY_PARAMETER_NAMES, Type.EMPTY_ARRAY,
                null, (short) 0);
        int i = Arrays.binarySearch(methods, key, MethodInternal.NAME_AND_PARAMETER_COMPONENT_COMPARATOR);
        if (i < -methods.length) {
            return null;
        }

        MethodInfo method = new MethodInfo(this, i >= 0 ? methods[i] : methods[++i * -1]);
        return method.name().equals(name) ? method : null;
    }

    /**
     * Retrieves a field by the given name. Only fields declared in this class are available.
     * Locating inherited fields requires traversing the class hierarchy.
     *
     * @param name the name of the field
     * @return the field
     */
    public final FieldInfo field(String name) {
        FieldInternal key = new FieldInternal(Utils.toUTF8(name), VoidType.VOID, (short) 0);
        int i = Arrays.binarySearch(fields, key, FieldInternal.NAME_COMPARATOR);
        if (i < 0) {
            return null;
        }

        return new FieldInfo(this, fields[i]);
    }

    /**
     * Returns a list of all available fields. Only fields declared in this class are available.
     * Locating inherited fields requires traversing the class hierarchy. This list may be
     * empty, but never null.
     *
     * @return a list of fields
     */
    public final List<FieldInfo> fields() {
        return new FieldInfoGenerator(this, fields, EMPTY_POSITIONS);
    }

    public final List<FieldInfo> unsortedFields() {
        return new FieldInfoGenerator(this, fields, fieldPositions);
    }

    final FieldInternal[] fieldArray() {
        return fields;
    }

    final byte[] fieldPositionArray() {
        return fieldPositions;
    }

    /**
     * Retrieves a record component by the given name.
     *
     * @param name the name of the record component
     * @return the record component
     */
    public final RecordComponentInfo recordComponent(String name) {
        RecordComponentInternal key = new RecordComponentInternal(Utils.toUTF8(name), VoidType.VOID);
        int i = Arrays.binarySearch(recordComponents, key, RecordComponentInternal.NAME_COMPARATOR);
        if (i < 0) {
            return null;
        }
        return new RecordComponentInfo(this, recordComponents[i]);
    }

    /**
     * Returns a list of all record components declared by this class.
     * This list may be empty, but never null.
     *
     * @return a list of record components
     */
    public final List<RecordComponentInfo> recordComponents() {
        return new RecordComponentInfoGenerator(this, recordComponents, EMPTY_POSITIONS);
    }

    public final List<RecordComponentInfo> unsortedRecordComponents() {
        return new RecordComponentInfoGenerator(this, recordComponents, recordComponentPositions);
    }

    final RecordComponentInternal[] recordComponentArray() {
        return recordComponents;
    }

    final byte[] recordComponentPositionArray() {
        return recordComponentPositions;
    }

    /**
     * Returns a list of names for all interfaces this class implements. This list may be empty, but never null.
     * <p>
     * Note that this information is also available on the <code>Type</code> instances returned by
     * {@link #interfaceTypes}
     *
     * @return immutable list of names of interfaces implemented by this class
     */
    public final List<DotName> interfaceNames() {
        return new AbstractList<DotName>() {
            @Override
            public DotName get(int i) {
                return interfaceTypes[i].name();
            }

            @Override
            public int size() {
                return interfaceTypes.length;
            }
        };
    }

    /**
     * Returns the list of types in the {@code implements} clause of this class. These types may be generic types.
     * This list may be empty, but is never {@code null}.
     *
     * @return immutable list of types declared in the {@code implements} clause of this class
     */
    public final List<Type> interfaceTypes() {
        return Collections.unmodifiableList(Arrays.asList(interfaceTypes));
    }

    final Type[] interfaceTypeArray() {
        return interfaceTypes;
    }

    final Type[] copyInterfaceTypes() {
        return interfaceTypes.clone();
    }

    /**
     * Returns a super type represented by the extends clause of this class. This type might be a generic type.
     *
     * @return the super class type definition in the extends clause
     */
    public final Type superClassType() {
        return superClassType;
    }

    /**
     * Returns the generic type parameters of this class, if any. These will be returned as resolved type variables,
     * so if a parameter has a bound on another parameter, that information will be available.
     *
     * @return immutable list of generic type parameters of this class
     */
    public final List<TypeVariable> typeParameters() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<TypeVariable> list = (List) Arrays.asList(typeParameters);
        return Collections.unmodifiableList(list);
    }

    final Type[] typeParameterArray() {
        return typeParameters;
    }

    /**
     * Returns a boolean indicating the presence of a no-arg constructor, if supported by the underlying index store.
     * This information is available in indexes produced by Jandex 1.2.0 and later.
     *
     * @return <code>true</code> in case of the Java class has a no-copyParameters constructor, <code>false</code>
     *         if it does not, or it is not known
     * @since 1.2.0
     */
    public final boolean hasNoArgsConstructor() {
        return hasNoArgsConstructor;
    }

    /**
     * Returns the nesting type of this class, which could either be a standard top level class, an inner class,
     * an anonymous class, or a local class.
     *
     * <p>
     * For historical reasons, static nested classes are returned as <code>INNER</code>. You can differentiate
     * between a non-static nested class (inner class) and a static nested class by calling
     * {@link java.lang.reflect.Modifier#isStatic(int)} on the return of {@link #flags()}
     * </p>
     *
     * @return the nesting type of this class
     */
    public NestingType nestingType() {
        if (nestingInfo == null || nestingInfo.module != null) {
            return NestingType.TOP_LEVEL;
        } else if (nestingInfo.enclosingClass != null) {
            return NestingType.INNER;
        } else if (nestingInfo.simpleName != null) {
            return NestingType.LOCAL;
        }

        return NestingType.ANONYMOUS;
    }

    /**
     * Returns the source declared name of this class if it is an inner class, or a local class. Otherwise
     * this method will return null.
     *
     * @return the simple name of a top-level, local, or inner class, or null if this is an anonymous class
     */
    public String simpleName() {
        return nestingInfo != null ? nestingInfo.simpleName : name.local();
    }

    String nestingSimpleName() {
        return nestingInfo != null ? nestingInfo.simpleName : null;
    }

    /**
     * Returns the enclosing class if this is an inner class, or null if this is an anonymous, a local, or
     * a top level class.
     *
     * @return the enclosing class if this class is an inner class
     */
    public DotName enclosingClass() {
        return nestingInfo != null ? nestingInfo.enclosingClass : null;
    }

    /**
     * Returns the enclosing method of this class if it is a local, or anonymous class, and it is declared
     * within the body of a method or constructor. It will return null if this class is a top level, or an inner class.
     * It will also return null if the local or anonymous class is on an initializer.
     *
     * @return the enclosing method/constructor, if this class is local or anonymous, and it is within a
     *         method/constructor
     */
    public EnclosingMethodInfo enclosingMethod() {
        return nestingInfo != null ? nestingInfo.enclosingMethod : null;
    }

    /**
     * Returns the module information from this class if it is a module descriptor, i.e. {@code module-info}.
     *
     * @return the module descriptor for module classes, otherwise {@code null}
     */
    public ModuleInfo module() {
        return nestingInfo != null ? nestingInfo.module : null;
    }

    @Override
    public ClassInfo asClass() {
        return this;
    }

    @Override
    public FieldInfo asField() {
        throw new IllegalArgumentException("Not a field");
    }

    @Override
    public MethodInfo asMethod() {
        throw new IllegalArgumentException("Not a method");
    }

    @Override
    public MethodParameterInfo asMethodParameter() {
        throw new IllegalArgumentException("Not a method parameter");
    }

    @Override
    public TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public RecordComponentInfo asRecordComponent() {
        throw new IllegalArgumentException("Not a record component");
    }

    void setHasNoArgsConstructor(boolean hasNoArgsConstructor) {
        this.hasNoArgsConstructor = hasNoArgsConstructor;
    }

    void setFields(List<FieldInfo> fields, NameTable names) {
        final int size = fields.size();

        if (size == 0) {
            this.fields = FieldInternal.EMPTY_ARRAY;
            return;
        }

        this.fields = new FieldInternal[size];

        for (int i = 0; i < size; i++) {
            FieldInfo fieldInfo = fields.get(i);
            FieldInternal internal = names.intern(fieldInfo.fieldInternal());
            fieldInfo.setFieldInternal(internal);
            this.fields[i] = internal;
        }

        this.fieldPositions = sortAndGetPositions(this.fields, FieldInternal.NAME_COMPARATOR, names);
    }

    void setFieldArray(FieldInternal[] fields) {
        this.fields = fields;
    }

    void setFieldPositionArray(byte[] fieldPositions) {
        this.fieldPositions = fieldPositions;
    }

    void setMethodArray(MethodInternal[] methods) {
        this.methods = methods;
    }

    void setMethodPositionArray(byte[] methodPositions) {
        this.methodPositions = methodPositions;
    }

    void setMethods(List<MethodInfo> methods, NameTable names) {
        final int size = methods.size();

        if (size == 0) {
            this.methods = MethodInternal.EMPTY_ARRAY;
            return;
        }

        this.methods = new MethodInternal[size];

        for (int i = 0; i < size; i++) {
            MethodInfo methodInfo = methods.get(i);
            MethodInternal internal = names.intern(methodInfo.methodInternal());
            methodInfo.setMethodInternal(internal);
            this.methods[i] = internal;
        }

        this.methodPositions = sortAndGetPositions(this.methods, MethodInternal.NAME_AND_PARAMETER_COMPONENT_COMPARATOR, names);
    }

    void setRecordComponentArray(RecordComponentInternal[] recordComponents) {
        this.recordComponents = recordComponents;
    }

    void setRecordComponentPositionArray(byte[] recordComponentPositions) {
        this.recordComponentPositions = recordComponentPositions;
    }

    void setRecordComponents(List<RecordComponentInfo> recordComponents, NameTable names) {
        final int size = recordComponents.size();

        if (size == 0) {
            this.recordComponents = RecordComponentInternal.EMPTY_ARRAY;
            return;
        }

        this.recordComponents = new RecordComponentInternal[size];

        for (int i = 0; i < size; i++) {
            RecordComponentInfo recordComponentInfo = recordComponents.get(i);
            RecordComponentInternal internal = names.intern(recordComponentInfo.recordComponentInternal());
            recordComponentInfo.setRecordComponentInternal(internal);
            this.recordComponents[i] = internal;
        }

        this.recordComponentPositions = sortAndGetPositions(this.recordComponents, RecordComponentInternal.NAME_COMPARATOR,
                names);
    }

    /**
     * Sorts the array of internals using the provided comparator and returns an array
     * of offsets in the original order of internals.
     *
     * @param <T> An internal member type, FieldInternal or MethodInternal
     * @param internals Array of internal types set on the ClassInfo instance
     * @param comparator Comparator used to sort internals and locate original positions
     * @param names NameTable used to intern byte arrays of member positions
     * @return an array offsets in the array of internals in the order prior to sorting
     */
    static <T> byte[] sortAndGetPositions(T[] internals, Comparator<T> comparator, NameTable names) {
        final int size = internals.length;
        final boolean storePositions = (size > 1 && size <= MAX_POSITIONS);
        final Map<T, Integer> originalPositions;
        final byte[] positions;

        if (storePositions) {
            originalPositions = new IdentityHashMap<T, Integer>(size);
            for (int i = 0; i < size; i++) {
                originalPositions.put(internals[i], Integer.valueOf(i));
            }
        } else {
            originalPositions = null;
        }

        Arrays.sort(internals, comparator);

        if (storePositions) {
            positions = new byte[size];

            for (int i = 0; i < size; i++) {
                // `positions` stores the new position at the offset of the original position
                positions[originalPositions.get(internals[i])] = (byte) i;
            }
        } else {
            positions = EMPTY_POSITIONS;
        }

        return names.intern(positions);
    }

    void setSuperClassType(Type superClassType) {
        this.superClassType = superClassType;
    }

    void setInterfaceTypes(Type[] interfaceTypes) {
        this.interfaceTypes = interfaceTypes.length == 0 ? Type.EMPTY_ARRAY : interfaceTypes;
    }

    void setTypeParameters(Type[] typeParameters) {
        this.typeParameters = typeParameters.length == 0 ? Type.EMPTY_ARRAY : typeParameters;
    }

    void setInnerClassInfo(DotName enclosingClass, String simpleName, boolean knownInnerClass) {
        boolean setValues = enclosingClass != null || simpleName != null;

        // Always init known inner types since we might have an anonymous type
        // with a method-less encloser (static block).
        if (nestingInfo == null && (knownInnerClass || setValues)) {
            nestingInfo = new NestingInfo();
        }

        if (!setValues) {
            return;
        }

        nestingInfo.enclosingClass = enclosingClass;
        nestingInfo.simpleName = simpleName;
    }

    void setEnclosingMethod(EnclosingMethodInfo enclosingMethod) {
        if (enclosingMethod == null) {
            return;
        }

        if (nestingInfo == null) {
            nestingInfo = new NestingInfo();
        }

        nestingInfo.enclosingMethod = enclosingMethod;
    }

    void setModule(ModuleInfo module) {
        if (module == null) {
            return;
        }

        if (nestingInfo == null) {
            nestingInfo = new NestingInfo();
        }

        nestingInfo.module = module;
    }

    void setFlags(short flags) {
        this.flags = flags;
    }
}
