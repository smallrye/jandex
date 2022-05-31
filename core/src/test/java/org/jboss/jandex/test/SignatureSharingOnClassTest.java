package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class SignatureSharingOnClassTest {
    public interface WithMethodSignature {
        <E extends Runnable> E method(E arg);
    }

    public interface WithClassSignature<E extends Exception> extends Iterator<E> {
    }

    @Test
    public void classSignatureOnly() throws Exception {
        test(Index.of(WithClassSignature.class));
    }

    @Test
    public void classSignatureBeforeMethodSignature() throws Exception {
        test(Index.of(WithClassSignature.class, WithMethodSignature.class));
    }

    @Test
    public void classSignatureAfterMethodSignature() throws Exception {
        test(Index.of(WithMethodSignature.class, WithClassSignature.class));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(WithClassSignature.class);
        DotName bound = clazz.interfaceTypes().get(0).asParameterizedType() // Iterator<E>
                .arguments().get(0).asTypeVariable() // E
                .bounds().get(0).asClassType().name(); // E's bound

        assertEquals(DotName.createSimple(Exception.class.getName()), bound);
    }
}
