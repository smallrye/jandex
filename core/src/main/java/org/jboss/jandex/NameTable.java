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

package org.jboss.jandex;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of intern pools.
 *
 * @author Jason T. Greene
 */
class NameTable {
    private StrongInternPool<String> stringPool = StrongInternPool.forStrings();
    private StrongInternPool<Type> typePool = StrongInternPool.forTypes();
    private StrongInternPool<Type[]> typeListPool = StrongInternPool.forTypeArrays();
    private StrongInternPool<byte[]> bytePool = StrongInternPool.forByteArrays();
    private StrongInternPool<MethodInternal> methodPool = StrongInternPool.forMethods();
    private StrongInternPool<FieldInternal> fieldPool = StrongInternPool.forFields();
    private StrongInternPool<RecordComponentInternal> recordComponentPool = StrongInternPool.forRecordComponents();
    private Map<String, DotName> names = new HashMap<String, DotName>();

    DotName convertToName(String name) {
        return convertToName(name, '.');
    }

    DotName convertToName(String name, char delim) {
        DotName result = names.get(name);
        if (result != null)
            return result;

        int loc = lastIndexOf(name, delim);
        String local = intern(name.substring(loc + 1));
        DotName prefix = loc < 1 ? null : convertToName(intern(name.substring(0, loc)), delim);
        result = new DotName(prefix, local, true, loc > 0 && name.charAt(loc) == '$');

        names.put(name, result);

        return result;
    }

    private int lastIndexOf(String name, char delim) {
        // Begin at second last position to avoid empty local name
        int pos = name.length() - 1;
        while (--pos >= 0) {
            char c = name.charAt(pos);
            if (c == delim || c == '$') {
                break;
            }
        }

        // avoid splitting on '$' if previous char is a delimiter or the '$'
        // is in position 0, because subsequent split would produce an empty
        // local name
        if (pos >= 0 && name.charAt(pos) == '$' && (pos == 0 || name.charAt(pos - 1) == delim)) {
            pos--;
        }

        return pos;
    }

    DotName wrap(DotName prefix, String local, boolean inner) {
        DotName name = new DotName(prefix, intern(local), true, true);

        return intern(name, '.');
    }

    String intern(String string) {
        return stringPool.intern(string);
    }

    int positionOf(String string) {
        return stringPool.index().positionOf(string);
    }

    Type intern(Type type) {
        return typePool.intern(type);
    }

    Type[] intern(Type[] types) {
        return typeListPool.intern(types);
    }

    byte[] intern(byte[] bytes) {
        return bytePool.intern(bytes);
    }

    int positionOf(byte[] bytes) {
        return bytePool.index().positionOf(bytes);
    }

    MethodInternal intern(MethodInternal methodInternal) {
        return methodPool.intern(methodInternal);
    }

    int positionOf(MethodInternal methodInternal) {
        return methodPool.index().positionOf(methodInternal);
    }

    FieldInternal intern(FieldInternal fieldInternal) {
        return fieldPool.intern(fieldInternal);
    }

    int positionOf(FieldInternal fieldInternal) {
        return fieldPool.index().positionOf(fieldInternal);
    }

    RecordComponentInternal intern(RecordComponentInternal recordComponentInternal) {
        return recordComponentPool.intern(recordComponentInternal);
    }

    int positionOf(RecordComponentInternal recordComponentInternal) {
        return recordComponentPool.index().positionOf(recordComponentInternal);
    }

    StrongInternPool<String> stringPool() {
        return stringPool;
    }

    StrongInternPool<byte[]> bytePool() {
        return bytePool;
    }

    StrongInternPool<MethodInternal> methodPool() {
        return methodPool;
    }

    StrongInternPool<FieldInternal> fieldPool() {
        return fieldPool;
    }

    StrongInternPool<RecordComponentInternal> recordComponentPool() {
        return recordComponentPool;
    }

    DotName intern(DotName dotName, char delim) {
        String name = dotName.toString(delim);
        DotName old = names.get(name);
        if (old == null) {
            old = dotName;
            names.put(name, dotName);
        }

        return old;
    }
}
