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
 * <p>A primitive is considered equal to another primitive if it specifies the same primitive
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

    private static final Map<String, PrimitiveType> reverseMap = new HashMap<String, PrimitiveType>();

    static {
        reverseMap.put("byte", BYTE);
        reverseMap.put("char", CHAR);
        reverseMap.put("double", DOUBLE);
        reverseMap.put("float", FLOAT);
        reverseMap.put("int", INT);
        reverseMap.put("long", LONG);
        reverseMap.put("short", SHORT);
        reverseMap.put("boolean", BOOLEAN);
    }

    /**
     * Specifies the underlying Java primitive type for a <code>PrimitiveType</code>
     */
    public enum Primitive {
        /** Indicates a primitive byte type      */ BYTE,
        /** Indicates a primitive character type */ CHAR,
        /** Indicates a primitive double type    */ DOUBLE,
        /** Indicates a primitive float type     */ FLOAT,
        /** Indicates a primitive integer type   */ INT,
        /** Indicates a primitive long type      */ LONG,
        /** Indicates a primitive short type     */ SHORT,
        /** Indicates a primitive boolean type   */ BOOLEAN,
    }

    private final Primitive primitive;

    private PrimitiveType(Primitive primitive) {
        this(primitive, null);
    }

    private PrimitiveType(Primitive primitive, AnnotationInstance[] annotations) {
        super(new DotName(null, primitive.name().toLowerCase(Locale.ENGLISH), true, false), annotations);
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

    @Override
    public PrimitiveType asPrimitiveType() {
        return this;
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
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new PrimitiveType(primitive, newAnnotations);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + primitive.hashCode();
        return result;
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
}
