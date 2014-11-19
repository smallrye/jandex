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

import java.util.Arrays;

/**
 * An annotation value represents a specific name and value combination in the
 * parameter list of an annotation instance. It also can represent a nested
 * array element in the case of an array value.
 *
 * <p>
 * An annotation value can be any Java primitive:
 * <ul>
 * <li>byte</li>
 * <li>short</li>
 * <li>int</li>
 * <li>char</li>
 * <li>float</li>
 * <li>double</li>
 * <li>long</li>
 * <li>boolean</li>
 * </ul>
 *
 * <p>
 * As well as any the following specialty types:
 * <ul>
 * <li>String</li>
 * <li>Class</li>
 * <li>Enum</li>
 * <li>Nested annotation</li>
 * </ul>
 *
 * <p>
 * In addition a value can be a single-dimension array of any of the above types
 *
 * <p>
 * To access a value, the proper typed method must be used that matches the
 * expected type of the annotation parameter. In addition, some methods will
 * allow conversion of different types. For example, a byte can be returned as
 * an integer using {@link #asInt()}. Also all value types support a
 * String representation.
 *
 * <p>
 * To determine the underlying type, {@link #kind()} can be used.
 * </p>
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe
 * publication.
 *
 * @author Jason T. Greene
 *
 */
public abstract class AnnotationValue {
    static final AnnotationValue[] EMPTY_VALUE_ARRAY = new AnnotationValue[0];

    /**
     * Specifies the kind of annotation value, which can be used to determine the underlying Java type.
     */
    public enum Kind {
        /** Indicates a primitive byte value      */ BYTE,
        /** Indicates a primitive short value     */ SHORT,
        /** Indicates a primitive integer value   */ INTEGER,
        /** Indicates a primitive character value */ CHARACTER,
        /** Indicates a primitive float value     */ FLOAT,
        /** Indicates a primitive double value    */ DOUBLE,
        /** Indicates a primitive long value      */ LONG,
        /** Indicates a primitive boolean value   */ BOOLEAN,
        /** Indicates a Java Class value          */ CLASS,
        /** Indicates a Java String value         */ STRING,
        /** Indicates a Java Enum value           */ ENUM,
        /** Indicates an array value              */ ARRAY,
        /** Indicates a nested annotation value   */ NESTED,
        /** Indicates the value type is unknown   */ UNKNOWN
    }

    private final String name;

    AnnotationValue(String name) {
        this.name = name;
    }

    public static AnnotationValue createByteValue(String name, byte b) {
        return new ByteValue(name, b);
    }

    public static AnnotationValue createShortValue(String name, short s) {
        return new ShortValue(name, s);
    }

    public static AnnotationValue createIntegerValue(String name, int i) {
        return new IntegerValue(name, i);
    }

    public static AnnotationValue createCharacterValue(String name, char c) {
        return new CharacterValue(name, c);
    }

    public static AnnotationValue createFloatValue(String name, float f) {
        return new FloatValue(name, f);
    }

    public static AnnotationValue createDouleValue(String name, double d) {
        return new DoubleValue(name, d);
    }

    public static AnnotationValue createLongalue(String name, long l) {
        return new LongValue(name, l);
    }

    public static AnnotationValue createBooleanValue(String name, boolean bool) {
        return new BooleanValue(name, bool);
    }

    public static AnnotationValue createStringValue(String name, String string) {
        return new StringValue(name, string);
    }

    public static AnnotationValue createClassValue(String name, Type type) {
        return new ClassValue(name, type);
    }

    public static AnnotationValue createEnumValue(String name, DotName typeName, String value) {
        return new EnumValue(name, typeName, value);
    }

    public static AnnotationValue createArrayValue(String name, AnnotationValue[] values) {
        return new ArrayValue(name, values);
    }

    public static AnnotationValue createNestedAnnotationValue(String name, AnnotationInstance instance)
    {
        return new NestedAnnotation(name, instance);
    }

    /**
     * Returns the name of this value, which is typically the parameter name in the annotation
     * declaration. The value may not represent a parameter (e.g an array element member), in
     * which case name will simply return an empty string ("")
     *
     * @return the name of this value
     */
    public final String name() {
        return name;
    }

