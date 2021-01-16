/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex.test.bridge;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BridgeMethodTest {
    public static class ArrayWithNullableElementsConsumer
            implements Consumer<@Nullable Object[]> {
        @Override
        public void accept(@Nullable Object[] objects) {
        }
    }

    @Test
    public void arrayWithNullableElements() throws IOException {
        verifyMethodSignature(
                ArrayWithNullableElementsConsumer.class,
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.Object",
                "@Nullable java.lang.Object[]");
    }

    public static class NullableArrayConsumer
            implements Consumer<Object @Nullable []> {
        @Override
        public void accept(Object @Nullable [] objects) {
        }
    }

    @Test
    public void nullableArray() throws IOException {
        verifyMethodSignature(
                NullableArrayConsumer.class,
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "@Nullable java.lang.Object",
                "java.lang.Object @Nullable []");
    }

    public static class NullableArrayWithNullableElementsConsumer
            implements Consumer<@Nullable Object @Nullable []> {
        @Override
        public void accept(@Nullable Object @Nullable [] objects) {
        }
    }

    @Test
    public void nullableArrayWithNullableElementsConsumer() throws IOException {
        verifyMethodSignature(
                NullableArrayWithNullableElementsConsumer.class,
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "@Nullable java.lang.Object",
                "@Nullable java.lang.Object @Nullable []");
    }

    public static class ArrayWithNullableElementsSupplier
            implements Supplier<@Nullable Object[]> {
        @Override
        public @Nullable Object[] get() {
            return new Object[0];
        }
    }

    @Test
    public void arrayWithNullableElementsSupplier() throws IOException {
        verifyMethodSignature(
                ArrayWithNullableElementsSupplier.class,
                "get",
                TypeTarget.Usage.EMPTY,
                "java.lang.Object",
                "@Nullable java.lang.Object[]");
    }

    public static class NullableArraySupplier
            implements Supplier<Object @Nullable []> {
        @Override
        public Object @Nullable [] get() {
            return null;
        }
    }

    @Test
    public void nullableArraySupplier() throws IOException {
        verifyMethodSignature(
                NullableArraySupplier.class,
                "get",
                TypeTarget.Usage.EMPTY,
                "@Nullable java.lang.Object",
                "java.lang.Object @Nullable []");
    }

    private boolean isBridge(MethodInfo methodInfo) {
        int bridgeModifiers = 0x1000 /* SYNTHETIC */ | 0x40 /* BRIDGE */;
        return (methodInfo.flags() & bridgeModifiers) == bridgeModifiers;
    }

    private InputStream getClassBytes(Class<?> klass) {
        String fileName = klass.getName();
        fileName = fileName.substring(fileName.lastIndexOf('.') + 1);
        return klass.getResourceAsStream(fileName + ".class");
    }

    private void verifyMethodSignature(
            Class<?> klass,
            String methodName,
            TypeTarget.Usage usage,
            String expectedBridgeType,
            String expectedNonBridgeType) throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes(klass));
        int methods = 0;
        for (MethodInfo method : info.methods()) {
            if (!methodName.equals(method.name())) {
                continue;
            }
            String expectedType = isBridge(method) ? expectedBridgeType : expectedNonBridgeType;
            Type type;
            switch (usage) {
                case METHOD_PARAMETER:
                    type = method.parameters().get(0);
                    break;
                case EMPTY:
                    type = method.returnType();
                    break;
                default:
                    throw new IllegalArgumentException("Expected METHOD_PARAMETER or EMPTY, got " + usage);
            }
            Assert.assertEquals(type + " signature for " +
                            (isBridge(method) ? "" : "non-") + "bridge method " + method,
                    expectedType, type.toString());
            methods++;
        }
        if (methods == 0) {
            Assert.fail("At least one '" + methodName + "' method is expected in " + klass);
        }
    }
}
