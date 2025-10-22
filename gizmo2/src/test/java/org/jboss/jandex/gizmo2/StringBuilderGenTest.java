package org.jboss.jandex.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.TestClassMaker;
import io.quarkus.gizmo2.desc.MethodDesc;

public class StringBuilderGenTest {
    @Test
    public void testStringBuilder() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestStringBuilder", cc -> {
            MethodDesc charSeq = cc.staticMethod("createCharSequence", mc -> {
                mc.returning(CharSequence.class);
                mc.body(bc -> {
                    LocalVar strBuilder = bc.localVar("stringBuilder", bc.new_(StringBuilder.class));
                    StringBuilderGen.of(strBuilder, bc).append("ghi");
                    bc.return_(strBuilder);
                });
            });

            cc.staticMethod("createString", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    bc.return_(StringBuilderGen.ofNew(bc)
                            .append(Const.of(true))
                            .append(Const.of((byte) 1))
                            .append(Const.of((short) 2))
                            .append(Const.of(3))
                            .append(Const.of(4L))
                            .append(Const.of(5.0F))
                            .append(Const.of(6.0))
                            .append(Const.of('a'))
                            .append(bc.newArray(char.class, Const.of('b'), Const.of('c')))
                            .append(Const.of("def"))
                            .append(bc.invokeStatic(charSeq))
                            .append(bc.new_(MyObject.class))
                            .append(Const.ofNull(Object.class))
                            .append("...")
                            .append('!')
                            .toString_());
                });
            });
        });
        assertEquals("true12345.06.0abcdefghijklmnull...!", tcm.staticMethod("createString", Supplier.class).get());
    }

    public static class MyObject {
        @Override
        public String toString() {
            return "jklm";
        }
    }

    @Test
    public void testStringBuilderWithControlFlow() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestStringBuilder", cc -> {
            cc.staticMethod("createString", mc -> {
                mc.returning(Object.class); // always `String`
                mc.body(b0 -> {
                    LocalVar msg = b0.localVar("msg", b0.new_(StringBuilder.class));
                    StringBuilderGen msgBuilder = StringBuilderGen.of(msg, b0).append("FooBar");
                    LocalVar i = b0.localVar("i", Const.of(0));
                    b0.while_(b1 -> b1.yield(b1.lt(i, 5)), b1 -> {
                        StringBuilderGen.of(msg, b1).append("Baz").append(i);
                        b1.inc(i);
                    });
                    msgBuilder.append("Quux");
                    b0.return_(msgBuilder.toString_());
                });
            });
        });
        assertEquals("FooBarBaz0Baz1Baz2Baz3Baz4Quux", tcm.staticMethod("createString", Supplier.class).get());
    }
}
