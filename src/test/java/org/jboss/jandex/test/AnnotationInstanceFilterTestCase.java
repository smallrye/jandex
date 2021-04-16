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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.RecordComponentInfo;
import org.jboss.jandex.TypeTarget;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationInstanceFilterTestCase {
    private static class Hit implements AnnotationTarget{
        @Override
        public Kind kind() {
            return null;
        }

        @Override
        public ClassInfo asClass() {
            return null;
        }

        @Override
        public FieldInfo asField() {
            return null;
        }

        @Override
        public MethodInfo asMethod() {
            return null;
        }

        @Override
        public MethodParameterInfo asMethodParameter() {
            return null;
        }

        @Override
        public TypeTarget asType() {
            return null;
        }

        @Override
        public RecordComponentInfo asRecordComponent() {
            return null;
        }
    }
    private static class Miss implements AnnotationTarget{
        @Override
        public Kind kind() {
            return null;
        }

        @Override
        public ClassInfo asClass() {
            return null;
        }

        @Override
        public FieldInfo asField() {
            return null;
        }

        @Override
        public MethodInfo asMethod() {
            return null;
        }

        @Override
        public MethodParameterInfo asMethodParameter() {
            return null;
        }

        @Override
        public TypeTarget asType() {
            return null;
        }

        @Override
        public RecordComponentInfo asRecordComponent() {
            return null;
        }
    }

    @Test
    public void testFilter() throws Exception {
        Map<DotName, List<AnnotationInstance>> map = new HashMap<DotName, List<AnnotationInstance>>();

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 9; i++) {
                DotName foo = DotName.createSimple("foo" + j);
                record(map, foo, AnnotationInstance.create(foo, i % 3 == 0 ? new Hit() : new Miss(), Collections.<AnnotationValue>emptyList()));
            }
            for (int i = 0; i < 9; i++) {
                DotName foo = DotName.createSimple("allHits" + j);
                record(map, foo, AnnotationInstance.create(foo, new Hit(), Collections.<AnnotationValue>emptyList()));
            }

            for (int i = 0; i < 9; i++) {
                DotName foo = DotName.createSimple("allMisses" + j);
                record(map, foo, AnnotationInstance.create(foo, new Miss(), Collections.<AnnotationValue>emptyList()));
            }
        }

        verify(map, Hit.class, 36);
        verify(map, Miss.class, 45);

    }

    private void verify(Map<DotName, List<AnnotationInstance>> map, Class<?> clazz, int expected) throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Collection<AnnotationInstance> filter1 = createFilter(map, clazz);
        Assert.assertEquals(expected, filter1.size());

        int count = 0;
        for (AnnotationInstance instance : filter1) {
            if (instance.target().getClass() == clazz) {
                count++;
            }
        }

        Assert.assertEquals(expected, count);
    }

    @SuppressWarnings("unchecked")
    private Collection<AnnotationInstance> createFilter(Map<DotName, List<AnnotationInstance>> map, Class<?> clazz) throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Constructor<?> constructor = Class.forName("org.jboss.jandex.AnnotationTargetFilterCollection")
                .getDeclaredConstructor(Map.class, Class.class);
        constructor.setAccessible(true);
        return (Collection<AnnotationInstance>) constructor.newInstance(map, clazz);
    }

    private void record(Map<DotName, List<AnnotationInstance>> map, DotName key, AnnotationInstance instance) {
        List<AnnotationInstance> list = map.get(key);
        if (list == null) {
            list = new ArrayList<AnnotationInstance>();
            map.put(key, list);
        }

        list.add(instance);
    }


}
