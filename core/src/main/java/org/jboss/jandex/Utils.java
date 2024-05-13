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

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common utilities
 *
 * @author Jason T. Greene
 */
class Utils {
    static final byte[] INIT_METHOD_NAME = Utils.toUTF8("<init>");

    static byte[] toUTF8(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    static String fromUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static <K, V> Map<K, V[]> unfold(Map<K, List<V>> map, Class<V> listElementType) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<K, V[]> result = new HashMap<>();
        map.forEach((key, value) -> {
            V[] array = (V[]) Array.newInstance(listElementType, value.size());
            result.put(key, value.toArray(array));
        });
        return result;
    }

    static <T> List<T> listOfCapacity(int capacity) {
        return capacity > 0 ? new ArrayList<T>(capacity) : Collections.<T> emptyList();
    }
}
