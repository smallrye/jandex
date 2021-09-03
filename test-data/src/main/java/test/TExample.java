package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

/**
 * @author Jason T. Greene
 */
public class TExample {
    static class T1 {
        static class T2 {
            class T3<X, Y, Z> {
                class T4 extends T3 {

                }
            }
        }
    }

    class AA {
        class BB {
            class CC {

            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface A {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface B {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface C {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface D {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface E {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface F {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface G {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface H {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface I {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface J {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface K {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface L {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface M {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @interface U {
    }

    class O1 {
        class O2<X, Y> {
            class O3 {
                class Nested<R, S> {

                }
            }
        }
    }

    class S {
    }

    class T {
    }

    class V {
    }

    class Document {
    }

    private T1.T2.T3<String, @Foo Integer, Long>.T4 theField;
    private T1.T2.@Foo T3.T4 theField2;
    private @Foo AA.BB.CC theField3;
    private @Foo int primitive;
    @A
    Map<@B ? extends @C String, @D List<@E Object>> bar1;
    @I
    String @F [] @G [] @H [] bar2;
    @I
    String @F [][] @H [] bar3;
    @M
    O1.@L O2.@K O3.@J Nested bar4;
    @A
    Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>> bar5;
    @H
    O1.@E O2<@F S, @G T>.@D O3.@A Nested<@B U, @C V> bar6;
}
