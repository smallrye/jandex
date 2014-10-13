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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a field that was annotated.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class FieldInfo implements AnnotationTarget {
    static final FieldInfo[] EMPTY_ARRAY = new FieldInfo[0];
    private final String name;
    private Type type;
    private final short flags;
    private final ClassInfo clazz;
    private AnnotationInstance[] annotations;

    static final NameComparator NAME_COMPARATOR = new NameComparator();

    static class NameComparator implements Comparator<FieldInfo> {
        public int compare(FieldInfo instance, FieldInfo instance2) {
            return instance.name.compareTo(instance2.name);
        }
    }

    FieldInfo(ClassInfo clazz, String name, Type type, short flags) {
        this.clazz = clazz;
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.annotations = AnnotationInstance.EMPTY_ARRAY;
    }

    /**
     * Construct a new mock Field instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param type the Java field type
     * @param flags the field attributes
     * @return a mock field
     */
    public static FieldInfo create(ClassInfo clazz, String name, Type type, short flags) {
         if (clazz == null)
             throw new IllegalArgumentException("Clazz can't be null");

         if (name == null)
             throw new IllegalArgumentException("Name can't be null");

        return new FieldInfo(clazz, name, type, flags);
    }


    /**
     * Returns the local name of the field
     *
     * @return the local name of the field
     */
    public final String name() {
        return name;
    }

    /**
     * Returns the class which declared the field
     *
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns the Java Type of this field.
     *
     * @return the type
     */
    public final Type type() {
        return type;
    }

    public List<AnnotationInstance> annotations() {
        return Collections.unmodifiableList(Arrays.asList(annotations));
    }

    /**
     * Returns the access fields of this field. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this field
     */
    public final short flags() {
        return flags;
    }

    public String toString() {
        return type + " " + clazz.name() + "." + name;
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
