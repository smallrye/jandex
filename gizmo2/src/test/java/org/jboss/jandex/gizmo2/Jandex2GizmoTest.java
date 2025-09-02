package org.jboss.jandex.gizmo2;

import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Map;
import static java.lang.constant.ConstantDescs.CD_Number;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Short;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;
import static org.jboss.jandex.gizmo2.Jandex2Gizmo.classDescOf;
import static org.jboss.jandex.gizmo2.Jandex2Gizmo.constructorDescOf;
import static org.jboss.jandex.gizmo2.Jandex2Gizmo.fieldDescOf;
import static org.jboss.jandex.gizmo2.Jandex2Gizmo.genericTypeOf;
import static org.jboss.jandex.gizmo2.Jandex2Gizmo.methodDescOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.VoidType;
import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Reflection2Gizmo;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

public class Jandex2GizmoTest {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnn {
        int value();
    }

    @SuppressWarnings({ "InnerClassMayBeStatic", "unused" })
    static class A<T> {
        class B {
        }

        class C<U> {
        }

        static class D {
            static class E {
            }

            class F<U> {
            }
        }

        static class G<U> {
            static class H<V> {
            }

            class I {
            }
        }
    }

    static class FooBar {
        int f1;
        A<String>.B f2;
        Integer[] f3;

        FooBar(int p1, A<String>.B p2, Integer[] p3) {
        }

        void m1(String p1) {
        }

        A<Integer> m2(A<String>.B p1, List<String> p2) {
            return null;
        }

        <T extends Number> T m3(List<?> p1, Map<? extends T, ? super String> p2) {
            return null;
        }

        static <T> void foobar(
                @MyAnn(1) int p1,
                @MyAnn(2) String p2,
                @MyAnn(3) List<@MyAnn(4) String> p3,
                @MyAnn(5) String[] @MyAnn(6) [] p4,
                @MyAnn(7) List<@MyAnn(8) ? extends @MyAnn(9) String> p5,
                @MyAnn(10) Map<@MyAnn(11) ?, @MyAnn(12) ? super @MyAnn(13) String> p6,
                @MyAnn(14) A<@MyAnn(15) String>.@MyAnn(16) B p7,
                @MyAnn(17) A<@MyAnn(18) String>.@MyAnn(19) C<@MyAnn(20) String> p8,
                A.@MyAnn(21) D p9,
                A.D.@MyAnn(22) E p10,
                A.@MyAnn(23) D.@MyAnn(24) F<@MyAnn(25) String> p11,
                A.@MyAnn(26) G<@MyAnn(27) String> p12,
                A.G.H<@MyAnn(28) String> p13,
                A.G<@MyAnn(29) String>.@MyAnn(30) I p14,
                @MyAnn(31) T p15,
                @MyAnn(32) List<@MyAnn(33) ? super @MyAnn(34) T> p16) {
        }
    }

    static final ClassDesc A_DESC = Reflection2Gizmo.classDescOf(A.class);

    static final ClassDesc A_B_DESC = A_DESC.nested("B");

    static final ClassDesc FOO_BAR_DESC = Reflection2Gizmo.classDescOf(FooBar.class);

