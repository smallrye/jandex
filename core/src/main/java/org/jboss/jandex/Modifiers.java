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
package org.jboss.jandex;

/**
 *
 * @see java.lang.reflect.Modifier
 */
final class Modifiers {

    // @formatter:off
    static final int BRIDGE     = 0x00000040;

    static final int SYNTHETIC  = 0x00001000;
    static final int ANNOTATION = 0x00002000;
    static final int ENUM       = 0x00004000;
    static final int MANDATED   = 0x00008000;
    // @formatter:on

    static boolean isAnnotation(int mod) {
        return (mod & ANNOTATION) != 0;
    }

    static boolean isEnum(int mod) {
        return (mod & ENUM) != 0;
    }

    static boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }

}
