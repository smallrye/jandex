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

    // relies on no collisions between hashes of `DotName.STRING_NAME` and `DotName.OBJECT_NAME` modulo cache size
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
}
