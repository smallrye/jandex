package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@RecordExample.RecordAnnotation("Example")
public record RecordExample(@Nullable Integer id,
                            @Nullable
                                @ComponentAnnotation("nameComponent")
                                @FieldAnnotation("nameField")
                                @AccessorAnnotation("nameAccessor")
                                    String name) {


    static String staticField;

    static String getStaticField() {
        return staticField;
    }

    @RecordExample.RecordAnnotation("Empty")
    public static record NestedEmptyRecord() {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public @interface RecordAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.RECORD_COMPONENT })
    public @interface ComponentAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface FieldAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public @interface AccessorAnnotation {
        String value();
    }

}
