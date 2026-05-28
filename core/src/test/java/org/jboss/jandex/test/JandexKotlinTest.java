package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JandexKotlin;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class JandexKotlinTest {
    @Test
    public void test() throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexClass(JandexKotlinTest.class);
        indexer.index(JandexKotlinTest.class.getResourceAsStream("/kotest/KoClass.class"));
        Index index = indexer.complete();

        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        ClassInfo testClass = index.getClassByName(JandexKotlinTest.class);
        assertNotNull(testClass);

        ClassInfo koClass = index.getClassByName("kotest.KoClass");
        assertNotNull(koClass);

        assertTrue(JandexKotlin.isKotlinClass(koClass));
        assertFalse(JandexKotlin.isKotlinClass(testClass));

        assertTrue(JandexKotlin.isKotlinMethod(koClass.firstMethod("classicFunction")));
        assertTrue(JandexKotlin.isKotlinMethod(koClass.firstMethod("suspendFunction")));
        assertFalse(JandexKotlin.isKotlinMethod(testClass.firstMethod("doTest")));

        assertFalse(JandexKotlin.isKotlinSuspendMethod(koClass.firstMethod("classicFunction")));
        assertTrue(JandexKotlin.isKotlinSuspendMethod(koClass.firstMethod("suspendFunction")));
        assertFalse(JandexKotlin.isKotlinSuspendMethod(testClass.firstMethod("doTest")));

        for (MethodParameterInfo param : koClass.firstMethod("classicFunction").parameters()) {
            assertFalse(JandexKotlin.isKotlinContinuationParameter(param));
        }
        assertFalse(JandexKotlin.isKotlinContinuationParameter(koClass.firstMethod("suspendFunction").parameters().get(0)));
        assertTrue(JandexKotlin.isKotlinContinuationParameter(koClass.firstMethod("suspendFunction").parameters().get(1)));
        for (MethodParameterInfo param : testClass.firstMethod("doTest").parameters()) {
            assertFalse(JandexKotlin.isKotlinContinuationParameter(param));
        }

        // the method is declared to return `kotlin.String`, but this type (and a lot of others)
        // is compiled to `java.lang.String` by Kotlin for better compatibility with Java
        Type type = JandexKotlin.getKotlinSuspendMethodResult(koClass.firstMethod("suspendFunction"));
        assertNotNull(type);
        assertEquals(DotName.STRING_NAME, type.name());

        assertThrows(IllegalArgumentException.class, () -> {
            JandexKotlin.getKotlinSuspendMethodResult(koClass.firstMethod("classicFunction"));
        });
    }
}
