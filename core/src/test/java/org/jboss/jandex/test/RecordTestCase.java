/*
 * JBoss, Home of Professional Open Source. Copyright 2021 Red Hat, Inc., and
 * individual contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.RecordComponentInfo;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecordTestCase {

    Index index;

    @BeforeEach
    public void setup() throws IOException {
        index = buildIndex();
    }

    @Test
    public void testRecordHasAnnotation() {
        DotName recordAnnotation = DotName.createSimple("test.RecordExample$RecordAnnotation");

        ClassInfo rec = index.getClassByName(DotName.createSimple("test.RecordExample"));
        assertNotNull(rec);
        assertTrue(rec.isRecord());
        AnnotationInstance anno = rec.declaredAnnotation(recordAnnotation);
        assertNotNull(anno);
        assertEquals("Example", anno.value().asString());

        ClassInfo nestedRec = index.getClassByName(DotName.createSimple("test.RecordExample$NestedEmptyRecord"));
        assertNotNull(rec);
        assertTrue(rec.isRecord());
        assertEquals(ClassInfo.NestingType.INNER, nestedRec.nestingType());
        assertEquals("Empty", nestedRec.declaredAnnotation(recordAnnotation).value().asString());

    }

    @Test
    public void testRecordComponentHasAnnotation() {
        ClassInfo rec = index.getClassByName(DotName.createSimple("test.RecordExample"));
        List<AnnotationInstance> componentAnnos = rec.annotationsMap()
                .get(DotName.createSimple("test.RecordExample$ComponentAnnotation"));
        assertNotNull(componentAnnos);
        assertEquals(1, componentAnnos.size());
        assertEquals(AnnotationTarget.Kind.RECORD_COMPONENT, componentAnnos.get(0).target().kind());
        assertEquals("name", componentAnnos.get(0).target().asRecordComponent().name());
        assertEquals("nameComponent", componentAnnos.get(0).value().asString());

        assertEquals(4, rec.recordComponents().size());

        RecordComponentInfo idComponent = rec.recordComponent("id");
        assertNotNull(idComponent);
        List<AnnotationInstance> idAnnotations = idComponent.annotations();
        assertNotNull(idAnnotations);
        assertEquals(1, idAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, idAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", idAnnotations.get(0).name().toString());

        RecordComponentInfo nameComponent = rec.recordComponent("name");
        assertNotNull(nameComponent);
        List<AnnotationInstance> nameAnnotations = nameComponent.annotations();
        assertNotNull(nameAnnotations);
        assertEquals(2, nameAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, nameAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", nameAnnotations.get(0).name().toString());
        assertEquals(AnnotationTarget.Kind.RECORD_COMPONENT, nameAnnotations.get(1).target().kind());
        assertEquals("name", nameAnnotations.get(1).target().asRecordComponent().name());
        assertEquals("test.RecordExample$ComponentAnnotation", nameAnnotations.get(1).name().toString());
        assertEquals("nameComponent", nameAnnotations.get(1).value().asString());

        assertNull(rec.recordComponent("nonexisting"));
    }

    @Test
    public void testComponentFieldHasAnnotation() {
        ClassInfo rec = index.getClassByName(DotName.createSimple("test.RecordExample"));

        List<AnnotationInstance> idAnnotations = rec.field("id").annotations();
        assertNotNull(idAnnotations);
        assertEquals(1, idAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, idAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", idAnnotations.get(0).name().toString());

        List<AnnotationInstance> nameAnnotations = rec.field("name").annotations();
        assertNotNull(nameAnnotations);
        assertEquals(2, nameAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, nameAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", nameAnnotations.get(0).name().toString());
        assertEquals(AnnotationTarget.Kind.FIELD, nameAnnotations.get(1).target().kind());
        assertEquals("name", nameAnnotations.get(1).target().asField().name());
        assertEquals("test.RecordExample$FieldAnnotation", nameAnnotations.get(1).name().toString());
        assertEquals("nameField", nameAnnotations.get(1).value().asString());
    }

    @Test
    public void testComponentAccessorHasAnnotation() {
        ClassInfo rec = index.getClassByName(DotName.createSimple("test.RecordExample"));

        List<AnnotationInstance> idAnnotations = rec.method("id").annotations();
        assertNotNull(idAnnotations);
        assertEquals(1, idAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, idAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", idAnnotations.get(0).name().toString());

        List<AnnotationInstance> nameAnnotations = rec.method("name").annotations();
        assertNotNull(nameAnnotations);
        assertEquals(2, nameAnnotations.size());
        assertEquals(AnnotationTarget.Kind.TYPE, nameAnnotations.get(0).target().kind());
        assertEquals("test.Nullable", nameAnnotations.get(0).name().toString());
        assertEquals(AnnotationTarget.Kind.METHOD, nameAnnotations.get(1).target().kind());
        assertEquals("name", nameAnnotations.get(1).target().asMethod().name());
        assertEquals("test.RecordExample$AccessorAnnotation", nameAnnotations.get(1).name().toString());
        assertEquals("nameAccessor", nameAnnotations.get(1).value().asString());
    }

    @Test
    public void testRecordSignatureProcessed() {
        ClassInfo rec = index.getClassByName(DotName.createSimple("test.RecordExample"));
        assertNotNull(rec);
        assertTrue(rec.isRecord());

        assertEquals(1, rec.typeParameters().size());
        assertEquals("T", rec.typeParameters().get(0).identifier());
    }

    @Test
    public void canonicalCtor() {
        ClassInfo rec = index.getClassByName("test.RecordWithNoComponentsAndDefaultCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithNoComponentsAndCompactCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithNoComponentsAndCustomCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithDefaultCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithCompactCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithCustomCanonicalCtor");
        assertEquals(1, rec.constructors().size());
        assertEquals(rec.constructors().get(0), rec.canonicalRecordConstructor());

        rec = index.getClassByName("test.RecordWithMultipleCtorsAndDefaultCanonicalCtor");
        assertEquals(4, rec.constructors().size());
        assertEquals(2, rec.canonicalRecordConstructor().parametersCount());
        assertEquals(PrimitiveType.INT, rec.canonicalRecordConstructor().parameterType(0));
        assertEquals(ClassType.create(String.class), rec.canonicalRecordConstructor().parameterType(1));

        rec = index.getClassByName("test.RecordWithMultipleCtorsAndCompactCanonicalCtor");
        assertEquals(4, rec.constructors().size());
        assertEquals(2, rec.canonicalRecordConstructor().parametersCount());
        assertEquals(PrimitiveType.INT, rec.canonicalRecordConstructor().parameterType(0));
        assertEquals(ClassType.create(String.class), rec.canonicalRecordConstructor().parameterType(1));

        rec = index.getClassByName("test.RecordWithMultipleCtorsAndCustomCanonicalCtor");
        assertEquals(4, rec.constructors().size());
        assertEquals(2, rec.canonicalRecordConstructor().parametersCount());
        assertEquals(PrimitiveType.INT, rec.canonicalRecordConstructor().parameterType(0));
        assertEquals(ClassType.create(String.class), rec.canonicalRecordConstructor().parameterType(1));

        assertNull(index.getClassByName(RecordTestCase.class).canonicalRecordConstructor());
    }

    private Index buildIndex() throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample$NestedEmptyRecord.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample$RecordAnnotation.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample$ComponentAnnotation.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample$FieldAnnotation.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordExample$AccessorAnnotation.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithCompactCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithCustomCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithDefaultCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithMultipleCtorsAndCompactCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithMultipleCtorsAndCustomCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithMultipleCtorsAndDefaultCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithNoComponentsAndCompactCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithNoComponentsAndCustomCanonicalCtor.class"));
        indexer.index(getClass().getClassLoader().getResourceAsStream("test/RecordWithNoComponentsAndDefaultCanonicalCtor.class"));

        indexer.indexClass(RecordTestCase.class);

        Index index = indexer.complete();
        return IndexingUtil.roundtrip(index);
    }

}
