package org.jboss.jandex;

import java.util.AbstractList;

class MethodInfoGenerator extends AbstractList<MethodInfo> {
    private final MethodInternal[] methods;
    private final ClassInfo clazz;

    public MethodInfoGenerator(ClassInfo clazz, MethodInternal[] methods) {
        this.clazz = clazz;
        this.methods = methods;
    }

    @Override
    public MethodInfo get(int i) {
        return new MethodInfo(clazz, methods[i]);
    }

    @Override
    public int size() {
        return methods.length;
    }
}
