package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeAnnotationOnLocalClassTypeTest {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TypeAnn {
        String value();

        DotName DOT_NAME = DotName.createSimple(TypeAnn.class.getName());
    }

    class C1 {
        class C2 {
            class C3 {
                class C4 {
                    class C5 {
                    }
                }
            }
        }
    }

    static class SC1 {
        static class SC2 {
            static class SC3 {
                static class SC4 {
                    static class SC5 {
                        class C6 {
                            class C7 {
                                class C8 {
                                    class C9 {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static class NestedClass {
        class NestedInnerClass {
        }
    }

    class InnerClass {
        class InnerInnerClass {
        }

        Object method() {
            class LocalClass extends RuntimeException {
            }

            class AnotherLocalClass extends @TypeAnn("local:extends") LocalClass {
                @TypeAnn("test")
                TypeAnnotationOnLocalClassTypeTest.@TypeAnn("c1") C1.C2.C3.@TypeAnn("c4") C4.C5 a;

                TypeAnnotationOnLocalClassTypeTest.SC1.SC2.SC3.SC4.@TypeAnn("sc5") SC5 b;

                TypeAnnotationOnLocalClassTypeTest.SC1.SC2.SC3.SC4.@TypeAnn("sc5") SC5.C6.@TypeAnn("c7") C7.@TypeAnn("c8") C8.C9 c;

                TypeAnnotationOnLocalClassTypeTest.@TypeAnn("nested") NestedClass.@TypeAnn("nested inner") NestedInnerClass nested;

                @TypeAnn("test")
                TypeAnnotationOnLocalClassTypeTest.@TypeAnn("inner") InnerClass.@TypeAnn("inner inner") InnerInnerClass inner;

                @TypeAnn("local:field")
                LocalClass local;

                LocalClass[] @TypeAnn("local:array") [] localArray;

                @TypeAnn("local:array element")
                LocalClass @TypeAnn("local:array dimension") [] localArrayBoth;

                @TypeAnn("local:method return")
                LocalClass methodA() {
                    return null;
                }

                void methodB(@TypeAnn("local:param") LocalClass param) {
                }

                <@TypeAnn("local:type param") X extends @TypeAnn("local:type param bound") LocalClass> void methodC() {
                }

                void methodD(List<@TypeAnn("local:type arg") LocalClass> param) {
                }

                void methodE() throws @TypeAnn("local:throws") LocalClass {
                }
            }

            return new AnotherLocalClass();
        }
    }

    @Test
    public void test() throws IOException {
        Class<?> clazz = new TypeAnnotationOnLocalClassTypeTest().new InnerClass().method().getClass();
        Index index = Index.of(clazz);

        test(index, clazz);

        test(IndexingUtil.roundtrip(index), clazz);
    }

    private void test(Index index, Class<?> clazz) {
        ClassInfo classInfo = index.getClassByName(clazz);
        assertNotNull(classInfo);
        assertEquals(clazz.getName(), classInfo.name().toString());

        {
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:extends\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    classInfo.superClassType().toString());
            assertEquals(1, classInfo.annotations()
                    .stream()
                    .filter(it -> it.name().equals(TypeAnn.DOT_NAME) && it.value().asString().equals("local:extends"))
                    .count());
        }

        {
            FieldInfo a = classInfo.field("a");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"test\") TypeAnnotationOnLocalClassTypeTest.@TypeAnn(\"c1\") C1.C2.C3.@TypeAnn(\"c4\") C4.C5",
                    a.type().toString());
            assertEquals(3, a.annotations().size());
        }

        {
            FieldInfo b = classInfo.field("b");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"sc5\") TypeAnnotationOnLocalClassTypeTest$SC1$SC2$SC3$SC4$SC5",
                    b.type().toString());
            assertEquals(1, b.annotations().size());
        }

        {
            FieldInfo c = classInfo.field("c");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"sc5\") TypeAnnotationOnLocalClassTypeTest$SC1$SC2$SC3$SC4$SC5.C6.@TypeAnn(\"c7\") C7.@TypeAnn(\"c8\") C8.C9",
                    c.type().toString());
            assertEquals(3, c.annotations().size());
        }

        {
            FieldInfo nested = classInfo.field("nested");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"nested\") TypeAnnotationOnLocalClassTypeTest$NestedClass.@TypeAnn(\"nested inner\") NestedInnerClass",
                    nested.type().toString());
            assertEquals(2, nested.annotations().size());
        }

        {
            FieldInfo inner = classInfo.field("inner");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"test\") TypeAnnotationOnLocalClassTypeTest.@TypeAnn(\"inner\") InnerClass.@TypeAnn(\"inner inner\") InnerInnerClass",
                    inner.type().toString());
            assertEquals(3, inner.annotations().size());
        }

        {
            FieldInfo local = classInfo.field("local");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:field\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    local.type().toString());
            assertEquals(1, local.annotations().size());
        }

        {
            FieldInfo localArray = classInfo.field("localArray");
            assertEquals(
                    "org.jboss.jandex.test.TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass[] @TypeAnn(\"local:array\") []",
                    localArray.type().toString());
            assertEquals(1, localArray.annotations().size());
        }

        {
            FieldInfo localArrayBoth = classInfo.field("localArrayBoth");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:array element\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass @TypeAnn(\"local:array dimension\") []",
                    localArrayBoth.type().toString());
            assertEquals(2, localArrayBoth.annotations().size());
        }

        {
            MethodInfo methodA = classInfo.firstMethod("methodA");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:method return\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    methodA.returnType().toString());
            assertEquals(1, methodA.annotations().size());
        }

        {
            MethodInfo methodB = classInfo.firstMethod("methodB");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:param\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    methodB.parameterType(0).toString());
            assertEquals(1, methodB.annotations().size());
        }

        {
            MethodInfo methodC = classInfo.firstMethod("methodC");
            assertEquals(
                    "@TypeAnn(\"local:type param\") X extends org.jboss.jandex.test.@TypeAnn(\"local:type param bound\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    methodC.typeParameters().get(0).toString());
            assertEquals(2, methodC.annotations().size());
        }

        {
            MethodInfo methodD = classInfo.firstMethod("methodD");
            assertEquals(
                    "java.util.List<org.jboss.jandex.test.@TypeAnn(\"local:type arg\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass>",
                    methodD.parameterType(0).toString());
            assertEquals(1, methodD.annotations().size());
        }

        {
            MethodInfo methodE = classInfo.firstMethod("methodE");
            assertEquals(
                    "org.jboss.jandex.test.@TypeAnn(\"local:throws\") TypeAnnotationOnLocalClassTypeTest$InnerClass$1LocalClass",
                    methodE.exceptions().get(0).toString());
            assertEquals(1, methodE.annotations().size());
        }
    }
}
