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
public final class PrimitiveType extends Type {
    public static final PrimitiveType BYTE = new PrimitiveType(new DotName(null, "byte", true, false));
    public static final PrimitiveType CHAR = new PrimitiveType(new DotName(null, "char", true, false));
    public static final PrimitiveType DOUBLE = new PrimitiveType(new DotName(null, "double", true, false));
    public static final PrimitiveType FLOAT = new PrimitiveType(new DotName(null, "float", true, false));
    public static final PrimitiveType INT = new PrimitiveType(new DotName(null, "int", true, false));
    public static final PrimitiveType LONG = new PrimitiveType(new DotName(null, "long", true, false));
    public static final PrimitiveType SHORT = new PrimitiveType(new DotName(null, "short", true, false));
    public static final PrimitiveType BOOLEAN = new PrimitiveType(new DotName(null, "boolean", true, false));

    private static final Map<String, PrimitiveType> reverseMap = new HashMap<String, PrimitiveType>();

    static {
        reverseMap.put("byte", BYTE);
        reverseMap.put("char", CHAR);
        reverseMap.put("double", DOUBLE);
        reverseMap.put("float", FLOAT);
        reverseMap.put("int", INT);
        reverseMap.put("long", LONG);
        reverseMap.put("short", SHORT);
        reverseMap.put("boolean", BOOLEAN);
    }

    private PrimitiveType(DotName name) {
        super(name);
    }

    @Override
    public Kind kind() {
        return Kind.PRIMITIVE;
    }

    char toCode() {
        if (this == BYTE) {
            return 'B';
        } else if (this == CHAR) {
            return 'C';
        } else if (this == DOUBLE) {
            return 'D';
        } else if (this == FLOAT) {
            return 'F';
        } else if (this == INT) {
            return 'I';
        } else if (this == LONG) {
            return 'J';
        } else if (this == SHORT) {
            return 'S';
        }

        // BOOLEAN
        return 'Z';
    }

    static PrimitiveType decode(String name) {
        return reverseMap.get(name);
    }

    static PrimitiveType decode(char c) {
        switch (c) {
            case 'B':
                return PrimitiveType.BYTE;
            case 'C':
                return PrimitiveType.CHAR;
            case 'D':
                return PrimitiveType.DOUBLE;
            case 'F':
                return PrimitiveType.FLOAT;
            case 'I':
                return PrimitiveType.INT;
            case 'J':
                return PrimitiveType.LONG;
            case 'S':
                return PrimitiveType.SHORT;
            case 'Z':
                return PrimitiveType.BOOLEAN;
            default:
                return null;
        }
    }
}
