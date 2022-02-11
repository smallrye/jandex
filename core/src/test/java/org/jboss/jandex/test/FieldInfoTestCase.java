package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class FieldInfoTestCase {

    static enum FieldInfoTestEnum {
        VAL1,
        VAL2("value2"),
        VAL3;

        static FieldInfoTestEnum notVAL;

        String text;

        FieldInfoTestEnum() {

        }

        FieldInfoTestEnum(String text) {
            this.text = text;
        }
    }

    @Test
    public void testIsEnumConstant() throws IOException {
        DotName name = DotName.createSimple(FieldInfoTestEnum.class.getName());
        Indexer indexer = new Indexer();
        String path = name.toString().replace('.', '/').concat(".class");
        InputStream classStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
        indexer.index(classStream);
        Index index = indexer.complete();

        ClassInfo enumClass = index.getClassByName(name);
        int constCount = 0;
        for (FieldInfo f : enumClass.fields()) {
            if (f.isEnumConstant()) {
                constCount++;
            }
        }
        assertEquals(3, constCount);
    }

}
