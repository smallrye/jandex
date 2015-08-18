/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes a Jandex index file to a stream. The write process is somewhat more
 * expensive to allow for fast reads and a compact size. For more information on
 * the index content, see the documentation on {@link org.jboss.jandex.Indexer}.
 *
 * <p>
 * The IndexWriter operates on standard output streams, and also provides
 * suitable buffering.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * IndexWriter is not thread-safe and can not be shared between concurrent
 * threads.
 *
 * @see org.jboss.jandex.Indexer
 * @see org.jboss.jandex.Index
 * @author Jason T. Greene
 *
 */
final class IndexWriterV1 extends IndexWriterImpl {
    // babelfish (no h)
    private static final int MAGIC = 0xBABE1F15;
    static final int MIN_VERSION = 1;
    static final int MAX_VERSION = 3;
    private static final byte FIELD_TAG = 1;
    private static final byte METHOD_TAG = 2;
    private static final byte METHOD_PARAMATER_TAG = 3;
    private static final byte CLASS_TAG = 4;

    private static final int AVALUE_BYTE = 1;
    private static final int AVALUE_SHORT = 2;
    private static final int AVALUE_INT = 3;
    private static final int AVALUE_CHAR = 4;
    private static final int AVALUE_FLOAT = 5;
    private static final int AVALUE_DOUBLE = 6;
    private static final int AVALUE_LONG = 7;
    private static final int AVALUE_BOOLEAN = 8;
    private static final int AVALUE_STRING = 9;
    private static final int AVALUE_CLASS = 10;
    private static final int AVALUE_ENUM = 11;
    private static final int AVALUE_ARRAY = 12;
    private static final int AVALUE_NESTED = 13;

    private final OutputStream out;
    private StrongInternPool<String> pool;
    StrongInternPool<String>.Index poolIndex;
    private TreeMap<DotName, Integer> classTable;

    /**
     * Constructs an IndexWriter using the specified stream
     *
     * @param out a stream to write an index to
     */
    IndexWriterV1(OutputStream out) {
        this.out = out;
    }

    /**
     * Writes the specified index to the associated output stream. This may be called multiple times in order
     * to write multiple indexes.
     *
     * @param index the index to write to the stream
     * @param version the index file version
     * @return the number of bytes written to the stream
     * @throws java.io.IOException if any i/o error occurs
     */
    int write(Index index, int version) throws IOException {

        if (version < MIN_VERSION || version > MAX_VERSION) {
            throw new UnsupportedVersion("Version: " + version);
        }

        PackedDataOutputStream stream = new PackedDataOutputStream(new BufferedOutputStream(out));
        stream.writeInt(MAGIC);
        stream.writeByte(version);

        buildTables(index);
        writeClassTable(stream);
        writeStringTable(stream);
        writeClasses(stream, index, version);
        stream.flush();
        return stream.size();
    }

    private void writeStringTable(PackedDataOutputStream stream) throws IOException {
        stream.writePackedU32(pool.size());
        Iterator<String> iter = pool.iterator();
        while (iter.hasNext()) {
            String string = iter.next();
            stream.writeUTF(string);
        }
    }

    private void writeClassTable(PackedDataOutputStream stream) throws IOException {
        stream.writePackedU32(classTable.size());

        // Zero is reserved for null
        int pos = 1;
        for (Entry<DotName, Integer> entry : classTable.entrySet()) {
            entry.setValue(pos++);
            DotName name = entry.getKey();
            assert name.isComponentized();

            int nameDepth = 0;
            for (DotName prefix = name.prefix(); prefix != null; prefix = prefix.prefix())
                nameDepth++;

            stream.writePackedU32(nameDepth);
            stream.writeUTF(name.local());
        }
    }

    private int positionOf(String string) {
        // V1 format does not use 0 as a null placeholder
        int i = poolIndex.positionOf(string) - 1;
        if (i < 0)
            throw new IllegalStateException();

        return i;
    }

    private int positionOf(DotName className) {
        className = downgradeName(className);
        Integer i = classTable.get(className);
        if (i == null)
            throw new IllegalStateException("Class not found in class table:" + className);

        return i.intValue();
    }


