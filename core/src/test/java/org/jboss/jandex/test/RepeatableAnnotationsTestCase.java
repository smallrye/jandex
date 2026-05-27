/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

public class RepeatableAnnotationsTestCase {

    static final DotName ALPHA_NAME = DotName.createSimple("test.RepeatableAnnotationsExample$Alpha");
    static final DotName ALPHA_CONTAINER_NAME = DotName.createSimple("test.RepeatableAnnotationsExample$AlphaContainer");
    static final DotName MY_ANNOTATED_NAME = DotName.createSimple("test.RepeatableAnnotationsExample$MyAnnotated");

    @Test
    public void testIndexView() throws IOException {
        Index index = getIndexForClass(MY_ANNOTATED_NAME, ALPHA_NAME);
        assertIndexView(index.getAnnotationsWithRepeatable(ALPHA_NAME, index));
        assertIndexView(index.getAnnotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME));
    }

    private void assertIndexView(Collection<AnnotationInstance> annotations) {
        assertEquals(10, annotations.size());
        assertValues(find(annotations, Kind.CLASS, null), 0);
        assertValues(find(annotations, Kind.METHOD, "foo"), 1);
        assertValues(find(annotations, Kind.METHOD_PARAMETER, "fooName"), 11, 12);
        assertValues(find(annotations, Kind.METHOD, "bar"), 2, 3);
        assertValues(find(annotations, Kind.METHOD_PARAMETER, "barName"), 10);
        assertValues(find(annotations, Kind.FIELD, "myField"), -1, -2);
        assertValues(find(annotations, Kind.FIELD, "anotherField"), -3);
    }

    @Test
    public void testClassInfo() throws IOException {
        Index index = getIndexForClass(MY_ANNOTATED_NAME, ALPHA_NAME);
        ClassInfo alpha = index.getClassByName(MY_ANNOTATED_NAME);
        assertClassInfo(alpha.declaredAnnotationsWithRepeatable(ALPHA_NAME, index));
        assertClassInfo(alpha.declaredAnnotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME));
    }

    private void assertClassInfo(Collection<AnnotationInstance> annotations) {
        assertValues(annotations, 0);
    }

    @Test
    public void testMethodInfo() throws IOException {
        Index index = getIndexForClass(MY_ANNOTATED_NAME, ALPHA_NAME);
        ClassInfo alpha = index.getClassByName(MY_ANNOTATED_NAME);
        // MyAnnotated.foo()
        MethodInfo foo = alpha.firstMethod("foo");
        assertMethodInfoFoo(foo.annotationsWithRepeatable(ALPHA_NAME, index));
        assertMethodInfoFoo(foo.annotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME));
        // MyAnnotated.bar()
        MethodInfo bar = alpha.firstMethod("bar");
        assertMethodInfoBar(bar.annotationsWithRepeatable(ALPHA_NAME, index), bar);
        assertMethodInfoBar(bar.annotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME), bar);
    }

    private void assertMethodInfoFoo(Collection<AnnotationInstance> annotations) {
        assertEquals(3, annotations.size());
        assertValues(find(annotations, Kind.METHOD, null), 1);
        assertValues(find(annotations, Kind.METHOD_PARAMETER, null), 11, 12);
    }

    private void assertMethodInfoBar(Collection<AnnotationInstance> annotations, MethodInfo bar) {
        assertEquals(3, annotations.size());
        List<AnnotationInstance> barMethodInstance = find(annotations, Kind.METHOD, null);
        // Test the target of an instance coming from the container
        assertEquals(bar, barMethodInstance.get(0).target());
        assertValues(barMethodInstance, 2, 3);
        assertValues(find(annotations, Kind.METHOD_PARAMETER, null), 10);
    }

    @Test
    public void testFieldInfo() throws IOException {
        Index index = getIndexForClass(MY_ANNOTATED_NAME, ALPHA_NAME);
        ClassInfo alpha = index.getClassByName(MY_ANNOTATED_NAME);
        FieldInfo myField = alpha.field("myField");
        assertFieldInfoMyField(myField.annotationsWithRepeatable(ALPHA_NAME, index));
        assertFieldInfoMyField(myField.annotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME));
        FieldInfo anotherField = alpha.field("anotherField");
        assertFieldInfoAnotherField(anotherField.annotationsWithRepeatable(ALPHA_NAME, index));
        assertFieldInfoAnotherField(anotherField.annotationsWithRepeatable(ALPHA_NAME, ALPHA_CONTAINER_NAME));
        // Test that it's still possible to query the container annotation
        List<AnnotationInstance> direct = myField.annotations();
        assertEquals(1, direct.size());
        assertEquals(ALPHA_CONTAINER_NAME, direct.get(0).name());
    }

    private void assertFieldInfoMyField(Collection<AnnotationInstance> annotations) {
        assertValues(annotations, -1, -2);
    }

    private void assertFieldInfoAnotherField(Collection<AnnotationInstance> annotations) {
        assertValues(annotations, -3);
    }

    @Test
    public void testAnnotationDefinitionNotAvailable() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            Index index = getIndexForClass(MY_ANNOTATED_NAME);
            index.getAnnotationsWithRepeatable(ALPHA_NAME, index);
        });
    }

    private void assertValues(Collection<AnnotationInstance> instances, Integer... values) {
        assertEquals(values.length, instances.size());
        List<Integer> list = Arrays.asList(values);
        for (AnnotationInstance i : instances) {
            assertTrue(list.contains(i.value().asInt()), i + " is not found in " + Arrays.toString(values));
        }
    }

    private Index getIndexForClass(DotName... classes) throws IOException {
        Indexer indexer = new Indexer();
        for (DotName clazz : classes) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(clazz.toString().replace('.', '/') + ".class");
            indexer.index(stream);
        }
        return indexer.complete();
    }

    private List<AnnotationInstance> find(Collection<AnnotationInstance> instances, AnnotationTarget.Kind kind, String name) {
        List<AnnotationInstance> ret = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance instance : instances) {
            if (instance.target().kind() == kind) {
                switch (kind) {
                    case METHOD:
                        if (name != null && !instance.target().asMethod().name().equals(name)) {
                            continue;
                        }
                        break;
                    case FIELD:
                        if (name != null && !instance.target().asField().name().equals(name)) {
                            continue;
                        }
                        break;
                    case METHOD_PARAMETER:
                        if (name != null && !instance.target().asMethodParameter().name().equals(name)) {
                            continue;
                        }
                        break;
                    default:
                        break;
                }
                ret.add(instance);
            }
        }
        return ret;
    }

}
