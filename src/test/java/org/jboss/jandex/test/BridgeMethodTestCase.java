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

package org.jboss.jandex.test;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class BridgeMethodTestCase {

    @Test
    public void nestedConsumer() throws IOException {
         verifyMethodSignature(
                 "test/BridgeMethods$NestedConsumer.class",
                 "accept",
                 TypeTarget.Usage.METHOD_PARAMETER, "java.lang.Object",
                 "@Nullable test.BridgeMethods$NestedConsumer");
     }


    @Test
    public void arrayWithNullableElements() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$ArrayWithNullableElementsConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.Object",
                "@Nullable java.lang.Object[]");
    }


    @Test
    public void nullableArray() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArrayConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "@Nullable java.lang.Object",
                "java.lang.Object @Nullable []");
    }

    @Test
    public void nullableArrayWithNullableElementsConsumer() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArrayWithNullableElementsConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "@Nullable java.lang.Object",
                "@Nullable java.lang.Object @Nullable []");
    }


    @Test
    public void arrayWithNullableElementsSupplier() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$ArrayWithNullableElementsSupplier.class",
                "get",
                TypeTarget.Usage.EMPTY,
                "java.lang.Object",
                "@Nullable java.lang.Object[]");
    }


    @Test
    public void nullableArraySupplier() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArraySupplier.class",
                "get",
                TypeTarget.Usage.EMPTY,
                "@Nullable java.lang.Object",
                "java.lang.Object @Nullable []");
    }

    private boolean isBridge(MethodInfo methodInfo) {
        int bridgeModifiers = 0x1000 /* SYNTHETIC */ | 0x40 /* BRIDGE */;
        return (methodInfo.flags() & bridgeModifiers) == bridgeModifiers;
    }

    private InputStream getClassBytes(String klass) {
        return getClass().getClassLoader().getResourceAsStream(klass);
    }

    private void verifyMethodSignature(
            String klass,
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
