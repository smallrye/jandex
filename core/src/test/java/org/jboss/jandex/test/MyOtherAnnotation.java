package org.jboss.jandex.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jboss.jandex.DotName;

@Retention(RetentionPolicy.RUNTIME)
@interface MyOtherAnnotation {
    String value();

    DotName DOT_NAME = DotName.createSimple(MyOtherAnnotation.class.getName());
}
