package org.jboss.jandex;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jason T. Greene
 */
class Utils {
    private static Charset UTF8 = Charset.forName("UTF-8");

    static byte[] toUTF8(String string) {
        return string.getBytes(UTF8);
    }

    static String fromUTF8(byte[] bytes) {
        return new String(bytes, UTF8);
    }

    static <T> List<T> emptyOrWrap(List<T> list) {
        return list.size() == 0 ? Collections.<T>emptyList() : Collections.unmodifiableList(list);
    }

    static <T> Collection<T> emptyOrWrap(Collection<T> list) {
        return list.size() == 0 ? Collections.<T>emptyList() : Collections.unmodifiableCollection(list);
    }

    static <K, V> Map<K, V> emptyOrWrap(Map<K, V> map) {
        return map.size() == 0 ? Collections.<K, V>emptyMap() : Collections.unmodifiableMap(map);
    }
}
