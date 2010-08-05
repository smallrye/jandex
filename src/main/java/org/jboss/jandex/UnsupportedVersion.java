package org.jboss.jandex;

/**
 * The version encountered is not supported.
 * 
 * @author Jason T. Greene
 *
 */
public class UnsupportedVersion extends RuntimeException {
    private static final long serialVersionUID = 7480525486554902831L;

    public UnsupportedVersion(String message) {
        super(message);
    }    
}
