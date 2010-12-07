/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;


public abstract class AnnotationValue implements Comparable<AnnotationValue> {
    private final String name;

    AnnotationValue(String name) {
        this.name = name;
    }

    public final String name() {
        return name;
    }

    public int compareTo(AnnotationValue other) {
        return name.compareTo(other.name);
    }

    public abstract Object value();

    public int asInt() {
        throw new IllegalArgumentException("Not a number");
    }

    public long asLong() {
        throw new IllegalArgumentException("Not a number");
    }

    public short asShort() {
        throw new IllegalArgumentException("not a number");
    }

    public byte asByte() {
        throw new IllegalArgumentException("not a number");
    }

    public float asFloat() {
        throw new IllegalArgumentException("not a number");
    }

    public double asDouble() {
        throw new IllegalArgumentException("not a number");
    }

    public char asChar() {
        throw new IllegalArgumentException("not a character");
    }

    public boolean asBoolean() {
        throw new IllegalArgumentException("not a boolean");
    }

    public String asString() {
        return value().toString();
    }

    public String asEnum() {
       throw new IllegalArgumentException("not an enum");
    }

    public DotName asEnumType() {
        throw new IllegalArgumentException("not an enum");
     }

    public Type asClass() {
        throw new IllegalArgumentException("not a class");
     }

    public AnnotationInstance asNested() {
        throw new IllegalArgumentException("not a nested annotation");
    }

    AnnotationValue[] asArray() {
        throw new IllegalArgumentException("Not an array");
    }

    public int[] asIntArray() {
        throw new IllegalArgumentException("Not a number array");
    }

    public long[] asLongArray() {
        throw new IllegalArgumentException("Not a number array");
    }

    public short[] asShortArray() {
        throw new IllegalArgumentException("not a number array");
    }

    public byte[] asByteArray() {
        throw new IllegalArgumentException("not a number array");
    }

    public float[] asFloatArray() {
        throw new IllegalArgumentException("not a number array");
    }

    public double[] asDoubleArray() {
        throw new IllegalArgumentException("not a number array");
    }

    public char[] asCharArray() {
        throw new IllegalArgumentException("not a character array");
    }

    public boolean[] asBooleanArray() {
        throw new IllegalArgumentException("not a boolean array");
    }

    public String[] asStringArray() {
        throw new IllegalArgumentException("not a string array");
    }

    public String[] asEnumArray() {
       throw new IllegalArgumentException("not an enum array");
    }

    public DotName[] asEnumTypeArray() {
        throw new IllegalArgumentException("not an enum array");
    }

    public Type[] asClassArray() {
        throw new IllegalArgumentException("not a class array");
     }

    public AnnotationInstance[] asNestedArray() {
        throw new IllegalArgumentException("not a nested annotation array");
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (name.length() > 0)
            builder.append(name).append(" = ");
        return builder.append(value()).toString();
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

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (super.name.length() > 0)
                builder.append(super.name).append(" = ");

            return builder.append('"').append(value).append('"').toString();
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

        public char asChar() {
            return value;
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

        public String asEnum() {
            return value;
        }

        public DotName asEnumType() {
            return typeName;
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

        public Type asClass() {
            return type;
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

        public AnnotationInstance asNested() {
            return value;
        }
    }

    static final class ArrayValue extends AnnotationValue {
        private final AnnotationValue[] value;

        ArrayValue(String name, AnnotationValue value[]) {
            super(name);
            this.value = value;
        }

        public AnnotationValue[] value() {
            return value;
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
    }
}