    /**
     * Returns a detyped value that represents the underlying annotation value.
     * It is recommended that the type specific methods be used instead.
     *
     * @return the underlying value
     */
    public abstract Object value();


    /**
     * Returns the kind of this value. The kind includes all Java primitives, String and Enum types, nested values,
     * and finally arrays of the above. Since the return type is itself an enumeration, it can be used with
     * Java switch statements.
     *
     * <p>
     * A special {@link org.jboss.jandex.AnnotationValue.Kind#UNKNOWN} kind is used to refer to components
     * of zero-length arrays, as the underlying type is not known.
     *
     * @return the kind of value
     * @since 2.0
     */
    public abstract Kind kind();

    /**
     * Returns the kind of value that represents the component type of this array if it can be determined.
     * Otherwise, this method returns {@link Kind#UNKNOWN}. This happens when the annotation value represents
     * an empty array. If the underlying value is not an array an exception will be thrown.
     *
     * @return the kind of component, or {@link Kind#UNKNOWN} if it can not be determined.
     * @throws java.lang.IllegalArgumentException if not an array
     * @since 2.0
     */
    public Kind componentKind() {
        throw new IllegalArgumentException("Not an array");
    }

    /**
     * Converts the underlying numerical type to an integer as if it was
     * casted in Java.
     *
     * @return an integer representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public int asInt() {
        throw new IllegalArgumentException("Not a number");
    }


    /**
     * Converts the underlying numerical type to an long as if it was
     * casted in Java.
     *
     * @return a long representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public long asLong() {
        throw new IllegalArgumentException("Not a number");
    }

    /**
     * Converts the underlying numerical type to a short as if it was
     * casted in Java.
     *
     * @return a short representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public short asShort() {
        throw new IllegalArgumentException("not a number");
    }

    /**
     * Converts the underlying numerical type to a byte as if it was
     * casted in Java.
     *
     * @return a byte representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public byte asByte() {
        throw new IllegalArgumentException("not a number");
    }

    /**
     * Converts the underlying numerical type to a float as if it was
     * casted in Java.
     *
     * @return a float representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public float asFloat() {
        throw new IllegalArgumentException("not a number");
    }

    /**
     * Converts the underlying numerical type to a double as if it was
     * casted in Java.
     *
     * @return a double representing the numerical parameter
     * @throws IllegalArgumentException if the value is not numerical
     */
    public double asDouble() {
        throw new IllegalArgumentException("not a number");
    }

    /**
     * Returns the underlying character value as Java primitive char.
     *
     * @return a char representing the character parameter
     * @throws IllegalArgumentException if the value is not a character
     */
    public char asChar() {
        throw new IllegalArgumentException("not a character");
    }

    /**
     * Returns the underlying boolean value as Java primitive boolean.
     *
     * @return a boolean representing the character parameter
     * @throws IllegalArgumentException if the value is not a boolean
     */
    public boolean asBoolean() {
        throw new IllegalArgumentException("not a boolean");
    }

    /**
     * Returns the string representation of the underlying value type.
     * The representation may or may not be convertible to the type
     * it represents. This is best used on String types, but can also
     * provide a useful way to quickly convert a value to a String.
     *
     * @return a string representing the value parameter
     */
    public String asString() {
        return value().toString();
    }

    /**
     * Returns the constant name, in string form, that represents the
     * Java enumeration of this value. The value is the same as the
     * one returned by {@link Enum#name()}.
     *
     * @return the string name of a Java enumeration
     * @throws IllegalArgumentException if the value is not an enum
     */
    public String asEnum() {
       throw new IllegalArgumentException("not an enum");
    }

    /**
     * Returns the type name, in DotName form, that represents the
     * Java enumeration of this value. The value is the same
     * as the one returned by {@link Enum#getClass()}.
     *
     * @return the type name of a Java enumeration
     * @throws IllegalArgumentException if the value is not an enum
     */
    public DotName asEnumType() {
        throw new IllegalArgumentException("not an enum");
     }

    /**
     * Returns the class name, in {@link Type} form, that represents a Java
     * Class used by this value. In addition to standard class name, it can also
     * refer to specialty types, such as {@link Void} and primitive types (e.g.
     * int.class). More specifically, any erased type that a method can return
     * is a valid annotation Class type.
     *
     * @return the Java type of this value
     * @throws IllegalArgumentException if the value is not a Class
     */
    public Type asClass() {
        throw new IllegalArgumentException("not a class");
    }

