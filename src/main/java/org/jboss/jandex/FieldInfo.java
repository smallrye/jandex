package org.jboss.jandex;

import java.lang.reflect.Modifier;

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
    private final String name;
    private final Type type;
    private final short flags;
    private final ClassInfo clazz;

    FieldInfo(ClassInfo clazz, String name, Type type, short flags) {
        this.clazz = clazz;
        this.name = name;
        this.type = type;
        this.flags = flags;
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
}
