package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class AnnotationInstanceBuilderTest {
    public enum SimpleEnum {
        FOO,
        BAR,
        BAZ,
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface SimpleAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ComplexAnnotation {
        boolean bool();

        byte b();

        short s();

        int i();

        long l();

        float f();

        double d();

        char ch();

        String str();

        SimpleEnum en();

        Class<?> cls();

        SimpleAnnotation nested();

        boolean[] boolArray();

        byte[] bArray();

        short[] sArray();

        int[] iArray();

        long[] lArray();

        float[] fArray();

        double[] dArray();

        char[] chArray();

        String[] strArray();

        SimpleEnum[] enArray();

        Class<?>[] clsArray();

        SimpleAnnotation[] nestedArray();
    }

    @Test
    public void test() throws ReflectiveOperationException {
        verify(complexAnnotation_manual());
        verify(complexAnnotation_builder());
    }

    private static void verify(AnnotationInstance ann) {
        assertEquals(true, ann.value("bool").asBoolean());
        assertEquals((byte) 1, ann.value("b").asByte());
        assertEquals((short) 2, ann.value("s").asShort());
        assertEquals(3, ann.value("i").asInt());
        assertEquals(4L, ann.value("l").asLong());
        assertEquals(5.0F, ann.value("f").asFloat());
        assertEquals(6.0, ann.value("d").asDouble());
        assertEquals('a', ann.value("ch").asChar());
        assertEquals("bc", ann.value("str").asString());
        assertEquals(SimpleEnum.FOO.name(), ann.value("en").asEnum());
        assertEquals(SimpleEnum.class.getName(), ann.value("en").asEnumType().toString());
        assertEquals(Object.class.getName(), ann.value("cls").asClass().name().toString());
        assertEquals("one", ann.value("nested").asNested().value().asString());

        assertEquals(2, ann.value("boolArray").asBooleanArray().length);
        assertEquals(true, ann.value("boolArray").asBooleanArray()[0]);
        assertEquals(false, ann.value("boolArray").asBooleanArray()[1]);
        assertEquals(2, ann.value("bArray").asByteArray().length);
        assertEquals((byte) 7, ann.value("bArray").asByteArray()[0]);
        assertEquals((byte) 8, ann.value("bArray").asByteArray()[1]);
        assertEquals(2, ann.value("sArray").asShortArray().length);
        assertEquals((short) 9, ann.value("sArray").asShortArray()[0]);
        assertEquals((short) 10, ann.value("sArray").asShortArray()[1]);
        assertEquals(2, ann.value("iArray").asIntArray().length);
        assertEquals(11, ann.value("iArray").asIntArray()[0]);
        assertEquals(12, ann.value("iArray").asIntArray()[1]);
        assertEquals(2, ann.value("lArray").asLongArray().length);
        assertEquals(13L, ann.value("lArray").asLongArray()[0]);
        assertEquals(14L, ann.value("lArray").asLongArray()[1]);
        assertEquals(2, ann.value("fArray").asFloatArray().length);
        assertEquals(15.0F, ann.value("fArray").asFloatArray()[0]);
        assertEquals(16.0F, ann.value("fArray").asFloatArray()[1]);
        assertEquals(2, ann.value("dArray").asDoubleArray().length);
        assertEquals(17.0, ann.value("dArray").asDoubleArray()[0]);
        assertEquals(18.0, ann.value("dArray").asDoubleArray()[1]);
        assertEquals(2, ann.value("chArray").asCharArray().length);
        assertEquals('d', ann.value("chArray").asCharArray()[0]);
        assertEquals('e', ann.value("chArray").asCharArray()[1]);
        assertEquals(2, ann.value("strArray").asStringArray().length);
        assertEquals("fg", ann.value("strArray").asStringArray()[0]);
        assertEquals("hi", ann.value("strArray").asStringArray()[1]);
        assertEquals(2, ann.value("enArray").asEnumArray().length);
        assertEquals(SimpleEnum.BAR.name(), ann.value("enArray").asEnumArray()[0]);
        assertEquals(SimpleEnum.class.getName(), ann.value("enArray").asEnumTypeArray()[0].toString());
        assertEquals(SimpleEnum.BAZ.name(), ann.value("enArray").asEnumArray()[1]);
        assertEquals(SimpleEnum.class.getName(), ann.value("enArray").asEnumTypeArray()[1].toString());
        assertEquals(2, ann.value("clsArray").asClassArray().length);
        assertEquals(String.class.getName(), ann.value("clsArray").asClassArray()[0].name().toString());
        assertEquals(Number.class.getName(), ann.value("clsArray").asClassArray()[1].name().toString());
        assertEquals(2, ann.value("nestedArray").asNestedArray().length);
        assertEquals("two", ann.value("nestedArray").asNestedArray()[0].value().asString());
        assertEquals("three", ann.value("nestedArray").asNestedArray()[1].value().asString());
    }

    private static AnnotationInstance complexAnnotation_manual() {
        return AnnotationInstance.create(DotName.createSimple(ComplexAnnotation.class.getName()), null, Arrays.asList(
                AnnotationValue.createBooleanValue("bool", true),
                AnnotationValue.createByteValue("b", (byte) 1),
                AnnotationValue.createShortValue("s", (short) 2),
                AnnotationValue.createIntegerValue("i", 3),
                AnnotationValue.createLongValue("l", 4L),
                AnnotationValue.createFloatValue("f", 5.0F),
                AnnotationValue.createDoubleValue("d", 6.0),
                AnnotationValue.createCharacterValue("ch", 'a'),
                AnnotationValue.createStringValue("str", "bc"),
                AnnotationValue.createEnumValue("en", DotName.createSimple(SimpleEnum.class.getName()), "FOO"),
                AnnotationValue.createClassValue("cls", Type.create(DotName.createSimple("java.lang.Object"), Type.Kind.CLASS)),
                AnnotationValue.createNestedAnnotationValue("nested", simpleAnnotation_manual("one")),

                AnnotationValue.createArrayValue("boolArray", new AnnotationValue[] {
                        AnnotationValue.createBooleanValue("", true),
                        AnnotationValue.createBooleanValue("", false),
                }),
                AnnotationValue.createArrayValue("bArray", new AnnotationValue[] {
                        AnnotationValue.createByteValue("", (byte) 7),
                        AnnotationValue.createByteValue("", (byte) 8),
                }),
                AnnotationValue.createArrayValue("sArray", new AnnotationValue[] {
                        AnnotationValue.createShortValue("", (short) 9),
                        AnnotationValue.createShortValue("", (short) 10),
                }),
                AnnotationValue.createArrayValue("iArray", new AnnotationValue[] {
                        AnnotationValue.createIntegerValue("", 11),
                        AnnotationValue.createIntegerValue("", 12),
                }),
                AnnotationValue.createArrayValue("lArray", new AnnotationValue[] {
                        AnnotationValue.createLongValue("", 13L),
                        AnnotationValue.createLongValue("", 14L),
                }),
                AnnotationValue.createArrayValue("fArray", new AnnotationValue[] {
                        AnnotationValue.createFloatValue("", 15.0F),
                        AnnotationValue.createFloatValue("", 16.0F),
                }),
                AnnotationValue.createArrayValue("dArray", new AnnotationValue[] {
                        AnnotationValue.createDoubleValue("", 17.0),
                        AnnotationValue.createDoubleValue("", 18.0),
                }),
                AnnotationValue.createArrayValue("chArray", new AnnotationValue[] {
                        AnnotationValue.createCharacterValue("", 'd'),
                        AnnotationValue.createCharacterValue("", 'e'),
                }),
                AnnotationValue.createArrayValue("strArray", new AnnotationValue[] {
                        AnnotationValue.createStringValue("", "fg"),
                        AnnotationValue.createStringValue("", "hi"),
                }),
                AnnotationValue.createArrayValue("enArray", new AnnotationValue[] {
                        AnnotationValue.createEnumValue("", DotName.createSimple(SimpleEnum.class.getName()), "BAR"),
                        AnnotationValue.createEnumValue("", DotName.createSimple(SimpleEnum.class.getName()), "BAZ"),
                }),
                AnnotationValue.createArrayValue("clsArray", new AnnotationValue[] {
                        AnnotationValue.createClassValue("",
                                Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS)),
                        AnnotationValue.createClassValue("",
                                Type.create(DotName.createSimple("java.lang.Number"), Type.Kind.CLASS)),
                }),
                AnnotationValue.createArrayValue("nestedArray", new AnnotationValue[] {
                        AnnotationValue.createNestedAnnotationValue("", simpleAnnotation_manual("two")),
                        AnnotationValue.createNestedAnnotationValue("", simpleAnnotation_manual("three")),
                })));
    }

    private static AnnotationInstance simpleAnnotation_manual(String value) {
        return AnnotationInstance.create(DotName.createSimple(SimpleAnnotation.class.getName()), null,
                Collections.singletonList(AnnotationValue.createStringValue("value", value)));
    }

    private static AnnotationInstance complexAnnotation_builder() {
        return AnnotationInstance.builder(ComplexAnnotation.class)
                .add("bool", true)
                .add("b", (byte) 1)
                .add("s", (short) 2)
                .add("i", 3)
                .add("l", 4L)
                .add("f", 5.0F)
                .add("d", 6.0)
                .add("ch", 'a')
                .add("str", "bc")
                .add("en", SimpleEnum.class, "FOO")
                .add("cls", Type.create(DotName.OBJECT_NAME, Type.Kind.CLASS))
                .add("nested", simpleAnnotation_builder("one"))
                .add("boolArray", new boolean[] { true, false })
                .add("bArray", new byte[] { (byte) 7, (byte) 8 })
                .add("sArray", new short[] { (short) 9, (short) 10 })
                .add("iArray", new int[] { 11, 12 })
                .add("lArray", new long[] { 13L, 14L })
                .add("fArray", new float[] { 15.0F, 16.0F })
                .add("dArray", new double[] { 17.0, 18.0 })
                .add("chArray", new char[] { 'd', 'e' })
                .add("strArray", new String[] { "fg", "hi" })
                .add("enArray", new SimpleEnum[] { SimpleEnum.BAR, SimpleEnum.BAZ })
                .add("clsArray", new Class[] { String.class, Number.class })
                .add("nestedArray", new AnnotationInstance[] { simpleAnnotation_builder("two"),
                        simpleAnnotation_builder("three") })
                .build();
    }

    private static AnnotationInstance simpleAnnotation_builder(String value) {
        return AnnotationInstance.builder(SimpleAnnotation.class)
                .value(value)
                .build();
    }
}
