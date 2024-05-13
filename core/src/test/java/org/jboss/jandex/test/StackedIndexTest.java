package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.StackedIndex;
import org.jboss.jandex.test.util.IOUtil;
import org.junit.jupiter.api.Test;

public class StackedIndexTest {
    // this test only verifies annotation access, most of other methods are tested in `IndexNavigationTest`

    // `AnnotatedClass3` is intentionally a copy of `AnnotatedClass1` with modified annotation members,
    // so that we can load its bytecode, replace all occurrences of `AnnotatedClass3` with `AnnotatedClass1`,
    // index both of them separately and have two indexes with the same class, only with different annotations
    // (`AnnotatedClass2` and `AnnotatedClass4` also look the same, but are still treated as different classes)

    @MyRepeatableAnnotation("cr1")
    @MyRepeatableAnnotation.List({
            @MyRepeatableAnnotation("cr2"),
            @MyRepeatableAnnotation("cr3")
    })
    @MyAnnotation("c1")
    static class AnnotatedClass1<@MyAnnotation("c2") T extends @MyAnnotation("c3") Number> {
        @MyRepeatableAnnotation("fr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("fr2"),
                @MyRepeatableAnnotation("fr3")
        })
        @MyAnnotation("f1")
        Map<@MyAnnotation("f2") String, @MyAnnotation("f3") List<? extends @MyAnnotation("f4") Number>> field;

        @MyRepeatableAnnotation("mr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("mr2"),
                @MyRepeatableAnnotation("mr3")
        })
        @MyAnnotation("m1")
        boolean method(
                @MyAnnotation("m2") Map<@MyAnnotation("m3") String, @MyAnnotation("m4") List<? extends @MyAnnotation("m5") Number>> param,
                @MyAnnotation("m6") int @MyAnnotation("m7") [] @MyAnnotation("m8") [] otherParam) {
            return false;
        }
    }

    @MyAnnotation("extra1")
    @MyRepeatableAnnotation("extra2")
    static class AnnotatedClass2 {
    }

    @MyRepeatableAnnotation("XXXcr1")
    @MyRepeatableAnnotation.List({
            @MyRepeatableAnnotation("XXXcr2"),
            @MyRepeatableAnnotation("XXXcr3")
    })
    @MyAnnotation("XXXc1")
    static class AnnotatedClass3<@MyAnnotation("XXXc2") T extends @MyAnnotation("XXXc3") Number> {
        @MyRepeatableAnnotation("XXXfr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("XXXfr2"),
                @MyRepeatableAnnotation("XXXfr3")
        })
        @MyAnnotation("XXXf1")
        Map<@MyAnnotation("XXXf2") String, @MyAnnotation("XXXf3") List<? extends @MyAnnotation("XXXf4") Number>> field;

        @MyRepeatableAnnotation("XXXmr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("XXXmr2"),
                @MyRepeatableAnnotation("XXXmr3")
        })
        @MyAnnotation("XXXm1")
        boolean method(
                @MyAnnotation("XXXm2") Map<@MyAnnotation("XXXm3") String, @MyAnnotation("XXXm4") List<? extends @MyAnnotation("XXXm5") Number>> param,
                @MyAnnotation("XXXm6") int @MyAnnotation("XXXm7") [] @MyAnnotation("XXXm8") [] otherParam) {
            return false;
        }
    }

    @MyAnnotation("extra3")
    @MyRepeatableAnnotation("extra4")
    static class AnnotatedClass4 {
    }

    @Test
    public void test() throws IOException {
        Index upperIndex = Index.of(AnnotatedClass1.class, AnnotatedClass2.class);
        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(annotatedClass3AsAnnotatedClass1()));
        indexer.indexClass(AnnotatedClass4.class);
        Index lowerIndex = indexer.complete();
        Index baseIndex = Index.of(MyAnnotation.class, MyRepeatableAnnotation.class);

        StackedIndex index = StackedIndex.create(baseIndex, lowerIndex, upperIndex);

        Collection<AnnotationInstance> annotations = index.getAnnotations(MyAnnotation.DOT_NAME);
        assertEquals(19 + 2, annotations.size()); // 19 from AnnotatedClass1, 2 from AnnotatedClass2/4
        for (AnnotationInstance annotation : annotations) {
            assertFalse(annotation.value().asString().startsWith("XXX"));
        }

        // ecj also puts the `MyRepeatableAnnotation` and `MyRepeatableAnnotation.List` annotations
        // on the _types_ of `AnnotatedClassN.field` and `method`, contrary to the `@Target` declarations

        annotations = index.getAnnotations(MyRepeatableAnnotation.DOT_NAME);
        assertEquals(CompiledWith.ecj() ? 11 : 5, annotations.size());
        for (AnnotationInstance annotation : annotations) {
            assertFalse(annotation.value().asString().startsWith("XXX"));
        }

        annotations = index.getAnnotations(MyRepeatableAnnotation.List.DOT_NAME);
        assertEquals(CompiledWith.ecj() ? 5 : 3, annotations.size());
        for (AnnotationInstance annotation : annotations) {
            assertFalse(annotation.value().asString().startsWith("XXX"));
        }

        annotations = index.getAnnotationsWithRepeatable(MyRepeatableAnnotation.DOT_NAME, index);
        assertEquals(CompiledWith.ecj() ? 21 : 11, annotations.size());
        for (AnnotationInstance annotation : annotations) {
            assertFalse(annotation.value().asString().startsWith("XXX"));
        }
    }

    private static byte[] annotatedClass3AsAnnotatedClass1() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String path = "/" + AnnotatedClass3.class.getName().replace('.', '/') + ".class";
        try (InputStream in = StackedIndexTest.class.getResourceAsStream(path)) {
            IOUtil.copy(in, out);
        }
        byte[] clazz = out.toByteArray();
        IOUtil.searchAndReplace(clazz, "AnnotatedClass3".getBytes(StandardCharsets.UTF_8),
                "AnnotatedClass1".getBytes(StandardCharsets.UTF_8));
        return clazz;
    }
}