    @Test
    public void classDescFromDotName() {
        assertEquals(CD_void, classDescOf(DotName.createSimple("void")));

        assertEquals(CD_boolean, classDescOf(DotName.createSimple("boolean")));
        assertEquals(CD_byte, classDescOf(DotName.createSimple("byte")));
        assertEquals(CD_short, classDescOf(DotName.createSimple("short")));
        assertEquals(CD_int, classDescOf(DotName.createSimple("int")));
        assertEquals(CD_long, classDescOf(DotName.createSimple("long")));
        assertEquals(CD_float, classDescOf(DotName.createSimple("float")));
        assertEquals(CD_double, classDescOf(DotName.createSimple("double")));
        assertEquals(CD_char, classDescOf(DotName.createSimple("char")));

        assertEquals(CD_Boolean, classDescOf(DotName.BOOLEAN_CLASS_NAME));
        assertEquals(CD_Byte, classDescOf(DotName.BYTE_CLASS_NAME));
        assertEquals(CD_Short, classDescOf(DotName.SHORT_CLASS_NAME));
        assertEquals(CD_Integer, classDescOf(DotName.INTEGER_CLASS_NAME));
        assertEquals(CD_Long, classDescOf(DotName.LONG_CLASS_NAME));
        assertEquals(CD_Float, classDescOf(DotName.FLOAT_CLASS_NAME));
        assertEquals(CD_Double, classDescOf(DotName.DOUBLE_CLASS_NAME));
        assertEquals(CD_Character, classDescOf(DotName.CHARACTER_CLASS_NAME));

        assertEquals(CD_String, classDescOf(DotName.STRING_NAME));
        assertEquals(CD_String, classDescOf(DotName.createSimple("java.lang.String")));
        assertEquals(CD_Object, classDescOf(DotName.OBJECT_NAME));
        assertEquals(CD_Object, classDescOf(DotName.createSimple("java.lang.Object")));

        // see `Type.name()` for how array types are represented
        assertEquals(CD_boolean.arrayType(), classDescOf(DotName.createSimple("[Z")));
        assertEquals(CD_byte.arrayType().arrayType(), classDescOf(DotName.createSimple("[[B")));
        assertEquals(CD_short.arrayType(3), classDescOf(DotName.createSimple("[[[S")));
        assertEquals(CD_int.arrayType(4), classDescOf(DotName.createSimple("[[[[I")));
        assertEquals(CD_long.arrayType(1), classDescOf(DotName.createSimple("[J")));
        assertEquals(CD_float.arrayType(2), classDescOf(DotName.createSimple("[[F")));
        assertEquals(CD_double.arrayType(3), classDescOf(DotName.createSimple("[[[D")));
        assertEquals(CD_char.arrayType(4), classDescOf(DotName.createSimple("[[[[C")));

        assertEquals(CD_String.arrayType(), classDescOf(DotName.createSimple("[Ljava.lang.String;")));
        assertEquals(CD_Object.arrayType(2), classDescOf(DotName.createSimple("[[Ljava.lang.Object;")));

        // test caching
        assertSame(classDescOf(DotName.STRING_NAME), classDescOf(DotName.createSimple("java.lang.String")));
        assertSame(classDescOf(DotName.OBJECT_NAME), classDescOf(DotName.createSimple("java.lang.Object")));
    }

