package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.junit.jupiter.api.Test;

public class TypeVariableTest {

    static class Ju<T1, T2 extends Number, @MyAnnotation("t3") T3 extends Number & Serializable> {
    }

    @Test
    public void testTypeVariableBuilder() throws IOException {
        Index index = Index.of(Ju.class);

        ClassInfo juClass = index.getClassByName(Ju.class);
        Type t1Type = juClass.typeParameters().get(0);
        Type t2Type = juClass.typeParameters().get(1);
        Type t3Type = juClass.typeParameters().get(2);

        // T1
        assertEquals(t1Type, TypeVariable.builder("T1").build());
        assertEquals(t1Type, TypeVariable.create("T1"));
        // T2 extends Number
        assertEquals(t2Type, TypeVariable.builder("T2")
                .addBound(ClassType.create(Number.class))
                .build());
        // @MyAnnotation("t3") T3 extends Number & Serializable
        assertEquals(t3Type, TypeVariable.builder("T3")
                .addBound(ClassType.create(Number.class))
                .addBound(ClassType.create(Serializable.class))
                .addAnnotation(AnnotationInstance.builder(MyAnnotation.class).value("t3").build())
                .build());
    }

}
