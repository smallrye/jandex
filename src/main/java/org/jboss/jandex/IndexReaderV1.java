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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a Jandex index file and returns the saved index. See {@link org.jboss.jandex.Indexer}
 * for a thorough description of how the Index data is produced.
 *
 * <p>
 * An IndexReader loads the stream passed to it's constructor and applies the
 * appropriate buffering. The Jandex index format is designed for efficient
 * reading and low final memory storage.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * IndexReader is not thread-safe and can not be shared between concurrent
 * threads. The resulting index, however, is.
 *
 * @author Jason T. Greene
 */
final class IndexReaderV1 extends IndexReaderImpl {
    static final int MIN_VERSION = 2;
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

    private PackedDataInputStream input;
    private DotName[] classTable;
    private String[] stringTable;
    private HashMap<DotName, List<AnnotationInstance>> masterAnnotations;


    /**
     * Constructs a new IndedReader using the passed stream. The stream is not
     * read from until the read method is called.
     *
     * @param input a stream which points to a jandex index file
     */
    IndexReaderV1(PackedDataInputStream input) {
        this.input = input;
    }

    /**
     * Read the index at the associated stream of this reader. This method can be called multiple
     * times if the stream contains multiple index files.
     *
     * @return the Index contained in the stream
     * @throws java.io.IOException if an I/O error occurs
     * @throws IllegalArgumentException if the stream does not point to Jandex index data
     * @throws org.jboss.jandex.UnsupportedVersion if the index data is tagged with a version not known to this reader
     */
    Index read(int version) throws IOException {
        try {
            PackedDataInputStream stream = this.input;
            masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>();
            readClassTable(stream);
            readStringTable(stream);
            return readClasses(stream, version);
        } finally {
            classTable = null;
            stringTable = null;
            masterAnnotations = null;
        }
    }


    private Index readClasses(PackedDataInputStream stream, int version) throws IOException {
        int entries = stream.readPackedU32();
        HashMap<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>();
        HashMap<DotName, List<ClassInfo>> implementors = new HashMap<DotName, List<ClassInfo>>();
        HashMap<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>();
        masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>();

        for (int i = 0; i < entries; i++) {
            DotName name = classTable[stream.readPackedU32()];
            DotName superName = classTable[stream.readPackedU32()];
            short flags = stream.readShort();
            // No copyParameters supported in version 3+
            boolean hasNoArgsConstructor = version >= 3 && stream.readBoolean();

            int numIntfs = stream.readPackedU32();
            List<Type> interfaces = new ArrayList<Type>(numIntfs);
            for (int j = 0; j < numIntfs; j++) {
                interfaces.add(new ClassType(classTable[stream.readPackedU32()]));
            }

            Type[] interfaceTypes = interfaces.toArray(new Type[interfaces.size()]);

            Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>();
            Type superClassType = superName == null ? null : new ClassType(superName);
            ClassInfo clazz = new ClassInfo(name, superClassType, flags, interfaceTypes, annotations, hasNoArgsConstructor);
            classes.put(name, clazz);
            addClassToMap(subclasses, superName, clazz);
            for (Type interfaceName : interfaces) {
                addClassToMap(implementors, interfaceName.name(), clazz);
            }
            readAnnotations(stream, annotations, clazz);
        }

        return Index.create(masterAnnotations, subclasses, implementors, classes);
    }


