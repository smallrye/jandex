package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class DotNameComponentizationTest {
    static class Foo<T> {
        // must not be `static` in this test
        class $Bar {
        }

        static <A> void baz(Foo<A>.$Bar foobar) {
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(Foo.class);

        doTest(index);

        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        assertEquals(1, index.getKnownClasses().size());
        DotName name = index.getKnownClasses().iterator().next().firstMethod("baz").parameterTypes().get(0).name();

        {
            DotName org = DotName.createComponentized(null, "org");
            DotName jboss = DotName.createComponentized(org, "jboss");
            DotName jandex = DotName.createComponentized(jboss, "jandex");
            DotName test = DotName.createComponentized(jandex, "test");
            DotName dotNameComponentizationTest = DotName.createComponentized(test, "DotNameComponentizationTest");
            DotName foo = DotName.createComponentized(dotNameComponentizationTest, "Foo", true);
            DotName bar = DotName.createComponentized(foo, "$Bar", true);

            assertEquals(bar, name);
        }

        {
            DotName org = DotName.createComponentized(null, "org");
            DotName jboss = DotName.createComponentized(org, "jboss");
            DotName jandex = DotName.createComponentized(jboss, "jandex");
            DotName test = DotName.createComponentized(jandex, "test");
            DotName dotNameComponentizationTest = DotName.createComponentized(test, "DotNameComponentizationTest");
            DotName fooBar = DotName.createComponentized(dotNameComponentizationTest, "Foo$$Bar", true);

            assertEquals(fooBar, name);
        }
    }
}
