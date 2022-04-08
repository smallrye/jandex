package org.jboss.jandex.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.jandex.DotName;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER,
        ElementType.TYPE_USE })
@interface MyAnnotation {
    String value();

    DotName DOT_NAME = DotName.createSimple(MyAnnotation.class.getName());
}
