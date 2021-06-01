package test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class TypeUseExample {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface TypeParameterAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface TypeParameterBoundTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface ClassExtendsAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface FieldTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface MethodParameterTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface MethodReturnTypeAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE_USE})
    public @interface MethodThrowsTypeAnnotation {
    }

    public static class AbstractClass {

    }

    public static class TestSubject<@TypeParameterAnnotation T extends @TypeParameterBoundTypeAnnotation Serializable>
            extends @ClassExtendsAnnotation AbstractClass {
        private @FieldTypeAnnotation String field;

        public void method(@MethodParameterTypeAnnotation String methodParam) {
        }

        public List<@MethodReturnTypeAnnotation String> methodWithAnnotatedReturnType() {
            return null;
        }

        public void methodWithThrows() throws @MethodThrowsTypeAnnotation IOException {
        }
    }
}
