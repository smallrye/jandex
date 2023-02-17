package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class ClassTypeTest {

    @MyAnnotation("ccc")
    String ccc;

    @Test
    public void testClassTypeBuilder() throws IOException {
        Index index = Index.of(ClassTypeTest.class);
        ClassInfo testClass = index.getClassByName(ClassTypeTest.class);

        assertEquals(ClassType.OBJECT_TYPE, ClassType.builder(Object.class).build());

        Type cccType = testClass.field("ccc").type();
        assertEquals(cccType, ClassType.builder(String.class)
                .addAnnotation(AnnotationInstance.builder(MyAnnotation.class).add("value", "ccc").build()).build());
    }
}
