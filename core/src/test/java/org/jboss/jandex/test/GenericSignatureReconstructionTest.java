package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.GenericSignature;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.data.ClassInheritance;
import org.jboss.jandex.test.data.OuterParam;
import org.jboss.jandex.test.data.OuterParamBound;
import org.jboss.jandex.test.data.OuterRaw;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GenericSignatureReconstructionTest {
    private static IndexView index;

    @BeforeAll
    public static void setUp() {
        try {
            index = Index.of(
                    OuterRaw.class,
                    OuterRaw.InnerRaw.class, OuterRaw.InnerParam.class, OuterRaw.InnerParamBound.class,
                    OuterRaw.NestedRaw.class, OuterRaw.NestedParam.class, OuterRaw.NestedParamBound.class,
                    OuterParam.class,
                    OuterParam.InnerRaw.class, OuterParam.InnerParam.class, OuterParam.InnerParamBound.class,
                    OuterParam.NestedRaw.class, OuterParam.NestedParam.class, OuterParam.NestedParamBound.class,
                    OuterParamBound.class,
                    OuterParamBound.InnerRaw.class, OuterParamBound.InnerParam.class, OuterParamBound.InnerParamBound.class,
                    OuterParamBound.NestedRaw.class, OuterParamBound.NestedParam.class, OuterParamBound.NestedParamBound.class,
                    ClassInheritance.class,
                    ClassInheritance.PlainClass.class,
                    ClassInheritance.ParamClass.class,
                    ClassInheritance.ParamBoundClass.class,
                    ClassInheritance.PlainInterface.class,
                    ClassInheritance.ParamInterface.class,
                    ClassInheritance.ParamBoundInterface.class,
                    ClassInheritance.PlainClassExtendsPlainClass.class,
                    ClassInheritance.PlainClassExtendsParamClass.class,
                    ClassInheritance.PlainClassExtendsParamBoundClass.class,
                    ClassInheritance.ParamClassExtendsPlainClass.class,
                    ClassInheritance.ParamClassExtendsParamClass1.class,
                    ClassInheritance.ParamClassExtendsParamClass2.class,
                    ClassInheritance.ParamClassExtendsParamBoundClass.class,
                    ClassInheritance.ParamBoundClassExtendsPlainClass.class,
                    ClassInheritance.ParamBoundClassExtendsParamClass1.class,
                    ClassInheritance.ParamBoundClassExtendsParamClass2.class,
                    ClassInheritance.ParamBoundClassExtendsParamBoundClass1.class,
                    ClassInheritance.ParamBoundClassExtendsParamBoundClass2.class,
                    ClassInheritance.PlainClassImplementsPlainInterface.class,
                    ClassInheritance.PlainClassImplementsParamInterface.class,
                    ClassInheritance.PlainClassImplementsParamBoundInterface.class,
                    ClassInheritance.ParamClassImplementsPlainInterface.class,
                    ClassInheritance.ParamClassImplementsParamInterface1.class,
                    ClassInheritance.ParamClassImplementsParamInterface2.class,
                    ClassInheritance.ParamClassImplementsParamBoundInterface.class,
                    ClassInheritance.ParamBoundClassImplementsPlainInterface.class,
                    ClassInheritance.ParamBoundClassImplementsParamInterface1.class,
                    ClassInheritance.ParamBoundClassImplementsParamInterface2.class,
                    ClassInheritance.ParamBoundClassImplementsParamBoundInterface1.class,
                    ClassInheritance.ParamBoundClassImplementsParamBoundInterface2.class,
                    ClassInheritance.PlainClassExtendsPlainClassImplementsPlainInterface.class,
                    ClassInheritance.PlainClassExtendsParamClassImplementsParamInterface.class,
                    ClassInheritance.PlainClassExtendsParamBoundClassImplementsParamBoundInterface.class,
                    ClassInheritance.ParamClassExtendsPlainClassImplementsPlainInterface.class,
                    ClassInheritance.ParamClassExtendsParamClassImplementsParamInterface1.class,
                    ClassInheritance.ParamClassExtendsParamClassImplementsParamInterface2.class,
                    ClassInheritance.ParamClassExtendsParamBoundClassImplementsParamBoundInterface.class,
                    ClassInheritance.ParamBoundClassExtendsPlainClassImplementsPlainInterface.class,
                    ClassInheritance.ParamBoundClassExtendsParamClassImplementsParamInterface1.class,
                    ClassInheritance.ParamBoundClassExtendsParamClassImplementsParamInterface2.class,
                    ClassInheritance.ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface1.class,
                    ClassInheritance.ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface2.class,
                    ClassInheritance.OuterParam.class,
                    ClassInheritance.OuterParam.NestedParam.class,
                    ClassInheritance.OuterParam.InnerParam.class,
                    ClassInheritance.OuterParam.InnerParam.InnerInnerRaw.class,
                    ClassInheritance.OuterParam.InnerParam.InnerInnerRaw.InnerInnerInnerParam.class,
                    ClassInheritance.OuterParam.InnerParam.InnerInnerRaw.InnerInnerInnerParam.Test.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // expected signatures here were obtained from classes compiled with OpenJDK 11.0.17
    // and then dumped using `javap -v`

    @Test
    public void outerRaw() {
        assertClassSignature(OuterRaw.class,
                null);
        assertClassSignature(OuterRaw.NestedRaw.class,
                null);
        assertClassSignature(OuterRaw.NestedParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterRaw.NestedParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");
        assertClassSignature(OuterRaw.InnerRaw.class,
                null);
        assertClassSignature(OuterRaw.InnerParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterRaw.InnerParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");

        assertMethodSignature(OuterRaw.class, "methodA",
                null);
        assertMethodSignature(OuterRaw.class, "methodB",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(TU;Lorg/jboss/jandex/test/data/OuterRaw;)TT;");
        assertMethodSignature(OuterRaw.NestedRaw.class, "methodC",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;Lorg/jboss/jandex/test/data/OuterRaw$NestedRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterRaw.NestedParam.class, "methodD",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;Lorg/jboss/jandex/test/data/OuterRaw$NestedParam<TX;>;)TT;");
        assertMethodSignature(OuterRaw.NestedParamBound.class, "methodE",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;Lorg/jboss/jandex/test/data/OuterRaw$NestedParamBound<TX;>;)TT;^TV;");
        assertMethodSignature(OuterRaw.InnerRaw.class, "methodF",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;Lorg/jboss/jandex/test/data/OuterRaw$InnerRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterRaw.InnerParam.class, "methodG",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;Lorg/jboss/jandex/test/data/OuterRaw$InnerParam<TX;>;)TT;");
        assertMethodSignature(OuterRaw.InnerParamBound.class, "methodH",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;Lorg/jboss/jandex/test/data/OuterRaw$InnerParamBound<TX;>;)TT;^TV;");

        assertFieldSignature(OuterRaw.class, "fieldA",
                null);
        assertFieldSignature(OuterRaw.class, "fieldB",
                "Ljava/util/List<Ljava/lang/String;>;");
        assertFieldSignature(OuterRaw.class, "fieldC",
                "Ljava/util/List<*>;");
        assertFieldSignature(OuterRaw.class, "fieldD",
                "Ljava/util/List<+Ljava/lang/CharSequence;>;");
        assertFieldSignature(OuterRaw.class, "fieldE",
                "Ljava/util/List<-Ljava/lang/String;>;");
        assertFieldSignature(OuterRaw.NestedParam.class, "fieldF",
                "TX;");
        assertFieldSignature(OuterRaw.NestedParam.class, "fieldG",
                "Lorg/jboss/jandex/test/data/OuterRaw$NestedParam<TX;>;");
        assertFieldSignature(OuterRaw.NestedParamBound.class, "fieldH",
                "Ljava/util/List<TX;>;");
        assertFieldSignature(OuterRaw.NestedParamBound.class, "fieldI",
                "Lorg/jboss/jandex/test/data/OuterRaw$NestedParamBound<Ljava/lang/Integer;>;");
        assertFieldSignature(OuterRaw.InnerParam.class, "fieldJ",
                "TX;");
        assertFieldSignature(OuterRaw.InnerParam.class, "fieldK",
                "Lorg/jboss/jandex/test/data/OuterRaw$InnerParam<Ljava/lang/Integer;>;");
        assertFieldSignature(OuterRaw.InnerParamBound.class, "fieldL",
                "Ljava/util/List<TX;>;");
        assertFieldSignature(OuterRaw.InnerParamBound.class, "fieldM",
                "Lorg/jboss/jandex/test/data/OuterRaw$InnerParamBound<TX;>;");
    }

    @Test
    public void outerParam() {
        assertClassSignature(OuterParam.class,
                "<W:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterParam.NestedRaw.class,
                null);
        assertClassSignature(OuterParam.NestedParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterParam.NestedParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");
        assertClassSignature(OuterParam.InnerRaw.class,
                null);
        assertClassSignature(OuterParam.InnerParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterParam.InnerParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");

        assertMethodSignature(OuterParam.class, "methodA",
                "(Ljava/lang/String;TW;Lorg/jboss/jandex/test/data/OuterParam<TW;>;)Ljava/lang/String;");
        assertMethodSignature(OuterParam.class, "methodB",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(TU;TW;Lorg/jboss/jandex/test/data/OuterParam<TW;>;)TT;");
        assertMethodSignature(OuterParam.NestedRaw.class, "methodC",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;Lorg/jboss/jandex/test/data/OuterParam$NestedRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterParam.NestedParam.class, "methodD",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;Lorg/jboss/jandex/test/data/OuterParam$NestedParam<TX;>;)TT;");
        assertMethodSignature(OuterParam.NestedParamBound.class, "methodE",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;Lorg/jboss/jandex/test/data/OuterParam$NestedParamBound<TX;>;)TT;^TV;");
        assertMethodSignature(OuterParam.InnerRaw.class, "methodF",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;TW;Lorg/jboss/jandex/test/data/OuterParam<TW;>.InnerRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterParam.InnerParam.class, "methodG",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;TW;Lorg/jboss/jandex/test/data/OuterParam<TW;>.InnerParam<TX;>;)TT;");
        assertMethodSignature(OuterParam.InnerParamBound.class, "methodH",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;TW;Lorg/jboss/jandex/test/data/OuterParam<TW;>.InnerParamBound<TX;>;)TT;^TV;");

        assertFieldSignature(OuterParam.class, "fieldA",
                "TW;");
        assertFieldSignature(OuterParam.class, "fieldB",
                "Ljava/util/List<TW;>;");
        assertFieldSignature(OuterParam.class, "fieldC",
                "Ljava/util/List<+TW;>;");
        assertFieldSignature(OuterParam.class, "fieldD",
                "Ljava/util/List<-TW;>;");
        assertFieldSignature(OuterParam.InnerRaw.class, "fieldE",
                "Ljava/util/List<Lorg/jboss/jandex/test/data/OuterParam<+TW;>.InnerRaw;>;");
        assertFieldSignature(OuterParam.InnerParam.class, "fieldF",
                "Ljava/util/List<Lorg/jboss/jandex/test/data/OuterParam<-TW;>.InnerParam<*>;>;");
        assertFieldSignature(OuterParam.InnerParamBound.class, "fieldG",
                "Ljava/util/Map<+TX;TW;>;");
        assertFieldSignature(OuterParam.InnerParamBound.class, "fieldH",
                "Ljava/util/Map<-TX;Lorg/jboss/jandex/test/data/OuterParam<Ljava/lang/String;>.InnerParamBound<Ljava/lang/Integer;>;>;");
    }

    @Test
    public void outerParamBound() {
        assertClassSignature(OuterParamBound.class,
                "<W:Ljava/lang/Number;:Ljava/lang/Comparable<TW;>;>Ljava/lang/Object;");
        assertClassSignature(OuterParamBound.NestedRaw.class,
                null);
        assertClassSignature(OuterParamBound.NestedParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterParamBound.NestedParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");
        assertClassSignature(OuterParamBound.InnerRaw.class,
                null);
        assertClassSignature(OuterParamBound.InnerParam.class,
                "<X:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(OuterParamBound.InnerParamBound.class,
                "<X:Ljava/lang/Number;:Ljava/lang/Comparable<TX;>;>Ljava/lang/Object;");

        assertMethodSignature(OuterParamBound.class, "methodA",
                "(Ljava/lang/String;TW;Lorg/jboss/jandex/test/data/OuterParamBound<TW;>;)Ljava/lang/String;");
        assertMethodSignature(OuterParamBound.class, "methodB",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(TU;TW;Lorg/jboss/jandex/test/data/OuterParamBound<TW;>;)TT;");
        assertMethodSignature(OuterParamBound.NestedRaw.class, "methodC",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;Lorg/jboss/jandex/test/data/OuterParamBound$NestedRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterParamBound.NestedParam.class, "methodD",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;Lorg/jboss/jandex/test/data/OuterParamBound$NestedParam<TX;>;)TT;");
        assertMethodSignature(OuterParamBound.NestedParamBound.class, "methodE",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;Lorg/jboss/jandex/test/data/OuterParamBound$NestedParamBound<TX;>;)TT;^TV;");
        assertMethodSignature(OuterParamBound.InnerRaw.class, "methodF",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;TW;Lorg/jboss/jandex/test/data/OuterParamBound<TW;>.InnerRaw;)TT;^Ljava/lang/IllegalArgumentException;^TV;");
        assertMethodSignature(OuterParamBound.InnerParam.class, "methodG",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<-TU;>;TX;TW;Lorg/jboss/jandex/test/data/OuterParamBound<TW;>.InnerParam<TX;>;)TT;");
        assertMethodSignature(OuterParamBound.InnerParamBound.class, "methodH",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<*>;TX;TW;Lorg/jboss/jandex/test/data/OuterParamBound<TW;>.InnerParamBound<TX;>;)TT;^TV;");

        assertFieldSignature(OuterParamBound.class, "fieldA",
                "TW;");
        assertFieldSignature(OuterParamBound.class, "fieldB",
                "Ljava/util/List<TW;>;");
        assertFieldSignature(OuterParamBound.class, "fieldC",
                "Ljava/util/List<+TW;>;");
        assertFieldSignature(OuterParamBound.class, "fieldD",
                "Ljava/util/List<-TW;>;");
        assertFieldSignature(OuterParamBound.InnerRaw.class, "fieldE",
                "Ljava/util/List<Lorg/jboss/jandex/test/data/OuterParamBound<+Ljava/lang/Integer;>.InnerParam<-Ljava/lang/String;>;>;");
        assertFieldSignature(OuterParamBound.InnerParam.class, "fieldF",
                "Ljava/util/List<Lorg/jboss/jandex/test/data/OuterParamBound<Ljava/lang/Integer;>.InnerParam<+TW;>;>;");
        assertFieldSignature(OuterParamBound.InnerParam.class, "fieldG",
                "Ljava/util/Map<TX;-TW;>;");
        assertFieldSignature(OuterParamBound.InnerParamBound.class, "fieldH",
                "Ljava/util/List<Lorg/jboss/jandex/test/data/OuterParamBound<TX;>.InnerParamBound<TW;>;>;");
        assertFieldSignature(OuterParamBound.InnerParamBound.class, "fieldI",
                "Ljava/util/Map<+TX;TW;>;");
    }

    @Test
    public void classInheritance() {
        assertClassSignature(ClassInheritance.PlainClass.class,
                null);
        assertClassSignature(ClassInheritance.ParamClass.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(ClassInheritance.ParamBoundClass.class,
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Ljava/lang/Object;");
        assertClassSignature(ClassInheritance.PlainInterface.class,
                null);
        assertClassSignature(ClassInheritance.ParamInterface.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;");
        assertClassSignature(ClassInheritance.ParamBoundInterface.class,
                "<T::Ljava/lang/Comparable<Ljava/lang/Integer;>;:Ljava/io/Serializable;>Ljava/lang/Object;");
        assertClassSignature(ClassInheritance.PlainClassExtendsPlainClass.class,
                null);
        assertClassSignature(ClassInheritance.PlainClassExtendsParamClass.class,
                "Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.PlainClassExtendsParamBoundClass.class,
                "Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsPlainClass.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$PlainClass;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamClass1.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamClass2.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<TT;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamBoundClass.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsPlainClass.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$PlainClass;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamClass1.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamClass2.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<TT;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamBoundClass1.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamBoundClass2.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<TT;>;");
        assertClassSignature(ClassInheritance.PlainClassImplementsPlainInterface.class,
                null);
        assertClassSignature(ClassInheritance.PlainClassImplementsParamInterface.class,
                "Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.PlainClassImplementsParamBoundInterface.class,
                "Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamClassImplementsPlainInterface.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$PlainInterface;");
        assertClassSignature(ClassInheritance.ParamClassImplementsParamInterface1.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamClassImplementsParamInterface2.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<TT;>;");
        assertClassSignature(ClassInheritance.ParamClassImplementsParamBoundInterface.class,
                "<T:Ljava/lang/Object;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassImplementsPlainInterface.class,
                "<T:Ljava/lang/Integer;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$PlainInterface;");
        assertClassSignature(ClassInheritance.ParamBoundClassImplementsParamInterface1.class,
                "<T:Ljava/lang/Integer;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassImplementsParamInterface2.class,
                "<T:Ljava/lang/Integer;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<TT;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassImplementsParamBoundInterface1.class,
                "<T:Ljava/lang/Integer;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassImplementsParamBoundInterface2.class,
                "<T:Ljava/lang/Integer;>Ljava/lang/Object;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<TT;>;");
        assertClassSignature(ClassInheritance.PlainClassExtendsPlainClassImplementsPlainInterface.class,
                null);
        assertClassSignature(ClassInheritance.PlainClassExtendsParamClassImplementsParamInterface.class,
                "Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.PlainClassExtendsParamBoundClassImplementsParamBoundInterface.class,
                "Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsPlainClassImplementsPlainInterface.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$PlainClass;Lorg/jboss/jandex/test/data/ClassInheritance$PlainInterface;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamClassImplementsParamInterface1.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamClassImplementsParamInterface2.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<TT;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<TT;>;");
        assertClassSignature(ClassInheritance.ParamClassExtendsParamBoundClassImplementsParamBoundInterface.class,
                "<T:Ljava/lang/Object;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsPlainClassImplementsPlainInterface.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$PlainClass;Lorg/jboss/jandex/test/data/ClassInheritance$PlainInterface;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamClassImplementsParamInterface1.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamClassImplementsParamInterface2.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<TT;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<TT;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface1.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<Ljava/lang/Integer;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<Ljava/lang/Integer;>;");
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface2.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundClass<TT;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamBoundInterface<TT;>;");

        assertClassSignature(ClassInheritance.OuterParam.InnerParam.InnerInnerRaw.InnerInnerInnerParam.Test.class,
                "<X:Ljava/lang/String;Y:Ljava/lang/Integer;>Lorg/jboss/jandex/test/data/ClassInheritance$OuterParam<TX;>.InnerParam<TY;>.InnerInnerRaw.InnerInnerInnerParam<Ljava/lang/String;>;Lorg/jboss/jandex/test/data/ClassInheritance$OuterParam$NestedParam<TV;>;");
    }

    @Test
    public void withSubstitution() {
        assertClassSignature(OuterParamBound.class,
                "<W:Ljava/lang/Number;:Ljava/lang/Comparable<Ljava/lang/String;>;>Ljava/lang/Object;",
                id -> "W".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertClassSignature(ClassInheritance.ParamBoundInterface.class,
                "<T::Ljava/lang/Comparable<Ljava/lang/Integer;>;:Ljava/io/Serializable;>Ljava/lang/Object;",
                id -> "T".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertClassSignature(ClassInheritance.ParamBoundClassExtendsParamClassImplementsParamInterface2.class,
                "<T:Ljava/lang/Integer;:Ljava/lang/Comparable<Ljava/lang/Integer;>;>Lorg/jboss/jandex/test/data/ClassInheritance$ParamClass<Ljava/lang/String;>;Lorg/jboss/jandex/test/data/ClassInheritance$ParamInterface<Ljava/lang/String;>;",
                id -> "T".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);

        assertMethodSignature(OuterRaw.class, "methodB",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<Ljava/lang/String;>;V:Ljava/lang/Exception;>(Ljava/lang/String;Lorg/jboss/jandex/test/data/OuterRaw;)TT;",
                id -> "U".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertMethodSignature(OuterRaw.class, "methodB",
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<Ljava/lang/String;>;U::Ljava/lang/Comparable<Ljava/lang/String;>;V:Ljava/lang/Exception;>(Ljava/lang/String;Lorg/jboss/jandex/test/data/OuterRaw;)Ljava/lang/String;",
                id -> "T".equals(id) || "U".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);

        assertFieldSignature(OuterRaw.InnerParamBound.class, "fieldL",
                "Ljava/util/List<Ljava/lang/String;>;",
                id -> "X".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertFieldSignature(OuterRaw.InnerParamBound.class, "fieldM",
                "Lorg/jboss/jandex/test/data/OuterRaw$InnerParamBound<Ljava/lang/String;>;",
                id -> "X".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
    }

    private static void assertClassSignature(Class<?> clazz, String expectedSignature) {
        assertClassSignature(clazz, expectedSignature, GenericSignature.NO_SUBSTITUTION);
    }

    private static void assertClassSignature(Class<?> clazz, String expectedSignature,
            Function<String, Type> subst) {
        ClassInfo classInfo = index.getClassByName(clazz);
        if (classInfo != null) {
            String actualSignature = classInfo.genericSignatureIfRequired(subst);
            assertEquals(expectedSignature, actualSignature);
            return;
        }

        fail("Couldn't find class " + clazz.getName() + " in test index");
    }

    private static void assertMethodSignature(Class<?> clazz, String method, String expectedSignature) {
        assertMethodSignature(clazz, method, expectedSignature, GenericSignature.NO_SUBSTITUTION);
    }

    private static void assertMethodSignature(Class<?> clazz, String method, String expectedSignature,
            Function<String, Type> subst) {
        ClassInfo classInfo = index.getClassByName(clazz);
        if (classInfo != null) {
            MethodInfo methodInfo = classInfo.firstMethod(method);
            if (methodInfo != null) {
                String actualSignature = methodInfo.genericSignatureIfRequired(subst);
                assertEquals(expectedSignature, actualSignature);
                return;
            }
        }

        fail("Couldn't find method " + clazz.getName() + "#" + method + " in test index");
    }

    private static void assertFieldSignature(Class<?> clazz, String field, String expectedSignature) {
        assertFieldSignature(clazz, field, expectedSignature, GenericSignature.NO_SUBSTITUTION);
    }

    private static void assertFieldSignature(Class<?> clazz, String field, String expectedSignature,
            Function<String, Type> subst) {
        ClassInfo classInfo = index.getClassByName(clazz);
        if (classInfo != null) {
            FieldInfo fieldInfo = classInfo.field(field);
            if (fieldInfo != null) {
                String actualSignature = fieldInfo.genericSignatureIfRequired(subst);
                assertEquals(expectedSignature, actualSignature);
                return;
            }
        }

        fail("Couldn't find field " + clazz.getName() + "#" + field + " in test index");
    }
}
