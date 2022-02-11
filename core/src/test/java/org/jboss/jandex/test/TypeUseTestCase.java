/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.TypeTarget;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeUseTestCase {

    private static final String TEST_SUBJECT_CLAZZ = "test.TypeUseExample$TestSubject";

    @Test
    public void testTypeParameter() throws IOException {
        doTestTypeUse("test.TypeUseExample$TypeParameterAnnotation", TypeTarget.Usage.TYPE_PARAMETER,
                AnnotationTarget.Kind.CLASS);
    }

    @Test
    public void testTypeParameterBoundType() throws IOException {
        doTestTypeUse("test.TypeUseExample$TypeParameterBoundTypeAnnotation", TypeTarget.Usage.TYPE_PARAMETER_BOUND,
                AnnotationTarget.Kind.CLASS);
    }

    @Test
    public void testClassExtends() throws IOException {
        doTestTypeUse("test.TypeUseExample$ClassExtendsAnnotation", TypeTarget.Usage.CLASS_EXTENDS,
                AnnotationTarget.Kind.CLASS);
    }

    @Test
    public void testFieldType() throws IOException {
        doTestTypeUse("test.TypeUseExample$FieldTypeAnnotation", TypeTarget.Usage.EMPTY, AnnotationTarget.Kind.FIELD);
    }

    /**
     * Test annotations that target the type of a parameter
     * (such as Hibernate Validator's @Valid annotation)
     * have an accurate representation,
     * even after being written to / read from an index file.
     */
    @Test
    public void testMethodParameterType() throws IOException {
        doTestTypeUse("test.TypeUseExample$MethodParameterTypeAnnotation", TypeTarget.Usage.METHOD_PARAMETER,
                AnnotationTarget.Kind.METHOD);
    }

    @Test
    public void testMethodReturnType() throws IOException {
        doTestTypeUse("test.TypeUseExample$MethodReturnTypeAnnotation", TypeTarget.Usage.EMPTY, AnnotationTarget.Kind.METHOD);
    }

    @Test
    public void testMethodThrowsType() throws IOException {
        doTestTypeUse("test.TypeUseExample$MethodThrowsTypeAnnotation", TypeTarget.Usage.THROWS, AnnotationTarget.Kind.METHOD);
    }

    private void doTestTypeUse(String annotationClass,
            TypeTarget.Usage expectedUsage, AnnotationTarget.Kind expectedEnclosingTargetKind) throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(TEST_SUBJECT_CLAZZ.replace('.', '/') + ".class");
        indexer.index(stream);
        Index originalIndex = indexer.complete();

        // This has always worked fine
        verifyTypeUseAnnotations(originalIndex, annotationClass, expectedUsage, expectedEnclosingTargetKind);

        Index indexAfterWriteRead = IndexingUtil.roundtrip(originalIndex);

        // This used to fail with a ClassCastException because after the index was written, then read again,
        // the enclosing target in MethodParameterTypeTarget became a type,
        // and enclosingTarget() casts the enclosing target to MethodInfo...
        verifyTypeUseAnnotations(indexAfterWriteRead, annotationClass, expectedUsage, expectedEnclosingTargetKind);
    }

    private void verifyTypeUseAnnotations(Index index, String annotationClass,
            TypeTarget.Usage expectedUsage, AnnotationTarget.Kind expectedEnclosingTargetKind) {
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple(annotationClass));

        // There must be exactly two copies of the annotation: one for the parameter and one for the parameter type.
        assertEquals(1, annotations.size());
        AnnotationInstance annotation = annotations.get(0);
        assertEquals(AnnotationTarget.Kind.TYPE, annotation.target().kind());

        TypeTarget target = annotation.target().asType();
        assertEquals(expectedUsage, target.usage());

        // The enclosing target of the type use annotation must be the method/field/etc.
        // Note this used to fail with a ClassCastException, but only for an index that was written then read again!
        AnnotationTarget enclosingTarget = target.enclosingTarget();
        assertEquals(expectedEnclosingTargetKind, enclosingTarget.kind());

        // enclosingTarget()...declaringClass() used to return a null because we forgot to set it when reading an index.
        if (enclosingTarget.kind() == AnnotationTarget.Kind.METHOD) {
            assertEquals(TEST_SUBJECT_CLAZZ, enclosingTarget.asMethod().declaringClass().name().toString());
        } else if (enclosingTarget.kind() == AnnotationTarget.Kind.FIELD) {
            assertEquals(TEST_SUBJECT_CLAZZ, enclosingTarget.asField().declaringClass().name().toString());
        }
    }

}
