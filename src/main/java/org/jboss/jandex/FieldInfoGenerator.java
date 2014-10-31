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

import java.util.AbstractList;


/**
 * A list which wraps FieldInternal objects with a FieldInfo, so that
 * the declaring class' reference can be set. This lazy construction
 * is used to conserve memory usage.
 *
 * @author Jason T. Greene
 */
class FieldInfoGenerator extends AbstractList<FieldInfo> {
    private final FieldInternal[] fields;
    private final ClassInfo clazz;

    public FieldInfoGenerator(ClassInfo clazz, FieldInternal[] fields) {
        this.clazz = clazz;
        this.fields = fields;
    }

    @Override
    public FieldInfo get(int i) {
        return new FieldInfo(clazz, fields[i]);
    }

    @Override
    public int size() {
        return fields.length;
    }
}