    private void writeClasses(PackedDataOutputStream stream, Index index, int version) throws IOException {
        Collection<ClassInfo> classes = index.getKnownClasses();
        stream.writePackedU32(classes.size());
        for (ClassInfo clazz: classes) {
            stream.writePackedU32(positionOf(clazz.name()));
            stream.writePackedU32(clazz.superName() == null ? 0 : positionOf(clazz.superName()));
            stream.writeShort(clazz.flags());

            // hasNoArgsConstructor supported since version 3
            if (version >= 3) {
                stream.writeBoolean(clazz.hasNoArgsConstructor());
            }

            DotName[] interfaces = clazz.interfaces();
            stream.writePackedU32(interfaces.length);
            for (DotName intf: interfaces)
                stream.writePackedU32(positionOf(intf));

            Set<Entry<DotName, List<AnnotationInstance>>> entrySet = clazz.annotations().entrySet();
            stream.writePackedU32(entrySet.size());
            for (Entry<DotName, List<AnnotationInstance>> entry :  entrySet) {
                stream.writePackedU32(positionOf(entry.getKey()));

                List<AnnotationInstance> instances = entry.getValue();
                stream.writePackedU32(instances.size());
                for (AnnotationInstance instance : instances) {
                    AnnotationTarget target = instance.target();
                    if (target instanceof FieldInfo) {
                        FieldInfo field = (FieldInfo) target;
                        stream.writeByte(FIELD_TAG);
                        stream.writePackedU32(positionOf(field.name()));
                        writeType(stream, field.type());
                        stream.writeShort(field.flags());
                    } else if (target instanceof MethodInfo) {
                        MethodInfo method = (MethodInfo) target;
                        stream.writeByte(METHOD_TAG);
                        stream.writePackedU32(positionOf(method.name()));
                        stream.writePackedU32(method.args().length);
                        for (int i = 0; i < method.args().length; i ++) {
                            writeType(stream, method.args()[i]);
                        }
                        writeType(stream, method.returnType());
                        stream.writeShort(method.flags());
                    } else if (target instanceof MethodParameterInfo) {
                        MethodParameterInfo param = (MethodParameterInfo) target;
                        MethodInfo method = param.method();
                        stream.writeByte(METHOD_PARAMATER_TAG);
                        stream.writePackedU32(positionOf(method.name()));
                        stream.writePackedU32(method.args().length);
                        for (int i = 0; i < method.args().length; i ++) {
                            writeType(stream, method.args()[i]);
                        }
                        writeType(stream, method.returnType());
                        stream.writeShort(method.flags());
                        stream.writePackedU32(param.position());
                    } else if (target instanceof ClassInfo) {
                        stream.writeByte(CLASS_TAG);
                    } else throw new IllegalStateException("Unknown target");

                    Collection<AnnotationValue> values = instance.values();
                    writeAnnotationValues(stream, values);
                }
            }
        }
    }

    private void writeAnnotationValues(PackedDataOutputStream stream, Collection<AnnotationValue> values) throws IOException {
        stream.writePackedU32(values.size());
        for (AnnotationValue value : values) {
            writeAnnotationValue(stream, value);
        }
    }

    private void writeAnnotationValue(PackedDataOutputStream stream, AnnotationValue value) throws IOException {
        stream.writePackedU32(positionOf(value.name()));
        if (value instanceof AnnotationValue.ByteValue) {
            stream.writeByte(AVALUE_BYTE);
            stream.writeByte(value.asByte() & 0xFF);
        } else if  (value instanceof AnnotationValue.ShortValue) {
            stream.writeByte(AVALUE_SHORT);
            stream.writePackedU32(value.asShort() & 0xFFFF);
        } else if (value instanceof AnnotationValue.IntegerValue) {
            stream.writeByte(AVALUE_INT);
            stream.writePackedU32(value.asInt());
        } else if (value instanceof AnnotationValue.CharacterValue) {
            stream.writeByte(AVALUE_CHAR);
            stream.writePackedU32(value.asChar());
        } else if (value instanceof AnnotationValue.FloatValue) {
            stream.writeByte(AVALUE_FLOAT);
            stream.writeFloat(value.asFloat());
        } else if (value instanceof AnnotationValue.DoubleValue) {
            stream.writeByte(AVALUE_DOUBLE);
            stream.writeDouble(value.asDouble());
        } else if (value instanceof AnnotationValue.LongValue) {
            stream.writeByte(AVALUE_LONG);
            stream.writeLong(value.asLong());
        } else if (value instanceof AnnotationValue.BooleanValue) {
            stream.writeByte(AVALUE_BOOLEAN);
            stream.writeBoolean(value.asBoolean());
        } else if (value instanceof AnnotationValue.StringValue) {
            stream.writeByte(AVALUE_STRING);
            stream.writePackedU32(positionOf(value.asString()));
        } else if (value instanceof AnnotationValue.ClassValue) {
            stream.writeByte(AVALUE_CLASS);
            writeType(stream, value.asClass());
        } else if (value instanceof AnnotationValue.EnumValue) {
            stream.writeByte(AVALUE_ENUM);
            stream.writePackedU32(positionOf(value.asEnumType()));
            stream.writePackedU32(positionOf(value.asEnum()));
        } else if (value instanceof AnnotationValue.ArrayValue) {
            AnnotationValue[] array = value.asArray();
            int length = array.length;
            stream.writeByte(AVALUE_ARRAY);
            stream.writePackedU32(length);

            for (int i = 0; i < length; i++) {
                writeAnnotationValue(stream, array[i]);
            }
        } else if (value instanceof AnnotationValue.NestedAnnotation) {
            AnnotationInstance instance = value.asNested();
            Collection<AnnotationValue> values = instance.values();

            stream.writeByte(AVALUE_NESTED);
            stream.writePackedU32(positionOf(instance.name()));
            writeAnnotationValues(stream, values);
        }
    }

