package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class SignatureSharingOnOwnClassTest {
    public interface WithSignatures<E extends Runnable> {
        <E extends Comparable<E>> E method();
    }

    @Test
    public void test() throws Exception {
        Index index = Index.of(WithSignatures.class);
        ClassInfo clazz = index.getClassByName(WithSignatures.class);
        Type typeVariable = clazz.firstMethod("method").returnType().asTypeVariable() // E extends Comparable<E>
                .bounds().get(0).asParameterizedType() // Comparable<E>
                .arguments().get(0); // E

        assertEquals(Type.Kind.UNRESOLVED_TYPE_VARIABLE, typeVariable.kind());
        assertEquals("E", typeVariable.asUnresolvedTypeVariable().identifier());
    }
}
