package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.junit.jupiter.api.Test;

public class SignatureSharingOnMethodTest {
    public interface WithClassSignature<E extends Exception> {
    }

    public interface WithMethodSignature {
        <E extends Comparable<E>> E method();
    }

    @Test
    public void methodSignatureOnly() throws Exception {
        test(Index.of(WithMethodSignature.class));
    }

    @Test
    public void methodSignatureBeforeClassSignature() throws Exception {
        test(Index.of(WithMethodSignature.class, WithClassSignature.class));
    }

    @Test
    public void methodSignatureAfterClassSignature() throws Exception {
        test(Index.of(WithClassSignature.class, WithMethodSignature.class));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(WithMethodSignature.class);
        TypeVariable typeVariable = clazz.firstMethod("method").returnType().asTypeVariable(); // E extends Comparable<E>

        Type reference = typeVariable
                .bounds().get(0).asParameterizedType() // Comparable<E>
                .arguments().get(0); // E

        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, reference.kind());
        assertEquals("E", reference.asTypeVariableReference().identifier());
        assertSame(typeVariable, reference.asTypeVariableReference().follow());
    }
}
