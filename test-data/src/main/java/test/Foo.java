package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jason T. Greene
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE_USE})
public @interface Foo  {
}
