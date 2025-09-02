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
import java.util.Comparator;
import java.util.List;

/**
 * The shared internal representation for FieldInfo objects.
 *
 * @author Jason T. Greene
 */
final class FieldInternal {
    static final FieldInternal[] EMPTY_ARRAY = new FieldInternal[0];
    private final byte[] name;
    private Type type;
    private final short flags;
    private AnnotationInstance[] annotations;

    static final NameComparator NAME_COMPARATOR = new NameComparator();

    static class NameComparator implements Comparator<FieldInternal> {

        private int compare(byte[] left, byte[] right) {
            for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
                int a = (left[i] & 0xff);
                int b = (right[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return left.length - right.length;
        }

        public int compare(FieldInternal instance, FieldInternal instance2) {
            return compare(instance.name, instance2.name); //instance.name.compareTo(instance2.name);
        }
    }

    FieldInternal(byte[] name, Type type, short flags) {
        this(name, type, flags, AnnotationInstance.EMPTY_ARRAY);
    }

    FieldInternal(byte[] name, Type type, short flags, AnnotationInstance[] annotations) {
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.annotations = annotations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldInternal that = (FieldInternal) o;

        if (flags != that.flags) {
            return false;
        }
        if (!Arrays.equals(annotations, that.annotations)) {
            return false;
        }
        if (!Arrays.equals(name, that.name)) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + type.hashCode();
        result = 31 * result + (int) flags;
        return result;
    }

    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldInternal that = (FieldInternal) o;

        if (flags != that.flags) {
            return false;
        }
        if (!Arrays.equals(annotations, that.annotations)) {
            return false;
        }
        if (!Arrays.equals(name, that.name)) {
            return false;
        }
        if (!type.internEquals(that.type)) {
            return false;
        }

        return true;
    }

    int internHashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + type.internHashCode();
        result = 31 * result + (int) flags;
        result = 31 * result + Arrays.hashCode(annotations);
        return result;
    }

    final String name() {
        return Utils.fromUTF8(name);
    }

    final byte[] nameBytes() {
        return name;
    }

    final Type type() {
        return type;
    }

    final List<AnnotationInstance> annotations() {
        return new ImmutableArrayList<>(annotations);
    }

    final AnnotationInstance[] annotationArray() {
        return annotations;
    }

    final AnnotationInstance annotation(DotName name) {
        return AnnotationInstance.binarySearch(annotations, name);
    }

    final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    final short flags() {
        return flags;
    }

    final boolean isEnumConstant() {
        return (flags & Modifiers.ENUM) != 0;
    }

    @Override
    public String toString() {
        return type.toString(true) + " " + name();
    }

    public String toString(ClassInfo clazz) {
        return type.toString(true) + " " + clazz.name() + "." + name();
    }

    void setType(Type type) {
        this.type = type;
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        if (annotations.size() > 0) {
            this.annotations = annotations.toArray(new AnnotationInstance[annotations.size()]);
            Arrays.sort(this.annotations, AnnotationInstance.NAME_COMPARATOR);
        }
    }
}
