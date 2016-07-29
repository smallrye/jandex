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
import java.util.List;
import java.util.Map;

/**
 * Represents a class entry in an index. A ClassInfo is only a partial view of a
 * Java class, it is not intended as a complete replacement for Java reflection.
 * Only the methods and fields which are references by an annotation are stored.
 *
 * <p>Global information including the parent class, implemented methodParameters, and
 * access flags are also provided since this information is often necessary.
 *
 * <p>Note that a parent class and interface may exist outside of the scope of the
 * index (e.g. classes in a different jar) so the references are stored as names
 * instead of direct references. It is expected that multiple indexes may need
 * to be queried to assemble a full hierarchy in a complex multi-jar environment
 * (e.g. an application server).
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class ClassInfo implements AnnotationTarget {

    private final DotName name;
    private final Map<DotName, List<AnnotationInstance>> annotations;

    // Not final to allow lazy initialization, immutable once published
    private  short flags;
    private Type[] interfaceTypes;
    private Type superClassType;
    private Type[] typeParameters;
    private MethodInternal[] methods;
    private FieldInternal[] fields;
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
        ANONYMOUS}

    private static final class NestingInfo {
        private DotName enclosingClass;
        private String simpleName;
        private EnclosingMethodInfo enclosingMethod;
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

    ClassInfo(DotName name, Type superClassType, short flags, Type[] interfaceTypes, Map<DotName, List<AnnotationInstance>> annotations) {
        this(name, superClassType, flags, interfaceTypes, annotations, false);
    }

    ClassInfo(DotName name, Type superClassType, short flags, Type[] interfaceTypes, Map<DotName, List<AnnotationInstance>> annotations, boolean hasNoArgsConstructor) {
        this.name = name;
        this.superClassType = superClassType;
        this.flags = flags;
        this.interfaceTypes = interfaceTypes.length == 0 ? Type.EMPTY_ARRAY : interfaceTypes;
        this.annotations = Collections.unmodifiableMap(annotations);  // FIXME
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
     * @param interfaces the methodParameters this class implements
     * @param annotations the annotations on this class
     * @param hasNoArgsConstructor whether this class has a no arg constructor
     * @return a new mock class representation
     */
    @Deprecated
    public static ClassInfo create(DotName name, DotName superName, short flags, DotName[] interfaces, Map<DotName, List<AnnotationInstance>> annotations, boolean hasNoArgsConstructor) {
        Type[] interfaceTypes = new Type[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceTypes[i] = new ClassType(interfaces[i]);
        }

        ClassType superClassType = superName == null ? null : new ClassType(superName);
        return new ClassInfo(name, superClassType, flags, interfaceTypes, annotations, hasNoArgsConstructor);
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
     * Returns a map indexed by annotation name, with a value list of annotation instances.
     * The annotation instances in this map correspond to both annotations on the class,
     * and every nested element of the class (fields, types, methods, etc).
     *
     * <p>The target of the annotation instance can be used to determine the location of
     * the annotation usage.</p>
     *
     * @return the annotations specified on this class and its elements
     */
    public final Map<DotName, List<AnnotationInstance>> annotations() {
        return annotations;
    }

    /**
     * Returns a list of all annotations directly declared on this class.
     *
     * @return the list of annotations declared on this class
     */
    public final Collection<AnnotationInstance> classAnnotations() {
        return new AnnotationTargetFilterCollection<ClassInfo>(annotations, ClassInfo.class);
    }

    /**
     * Returns a list of all methods declared in this class. This includes constructors
     * and static initializer blocks which have the special JVM assigned names of "&lt;init&gt;"
     * and "&lt;clinit&gt;", respectively. It does not, however, include inherited methods.
     * These must be discovered by traversing the class hierarchy.
     *
     * <p>This list may be empty, but never null.</p>
     *
     * @return the list of methods declared in this class
     */
    public final List<MethodInfo> methods() {
        return new MethodInfoGenerator(this, methods);
    }

    final MethodInternal[] methodArray() {
        return methods;
    }

    /**
     * Retrieves a method based on its signature, which includes a method name and an argument list.
     * The argument list is compared based on the underlying raw type of the type arguments. As an example,
     * a generic type parameter "T" is equivalent to <code>java.lang.Object</code>, since the raw form
     * of a type parameter is its upper bound.
     *
     * <p>Eligible methods include constructors and static initializer blocks which have the special JVM
     * assigned names of "&lt;init&gt;" and "&lt;clinit&gt;", respectively. This does not, however, include
     * inherited methods. These must be discovered by traversing the class hierarchy.</p>
     *
     * @param name the name of the method to find
     * @param parameters the type parameters of the method
     * @return the located method or null if not found
     */
    public final MethodInfo method(String name, Type... parameters) {
        MethodInternal key = new MethodInternal(Utils.toUTF8(name), parameters, null, (short) 0);
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
        MethodInternal key = new MethodInternal(Utils.toUTF8(name), Type.EMPTY_ARRAY, null, (short) 0);
        int i = Arrays.binarySearch(methods, key, MethodInternal.NAME_AND_PARAMETER_COMPONENT_COMPARATOR);
        if (i < -methods.length) {
            return null;
        }

        MethodInfo method = new MethodInfo(this,i >= 0 ? methods[i] : methods[++i * -1]);
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
        FieldInternal key = new FieldInternal(Utils.toUTF8(name), VoidType.VOID, (short)0);
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
        return new FieldInfoGenerator(this, fields);
    }

    final FieldInternal[] fieldArray() {
        return fields;
    }


    /**
     * Returns a list of names for all interfaces this class implements. This list may be empty, but never null.
     *
     * <p>Note that this information is also available on the <code>Type</code> instances returned by
     * {@link #interfaceTypes}</p>
     *
     * @return the list of names implemented by this class
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
     * Returns the list of types in the implements clause of this class. These types may be generic types.
     * This list may be empty, but never null
     *
     * @return the list of types declared in the implements clause of this class
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
     * @return the generic type parameters of this class
     */
    public final List<TypeVariable> typeParameters() {
        @SuppressWarnings("unchecked")
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
     * <p>For historical reasons, static nested classes are returned as <code>INNER</code>. You can differentiate
     * between a non-static nested class (inner class) and a static nested class by calling
     * {@link java.lang.reflect.Modifier#isStatic(int)} on the return of {@link #flags()} </p>
     *
     * @return the nesting type of this class
     */
    public NestingType nestingType() {
        if (nestingInfo == null) {
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
     * @return the simple name of a local or inner class, or null if this is a top level or anonymous class
     */
    public String simpleName() {
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
     * method/constructor
     */
    public EnclosingMethodInfo enclosingMethod() {
        return nestingInfo != null ? nestingInfo.enclosingMethod : null;
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

    void setHasNoArgsConstructor(boolean hasNoArgsConstructor) {
        this.hasNoArgsConstructor = hasNoArgsConstructor;
    }

    void setFields(List<FieldInfo> fields, NameTable names) {
        if (fields.size() == 0) {
            this.fields = FieldInternal.EMPTY_ARRAY;
            return;
        }
        this.fields = new FieldInternal[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo fieldInfo = fields.get(i);
            FieldInternal internal = names.intern(fieldInfo.fieldInternal());
            fieldInfo.setFieldInternal(internal);
            this.fields[i] = internal;
        }
        Arrays.sort(this.fields, FieldInternal.NAME_COMPARATOR);
    }

    void setFieldArray(FieldInternal[] fields) {
        this.fields = fields;
    }

    void setMethodArray(MethodInternal[] methods) {
        this.methods = methods;
    }

    void setMethods(List<MethodInfo> methods, NameTable names) {
        if (methods.size() == 0) {
            this.methods = MethodInternal.EMPTY_ARRAY;
            return;
        }

        this.methods = new MethodInternal[methods.size()];
        for (int i = 0; i < methods.size(); i++) {
            MethodInfo methodInfo = methods.get(i);
            MethodInternal internal = names.intern(methodInfo.methodInternal());
            methodInfo.setMethodInternal(internal);
            this.methods[i] = internal;
        }
        Arrays.sort(this.methods, MethodInternal.NAME_AND_PARAMETER_COMPONENT_COMPARATOR);
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

    void setInnerClassInfo(DotName enclosingClass, String simpleName) {
        if (enclosingClass == null && simpleName == null) {
            return;
        }

        if (nestingInfo == null) {
            nestingInfo = new NestingInfo();
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

    void setFlags(short flags) {
        this.flags = flags;
    }
}
