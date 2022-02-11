/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.UUID;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.junit.jupiter.api.Test;

public class AnnotationValuesTestCase {

    @Test
    public void testEquals() throws IOException {
        String name1 = "foo";
        String name2 = "bar";
        AnnotationValue diff = AnnotationValue.createStringValue(name1, UUID.randomUUID().toString());

        // String
        assertValues(AnnotationValue.createStringValue(name1, "1"), AnnotationValue.createStringValue(name1, "2"),
                AnnotationValue.createStringValue(name2, "1"));
        // Byte
        assertValues(AnnotationValue.createByteValue(name1, (byte) 1), AnnotationValue.createByteValue(name1, (byte) 2),
                AnnotationValue.createByteValue(name2, (byte) 3), diff);
        // Character
        assertValues(AnnotationValue.createCharacterValue(name1, '1'), AnnotationValue.createCharacterValue(name1, '2'),
                AnnotationValue.createCharacterValue(name2, '1'));
        // Double
        assertValues(AnnotationValue.createDoubleValue(name1, 1), AnnotationValue.createDoubleValue(name1, 2),
                AnnotationValue.createDoubleValue(name2, 1), diff);
        // Short
        assertValues(AnnotationValue.createShortValue(name1, (short) 1), AnnotationValue.createShortValue(name1, (short) 2),
                AnnotationValue.createShortValue(name2, (short) 1), diff);
        // Float
        assertValues(AnnotationValue.createFloatValue(name1, 1), AnnotationValue.createFloatValue(name1, 2),
                AnnotationValue.createFloatValue(name2, 1), diff);
        // Integer
        assertValues(AnnotationValue.createIntegerValue(name1, 1), AnnotationValue.createIntegerValue(name1, 2),
                AnnotationValue.createIntegerValue(name2, 1), diff);
        // Long
        assertValues(AnnotationValue.createLongValue(name1, 1), AnnotationValue.createLongValue(name1, 2),
                AnnotationValue.createLongValue(name2, 1), diff);
        // Boolean
        assertValues(AnnotationValue.createBooleanValue(name1, true), AnnotationValue.createBooleanValue(name1, false),
                AnnotationValue.createBooleanValue(name2, true), diff);
        // Enum
        DotName typeName = DotName.createSimple("org.acme.Foo");
        assertValues(AnnotationValue.createEnumValue(name1, typeName, "BAR"),
                AnnotationValue.createEnumValue(name1, typeName, "BAZ"),
                AnnotationValue.createEnumValue(name2, typeName, "BAR"), diff);
        // Class
        Type type = Type.create(typeName, Kind.CLASS);
        assertValues(AnnotationValue.createClassValue(name1, type),
                AnnotationValue.createClassValue(name1, Type.create(DotName.createSimple("org.acme.Bar"), Kind.CLASS)),
                AnnotationValue.createClassValue(name2, type), diff);
        // Array
        assertValues(
                AnnotationValue.createArrayValue(name1, new AnnotationValue[] { AnnotationValue.createLongValue(name1, 1) }),
                AnnotationValue.createArrayValue(name1, new AnnotationValue[] { AnnotationValue.createLongValue(name2, 1) }),
                diff);
    }

    void assertValues(AnnotationValue val, AnnotationValue... differentValues) {
        assertEquals(val, val);
        for (AnnotationValue diff : differentValues) {
            assertNotEquals(val, diff, "Values should not be equal: " + val + " and " + diff);
        }
    }

}
