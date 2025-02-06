package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// copy of `MyAnnotation`, just different retention
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER,
        ElementType.TYPE_USE })
@interface MyClassAnnotation {
    String value();
}