    @Test
    public void classDescFromType() {
        assertEquals(CD_void, classDescOf(VoidType.VOID));

        assertEquals(CD_boolean, classDescOf(PrimitiveType.BOOLEAN));
        assertEquals(CD_byte, classDescOf(PrimitiveType.BYTE));
        assertEquals(CD_short, classDescOf(PrimitiveType.SHORT));
        assertEquals(CD_int, classDescOf(PrimitiveType.INT));
        assertEquals(CD_long, classDescOf(PrimitiveType.LONG));
        assertEquals(CD_float, classDescOf(PrimitiveType.FLOAT));
        assertEquals(CD_double, classDescOf(PrimitiveType.DOUBLE));
        assertEquals(CD_char, classDescOf(PrimitiveType.CHAR));

        assertEquals(CD_Boolean, classDescOf(ClassType.BOOLEAN_CLASS));
        assertEquals(CD_Byte, classDescOf(ClassType.BYTE_CLASS));
        assertEquals(CD_Short, classDescOf(ClassType.SHORT_CLASS));
        assertEquals(CD_Integer, classDescOf(ClassType.INTEGER_CLASS));
        assertEquals(CD_Long, classDescOf(ClassType.LONG_CLASS));
        assertEquals(CD_Float, classDescOf(ClassType.FLOAT_CLASS));
        assertEquals(CD_Double, classDescOf(ClassType.DOUBLE_CLASS));
        assertEquals(CD_Character, classDescOf(ClassType.CHARACTER_CLASS));

        assertEquals(CD_String, classDescOf(ClassType.STRING_TYPE));
        assertEquals(CD_Object, classDescOf(ClassType.OBJECT_TYPE));

        assertEquals(CD_boolean.arrayType(), classDescOf(ArrayType.create(PrimitiveType.BOOLEAN, 1)));
        assertEquals(CD_byte.arrayType().arrayType(), classDescOf(ArrayType.create(PrimitiveType.BYTE, 2)));
        assertEquals(CD_short.arrayType(3), classDescOf(ArrayType.create(PrimitiveType.SHORT, 3)));
        assertEquals(CD_int.arrayType(4), classDescOf(ArrayType.create(PrimitiveType.INT, 4)));
        assertEquals(CD_long.arrayType(1), classDescOf(ArrayType.create(PrimitiveType.LONG, 1)));
        assertEquals(CD_float.arrayType(2), classDescOf(ArrayType.create(PrimitiveType.FLOAT, 2)));
        assertEquals(CD_double.arrayType(3), classDescOf(ArrayType.create(PrimitiveType.DOUBLE, 3)));
        assertEquals(CD_char.arrayType(4), classDescOf(ArrayType.create(PrimitiveType.CHAR, 4)));

        assertEquals(CD_String.arrayType(), classDescOf(ArrayType.create(ClassType.STRING_TYPE, 1)));
        assertEquals(CD_Object.arrayType(2), classDescOf(ArrayType.create(ClassType.OBJECT_TYPE, 2)));

        assertEquals(CD_List, classDescOf(ParameterizedType.builder(List.class).addArgument(String.class).build()));
        assertEquals(CD_List.arrayType(), classDescOf(ArrayType.create(
                ParameterizedType.builder(List.class).addArgument(String.class).build(), 1)));

        assertEquals(CD_String, classDescOf(TypeVariable.builder("T").addBound(String.class).build()));
        assertEquals(CD_Object, classDescOf(TypeVariable.create("T")));

        // test caching
        assertSame(classDescOf(ClassType.STRING_TYPE), classDescOf(ClassType.create(String.class)));
        assertSame(classDescOf(ClassType.OBJECT_TYPE), classDescOf(ClassType.create(Object.class)));
    }

    @Test
    public void classDescFromClass() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertNotNull(clazz);
        assertEquals(FOO_BAR_DESC, classDescOf(clazz));

