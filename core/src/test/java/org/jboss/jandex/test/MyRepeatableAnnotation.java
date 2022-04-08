package org.jboss.jandex.test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jboss.jandex.DotName;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MyRepeatableAnnotation.List.class)
@interface MyRepeatableAnnotation {
    String value();

    DotName DOT_NAME = DotName.createSimple(MyRepeatableAnnotation.class.getName());

    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        MyRepeatableAnnotation[] value();
    }
}
