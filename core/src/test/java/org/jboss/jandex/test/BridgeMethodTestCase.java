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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class BridgeMethodTestCase {

    @Test
    public void nestedConsumer() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NestedConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.Object",
                "test.@Nullable BridgeMethods$NestedConsumer");
    }

    @Test
    public void arrayWithNullableElements() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$ArrayWithNullableElementsConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.Object",
                "java.lang.@Nullable Object[]");
    }

    @Test
    public void nullableArray() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArrayConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.@Nullable Object",
                "java.lang.Object @Nullable []");
    }

    @Test
    public void nullableArrayWithNullableElementsConsumer() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArrayWithNullableElementsConsumer.class",
                "accept",
                TypeTarget.Usage.METHOD_PARAMETER, "java.lang.@Nullable Object",
                "java.lang.@Nullable Object @Nullable []");
    }

    @Test
    public void arrayWithNullableElementsSupplier() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$ArrayWithNullableElementsSupplier.class",
                "get",
                TypeTarget.Usage.EMPTY,
                "java.lang.Object",
                "java.lang.@Nullable Object[]");
    }

    @Test
    public void nullableArraySupplier() throws IOException {
        verifyMethodSignature(
                "test/BridgeMethods$NullableArraySupplier.class",
                "get",
                TypeTarget.Usage.EMPTY,
                "java.lang.@Nullable Object",
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
        ClassInfo info = IndexingUtil.indexSingle(getClassBytes(klass));
        int methods = 0;
        for (MethodInfo method : info.methods()) {
            if (!methodName.equals(method.name())) {
                continue;
            }
            String expectedType = isBridge(method) ? expectedBridgeType : expectedNonBridgeType;
            Type type;
            switch (usage) {
                case METHOD_PARAMETER:
                    type = method.parameterTypes().get(0);
                    break;
                case EMPTY:
                    type = method.returnType();
                    break;
                default:
                    throw new IllegalArgumentException("Expected METHOD_PARAMETER or EMPTY, got " + usage);
            }
            assertEquals(expectedType, type.toString(), type + " signature for " +
                    (isBridge(method) ? "" : "non-") + "bridge method " + method);
            methods++;
        }
        if (methods == 0) {
            fail("At least one '" + methodName + "' method is expected in " + klass);
        }
    }

    @Test
    public void returnType() throws IOException {
        DotName nullable = DotName.createSimple("test.Nullable");
        DotName untainted = DotName.createSimple("test.Untainted");

        ClassInfo clazz = IndexingUtil.indexSingle(getClassBytes("test/BridgeMethods$Subclass.class"));
        for (MethodInfo method : filterMethods(clazz, "typeVariable")) {
            if (method.returnType().name().equals(DotName.createSimple(Collection.class.getName()))) {
                // bridge method
                assertTrue(isBridge(method));
                assertEquals(Type.Kind.CLASS, method.returnType().kind());
                assertTrue(method.typeParameters().isEmpty());

                assertNotNull(method.annotation(nullable));
                assertEquals(Type.Kind.VOID, method.annotation(nullable).target().asType().target().kind());

                assertNotNull(method.annotation(untainted));
                assertEquals(Type.Kind.VOID, method.annotation(untainted).target().asType().target().kind());
            } else if (method.returnType().name().equals(DotName.createSimple(Set.class.getName()))) {
                // actual overridden method
                assertFalse(isBridge(method));
                assertEquals(Type.Kind.PARAMETERIZED_TYPE, method.returnType().kind());
                assertFalse(method.typeParameters().isEmpty());

                assertNotNull(method.annotation(nullable));
                assertEquals(Type.Kind.CLASS, method.annotation(nullable).target().asType().target().kind());
                assertNotNull(method.typeParameters().get(0).asTypeVariable()
                        .bounds().get(0).annotation(nullable));

                assertNotNull(method.annotation(untainted));
                assertEquals(Type.Kind.TYPE_VARIABLE,
                        method.annotation(untainted).target().asType().target().kind());
                assertNotNull(method.typeParameters().get(0).asTypeVariable().annotation(untainted));
            } else {
                fail();
            }
        }

        for (MethodInfo method : filterMethods(clazz, "wildcard")) {
            if (method.returnType().name().equals(DotName.createSimple(Collection.class.getName()))) {
                // bridge method
                assertTrue(isBridge(method));
                assertEquals(Type.Kind.CLASS, method.returnType().kind());

                assertNotNull(method.annotation(nullable));
                assertEquals(Type.Kind.VOID, method.annotation(nullable).target().asType().target().kind());

                assertNotNull(method.annotation(untainted));
                assertEquals(Type.Kind.VOID, method.annotation(untainted).target().asType().target().kind());
            } else if (method.returnType().name().equals(DotName.createSimple(Set.class.getName()))) {
                // actual overridden method
                assertFalse(isBridge(method));
                assertEquals(Type.Kind.PARAMETERIZED_TYPE, method.returnType().kind());

                assertNotNull(method.annotation(nullable));
                assertEquals(Type.Kind.CLASS, method.annotation(nullable).target().asType().target().kind());
                assertNotNull(method.returnType().asParameterizedType().arguments().get(0)
                        .asWildcardType().extendsBound().annotation(nullable));

                assertNotNull(method.annotation(untainted));
                assertEquals(Type.Kind.WILDCARD_TYPE,
                        method.annotation(untainted).target().asType().target().kind());
                assertNotNull(method.returnType().asParameterizedType().arguments().get(0).annotation(untainted));
            } else {
                fail();
            }
        }
    }

    private List<MethodInfo> filterMethods(ClassInfo clazz, String methodName) {
        List<MethodInfo> result = new ArrayList<MethodInfo>();
        for (MethodInfo method : clazz.methods()) {
            if (methodName.equals(method.name())) {
                result.add(method);
            }
        }
        return result;
    }
}
