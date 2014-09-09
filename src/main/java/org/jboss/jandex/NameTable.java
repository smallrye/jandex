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

import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jason T. Greene
 */
class NameTable {
    private StrongInternPool<String> stringPool = new StrongInternPool<String>();
    private StrongInternPool<DotName> namePool = new StrongInternPool<DotName>();
    private Map<String, DotName> names = new HashMap<String, DotName>();
    private Map<Slice, Type> types = new HashMap<Slice, Type>();
    private Map<Slice, Type[]> typeLists = new HashMap<Slice, Type[]>();

    static class Slice {
        private final String string;
        private final int start;
        private final int end;

        private Slice(String string, int start, int end) {
            this.string = string;
            this.start = start;
            this.end = end;
        }

        static Slice create(String string, int start, int end) {
            if (start < 0 || start >= end || end < 0 || end > string.length()) {
                throw new IllegalArgumentException();
            }

            return new Slice(string, start, end);
        }

        public int hashCode() {
            int hash = 0;
            int start = this.start;
            int end = this.end;

            for (int i = start; i < end; i++) {
                hash = 31 * hash + string.charAt(i);
            }

            return hash;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Slice)) {
                return false;
            }

            Slice otherSlice = (Slice) other;
            int otherStart = otherSlice.start;
            int otherEnd = otherSlice.end;
            int start = this.start;
            int end = this.end;
            String otherString = otherSlice.string;
            String string = this.string;

            if (otherEnd - otherStart != end - start) {
                return false;
            }

            while (start < end) {
                if (string.charAt(start++) != otherString.charAt(otherStart++)) {
                    return false;
                }
            }

            return true;
        }

        public String toString() {
            return string.substring(start, end);
        }
    }

    Slice createSlice(String string, int start,int  end) {
        return Slice.create(string, start, end);
    }

    Type getType(Slice slice) {
        return types.get(slice);
    }

    Type storeType(Slice slice, Type type) {
        types.put(slice, type);
        return type;
    }

    Type[] getTypeList(Slice slice) {
        return typeLists.get(slice);
    }

    Type[] storeTypeList(Slice slice, Type[] typeList) {
        typeLists.put(slice, typeList);
        return typeList;
    }

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