    private void writeType(PackedDataOutputStream stream, Type type) throws IOException {
        stream.writeByte(type.kind().ordinal());
        stream.writePackedU32(positionOf(type.name()));
    }

    private void buildTables(Index index) {
        pool = new StrongInternPool<String>();
        classTable = new TreeMap<DotName, Integer>();

        // Build the pool for all strings
        for (ClassInfo clazz: index.getKnownClasses()) {
            addClassName(clazz.name());
            if (clazz.superName() != null)
                addClassName(clazz.superName());

            for (DotName intf: clazz.interfaces())
                addClassName(intf);

            for (Entry<DotName, List<AnnotationInstance>> entry :  clazz.annotations().entrySet()) {
                addClassName(entry.getKey());

                for (AnnotationInstance instance: entry.getValue()) {
                    AnnotationTarget target = instance.target();
                    if (target instanceof FieldInfo) {
                        FieldInfo field = (FieldInfo) target;
                        intern(field.name());
                        addClassName(field.type().name());

                    } else if (target instanceof MethodInfo) {
                        MethodInfo method = (MethodInfo) target;
                        intern(method.name());
                        for (Type type : method.args())
                            addClassName(type.name());

                        addClassName(method.returnType().name());
                    }
                    else if (target instanceof MethodParameterInfo) {
                        MethodParameterInfo param = (MethodParameterInfo) target;
                        intern(param.method().name());
                        for (Type type : param.method().args())
                            addClassName(type.name());

                        addClassName(param.method().returnType().name());
                    }

                    for (AnnotationValue value : instance.values())
                        buildAValueEntries(index, value);
                }


            }
        }

        poolIndex = pool.index();
    }

    private void buildAValueEntries(Index index, AnnotationValue value) {
        intern(value.name());

        if (value instanceof AnnotationValue.StringValue) {
            intern(value.asString());
        } else if (value instanceof AnnotationValue.ClassValue) {
            addClassName(value.asClass().name());
        } else if (value instanceof AnnotationValue.EnumValue) {
            addClassName(value.asEnumType());
            intern(value.asEnum());
        } else if (value instanceof AnnotationValue.ArrayValue) {
            for (AnnotationValue entry : value.asArray())
                buildAValueEntries(index, entry);
        } else if (value instanceof AnnotationValue.NestedAnnotation) {
            AnnotationInstance instance = value.asNested();
            Collection<AnnotationValue> values = instance.values();

            addClassName(instance.name());
            for (AnnotationValue entry : values) {
                buildAValueEntries(index, entry);
            }
        }
    }

    private String intern(String name) {
        return pool.intern(name);
    }

    private void addClassName(DotName name) {
        name = downgradeName(name);

        if (! classTable.containsKey(name))
            classTable.put(name, null);

        DotName prefix = name.prefix();
        if (prefix != null)
            addClassName(prefix);
    }

    private DotName downgradeName(DotName name) {
        DotName n = name;
        StringBuilder builder = null;
        while (n.isInner()) {
            if (builder == null) {
                builder = new StringBuilder();
            }

            builder.insert(0, n.local()).insert(0, '$');
            if (! n.prefix().isInner()) {
                builder.insert(0, n.prefix().local());
                name = new DotName(n.prefix().prefix(), builder.toString(), true, false);
            }

            n = n.prefix();
        }
        return name;
    }

}
