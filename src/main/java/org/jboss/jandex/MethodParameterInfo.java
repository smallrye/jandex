package org.jboss.jandex;

/**
 * Represents an individual Java method parameter that was annotated.
 * 
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 * 
 * @author Jason T. Greene
 */
public final class MethodParameterInfo implements AnnotationTarget {
    private final MethodInfo method;
    private final short parameter;

    MethodParameterInfo(MethodInfo method,short parameter)
    {
        this.method = method;
        this.parameter = parameter;
    }

    /**
     * Returns the method this parameter belongs to.
     * 
     * @return the declaring Java method
     */
    public final MethodInfo method() {
        return method;
    }
    
    /**
     * Returns the 0 based position of this parameter.
     * 
     * @return the position of this parameter
     */
    public final short position() {
        return parameter;
    }
    
    public String toString() {
        return method + " #" + parameter;
    }
}
