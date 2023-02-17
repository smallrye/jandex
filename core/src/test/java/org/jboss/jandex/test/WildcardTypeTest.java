package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

public class WildcardTypeTest {

    List<?> fff;
    List<? extends Number> ggg;
    List<? super Number> hhh;
    List<@MyAnnotation("iii") ? super Number> iii;

    @Test
    public void testWildcardTypeFactoryMethods() throws IOException {
        Index index = Index.of(WildcardTypeTest.class);
        ClassInfo testClass = index.getClassByName(WildcardTypeTest.class);

        Type gggType = testClass.field("ggg").type();
        assertEquals(gggType.asParameterizedType().arguments().get(0),
                WildcardType.createUpperBound(Number.class));

        Type hhhType = testClass.field("hhh").type();
        assertEquals(hhhType.asParameterizedType().arguments().get(0),
                WildcardType.createLowerBound(Number.class));

        Type fffType = testClass.field("fff").type();
        assertEquals(fffType.asParameterizedType().arguments().get(0), WildcardType.UNBOUNDED);
    }

    @Test
    public void testWildcardTypeBuilder() throws IOException {
        Index index = Index.of(WildcardTypeTest.class);
        ClassInfo testClass = index.getClassByName(WildcardTypeTest.class);

        assertEquals(WildcardType.UNBOUNDED, WildcardType.builder().build());

        Type gggType = testClass.field("ggg").type();
        assertEquals(gggType.asParameterizedType().arguments().get(0),
                WildcardType.builder().setUpperBound(ClassType.create(Number.class)).build());

        Type hhhType = testClass.field("hhh").type();
        assertEquals(hhhType.asParameterizedType().arguments().get(0),
                WildcardType.builder().setLowerBound(Number.class).build());

        Type iiiType = testClass.field("iii").type();
        assertEquals(iiiType.asParameterizedType().arguments().get(0),
                WildcardType.builder().setLowerBound(ClassType.create(Number.class))
                        .addAnnotation(AnnotationInstance.builder(MyAnnotation.class).value("iii").build())
                        .build());
    }

}
