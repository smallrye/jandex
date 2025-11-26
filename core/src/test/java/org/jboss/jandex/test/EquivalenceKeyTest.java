package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.EquivalenceKey;
import org.jboss.jandex.Index;
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

    // relies on no collisions between hashes of `"java.lang.String"` and `"java.lang.Object"` modulo cache size
    @Test
    public void internedClasses() throws IOException {
        Index index = Index.of(String.class, Object.class);
        ClassInfo string = index.getClassByName(String.class);
        ClassInfo object = index.getClassByName(Object.class);

        assertSame(EquivalenceKey.of(string), EquivalenceKey.of(string));
        assertSame(EquivalenceKey.of(object), EquivalenceKey.of(object));

        assertNotSame(EquivalenceKey.of(string), EquivalenceKey.of(object));
    }
}
