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

package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.Test;

public class TypeAnnotationTestCase {

    @Test
    public void testIndexer() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("TExample.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("VExample$1Fun$O1$O2$O3$Nested.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("VExample$1Fun.class");
        indexer.index(stream);

        Index index = indexer.complete();

        for (FieldInfo field : index.getClassByName(DotName.createSimple("org.wildfly.security.TExample")).fields()) {
            System.out.println(field.type());
        }

        ClassInfo localClazz = index.getClassByName(DotName.createSimple("org.wildfly.security.VExample$1Fun"));
        for (MethodInfo method : localClazz.methods()) {
            Type[] args = method.args();
            if (args.length > 0) {
                System.out.println(args[0]);
            } else if (method.returnType().kind() != Type.Kind.VOID) {
                System.out.println(method.returnType());
            }  else {
                System.out.println(method.receiverType());
            }
        }

        ClassInfo clazz = index.getClassByName(DotName.createSimple("org.wildfly.security.VExample$1Fun$O1$O2$O3$Nested"));

        for (Type type : clazz.typeParameters()) {
            System.out.println(type);
        }
        System.out.println(clazz.superClassType());

        System.out.println(localClazz.enclosingMethod());
        System.out.println(localClazz.nestingType());
        System.out.println(localClazz.enclosingClass());
    }


}
