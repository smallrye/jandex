package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class UnboundedWildcardTest {
    private List<?> implicitlyUnboundedWildcard;

    private List<? extends Object> explicitlyUnboundedWildcard;

    private List<@MyAnnotation("wildcard") ?> annotatedImplicitlyUnboundedWildcard;

    private List<@MyAnnotation("wildcard") ? extends @MyAnnotation("bound") Object> annotatedExplicitlyUnboundedWildcard;

    @Test
    public void test() throws IOException {
        Index index = Index.of(UnboundedWildcardTest.class);
        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(UnboundedWildcardTest.class);

        FieldInfo implicitlyUnboundedWildcard = clazz.field("implicitlyUnboundedWildcard");
        assertEquals("java.util.List<?>",
                implicitlyUnboundedWildcard.type().toString());

        FieldInfo explicitlyUnboundedWildcard = clazz.field("explicitlyUnboundedWildcard");
        assertEquals("java.util.List<?>",
                explicitlyUnboundedWildcard.type().toString());

        FieldInfo annotatedImplicitlyUnboundedWildcard = clazz.field("annotatedImplicitlyUnboundedWildcard");
        assertEquals("java.util.List<@MyAnnotation(\"wildcard\") ?>",
                annotatedImplicitlyUnboundedWildcard.type().toString());

        FieldInfo annotatedExplicitlyUnboundedWildcard = clazz.field("annotatedExplicitlyUnboundedWildcard");
        assertEquals("java.util.List<@MyAnnotation(\"wildcard\") ? extends java.lang.@MyAnnotation(\"bound\") Object>",
                annotatedExplicitlyUnboundedWildcard.type().toString());
    }
}
