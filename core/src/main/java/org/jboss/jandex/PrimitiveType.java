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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a primitive Java type. While a set of constants is provided for easy of use,
 * instance equality should not be used to compare to them. Instead {@link #equals(Object)}
 * should be used.
 *
 * <p>
 * A primitive is considered equal to another primitive if it specifies the same primitive
 * enumeration value, and contains an equal set of annotation instances.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public final class PrimitiveType extends Type {
    public static final PrimitiveType BYTE = new PrimitiveType(Primitive.BYTE);
    public static final PrimitiveType CHAR = new PrimitiveType(Primitive.CHAR);
    public static final PrimitiveType DOUBLE = new PrimitiveType(Primitive.DOUBLE);
    public static final PrimitiveType FLOAT = new PrimitiveType(Primitive.FLOAT);
    public static final PrimitiveType INT = new PrimitiveType(Primitive.INT);
    public static final PrimitiveType LONG = new PrimitiveType(Primitive.LONG);
    public static final PrimitiveType SHORT = new PrimitiveType(Primitive.SHORT);
    public static final PrimitiveType BOOLEAN = new PrimitiveType(Primitive.BOOLEAN);

    private static final Map<String, PrimitiveType> reverseMap = new HashMap<>();

    private static final Map<Primitive, ClassType> boxingMap = new HashMap<>();
    private static final Map<DotName, PrimitiveType> unboxingMap = new HashMap<>();

    static {
        reverseMap.put("byte", BYTE);
        reverseMap.put("char", CHAR);
        reverseMap.put("double", DOUBLE);
        reverseMap.put("float", FLOAT);
        reverseMap.put("int", INT);
        reverseMap.put("long", LONG);
        reverseMap.put("short", SHORT);
        reverseMap.put("boolean", BOOLEAN);

        boxingMap.put(Primitive.BYTE, ClassType.BYTE_CLASS);
        boxingMap.put(Primitive.CHAR, ClassType.CHARACTER_CLASS);
        boxingMap.put(Primitive.DOUBLE, ClassType.DOUBLE_CLASS);
        boxingMap.put(Primitive.FLOAT, ClassType.FLOAT_CLASS);
        boxingMap.put(Primitive.INT, ClassType.INTEGER_CLASS);
        boxingMap.put(Primitive.LONG, ClassType.LONG_CLASS);
        boxingMap.put(Primitive.SHORT, ClassType.SHORT_CLASS);
        boxingMap.put(Primitive.BOOLEAN, ClassType.BOOLEAN_CLASS);

        unboxingMap.put(ClassType.BYTE_CLASS.name(), BYTE);
        unboxingMap.put(ClassType.CHARACTER_CLASS.name(), CHAR);
        unboxingMap.put(ClassType.DOUBLE_CLASS.name(), DOUBLE);
        unboxingMap.put(ClassType.FLOAT_CLASS.name(), FLOAT);
        unboxingMap.put(ClassType.INTEGER_CLASS.name(), INT);
        unboxingMap.put(ClassType.LONG_CLASS.name(), LONG);
        unboxingMap.put(ClassType.SHORT_CLASS.name(), SHORT);
        unboxingMap.put(ClassType.BOOLEAN_CLASS.name(), BOOLEAN);
    }

    /**
     * Specifies the underlying Java primitive type for a <code>PrimitiveType</code>
     */
    public enum Primitive {
        /** Indicates a primitive byte type */
        BYTE,
        /** Indicates a primitive character type */
        CHAR,
        /** Indicates a primitive double type */
        DOUBLE,
        /** Indicates a primitive float type */
        FLOAT,
        /** Indicates a primitive integer type */
        INT,
        /** Indicates a primitive long type */
        LONG,
        /** Indicates a primitive short type */
        SHORT,
        /** Indicates a primitive boolean type */
        BOOLEAN,
    }

    private final Primitive primitive;

    private PrimitiveType(Primitive primitive) {
        this(primitive, null);
    }

    private PrimitiveType(Primitive primitive, AnnotationInstance[] annotations) {
        super(DotName.createSimple(primitive.name().toLowerCase(Locale.ENGLISH)), annotations);
        this.primitive = primitive;
    }

    @Override
    public Kind kind() {
        return Kind.PRIMITIVE;
    }

    /**
     * The type of primitive this primitive type represents
     *
     * @return the primitive
     */
    public Primitive primitive() {
        return primitive;
    }

    /**
     * Returns a class type that is the result of a boxing conversion of the given {@code primitiveType}.
     * <p>
     * Returns {@code null} if {@code primitiveType} is {@code null}.
     *
     * @param primitiveType a primitive type, may be {@code null}
     * @return the corresponding class type, or {@code null} if {@code primitiveType} is {@code null}
     */
    public static ClassType box(PrimitiveType primitiveType) {
        if (primitiveType == null) {
            return null;
        }
        return boxingMap.get(primitiveType.primitive);
    }

    /**
     * Returns whether the given {@code type} is a wrapper class for any primitive type
     * (or, in other words, whether the {@code type} may be a result of a boxing conversion
     * of any primitive type.)
     * <p>
     * Returns {@code false} if {@code type} is not a {@link Type.Kind#CLASS} or when
     * {@code type} is {@code null}.
     *
     * @param type the type to check, may be {@code null}
     * @return whether the given {@code type} is a wrapper class for a primitive type
     */
    public static boolean isBox(Type type) {
        return type != null && type.kind() == Kind.CLASS && unboxingMap.containsKey(type.name());
    }

    /**
     * Returns a primitive type that is the result of an unboxing conversion of the given {@code classType}.
     * <p>
     * Returns {@code null} if no unboxing conversion exists for given class type
     * or if {@code classType} is {@code null}.
     *
     * @param classType a class type, may be {@code null}
     * @return the corresponding primitive type, or {@code null} if there's none
     */
    public static PrimitiveType unbox(ClassType classType) {
        if (classType == null) {
            return null;
        }
        return unboxingMap.get(classType.name());
    }

    @Override
    public PrimitiveType asPrimitiveType() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new PrimitiveType(primitive, newAnnotations);
    }

    char toCode() {
        Primitive primitive = this.primitive;
        if (primitive == Primitive.BYTE) {
            return 'B';
        } else if (primitive == Primitive.CHAR) {
            return 'C';
        } else if (primitive == Primitive.DOUBLE) {
            return 'D';
        } else if (primitive == Primitive.FLOAT) {
            return 'F';
        } else if (primitive == Primitive.INT) {
            return 'I';
        } else if (primitive == Primitive.LONG) {
            return 'J';
        } else if (primitive == Primitive.SHORT) {
            return 'S';
        }

        // BOOLEAN
        return 'Z';
    }

    static PrimitiveType decode(String name) {
        return reverseMap.get(name);
    }

    static PrimitiveType decode(char c) {
        switch (c) {
            case 'B':
                return PrimitiveType.BYTE;
            case 'C':
                return PrimitiveType.CHAR;
            case 'D':
                return PrimitiveType.DOUBLE;
            case 'F':
                return PrimitiveType.FLOAT;
            case 'I':
                return PrimitiveType.INT;
            case 'J':
                return PrimitiveType.LONG;
            case 'S':
                return PrimitiveType.SHORT;
            case 'Z':
                return PrimitiveType.BOOLEAN;
            default:
                return null;
        }
    }

    static PrimitiveType fromOridinal(int ordinal) {
        switch (ordinal) {
            case 0:
                return BYTE;
            case 1:
                return CHAR;
            case 2:
                return DOUBLE;
            case 3:
                return FLOAT;
            case 4:
                return INT;
            case 5:
                return LONG;
            case 6:
                return SHORT;
            case 7:
                return BOOLEAN;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PrimitiveType)) {
            return false;
        }

        PrimitiveType that = (PrimitiveType) o;
        return super.equals(o) && primitive == that.primitive;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + primitive.name().hashCode();
        return result;
    }

    @Override
    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PrimitiveType)) {
            return false;
        }

        PrimitiveType that = (PrimitiveType) o;
        return super.internEquals(o) && primitive == that.primitive;
    }

    @Override
    int internHashCode() {
        int result = super.internHashCode();
        result = 31 * result + primitive.name().hashCode();
        return result;
    }
}