    /**
     * Returns a nested annotation represented by this value. The nested annotation
     * will have a null target, but may contain an arbitrary amount of nested values
     *
     * @return the underlying nested annotation instance
     * @throws IllegalArgumentException if the value is not a nested annotation
      */
    public AnnotationInstance asNested() {
        throw new IllegalArgumentException("not a nested annotation");
    }

    AnnotationValue[] asArray() {
        throw new IllegalArgumentException("Not an array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive
     * integer array.
     *
     * @return an int array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */
    public int[] asIntArray() {
        throw new IllegalArgumentException("Not a numerical array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive
     * long array.
     *
     * @return a long array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */
    public long[] asLongArray() {
        throw new IllegalArgumentException("Not a numerical array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive short array.
     *
     * @return a short array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */
    public short[] asShortArray() {
        throw new IllegalArgumentException("not a numerical array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive byte array.
     *
     * @return a byte array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */
    public byte[] asByteArray() {
        throw new IllegalArgumentException("not a numerical array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive float array.
     *
     * @return a float array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */
    public float[] asFloatArray() {
        throw new IllegalArgumentException("not a numerical array");
    }

    /**
     * Converts an underlying numerical array to a Java primitive double array.
     *
     * @return a double array that represents this value
     * @throws IllegalArgumentException if this value is not a numerical array.
     */

    public double[] asDoubleArray() {
        throw new IllegalArgumentException("not a numerical array");
    }

    /**
     * Returns the underlying character array.
     *
     * @return a character array that represents this value
     * @throws IllegalArgumentException if this value is not a character array.
     */
    public char[] asCharArray() {
        throw new IllegalArgumentException("not a character array");
    }

    /**
     * Returns the underlying boolean array.
     *
     * @return a boolean array that represents this value
     * @throws IllegalArgumentException if this value is not a boolean array.
     */
    public boolean[] asBooleanArray() {
        throw new IllegalArgumentException("not a boolean array");
    }

    /**
     * Returns a string array representation of the underlying array value.
     * The behavior is identical to {@link #asString()} as if it were applied
     * to every array element.
     *
     * @return a string array representing the underlying array value
     * @throws IllegalArgumentException if this value is not an array
     */
    public String[] asStringArray() {
        throw new IllegalArgumentException("not a string array");
    }

    /**
     * Returns an array of the constant name, in string form, that represents the
     * Java enumeration of each array element The individual element values are
     * the same as the one returned by {@link Enum#name()}.
     *
     * @return an array of string names of a Java enums
     * @throws IllegalArgumentException if the value is not an enum array
     */
    public String[] asEnumArray() {
       throw new IllegalArgumentException("not an enum array");
    }

    /**
     * Returns an array of the type name, in DotName form, that represents the
     * Java enumeration of each array element. The individual element values are
     * the same as the one returned by {@link Enum#getClass()}. Note that JLS
     * restricts an enum array parameter to the same type. Also, when an empty
     * array is specified in a value, it's types can not be determined.
     *
     * @return an array of string type names of Java enum array elements
     * @throws IllegalArgumentException if the value is not an enum array
     */
    public DotName[] asEnumTypeArray() {
        throw new IllegalArgumentException("not an enum array");
    }

    /**
     * Returns an array of class types representing the underlying class array value.
     * Each element has the same behavior as @{link {@link #asClass()}
     *
     * @return a class array representing this class array value
     * @throws IllegalArgumentException if the value is not a class array
     */
    public Type[] asClassArray() {
        throw new IllegalArgumentException("not a class array");
     }

    /**
     * Returns an array of nested annotations representing the underlying annotation array value.
     * Each element has the same behavior as @{link {@link #asNested()}
     *
     * @return an annotation array representing this annotation array value
     * @throws IllegalArgumentException if the value is not an annotation array
     */
    public AnnotationInstance[] asNestedArray() {
        throw new IllegalArgumentException("not a nested annotation array");
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (name.length() > 0)
            builder.append(name).append(" = ");
        return builder.append(value()).toString();
    }

    /**
     * Compares this annotation value to another annotation value, and returns true if equal.
     *
     * @param o the annotation value to compare to.
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnnotationValue that = (AnnotationValue) o;
        return name.equals(that.name);
    }

    /**
     * Computes a hash code for this annotation value.
     *
     * @return the hash code for this annotation value
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    static final class StringValue extends AnnotationValue {
        private final String value;

        StringValue(String name, String value) {
            super(name);
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.STRING;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (super.name.length() > 0)
                builder.append(super.name).append(" = ");

            return builder.append('"').append(value).append('"').toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StringValue that = (StringValue) o;
            return super.equals(o) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }

    static final class ByteValue extends AnnotationValue {
        private final byte value;

        ByteValue(String name, byte value) {
            super(name);
            this.value = value;
        }

        public Byte value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.BYTE;
        }

        public int asInt() {
            return value;
        }

        public long asLong() {
            return value;
        }

        public short asShort() {
            return value;
        }

        public byte asByte() {
            return value;
        }

        public float asFloat() {
            return value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ByteValue byteValue = (ByteValue) o;
            return value == byteValue.value && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) value;
            return result;
        }
    }

    static final class CharacterValue extends AnnotationValue {
        private final char value;

        CharacterValue(String name, char value) {
            super(name);
            this.value = value;
        }

        public Character value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.CHARACTER;
        }

        public char asChar() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CharacterValue that = (CharacterValue) o;
            return value == that.value && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) value;
            return result;
        }
    }

    static final class DoubleValue extends AnnotationValue {
        private final double value;

        public DoubleValue(String name, double value) {
            super(name);
            this.value = value;
        }

        public Double value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.DOUBLE;
        }

        public int asInt() {
            return (int) value;
        }

        public long asLong() {
            return (long) value;
        }

        public short asShort() {
            return (short) value;
        }

        public byte asByte() {
            return (byte) value;
        }

        public float asFloat() {
            return (float) value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DoubleValue that = (DoubleValue) o;

            return Double.compare(that.value, value) == 0 && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            long temp;
            temp = Double.doubleToLongBits(value);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    static final class FloatValue extends AnnotationValue {
        private final float value;

        FloatValue(String name, float value) {
            super(name);
            this.value = value;
        }

        public Float value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.FLOAT;
        }

        public int asInt() {
            return (int) value;
        }

        public long asLong() {
            return (long) value;
        }

        public short asShort() {
            return (short) value;
        }

        public byte asByte() {
            return (byte) value;
        }

        public float asFloat() {
            return value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FloatValue that = (FloatValue) o;

            return Float.compare(that.value, value) == 0 && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
            return result;
        }
    }

    static final class ShortValue extends AnnotationValue {
        private final short value;

        ShortValue(String name, short value) {
            super(name);
            this.value = value;
        }

        public Short value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.SHORT;
        }

        public int asInt() {
            return value;
        }

        public long asLong() {
            return value;
        }

        public short asShort() {
            return value;
        }

        public byte asByte() {
            return (byte) value;
        }

        public float asFloat() {
            return value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ShortValue that = (ShortValue) o;

            return value == that.value && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) value;
            return result;
        }
    }

    static final class IntegerValue extends AnnotationValue {
        private final int value;

        IntegerValue(String name, int value) {
            super(name);
            this.value = value;
        }

        public Integer value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.INTEGER;
        }

        public int asInt() {
            return value;
        }

        public long asLong() {
            return value;
        }

        public short asShort() {
            return (short) value;
        }

        public byte asByte() {
            return (byte) value;
        }

        public float asFloat() {
            return value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            IntegerValue that = (IntegerValue) o;

            return value == that.value && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + value;
            return result;
        }
    }

    static final class LongValue extends AnnotationValue {
        private final long value;

        LongValue(String name, long value) {
            super(name);
            this.value = value;
        }

        public Long value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.LONG;
        }

        public int asInt() {
            return (int) value;
        }

        public long asLong() {
            return value;
        }

        public short asShort() {
            return (short) value;
        }

        public byte asByte() {
            return (byte) value;
        }

        public float asFloat() {
            return value;
        }

        public double asDouble() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LongValue longValue = (LongValue) o;

            return value == longValue.value && super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (value ^ (value >>> 32));
            return result;
        }
    }

    static final class BooleanValue extends AnnotationValue {
        private final boolean value;

        BooleanValue(String name, boolean value) {
            super(name);
            this.value = value;
        }

        public Boolean value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.BOOLEAN;
        }

        public boolean asBoolean() {
            return value;
        }

    }

    static final class EnumValue extends AnnotationValue {
        private final String value;
        private final DotName typeName;

        EnumValue(String name, DotName typeName, String value) {
            super(name);
            this.typeName = typeName;
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.ENUM;
        }

        public String asEnum() {
            return value;
        }

        public DotName asEnumType() {
            return typeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EnumValue enumValue = (EnumValue) o;

            return super.equals(o) && typeName.equals(enumValue.typeName) && value.equals(enumValue.value);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + value.hashCode();
            result = 31 * result + typeName.hashCode();
            return result;
        }
    }

    static final class ClassValue extends AnnotationValue {
        private final Type type;

        ClassValue(String name, Type type) {
            super(name);
            this.type = type;
        }

        public Type value() {
            return type;
        }

        @Override
        public Kind kind() {
            return Kind.CLASS;
        }

        public Type asClass() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClassValue that = (ClassValue) o;

            return super.equals(o) && type.equals(that.type);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    static final class NestedAnnotation extends AnnotationValue {
        private final AnnotationInstance value;

        NestedAnnotation(String name, AnnotationInstance value) {
            super(name);
            this.value = value;
        }

        public AnnotationInstance value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.NESTED;
        }

        public AnnotationInstance asNested() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NestedAnnotation that = (NestedAnnotation) o;

            return super.equals(o) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }

    static final class ArrayValue extends AnnotationValue {
        private final AnnotationValue[] value;

        ArrayValue(String name, AnnotationValue value[]) {
            super(name);
            this.value = value.length > 0 ? value : EMPTY_VALUE_ARRAY;
        }

        public AnnotationValue[] value() {
            return value;
        }

        @Override
        public Kind kind() {
            return Kind.ARRAY;
        }

        @Override
        public Kind componentKind() {
            return value.length > 0 ? value[0].kind() : Kind.UNKNOWN;
        }

        AnnotationValue[] asArray() {
            return value;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (super.name.length() > 0)
                builder.append(super.name).append(" = ");
            builder.append('[');
            for (int i = 0; i < value.length; i++) {
                builder.append(value[i]);
                if (i < value.length - 1)
                    builder.append(',');
            }
            return builder.append(']').toString();
        }

        public int[] asIntArray() {
            int length = value.length;
            int[] array = new int[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asInt();
            }

            return array;
        }

        public long[] asLongArray() {
            int length = value.length;
            long[] array = new long[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asLong();
            }

            return array;
        }

        public short[] asShortArray() {
            int length = value.length;
            short[] array = new short[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asShort();
            }

            return array;
        }

        public byte[] asByteArray() {
            int length = value.length;
            byte[] array = new byte[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asByte();
            }

            return array;
        }

        public float[] asFloatArray() {
            int length = value.length;
            float[] array = new float[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asFloat();
            }

            return array;
        }

        public double[] asDoubleArray() {
            int length = value.length;
            double[] array = new double[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asDouble();
            }
            return array;
        }

        public char[] asCharArray() {
            int length = value.length;
            char[] array = new char[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asChar();
            }

            return array;
        }

        public boolean[] asBooleanArray() {
            int length = value.length;
            boolean[] array = new boolean[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asBoolean();
            }

            return array;
        }

        public String[] asStringArray() {
            int length = value.length;
            String[] array = new String[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asString();
            }

            return array;
        }

        public String[] asEnumArray() {
            int length = value.length;
            String[] array = new String[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asEnum();
            }

            return array;
        }

        public Type[] asClassArray() {
            int length = value.length;
            Type[] array = new Type[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asClass();
            }

            return array;
        }

        public AnnotationInstance[] asNestedArray() {
            int length = value.length;
            AnnotationInstance[] array = new AnnotationInstance[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asNested();
            }

            return array;
        }

        public DotName[] asEnumTypeArray() {
            int length = value.length;
            DotName[] array = new DotName[length];

            for (int i = 0; i < length; i++) {
                array[i] = value[i].asEnumType();
            }

            return array;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ArrayValue that = (ArrayValue) o;
            return super.equals(o) && Arrays.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Arrays.hashCode(value);
            return result;
        }
    }
}