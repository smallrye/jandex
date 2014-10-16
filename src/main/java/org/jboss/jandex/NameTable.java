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
 * @author Jason T. Greene
 */
class NameTable {
    private StrongInternPool<String> stringPool = new StrongInternPool<String>();
    private StrongInternPool<Type> typePool = new StrongInternPool<Type>();
    private StrongInternPool<Type[]> typeListPool = new StrongInternPool<Type[]>();
    private StrongInternPool<byte[]> bPool = new StrongInternPool<byte[]>();
    private StrongInternPool<MethodInternal> mPool = new StrongInternPool<MethodInternal>();
    private StrongInternPool<FieldInternal> fpool = new StrongInternPool<FieldInternal>();
    private Map<String, DotName> names = new HashMap<String, DotName>();

    DotName convertToName(String name) {
        return convertToName(name, '.');
    }

    DotName convertToName(String name, char delim) {
        DotName result = names.get(name);
        if (result != null)
            return result;

        int loc = lastIndexOf(name, delim, '$');
        String local = intern(name.substring(loc + 1));
        DotName prefix = loc < 1 ? null : convertToName(intern(name.substring(0, loc)), delim);
        result = new DotName(prefix, local, true, loc > 0 && name.charAt(loc) == '$');

        names.put(name, result);

        return result;
    }

    private int lastIndexOf(String name, char delim1, char delim2) {
        int pos = name.length();
        while (--pos >= 0) {
            char c = name.charAt(pos);
            if (c == delim1 || c == delim2) {
                break;
            }
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

    Type intern(Type type) {
        return typePool.intern(type);
    }

    Type[] intern(Type[] types) {
        return typeListPool.intern(types);
    }

    byte[] intern(byte[] bytes) {
        return bPool.intern(bytes);
    }

    MethodInternal intern(MethodInternal methodInternal) {
        return mPool.intern(methodInternal);
    }

    FieldInternal intern(FieldInternal fieldInternal) {
        return fpool.intern(fieldInternal);
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
