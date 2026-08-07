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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class CompositeTestCase {

    private static final DotName BASE_NAME = DotName.createSimple("foo.Base");
    private static final ClassInfo BASE_INFO = ClassInfo.create(BASE_NAME, DotName.OBJECT_NAME, (short) 0, new DotName[0],
            Collections.<DotName, List<AnnotationInstance>> emptyMap(), false);
    private static final DotName BAR_NAME = DotName.createSimple("foo.Bar");
    private static final DotName FOO_NAME = DotName.createSimple("foo.Foo");

    private static final DotName DUP_IFACE = DotName.createSimple("foo.Iface");
    private static final DotName DUP_SUB = DotName.createSimple("foo.Sub");
    private static final DotName DUP_IMPL = DotName.createSimple("foo.Impl");

    @Test
    public void testComposite() {
        Index barIndex = createIndex(BAR_NAME);
        Index fooIndex = createIndex(FOO_NAME);

        CompositeIndex index = CompositeIndex.create(fooIndex, barIndex);
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple("foo.BarAnno"));
        int hit = 0;
        for (AnnotationInstance instance : annotations) {
            if (FOO_NAME.equals(((ClassInfo) instance.target()).name())) {
                hit |= 2;
            } else if (BAR_NAME.equals(((ClassInfo) instance.target()).name())) {
                hit |= 1;
            }
        }
        assertEquals(3, hit);

        assertEquals(5, verifyClasses(barIndex.getAllKnownSubclasses(DotName.OBJECT_NAME)));
        assertEquals(6, verifyClasses(fooIndex.getAllKnownSubclasses(DotName.OBJECT_NAME)));
        assertEquals(7, verifyClasses(index.getAllKnownSubclasses(DotName.OBJECT_NAME)));
    }

    private int verifyClasses(Collection<ClassInfo> allKnownSubclasses) {
        int hit;
        hit = 0;
        for (ClassInfo info : allKnownSubclasses) {
            if (BAR_NAME.equals(info.name())) {
                hit |= 1;
            } else if (FOO_NAME.equals(info.name())) {
                hit |= 2;
            } else if (BASE_NAME.equals(info.name())) {
                hit |= 4;
            }
        }
        return hit;
    }

    private Index createIndex(DotName name) {
        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
        ClassInfo classInfo = ClassInfo.create(name, BASE_NAME, (short) 0, new DotName[0], annotations, false);

        AnnotationValue[] values = new AnnotationValue[] { AnnotationValue.createStringValue("blah", "blah") };
        DotName annotationName = DotName.createSimple("foo.BarAnno");
        AnnotationInstance annotation = AnnotationInstance.create(annotationName, classInfo, values);
        annotations.put(annotationName, Collections.singletonList(annotation));

        Map<DotName, List<ClassInfo>> implementors = Collections.emptyMap();
        Map<DotName, ClassInfo> classes = Collections.singletonMap(name, classInfo);
        Map<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>();
        subclasses.put(DotName.OBJECT_NAME, Collections.singletonList(BASE_INFO));
        subclasses.put(BASE_NAME, Collections.singletonList(classInfo));

        return Index.create(annotations, subclasses, implementors, classes);
    }

    // Regression test for issue #174: a CompositeIndex must deduplicate results by class name.
    // ClassInfo has no equals/hashCode, so the previous HashSet<ClassInfo> accumulators deduped by
    // identity and let the same FQCN appear multiple times when merged across indexes. Here each
    // index contributes a DISTINCT ClassInfo instance for the same FQCN, so identity dedup would
    // have kept both.
    @Test
    public void testCompositeDeduplicatesByClassName() {
        CompositeIndex index = CompositeIndex.create(createDuplicatingIndex(), createDuplicatingIndex());

        assertEquals(1, index.getKnownDirectSubclasses(DotName.OBJECT_NAME).size());
        assertEquals(1, index.getAllKnownSubclasses(DotName.OBJECT_NAME).size());
        assertEquals(1, index.getKnownDirectImplementors(DUP_IFACE).size());
        assertEquals(1, index.getAllKnownImplementors(DUP_IFACE).size());
        assertEquals(1, index.getKnownDirectImplementations(DUP_IFACE).size());
        assertEquals(1, index.getAllKnownImplementations(DUP_IFACE).size());

        assertUniqueNames(index.getKnownDirectSubclasses(DotName.OBJECT_NAME));
        assertUniqueNames(index.getAllKnownSubclasses(DotName.OBJECT_NAME));
        assertUniqueNames(index.getKnownDirectImplementors(DUP_IFACE));
        assertUniqueNames(index.getAllKnownImplementors(DUP_IFACE));
    }

    private void assertUniqueNames(Collection<ClassInfo> classes) {
        Set<DotName> names = new HashSet<DotName>();
        for (ClassInfo clazz : classes) {
            assertTrue(names.add(clazz.name()), "duplicate class name in result: " + clazz.name());
        }
    }

    // Each invocation produces fresh ClassInfo instances for the same FQCNs.
    private Index createDuplicatingIndex() {
        ClassInfo sub = ClassInfo.create(DUP_SUB, DotName.OBJECT_NAME, (short) 0, new DotName[0],
                Collections.<DotName, List<AnnotationInstance>> emptyMap(), false);
        ClassInfo impl = ClassInfo.create(DUP_IMPL, DotName.OBJECT_NAME, (short) 0, new DotName[] { DUP_IFACE },
                Collections.<DotName, List<AnnotationInstance>> emptyMap(), false);

        Map<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>();
        subclasses.put(DotName.OBJECT_NAME, Collections.singletonList(sub));

        Map<DotName, List<ClassInfo>> implementors = new HashMap<DotName, List<ClassInfo>>();
        implementors.put(DUP_IFACE, Collections.singletonList(impl));

        Map<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>();
        classes.put(DUP_SUB, sub);
        classes.put(DUP_IMPL, impl);

        return Index.create(Collections.<DotName, List<AnnotationInstance>> emptyMap(), subclasses, implementors, classes);
    }
}
