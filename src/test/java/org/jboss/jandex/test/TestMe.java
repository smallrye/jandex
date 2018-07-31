package org.jboss.jandex.test;

/**
 * @author Jason T. Greene
 */
public @interface TestMe {
    public String value() default "Foo";
    public String test() default "haha";
}
