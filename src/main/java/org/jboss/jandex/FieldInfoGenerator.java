package org.jboss.jandex;

import java.util.AbstractList;

class FieldInfoGenerator extends AbstractList<FieldInfo> {
    private final FieldInternal[] fields;
    private final ClassInfo clazz;

    public FieldInfoGenerator(ClassInfo clazz, FieldInternal[] fields) {
        this.clazz = clazz;
        this.fields = fields;
    }

    @Override
    public FieldInfo get(int i) {
        return new FieldInfo(clazz, fields[i]);
    }

    @Override
    public int size() {
        return fields.length;
    }
}