    private void readAnnotations(PackedDataInputStream stream, Map<DotName, List<AnnotationInstance>> annotations, ClassInfo clazz)
            throws IOException {
        int numAnnotations = stream.readPackedU32();
        for (int j = 0; j < numAnnotations; j++) {
            DotName annotationName = classTable[stream.readPackedU32()];

            int numTargets = stream.readPackedU32();
            for (int k = 0; k < numTargets; k++) {
                int tag = stream.readPackedU32();

                AnnotationTarget target;
                switch (tag) {
                    case FIELD_TAG: {
                        String name = stringTable[stream.readPackedU32()];
                        Type type = readType(stream);
                        short flags = stream.readShort();
                        target = new FieldInfo(clazz, Utils.toUTF8(name), type, flags);
                        break;
                    }
                    case METHOD_TAG: {
                        target = readMethod(clazz, stream);
                        break;
                    }
                    case METHOD_PARAMATER_TAG: {
                        MethodInfo method = readMethod(clazz, stream);
                        target = new MethodParameterInfo(method, (short)stream.readPackedU32());
                        break;
                    }
                    case CLASS_TAG: {
                        target = clazz;
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException();
                }

                AnnotationValue[] values = readAnnotationValues(stream);
                AnnotationInstance instance = new AnnotationInstance(annotationName, target, values);

                recordAnnotation(masterAnnotations, annotationName, instance);
                recordAnnotation(annotations, annotationName, instance);

            }
        }
    }


    private AnnotationValue[] readAnnotationValues(PackedDataInputStream stream) throws IOException {
        int numValues = stream.readPackedU32();
        AnnotationValue[] values = new AnnotationValue[numValues];

        for (int i = 0; i < numValues; i++) {

            String name = stringTable[stream.readPackedU32()];
            int tag = stream.readByte();
            AnnotationValue value;
            switch (tag) {
                case AVALUE_BYTE:
                    value = new AnnotationValue.ByteValue(name, stream.readByte());
                    break;
                case AVALUE_SHORT:
                    value = new AnnotationValue.ShortValue(name, (short) stream.readPackedU32());
                    break;
                case AVALUE_INT:
                    value = new AnnotationValue.IntegerValue(name, stream.readPackedU32());
                    break;
                case AVALUE_CHAR:
                    value = new AnnotationValue.CharacterValue(name, (char) stream.readPackedU32());
                    break;
                case AVALUE_FLOAT:
                    value = new AnnotationValue.FloatValue(name, stream.readFloat());
                    break;
                case AVALUE_DOUBLE:
                    value = new AnnotationValue.DoubleValue(name, stream.readDouble());
                    break;
                case AVALUE_LONG:
                    value = new AnnotationValue.LongValue(name, stream.readLong());
                    break;
                case AVALUE_BOOLEAN:
                    value = new AnnotationValue.BooleanValue(name, stream.readBoolean());
                    break;
                case AVALUE_STRING:
                    value = new AnnotationValue.StringValue(name, stringTable[stream.readPackedU32()]);
                    break;
                case AVALUE_CLASS:
                    value = new AnnotationValue.ClassValue(name, readType(stream));
                    break;
                case AVALUE_ENUM:
                    value = new AnnotationValue.EnumValue(name, classTable[stream.readPackedU32()], stringTable[stream.readPackedU32()]);
                    break;
                case AVALUE_ARRAY:
                    value = new AnnotationValue.ArrayValue(name, readAnnotationValues(stream));
                    break;
                case AVALUE_NESTED: {
                    DotName nestedName = classTable[stream.readPackedU32()];
                    AnnotationInstance nestedInstance = new AnnotationInstance(nestedName, null, readAnnotationValues(stream));
                    value = new AnnotationValue.NestedAnnotation(name, nestedInstance);
                    break;
                }
                default:
                    throw new IllegalStateException("Invalid annotation value tag:" + tag);
            }

            values[i] = value;
        }

        return values;
    }

    private MethodInfo readMethod(ClassInfo clazz, PackedDataInputStream stream) throws IOException {
        String name = stringTable[stream.readPackedU32()];
        int numArgs = stream.readPackedU32();
        List<Type> args = new ArrayList<Type>(numArgs);
        for (int i = 0; i < numArgs; i ++) {
            args.add(readType(stream));
        }

        Type[] parameters = args.toArray(new Type[args.size()]);
        Type returnType = readType(stream);
        short flags = stream.readShort();

        byte[] bytes = Utils.toUTF8(name);
        return new MethodInfo(clazz, bytes, MethodInternal.EMPTY_PARAMETER_NAMES, parameters, returnType, flags);
    }

    private void recordAnnotation(Map<DotName, List<AnnotationInstance>> annotations, DotName annotation, AnnotationInstance instance) {
        List<AnnotationInstance> list = annotations.get(annotation);
        if (list == null) {
            list = new ArrayList<AnnotationInstance>();
            annotations.put(annotation, list);
        }

        list.add(instance);
    }

    private void addClassToMap(HashMap<DotName, List<ClassInfo>> map, DotName name, ClassInfo currentClass) {
        List<ClassInfo> list = map.get(name);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            map.put(name, list);
        }

        list.add(currentClass);
    }

    private Type readType(PackedDataInputStream stream) throws IOException {
        Type.Kind kind = Type.Kind.fromOrdinal(stream.readByte());
        DotName name = classTable[stream.readPackedU32()];
        return Type.create(name, kind);
    }


    private void readStringTable(PackedDataInputStream stream) throws IOException {
        int entries = stream.readPackedU32();
        stringTable = new String[entries];

        for (int i = 0; i < entries; i++) {
            stringTable[i] = stream.readUTF();
        }
    }


    private void readClassTable(PackedDataInputStream stream) throws IOException {
        int entries = stream.readPackedU32();
        int lastDepth = -1;
        DotName curr = null;

        // Null is the implicit first entry
        classTable = new DotName[++entries];
        for (int i = 1; i < entries; i++) {
            int depth = stream.readPackedU32();
            String local = stream.readUTF();

            if (depth <= lastDepth) {
                while (lastDepth-- >= depth)
                    curr = curr.prefix();
            }

            classTable[i] = curr = new DotName(curr, local, true, false);
            lastDepth = depth;
        }
    }

    int toDataVersion(int version) {
        // From 1 to 3, every version changed the available data

        return version;
    }
}
