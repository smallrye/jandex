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
package org.jboss.jandex;

import java.util.AbstractList;

/**
 * A list which wraps RecordComponentInternal objects with a RecordComponentInfo,
 * so that the declaring class' reference can be set. This lazy construction
 * is used to conserve memory usage.
 */
class RecordComponentInfoGenerator extends AbstractList<RecordComponentInfo> {
    private final RecordComponentInternal[] recordComponents;
    private final ClassInfo clazz;
    private final byte[] positions;

    public RecordComponentInfoGenerator(ClassInfo clazz, RecordComponentInternal[] recordComponents, byte[] positions) {
        this.clazz = clazz;
        this.recordComponents = recordComponents;
        this.positions = positions;
    }

    @Override
    public RecordComponentInfo get(int i) {
        RecordComponentInternal recordComponent = (positions.length > 0) ? recordComponents[positions[i] & 0xFF]
                : recordComponents[i];
        return new RecordComponentInfo(clazz, recordComponent);
    }

    @Override
    public int size() {
        return recordComponents.length;
    }
}
