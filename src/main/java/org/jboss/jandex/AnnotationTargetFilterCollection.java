/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.jandex;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Iteratively filters a map of multiple annotation targets to the
 * instances with a specific target type.
 *
 * @author Jason T. Greene
 */
class AnnotationTargetFilterCollection<T extends AnnotationTarget> extends AbstractCollection<AnnotationInstance> {

    private final Map<?, List<AnnotationInstance>> map;
    private final Class<T> type;
    private int size;

    AnnotationTargetFilterCollection(Map<?, List<AnnotationInstance>> map, Class<T> type) {
        this.map = map;
        this.type = type;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<AnnotationInstance> iterator() {
        return new Iterator<AnnotationInstance>() {
            final Iterator<List<AnnotationInstance>> mapIterator = map.values().iterator();
            AnnotationInstance next;
            Iterator<AnnotationInstance> nextList;

            void advance() {
                if (next != null) {
                    return;
                }

                Class<T> type = AnnotationTargetFilterCollection.this.type;

                while (true) {
                    if (nextList == null || !nextList.hasNext()) {
                        if (! mapIterator.hasNext()) {
                            return;
                        }

                        nextList = mapIterator.next().iterator();
                    }
                    Iterator<AnnotationInstance> nextList = this.nextList;
                    while (nextList.hasNext()) {
                        AnnotationInstance next = nextList.next();
                        if (next.target().getClass() == type) {
                            this.next = next;
                            return;
                        }
                    }
                }
            }


            @Override
            public boolean hasNext() {
                advance();

                return next != null;
            }

            @Override
            public AnnotationInstance next() {
                advance();

                AnnotationInstance next = this.next;
                this.next = null;
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int size() {
        if (size != 0) {
            return size;
        }

        if (map.size() == 0) {
            return 0;
        }

        int size = 0;
        Class<T> type = this.type;
        for (List<AnnotationInstance> instances : map.values()) {
            for (AnnotationInstance instance : instances) {
                if (type == instance.target().getClass()) {
                    size++;
                }
            }
        }

        return this.size = size;
    }
}
