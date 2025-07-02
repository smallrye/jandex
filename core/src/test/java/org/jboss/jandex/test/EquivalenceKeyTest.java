package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.EquivalenceKey;
import org.jboss.jandex.PrimitiveType;
import org.junit.jupiter.api.Test;

public class EquivalenceKeyTest {
    @Test
    public void internedPrimitiveTypes() {
        assertSame(EquivalenceKey.of(PrimitiveType.INT), EquivalenceKey.of(PrimitiveType.INT));
        assertNotSame(EquivalenceKey.of(PrimitiveType.INT), EquivalenceKey.of(PrimitiveType.LONG));
    }

    @Test
    public void internedClassTypes() {
        // componentized names
        assertSame(EquivalenceKey.of(ClassType.STRING_TYPE), EquivalenceKey.of(ClassType.STRING_TYPE));
        assertSame(EquivalenceKey.of(ClassType.create(DotName.STRING_NAME)),
                EquivalenceKey.of(ClassType.create(DotName.STRING_NAME)));

        assertNotSame(EquivalenceKey.of(ClassType.STRING_TYPE), EquivalenceKey.of(ClassType.OBJECT_TYPE));
        assertNotSame(EquivalenceKey.of(ClassType.create(DotName.STRING_NAME)),
                EquivalenceKey.of(ClassType.create(DotName.OBJECT_NAME)));

        // simple names
        DotName stringName = DotName.createSimple(String.class);
        DotName objectName = DotName.createSimple(Object.class);
        ClassType stringType = ClassType.create(stringName);
        ClassType objectType = ClassType.create(objectName);

        assertSame(EquivalenceKey.of(stringType), EquivalenceKey.of(stringType));
        assertSame(EquivalenceKey.of(ClassType.create(stringName)), EquivalenceKey.of(ClassType.create(stringName)));

        assertNotSame(EquivalenceKey.of(stringType), EquivalenceKey.of(objectType));
        assertNotSame(EquivalenceKey.of(ClassType.create(stringName)), EquivalenceKey.of(ClassType.create(objectName)));

        // mixed names
        assertSame(EquivalenceKey.of(stringType), EquivalenceKey.of(ClassType.STRING_TYPE));
        assertSame(EquivalenceKey.of(ClassType.create(stringName)), EquivalenceKey.of(ClassType.create(DotName.STRING_NAME)));

        assertNotSame(EquivalenceKey.of(stringType), EquivalenceKey.of(ClassType.OBJECT_TYPE));
        assertNotSame(EquivalenceKey.of(ClassType.create(stringName)),
                EquivalenceKey.of(ClassType.create(DotName.OBJECT_NAME)));
    }

    @Test
    public void notInternedClassTypes() {
        DotName name = DotName.createSimple(EquivalenceKeyTest.class);
        ClassType type = ClassType.create(name);
        assertNotSame(EquivalenceKey.of(type), EquivalenceKey.of(type));
        assertNotSame(EquivalenceKey.of(ClassType.create(name)), EquivalenceKey.of(ClassType.create(name)));
    }
}
