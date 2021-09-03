package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Jason T. Greene
 */
public class VExample {
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
    @Target(value = {ElementType.TYPE_USE})
    @interface A{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface B{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface C{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface D{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface E{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface F{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface G{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface H{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface I{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface J{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface K{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface L{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE_USE})
    @interface M{}

    abstract class U extends V {}



    class S{}
    class T{}
    abstract class V implements CharSequence {}



    class O1 {
        class O2<X, Y> {

            public Collection<@A ? extends Integer> foo(){return null;}

            class O3 {
                class Nested<@C R extends @H SU, @D SU extends CharSequence> extends O2<R, @A S>{
                    @Override
                    public List<@B ? extends @G Integer> foo() { return null;}
                }
            }
        }
    }

    public void fun() {
        class Fun {

            public void bar1(@A Map<@B ? extends @C String, @D List<@E Object>> bar1) {}
            public void bar2(@I String @F [] @G [] @H [] bar2) {}
            public void bar3(@I String @F [][] @H [] bar3) {}
            public void bar4(@M O1.@L O2.@K O3.@J Nested bar4) {}
            public void bar5(@A Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>> bar5) {}
            public void bar6(@H O1.@E O2 < @F S, @G T >.@D O3.@A Nested < @B U, @C V > bar6){}

            public @A Map<@B ? extends @C String, @D List<@E Object>> foo1(){return null;}
            public @I String @F [] @G [] @H [] foo2(){return null;}
            public @I String @F [][] @H [] foo3(){return null;}
            public @M O1.@L O2.@K O3.@J Nested foo4(){return null;}
            public @A Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>>foo5(){return null;}
            public @H O1.@E O2 < @F S, @G T >.@D O3.@A Nested < @B U, @C V > foo6(){return null;}


            public void receiverTest(@A Fun this) {

            }


        }

    }

    class Document {}

    public void bar1(@A Map<@B ? extends @C String, @D List<@E Object>> bar1) {}
    public void bar2(@I String @F [] @G [] @H [] bar2) {}
    public void bar3(@I String @F [][] @H [] bar3) {}
    public void bar4(@M O1.@L O2.@K O3.@J Nested bar4) {}
    public void bar5(@A Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>> bar5) {}
    public void bar6(@H O1.@E O2 < @F S, @G T >.@D O3.@A Nested < @B U, @C V > bar6){}

    public @A Map<@B ? extends @C String, @D List<@E Object>> foo1(){return null;}
    public @I String @F [] @G [] @H [] foo2(){return null;}
    public @I String @F [][] @H [] foo3(){return null;}
    public @M O1.@L O2.@K O3.@J Nested foo4(){return null;}
    public @A Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>>foo5(){return null;}
    public @H O1.@E O2 < @F S, @G T >.@D O3.@A Nested < @B U, @C V > foo6(){return null;}


    public void receiverTest(@A VExample this) {

    }

}
