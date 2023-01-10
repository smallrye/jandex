package org.jboss.jandex.test.data;

import java.util.List;

public class OuterRaw {
    public String fieldA;

    public List<String> fieldB;

    public List<?> fieldC;

    public List<? extends CharSequence> fieldD;

    public List<? super String> fieldE;

    public String methodA(String arg, OuterRaw self) throws IllegalArgumentException {
        return null;
    }

    public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodB(
            U arg, OuterRaw self) {
        return null;
    }

    public static class NestedRaw {
        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodC(
                List<? extends U> arg, NestedRaw self) throws IllegalArgumentException, V {
            return null;
        }
    }

    public static class NestedParam<X> {
        public X fieldF;
        public NestedParam<X> fieldG;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodD(
                List<? super U> arg, X arg2, NestedParam<X> self) throws IllegalArgumentException {
            return null;
        }
    }

    public static class NestedParamBound<X extends Number & Comparable<X>> {
        public List<X> fieldH;
        public NestedParamBound<Integer> fieldI;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodE(
                List<?> arg, X arg2, NestedParamBound<X> self) throws V {
            return null;
        }
    }

    public class InnerRaw {
        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodF(
                List<? extends U> arg, InnerRaw self) throws IllegalArgumentException, V {
            return null;
        }
    }

    public class InnerParam<X> {
        public X fieldJ;
        public InnerParam<Integer> fieldK;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodG(
                List<? super U> arg, X arg2, InnerParam<X> self) throws IllegalArgumentException {
            return null;
        }
    }

    public class InnerParamBound<X extends Number & Comparable<X>> {
        public List<X> fieldL;
        public InnerParamBound<X> fieldM;

        public <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T methodH(
                List<?> arg, X arg2, InnerParamBound<X> self) throws V {
            return null;
        }
    }
}
