package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.VoidType;
import org.junit.jupiter.api.Test;

public class TypeFromClassTest {
    @Test
    public void test() {
        Type type = Type.create(void.class);
        assertEquals(VoidType.VOID, type);

        type = Type.create(byte.class);
        assertEquals(PrimitiveType.BYTE, type);

        type = Type.create(Object.class);
        assertEquals(ClassType.create(DotName.OBJECT_NAME), type);

        type = Type.create(boolean[].class);
        assertEquals(ArrayType.create(PrimitiveType.BOOLEAN, 1), type);

        type = Type.create(String[][].class);
        assertEquals(ArrayType.create(ClassType.create(DotName.STRING_NAME), 2), type);
    }
}
