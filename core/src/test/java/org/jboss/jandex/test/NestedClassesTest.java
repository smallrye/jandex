package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class NestedClassesTest {
    static class A {
        static class B {
            static class C {
            }
        }

        static class D {
        }

        static Class<?>[] localClass() {
            class E {
                class F {
                }
            }

            return new Class<?>[] { E.class, E.F.class };
        }

        static Class<?>[] anonymousClass() {
            return new Object() {
                class G {
                }

                Class<?>[] get() {
                    return new Class<?>[] { this.getClass(), G.class };
                }
            }.get();
        }

        interface H {
            class I {
            }
        }

        enum J {
            ;
            static class K {
            }
        }

        @interface L {
            class M {
            }
        }

        static Class<?> localClassInStaticInitializer;
        static Class<?> anonymousClassInStaticInitializer;

        static {
            class N {
            }
            localClassInStaticInitializer = N.class;

            anonymousClassInStaticInitializer = new Object() {
            }.getClass();
        }

        static Class<?> anonymousClassInFieldInitializer = new Object() {
        }.getClass();
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(A.class, A.B.class, A.B.C.class, A.D.class,
                A.localClass()[0], A.localClass()[1], A.anonymousClass()[0], A.anonymousClass()[1],
                A.H.class, A.H.I.class, A.J.class, A.J.K.class, A.L.class, A.L.M.class,
                A.localClassInStaticInitializer, A.anonymousClassInStaticInitializer,
                A.anonymousClassInFieldInitializer);
        test(index);

        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        checkNestingType(index, A.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.class, "B", "D", "H", "J", "L");
        checkEnclosingClass(index, A.class, "NestedClassesTest");
        checkEnclosingMethod(index, A.class, null, null);
        checkEnclosingClassAlways(index, A.class, "NestedClassesTest");

        checkNestingType(index, A.B.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.B.class, "C");
        checkEnclosingClass(index, A.B.class, "A");
        checkEnclosingMethod(index, A.B.class, null, null);
        checkEnclosingClassAlways(index, A.B.class, "A");

        checkNestingType(index, A.B.C.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.B.C.class); // empty
        checkEnclosingClass(index, A.B.C.class, "B");
        checkEnclosingMethod(index, A.B.C.class, null, null);
        checkEnclosingClassAlways(index, A.B.C.class, "B");

        checkNestingType(index, A.D.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.D.class); // empty
        checkEnclosingClass(index, A.D.class, "A");
        checkEnclosingMethod(index, A.D.class, null, null);
        checkEnclosingClassAlways(index, A.D.class, "A");

        checkNestingType(index, A.localClass()[0], ClassInfo.NestingType.LOCAL);
        checkMemberClasses(index, A.localClass()[0], "F");
        checkEnclosingClass(index, A.localClass()[0], null);
        checkEnclosingMethod(index, A.localClass()[0], "localClass", "A");
        checkEnclosingClassAlways(index, A.localClass()[0], "A");

        checkNestingType(index, A.localClass()[1], ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.localClass()[1]); // empty
        checkEnclosingClass(index, A.localClass()[1], "not-null");
        checkEnclosingMethod(index, A.localClass()[1], null, null);
        checkEnclosingClassAlways(index, A.localClass()[1], "not-null");

        checkNestingType(index, A.anonymousClass()[0], ClassInfo.NestingType.ANONYMOUS);
        checkMemberClasses(index, A.anonymousClass()[0], "G");
        checkEnclosingClass(index, A.anonymousClass()[0], null);
        checkEnclosingMethod(index, A.anonymousClass()[0], "anonymousClass", "A");
        checkEnclosingClassAlways(index, A.anonymousClass()[0], "A");

        checkNestingType(index, A.anonymousClass()[1], ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.anonymousClass()[1]); // empty
        checkEnclosingClass(index, A.anonymousClass()[1], "not-null");
        checkEnclosingMethod(index, A.anonymousClass()[1], null, null);
        checkEnclosingClassAlways(index, A.anonymousClass()[1], "not-null");

        checkNestingType(index, A.H.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.H.class, "I");
        checkEnclosingClass(index, A.H.class, "A");
        checkEnclosingMethod(index, A.H.class, null, null);
        checkEnclosingClassAlways(index, A.H.class, "A");

        checkNestingType(index, A.H.I.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.H.I.class); // empty
        checkEnclosingClass(index, A.H.I.class, "H");
        checkEnclosingMethod(index, A.H.I.class, null, null);
        checkEnclosingClassAlways(index, A.H.I.class, "H");

        checkNestingType(index, A.J.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.J.class, "K");
        checkEnclosingClass(index, A.J.class, "A");
        checkEnclosingMethod(index, A.J.class, null, null);
        checkEnclosingClassAlways(index, A.J.class, "A");

        checkNestingType(index, A.J.K.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.J.K.class); // empty
        checkEnclosingClass(index, A.J.K.class, "J");
        checkEnclosingMethod(index, A.J.K.class, null, null);
        checkEnclosingClassAlways(index, A.J.K.class, "J");

        checkNestingType(index, A.L.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.L.class, "M");
        checkEnclosingClass(index, A.L.class, "A");
        checkEnclosingMethod(index, A.L.class, null, null);
        checkEnclosingClassAlways(index, A.L.class, "A");

        checkNestingType(index, A.L.M.class, ClassInfo.NestingType.INNER);
        checkMemberClasses(index, A.L.M.class); // empty
        checkEnclosingClass(index, A.L.M.class, "L");
        checkEnclosingMethod(index, A.L.M.class, null, null);
        checkEnclosingClassAlways(index, A.L.M.class, "L");

        checkNestingType(index, A.localClassInStaticInitializer, ClassInfo.NestingType.LOCAL);
        checkMemberClasses(index, A.localClassInStaticInitializer); // empty
        checkEnclosingClass(index, A.localClassInStaticInitializer, null);
        checkEnclosingMethod(index, A.localClassInStaticInitializer, null, null);
        checkEnclosingClassAlways(index, A.localClassInStaticInitializer, "A");

        checkNestingType(index, A.anonymousClassInStaticInitializer, ClassInfo.NestingType.ANONYMOUS);
        checkMemberClasses(index, A.anonymousClassInStaticInitializer); // empty
        checkEnclosingClass(index, A.anonymousClassInStaticInitializer, null);
        checkEnclosingMethod(index, A.anonymousClassInStaticInitializer, null, null);
        checkEnclosingClassAlways(index, A.anonymousClassInStaticInitializer, "A");

        checkNestingType(index, A.anonymousClassInFieldInitializer, ClassInfo.NestingType.ANONYMOUS);
        checkMemberClasses(index, A.anonymousClassInFieldInitializer); // empty
        checkEnclosingClass(index, A.anonymousClassInFieldInitializer, null);
        checkEnclosingMethod(index, A.anonymousClassInFieldInitializer, null, null);
        checkEnclosingClassAlways(index, A.anonymousClassInFieldInitializer, "A");
    }

    private void checkNestingType(Index index, Class<?> clazz, ClassInfo.NestingType expectedNestingType) {
        ClassInfo classInfo = index.getClassByName(DotName.createSimple(clazz.getName()));
        assertEquals(expectedNestingType, classInfo.nestingType());
    }

    private void checkMemberClasses(Index index, Class<?> clazz, String... expectedMemberClasses) {
        Set<DotName> foundMemberClasses = index.getClassByName(DotName.createSimple(clazz.getName())).memberClasses();
        Set<String> names = new HashSet<>();
        for (DotName foundMemberClass : foundMemberClasses) {
            names.add(foundMemberClass.local());
        }
        assertEquals(new HashSet<>(Arrays.asList(expectedMemberClasses)), names);
    }

    private void checkEnclosingClass(Index index, Class<?> clazz, String expectedEnclosingClassName) {
        doCheckEnclosingClass(index, clazz, ClassInfo::enclosingClass, expectedEnclosingClassName);
    }

    private void checkEnclosingClassAlways(Index index, Class<?> clazz, String expectedEnclosingClassName) {
        doCheckEnclosingClass(index, clazz, ClassInfo::enclosingClassAlways, expectedEnclosingClassName);
    }

    private void doCheckEnclosingClass(Index index, Class<?> clazz, Function<ClassInfo, DotName> enclosingClassGetter,
            String expectedEnclosingClassName) {
        ClassInfo classInfo = index.getClassByName(DotName.createSimple(clazz.getName()));
        DotName enclosingClass = enclosingClassGetter.apply(classInfo);

        if (expectedEnclosingClassName == null) {
            assertNull(enclosingClass);
        } else {
            assertNotNull(enclosingClass);
            if (!"not-null".equals(expectedEnclosingClassName)) {
                assertEquals(expectedEnclosingClassName, enclosingClass.local());
            }
        }
    }

    private void checkEnclosingMethod(Index index, Class<?> clazz, String expectedEnclosingMethodName,
            String expectedEnclosingClassName) {
        ClassInfo classInfo = index.getClassByName(DotName.createSimple(clazz.getName()));
        ClassInfo.EnclosingMethodInfo enclosingMethod = classInfo.enclosingMethod();

        if (expectedEnclosingMethodName == null) {
            assertNull(enclosingMethod);
        } else {
            assertNotNull(enclosingMethod);
            assertEquals(expectedEnclosingMethodName, enclosingMethod.name());
            assertNotNull(enclosingMethod.enclosingClass());
            assertEquals(expectedEnclosingClassName, enclosingMethod.enclosingClass().local());
        }
    }
}