        // test caching
        assertSame(classDescOf(clazz), classDescOf(clazz));
    }

    @Test
    public void fieldDescFromField() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertNotNull(clazz);
        FieldInfo f1 = clazz.field("f1");
        assertNotNull(f1);
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f1", CD_int), fieldDescOf(f1));
        FieldInfo f2 = clazz.field("f2");
        assertNotNull(f2);
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f2", A_B_DESC), fieldDescOf(f2));
        FieldInfo f3 = clazz.field("f3");
        assertNotNull(f3);
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f3", CD_Integer.arrayType()), fieldDescOf(f3));
    }

    @Test
    public void methodDescFromMethod() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertNotNull(clazz);
        MethodInfo m1 = clazz.firstMethod("m1");
        assertNotNull(m1);
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m1", MethodTypeDesc.of(CD_void, CD_String)), methodDescOf(m1));
        MethodInfo m2 = clazz.firstMethod("m2");
        assertNotNull(m2);
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m2", MethodTypeDesc.of(A_DESC, A_B_DESC, CD_List)), methodDescOf(m2));
        MethodInfo m3 = clazz.firstMethod("m3");
        assertNotNull(m3);
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m3", MethodTypeDesc.of(CD_Number, CD_List, CD_Map)), methodDescOf(m3));
    }

    @Test
    public void constructorDescFromConstructor() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertNotNull(clazz);
        MethodInfo c = clazz.constructors().get(0); // there's just one
        assertNotNull(c);
        assertEquals(ConstructorDesc.of(FOO_BAR_DESC, CD_int, A_B_DESC, CD_Integer.arrayType()), constructorDescOf(c));
    }

    @Test
    public void genericTypeFromType() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertNotNull(clazz);
        MethodInfo m = clazz.firstMethod("foobar");
        assertNotNull(m);
        assertEquals("int",
                genericTypeOf(m.parameterType(0)).toString());
        assertEquals("java.lang.String",
                genericTypeOf(m.parameterType(1)).toString());
        assertEquals("java.util.List<java.lang.String>",
                genericTypeOf(m.parameterType(2)).toString());
        assertEquals("java.lang.String[][]",
                genericTypeOf(m.parameterType(3)).toString());
        assertEquals("java.util.List<? extends java.lang.String>",
                genericTypeOf(m.parameterType(4)).toString());
        assertEquals("java.util.Map<?, ? super java.lang.String>",
                genericTypeOf(m.parameterType(5)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A<java.lang.String>.B",
                genericTypeOf(m.parameterType(6)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A<java.lang.String>.C<java.lang.String>",
                genericTypeOf(m.parameterType(7)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$D",
                genericTypeOf(m.parameterType(8)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$D$E",
                genericTypeOf(m.parameterType(9)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$D.F<java.lang.String>",
                genericTypeOf(m.parameterType(10)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$G<java.lang.String>",
                genericTypeOf(m.parameterType(11)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$G$H<java.lang.String>",
                genericTypeOf(m.parameterType(12)).toString());
        assertEquals("org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$G<java.lang.String>.I",
                genericTypeOf(m.parameterType(13)).toString());
        assertEquals("T",
                genericTypeOf(m.parameterType(14)).toString());
        assertEquals("java.util.List<? super T>",
                genericTypeOf(m.parameterType(15)).toString());
    }

    @Test
    public void genericTypeWithAnnotationsFromType() throws IOException {
        Index index = Index.of(MyAnn.class, FooBar.class);
        ClassInfo clazz = index.getClassByName(FooBar.class);
        assertNotNull(clazz);
        MethodInfo m = clazz.firstMethod("foobar");
        assertNotNull(m);
        assertEquals(
                "@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(1) int",
                genericTypeOf(m.parameterType(0), index).toString());
        assertEquals(
                "java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(2) String",
                genericTypeOf(m.parameterType(1), index).toString());
        assertEquals(
                "java.util.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(3) List<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(4) String>",
                genericTypeOf(m.parameterType(2), index).toString());
        assertEquals(
                "java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(5) String[] @org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(6) []",
                genericTypeOf(m.parameterType(3), index).toString());
        assertEquals(
                "java.util.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(7) List<@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(8) ? extends java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(9) String>",
                genericTypeOf(m.parameterType(4), index).toString());
        assertEquals(
                "java.util.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(10) Map<@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(11) ?, @org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(12) ? super java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(13) String>",
                genericTypeOf(m.parameterType(5), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(14) Jandex2GizmoTest$A<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(15) String>.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(16) B",
                genericTypeOf(m.parameterType(6), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(17) Jandex2GizmoTest$A<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(18) String>.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(19) C<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(20) String>",
                genericTypeOf(m.parameterType(7), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(21) Jandex2GizmoTest$A$D",
                genericTypeOf(m.parameterType(8), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(22) Jandex2GizmoTest$A$D$E",
                genericTypeOf(m.parameterType(9), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(23) Jandex2GizmoTest$A$D.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(24) F<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(25) String>",
                genericTypeOf(m.parameterType(10), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(26) Jandex2GizmoTest$A$G<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(27) String>",
                genericTypeOf(m.parameterType(11), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$G$H<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(28) String>",
                genericTypeOf(m.parameterType(12), index).toString());
        assertEquals(
                "org.jboss.jandex.gizmo2.Jandex2GizmoTest$A$G<java.lang.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(29) String>.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(30) I",
                genericTypeOf(m.parameterType(13), index).toString());
        assertEquals(
                "@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(31) T",
                genericTypeOf(m.parameterType(14), index).toString());
        assertEquals(
                "java.util.@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(32) List<@org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(33) ? super @org.jboss.jandex.gizmo2.Jandex2GizmoTest$MyAnn(34) T>",
                genericTypeOf(m.parameterType(15), index).toString());
    }
}
