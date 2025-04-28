package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

public class PrimitiveTypeTest {
    @Test
    public void boxing() {
        assertEquals(ClassType.create(Boolean.class), PrimitiveType.box(PrimitiveType.BOOLEAN));
        assertEquals(ClassType.create(Byte.class), PrimitiveType.box(PrimitiveType.BYTE));
        assertEquals(ClassType.create(Short.class), PrimitiveType.box(PrimitiveType.SHORT));
        assertEquals(ClassType.create(Integer.class), PrimitiveType.box(PrimitiveType.INT));
        assertEquals(ClassType.create(Long.class), PrimitiveType.box(PrimitiveType.LONG));
        assertEquals(ClassType.create(Float.class), PrimitiveType.box(PrimitiveType.FLOAT));
        assertEquals(ClassType.create(Double.class), PrimitiveType.box(PrimitiveType.DOUBLE));
        assertEquals(ClassType.create(Character.class), PrimitiveType.box(PrimitiveType.CHAR));

        assertNull(PrimitiveType.box(null));
    }

    @Test
    public void isBox() {
        assertTrue(PrimitiveType.isBox(ClassType.BOOLEAN_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.BYTE_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.SHORT_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.INTEGER_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.LONG_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.FLOAT_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.DOUBLE_CLASS));
        assertTrue(PrimitiveType.isBox(ClassType.CHARACTER_CLASS));

        assertFalse(PrimitiveType.isBox(PrimitiveType.BOOLEAN));
        assertFalse(PrimitiveType.isBox(PrimitiveType.BYTE));
        assertFalse(PrimitiveType.isBox(PrimitiveType.SHORT));
        assertFalse(PrimitiveType.isBox(PrimitiveType.INT));
        assertFalse(PrimitiveType.isBox(PrimitiveType.LONG));
        assertFalse(PrimitiveType.isBox(PrimitiveType.FLOAT));
        assertFalse(PrimitiveType.isBox(PrimitiveType.DOUBLE));
        assertFalse(PrimitiveType.isBox(PrimitiveType.CHAR));

        assertFalse(PrimitiveType.isBox(ClassType.OBJECT_TYPE));
        assertFalse(PrimitiveType.isBox(TypeVariable.builder("T").addBound(ClassType.BOOLEAN_CLASS).build()));
        assertFalse(PrimitiveType.isBox(WildcardType.createUpperBound(ClassType.INTEGER_CLASS)));
        assertFalse(PrimitiveType.isBox(null));
    }

    @Test
    public void unboxing() {
        assertEquals(PrimitiveType.BOOLEAN, PrimitiveType.unbox(ClassType.create(Boolean.class)));
        assertEquals(PrimitiveType.BYTE, PrimitiveType.unbox(ClassType.create(Byte.class)));
        assertEquals(PrimitiveType.SHORT, PrimitiveType.unbox(ClassType.create(Short.class)));
        assertEquals(PrimitiveType.INT, PrimitiveType.unbox(ClassType.create(Integer.class)));
        assertEquals(PrimitiveType.LONG, PrimitiveType.unbox(ClassType.create(Long.class)));
        assertEquals(PrimitiveType.FLOAT, PrimitiveType.unbox(ClassType.create(Float.class)));
        assertEquals(PrimitiveType.DOUBLE, PrimitiveType.unbox(ClassType.create(Double.class)));
        assertEquals(PrimitiveType.CHAR, PrimitiveType.unbox(ClassType.create(Character.class)));

        assertNull(PrimitiveType.unbox(ClassType.OBJECT_TYPE));
        assertNull(PrimitiveType.unbox(ClassType.create(DotName.STRING_NAME)));

        assertNull(PrimitiveType.unbox(null));
    }
}
