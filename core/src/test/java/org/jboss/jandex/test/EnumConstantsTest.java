package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class EnumConstantsTest {
    enum SimpleEnum {
        FOO,
        BAZ,
        QUUX,
        BAR,
    }

    enum ComplexEnum {
        ONE(1) {
            @Override
            int munge() {
                return value + 1;
            }
        },
        TWO(2) {
            @Override
            int munge() {
                return value * 2;
            }
        },
        THREE(3) {
            @Override
            int munge() {
                return value / 3;
            }
        },
        FOUR(4) {
            @Override
            int munge() {
                return value - 4;
            }
        },
        ;

        final int value;

        ComplexEnum(int value) {
            this.value = value;
        }

        abstract int munge();
    }

    enum EnumSingleton {
        INSTANCE
    }

    enum EmptyEnum {
    }

    static class NotAnEnum {
        static final NotAnEnum INSTANCE = new NotAnEnum(0);

        final int value;

        NotAnEnum(int value) {
            this.value = value;
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(SimpleEnum.class, ComplexEnum.class, EnumSingleton.class, EmptyEnum.class, NotAnEnum.class);
        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        ClassInfo simpleEnum = index.getClassByName(SimpleEnum.class);
        assertTrue(simpleEnum.isEnum());
        List<FieldInfo> simpleEnumConstants = simpleEnum.enumConstants();
        assertEquals(4, simpleEnumConstants.size());
        assertEquals("FOO", simpleEnumConstants.get(0).name());
        assertEquals(0, simpleEnumConstants.get(0).enumConstantOrdinal());
        assertEquals("BAZ", simpleEnumConstants.get(1).name());
        assertEquals(1, simpleEnumConstants.get(1).enumConstantOrdinal());
        assertEquals("QUUX", simpleEnumConstants.get(2).name());
        assertEquals(2, simpleEnumConstants.get(2).enumConstantOrdinal());
        assertEquals("BAR", simpleEnumConstants.get(3).name());
        assertEquals(3, simpleEnumConstants.get(3).enumConstantOrdinal());
        for (FieldInfo enumConstant : simpleEnumConstants) {
            assertTrue(enumConstant.isEnumConstant());
        }

        ClassInfo complexEnum = index.getClassByName(ComplexEnum.class);
        assertTrue(complexEnum.isEnum());
        List<FieldInfo> complexEnumConstants = complexEnum.enumConstants();
        assertEquals(4, complexEnumConstants.size());
        assertEquals("ONE", complexEnumConstants.get(0).name());
        assertEquals(0, complexEnumConstants.get(0).enumConstantOrdinal());
        assertEquals("TWO", complexEnumConstants.get(1).name());
        assertEquals(1, complexEnumConstants.get(1).enumConstantOrdinal());
        assertEquals("THREE", complexEnumConstants.get(2).name());
        assertEquals(2, complexEnumConstants.get(2).enumConstantOrdinal());
        assertEquals("FOUR", complexEnumConstants.get(3).name());
        assertEquals(3, complexEnumConstants.get(3).enumConstantOrdinal());
        for (FieldInfo enumConstant : complexEnumConstants) {
            assertTrue(enumConstant.isEnumConstant());
        }
        assertFalse(complexEnum.field("value").isEnumConstant());
        assertEquals(-1, complexEnum.field("value").enumConstantOrdinal());

        ClassInfo enumSingleton = index.getClassByName(EnumSingleton.class);
        assertTrue(enumSingleton.isEnum());
        List<FieldInfo> enumSingletonConstants = enumSingleton.enumConstants();
        assertEquals(1, enumSingletonConstants.size());
        assertEquals("INSTANCE", enumSingletonConstants.get(0).name());
        assertEquals(0, enumSingletonConstants.get(0).enumConstantOrdinal());
        for (FieldInfo enumConstant : enumSingletonConstants) {
            assertTrue(enumConstant.isEnumConstant());
        }

        ClassInfo emptyEnum = index.getClassByName(EmptyEnum.class);
        assertTrue(emptyEnum.isEnum());
        assertEquals(0, emptyEnum.enumConstants().size());

        ClassInfo notAnEnum = index.getClassByName(NotAnEnum.class);
        assertFalse(notAnEnum.isEnum());
        assertTrue(notAnEnum.enumConstants().isEmpty());
        assertEquals(-1, notAnEnum.field("INSTANCE").enumConstantOrdinal());
        assertEquals(-1, notAnEnum.field("value").enumConstantOrdinal());
    }
}
