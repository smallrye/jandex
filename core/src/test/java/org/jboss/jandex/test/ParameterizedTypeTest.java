package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

public class ParameterizedTypeTest {

    @SuppressWarnings("serial")
    public static class TypeVariableList<T> extends ArrayList<T> {
    }

    ArrayList<@MyAnnotation("lll") String> lll;
    List<Map<String, ? extends Serializable>> mmm;

    @Test
    public void testParameterizedTypeBuilder() throws IOException {
        Index index = Index.of(ParameterizedTypeTest.class, TypeVariableList.class);
        ClassInfo testClass = index.getClassByName(ParameterizedTypeTest.class);

        assertEquals(ParameterizedType.create(List.class, ClassType.create(String.class)),
                ParameterizedType.builder(List.class).addArgument(String.class).build());

        Type lllType = testClass.field("lll").type();
        assertEquals(lllType, ParameterizedType.builder(ArrayList.class)
                .addArgument(ClassType.builder(String.class)
                        .addAnnotation(AnnotationInstance.builder(MyAnnotation.class).value("lll").build())
                        .build())
                .build());

        Type mmmType = testClass.field("mmm").type();
        assertEquals(mmmType, ParameterizedType.builder(List.class).addArgument(ParameterizedType.builder(Map.class)
                .addArgument(String.class)
                .addArgument(WildcardType.builder().setUpperBound(Serializable.class).build())
                .build())
                .build());
        assertEquals(mmmType, ParameterizedType.create(List.class, ParameterizedType.create(Map.class,
                ClassType.create(String.class), WildcardType.create(ClassType.create(Serializable.class), true))));

        ClassInfo typeVariableList = index.getClassByName(TypeVariableList.class);
        assertNotNull(typeVariableList);
        Type typeVariableListType = ParameterizedType.builder(ArrayList.class)
                .addArgument(TypeVariable.create("T")).build();
        assertEquals(typeVariableList.superClassType(), typeVariableListType);
    }

}
