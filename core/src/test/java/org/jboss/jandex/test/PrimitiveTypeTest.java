package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType;
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
