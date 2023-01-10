package org.jboss.jandex.test.data;

import java.util.List;
import java.util.Map;

public class OuterParamBound<W extends Number & Comparable<W>> {
    public W fieldA;

    public List<W> fieldB;

    public List<? extends W> fieldC;

    public List<? super W> fieldD;

    public String methodA(String arg, W arg2, OuterParamBound<W> self) throws IllegalArgumentException {
        return null;
    }

    public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodB(
            U arg, W arg2, OuterParamBound<W> self) {
        return null;
    }

    public static class NestedRaw {
        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodC(
                List<? extends U> arg, NestedRaw self) throws IllegalArgumentException, V {
            return null;
        }
    }

    public static class NestedParam<X> {
        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodD(
                List<? super U> arg, X arg2, NestedParam<X> self) throws IllegalArgumentException {
            return null;
        }
    }

    public static class NestedParamBound<X extends Number & Comparable<X>> {
        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodE(
                List<?> arg, X arg2, NestedParamBound<X> self) throws V {
            return null;
        }
    }

    public class InnerRaw {
        public List<OuterParamBound<? extends Integer>.InnerParam<? super String>> fieldE;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodF(
                List<? extends U> arg, W arg2, InnerRaw self) throws IllegalArgumentException, V {
            return null;
        }
    }

    public class InnerParam<X> {
        public List<OuterParamBound<Integer>.InnerParam<? extends W>> fieldF;
        public Map<X, ? super W> fieldG;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodG(
                List<? super U> arg, X arg2, W arg3, InnerParam<X> self) throws IllegalArgumentException {
            return null;
        }
    }

    public class InnerParamBound<X extends Number & Comparable<X>> {
        public List<OuterParamBound<X>.InnerParamBound<W>> fieldH;
        public Map<? extends X, W> fieldI;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodH(
                List<?> arg, X arg2, W arg3, InnerParamBound<X> self) throws V {
            return null;
        }
    }
}
