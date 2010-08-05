package org.jboss.jandex;

import java.lang.reflect.Modifier;

/**
 * Represents a Java method that was annotated. 
 * 
 *  <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 * 
 * @author Jason T. Greene
 */
public final class MethodInfo implements AnnotationTarget {
    private final String name;
    private final Type[] args;
    private final Type returnType;
    private final short flags;
    private final ClassInfo clazz;

    MethodInfo(ClassInfo clazz, String name, Type[] args, Type returnType,  short flags) {
        this.clazz = clazz;
        this.name = name;
        this.args = args;
        this.returnType = returnType;
        this.flags = flags;
    }

    /**
     * Returns the name of this method
     * 
     * @return the name of the method
     */
    public final String name() {
        return name;
    }
    
    /**
     * Returns the class that declared this method
     * 
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns an array containing parameter types in parameter order. 
     * 
     * @return all parameter types
     */
    public final Type[] args() {
        return args;
    }
    
    /**
     * Returns this method's return parameter type.
     * If this method has a void return, a special void type is returned.
     * 
     * @return the type of this method's return value
     */
    public final Type returnType() {
        return returnType;
    }


    /**
     * Returns the access fields of this method. {@link Modifier} can be used on this value.
     * 
     * @return the access flags of this method
     */
    public final short flags() {
        return flags;
    }  
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType).append(' ').append(clazz.name()).append('.').append(name).append('(');
        for (int i = 0; i < args.length; i++) {
            builder.append(args[i]);
            if (i + 1 < args.length)
                builder.append(", ");
        }
        builder.append(')');
        
        return builder.toString();
    }
}
