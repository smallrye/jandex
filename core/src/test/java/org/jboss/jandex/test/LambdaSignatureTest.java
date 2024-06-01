package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.function.Supplier;

import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class LambdaSignatureTest {
    static class MyList<T> {
        MyList(T... elements) {
        }

        static Supplier<? extends MyList<Object>> supplier() {
            // for this lambda:
            // - javac emits a synthetic static method without signature
            // - ecj emits a synthetic static method with an invalid signature
            return MyList<Object>::new;
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(MyList.class);
        doTest(index);
        doTest(IndexingUtil.roundtrip(index, "47d44143aea39ed5921ffda8372800ad3c20b2505c8a9e0b2ef9ac7fde35ad3b"));
    }

    private void doTest(Index index) {
        assertNotNull(index.getClassByName(MyList.class));
    }
}
