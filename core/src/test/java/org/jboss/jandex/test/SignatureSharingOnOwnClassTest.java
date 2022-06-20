package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.junit.jupiter.api.Test;

public class SignatureSharingOnOwnClassTest {
    public interface WithSignatures<E extends Runnable> {
        <E extends Comparable<E>> E method();
    }

    @Test
    public void test() throws Exception {
        Index index = Index.of(WithSignatures.class);
        ClassInfo clazz = index.getClassByName(WithSignatures.class);
        TypeVariable typeVariable = clazz.firstMethod("method").returnType().asTypeVariable(); // E extends Comparable<E>

        Type reference = typeVariable
                .bounds().get(0).asParameterizedType() // Comparable<E>
                .arguments().get(0); // E

        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, reference.kind());
        assertEquals("E", reference.asTypeVariableReference().identifier());
        assertSame(typeVariable, reference.asTypeVariableReference().follow());
    }
}
