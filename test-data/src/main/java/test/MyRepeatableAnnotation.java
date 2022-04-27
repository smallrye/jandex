package test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MyRepeatableAnnotation.List.class)
@interface MyRepeatableAnnotation {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        MyRepeatableAnnotation[] value();
    }
}
