/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a Jandex index file and returns the saved index. See {@link Indexer}
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
final class IndexReaderV2 extends IndexReaderImpl {
    static final int MIN_VERSION = 6;
    static final int MAX_VERSION = 9;
    static final int MAX_DATA_VERSION = 4;
    private static final byte NULL_TARGET_TAG = 0;
    private static final byte FIELD_TAG = 1;
    private static final byte METHOD_TAG = 2;
    private static final byte METHOD_PARAMATER_TAG = 3;
    private static final byte CLASS_TAG = 4;
    private static final byte EMPTY_TYPE_TAG = 5;
    private static final byte CLASS_EXTENDS_TYPE_TAG = 6;
    private static final byte TYPE_PARAMETER_TAG = 7;
    private static final byte TYPE_PARAMETER_BOUND_TAG = 8;
    private static final byte METHOD_PARAMETER_TYPE_TAG = 9;
    private static final byte THROWS_TYPE_TAG = 10;
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
    private static final int HAS_ENCLOSING_METHOD = 1;
    private final static byte[] INIT_METHOD_NAME = Utils.toUTF8("<init>");

    private PackedDataInputStream input;
    private byte[][] byteTable;
    private String[] stringTable;
    private DotName[] nameTable;
    private Type[] typeTable;
    private Type[][] typeListTable;
    private AnnotationInstance[] annotationTable;
    private MethodInternal[] methodTable;
    private FieldInternal[] fieldTable;



    IndexReaderV2(PackedDataInputStream input) {
        this.input = input;
    }

    Index read(int version) throws IOException {
        try {
            PackedDataInputStream stream = this.input;
            int annotationsSize = stream.readPackedU32();
            int implementorsSize = stream.readPackedU32();
            int subclassesSize = stream.readPackedU32();


            readByteTable(stream);
            readStringTable(stream);
            readNameTable(stream);

            typeTable = new Type[stream.readPackedU32() + 1];
            typeListTable = new Type[stream.readPackedU32() + 1][];
            annotationTable = new AnnotationInstance[stream.readPackedU32() + 1];

            readTypeTable(stream);
            readTypeListTable(stream);
            readMethodTable(stream, version);
            readFieldTable(stream);
            return readClasses(stream, annotationsSize, implementorsSize, subclassesSize, version);
        } finally {
            byteTable = null;
            stringTable = null;
            nameTable = null;
            typeTable = null;
            typeListTable = null;
            annotationTable = null;
            methodTable = null;
            fieldTable = null;
        }
    }

    private void readByteTable(PackedDataInputStream stream) throws IOException {
        // Null is the implicit first entry
        int size = stream.readPackedU32() + 1;
        byte[][] byteTable = this.byteTable = new byte[size][];
        for (int i = 1; i < size; i++) {
            int len = stream.readPackedU32();
            byteTable[i] = new byte[len];
            stream.readFully(byteTable[i], 0, len);
        }
    }

    private void readStringTable(PackedDataInputStream stream) throws IOException {
        // Null is the implicit first entry
        int size = stream.readPackedU32() + 1;
        String[] stringTable = this.stringTable = new String[size];
        for (int i = 1; i < size; i++) {
            stringTable[i] = stream.readUTF();
        }
    }

    private void readNameTable(PackedDataInputStream stream) throws IOException {
        // Null is the implicit first entry
        int entries = stream.readPackedU32() + 1;
        int lastDepth = -1;
        DotName curr = null;

        nameTable = new DotName[entries];
        for (int i = 1; i < entries; i++) {
            int depth = stream.readPackedU32();
            boolean inner = (depth & 1) == 1;
            depth >>= 1;

            String local = stringTable[stream.readPackedU32()];

            if (depth <= lastDepth) {
                while (lastDepth-- >= depth) {
                    assert curr != null;
                    curr = curr.prefix();
                }
            }

            nameTable[i] = curr = new DotName(curr, local, true, inner);
            lastDepth = depth;
        }
    }

    private void readTypeTable(PackedDataInputStream stream) throws IOException {
        // Null is the implicit first entry
        for (int i = 1; i < typeTable.length; i++) {
            typeTable[i] = readTypeEntry(stream);
        }
    }

    private int findNextNull(Object[] array, int start) {
        while (start < array.length) {
            if (array[start] == null) {
                return start;
            }
            start++;
        }

        return array.length;
    }

    private void readTypeListTable(PackedDataInputStream stream) throws IOException {
        // Null is the implicit first entry
        Type[][] typeListTable = this.typeListTable;
        // Already emitted entries are omitted as gaps in the table portion
        for (int i = findNextNull(typeListTable, 1); i < typeListTable.length; i = findNextNull(typeListTable, i)) {
            typeListTable[i] = readTypeListEntry(stream);
        }
    }

    private AnnotationInstance[] readAnnotations(PackedDataInputStream stream, AnnotationTarget target) throws IOException {
        int size = stream.readPackedU32();
        if (size == 0) {
            return AnnotationInstance.EMPTY_ARRAY;
        }

        AnnotationInstance[] annotations = new AnnotationInstance[size];
        for (int i = 0; i < size; i++) {
            int reference = stream.readPackedU32();
            if (annotationTable[reference] == null) {
                annotationTable[reference] = readAnnotationEntry(stream, target);
            }

            annotations[i] = annotationTable[reference];
        }
        return annotations;
    }

    private AnnotationValue[] readAnnotationValues(PackedDataInputStream stream) throws IOException {
        int numValues = stream.readPackedU32();
        AnnotationValue[] values = new AnnotationValue[numValues];

        for (int i = 0; i < numValues; i++) {
            AnnotationValue value = readAnnotationValue(stream);
            values[i] = value;
        }

        return values;
    }

    private AnnotationValue readAnnotationValue(PackedDataInputStream stream) throws IOException {
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
                value = new AnnotationValue.ClassValue(name, typeTable[stream.readPackedU32()]);
                break;
            case AVALUE_ENUM:
                value = new AnnotationValue.EnumValue(name, nameTable[stream.readPackedU32()], stringTable[stream.readPackedU32()]);
                break;
            case AVALUE_ARRAY:
                value = new AnnotationValue.ArrayValue(name, readAnnotationValues(stream));
                break;
            case AVALUE_NESTED: {
                int reference = stream.readPackedU32();
                AnnotationInstance nestedInstance = annotationTable[reference];
                if (nestedInstance == null) {
                    nestedInstance = annotationTable[reference] = readAnnotationEntry(stream, null);
                }

                value = new AnnotationValue.NestedAnnotation(name, nestedInstance);
                break;
            }
            default:
                throw new IllegalStateException("Invalid annotation value tag:" + tag);
        }
        return value;
    }

    private AnnotationInstance readAnnotationEntry(PackedDataInputStream stream, AnnotationTarget caller) throws IOException {
        DotName name = nameTable[stream.readPackedU32()];
        AnnotationTarget target = readAnnotationTarget(stream, caller);
        AnnotationValue[] values = readAnnotationValues(stream);
        return new AnnotationInstance(name, target, values);
    }

    private Type[] readTypeListReference(PackedDataInputStream stream) throws IOException {
        int reference = stream.readPackedU32();
        Type[] types = typeListTable[reference];
        if (types != null) {
            return types;
        }

        return typeListTable[reference] = readTypeListEntry(stream);
    }

    private Type[] readTypeListEntry(PackedDataInputStream stream) throws IOException {
        int size = stream.readPackedU32();
        if (size == 0) {
            return Type.EMPTY_ARRAY;
        }

        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            types[i] = typeTable[stream.readPackedU32()];
        }

        return types;
    }


    private Type readTypeEntry(PackedDataInputStream stream) throws IOException {
        Type.Kind kind = Type.Kind.fromOrdinal(stream.readUnsignedByte());

        switch (kind) {
            case CLASS: {
                DotName name = nameTable[stream.readPackedU32()];
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new ClassType(name, annotations);
            }
            case ARRAY: {
                int dimensions = stream.readPackedU32();
                Type component = typeTable[stream.readPackedU32()];
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new ArrayType(component, dimensions, annotations);
            }
            case PRIMITIVE: {
                int primitive = stream.readUnsignedByte();
                Type type = PrimitiveType.fromOridinal(primitive);
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return annotations.length > 0 ? type.copyType(annotations) : type;
            }
            case VOID: {
                Type type = VoidType.VOID;
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return annotations.length > 0 ? type.copyType(annotations) : type;
            }
            case TYPE_VARIABLE: {
                String identifier = stringTable[stream.readPackedU32()];
                Type[] bounds = readTypeListReference(stream);
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new TypeVariable(identifier, bounds, annotations);
            }
            case UNRESOLVED_TYPE_VARIABLE: {
                String identifier = stringTable[stream.readPackedU32()];
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new UnresolvedTypeVariable(identifier, annotations);
            }
            case WILDCARD_TYPE: {
                boolean isExtends = stream.readPackedU32() == 1;
                Type bound = typeTable[stream.readPackedU32()];
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new WildcardType(bound, isExtends, annotations);

            }
            case PARAMETERIZED_TYPE: {
                DotName name = nameTable[stream.readPackedU32()];
                int reference = stream.readPackedU32();
                Type owner = typeTable[reference];
                Type[] parameters = readTypeListReference(stream);
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                return new ParameterizedType(name, parameters, owner, annotations);
            }
        }

        throw new IllegalStateException("Unrecognized type: " + kind);
    }

    private AnnotationTarget readAnnotationTarget(PackedDataInputStream stream, AnnotationTarget caller) throws IOException {
        byte tag = stream.readByte();
        switch (tag) {
            case NULL_TARGET_TAG:
                return null;
            case CLASS_TAG:
            case FIELD_TAG:
            case METHOD_TAG:
                return caller;
            case METHOD_PARAMATER_TAG: {
                short parameter = (short)stream.readPackedU32();
                return new MethodParameterInfo((MethodInfo)caller, parameter);
            }
            case EMPTY_TYPE_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                boolean isReceiver = stream.readPackedU32() == 1;
                return new EmptyTypeTarget(caller, target, isReceiver);
            }
            case CLASS_EXTENDS_TYPE_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                int pos = stream.readPackedU32();
                return new ClassExtendsTypeTarget(caller, target, pos);
            }
            case TYPE_PARAMETER_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                int pos = stream.readPackedU32();
                return new TypeParameterTypeTarget(caller, target, pos);
            }
            case TYPE_PARAMETER_BOUND_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                int pos = stream.readPackedU32();
                int bound = stream.readPackedU32();
                return new TypeParameterBoundTypeTarget(caller, target, pos, bound);
            }
            case METHOD_PARAMETER_TYPE_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                int pos = stream.readPackedU32();
                return new MethodParameterTypeTarget(caller, target, pos);
            }
            case THROWS_TYPE_TAG: {
                Type target = typeTable[stream.readPackedU32()];
                int pos = stream.readPackedU32();
                return new ThrowsTypeTarget(caller, target, pos);
            }
        }

        throw new IllegalStateException("Invalid tag: " + tag);
    }

    private void readMethodTable(PackedDataInputStream stream, int version) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        methodTable = new MethodInternal[size];
        for (int i = 1; i < size; i++) {
            methodTable[i] = readMethodEntry(stream, version);
        }

    }

    private void readFieldTable(PackedDataInputStream stream) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        fieldTable = new FieldInternal[size];
        for (int i = 1; i < size; i++) {
            fieldTable[i] = readFieldEntry(stream);
        }
    }

    private MethodInternal readMethodEntry(PackedDataInputStream stream, int version) throws IOException {
        byte[] name = byteTable[stream.readPackedU32()];
        short flags = (short) stream.readPackedU32();
        Type[] typeParameters = typeListTable[stream.readPackedU32()];
        int reference = stream.readPackedU32();
        Type receiverType = typeTable[reference];
        Type returnType = typeTable[stream.readPackedU32()];
        Type[] parameters = typeListTable[stream.readPackedU32()];
        Type[] exceptions = typeListTable[stream.readPackedU32()];
        AnnotationValue defaultValue = null;
        if (version >= 7) {
            boolean hasDefaultValue = stream.readByte() > 0;
            if (hasDefaultValue) {
                defaultValue = readAnnotationValue(stream);
            }
        }
        byte[][] methodParameterBytes = MethodInternal.EMPTY_PARAMETER_NAMES;
        if (version >= 8) {
            int size = stream.readPackedU32();
            if (size > 0 ) {
                methodParameterBytes = new byte[size][];
                for (int i = 0; i < size; i++) {
                    methodParameterBytes[i] = byteTable[stream.readPackedU32()];
                }
            }
        }

        MethodInfo methodInfo = new MethodInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, methodInfo);
        MethodInternal methodInternal = new MethodInternal(name, methodParameterBytes, parameters, returnType, flags,
                receiverType, typeParameters,
                exceptions, annotations, defaultValue);
        methodInfo.setMethodInternal(methodInternal);
        return methodInternal;
    }

    private FieldInternal readFieldEntry(PackedDataInputStream stream) throws IOException {
        byte[] name = byteTable[stream.readPackedU32()];
        short flags = (short) stream.readPackedU32();
        Type type = typeTable[stream.readPackedU32()];

        FieldInfo fieldInfo = new FieldInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, fieldInfo);
        FieldInternal fieldInternal = new FieldInternal(name, type, flags, annotations);
        fieldInfo.setFieldInternal(fieldInternal);
        return fieldInternal;
    }

    private ClassInfo readClassEntry(PackedDataInputStream stream,
                                     Map<DotName, List<AnnotationInstance>> masterAnnotations, int version) throws IOException {
        DotName name  = nameTable[stream.readPackedU32()];
        short flags = (short) stream.readPackedU32();
        Type superType = typeTable[stream.readPackedU32()];
        Type[] typeParameters = typeListTable[stream.readPackedU32()];
        Type[] interfaceTypes = typeListTable[stream.readPackedU32()];

        boolean hasEnclosingMethod = false;
        boolean hasNesting = false;
        if (version >= 9) {
            int nestingMask = stream.readUnsignedByte();
            if (nestingMask > 0) {
                hasNesting = true;
                hasEnclosingMethod = ((nestingMask & 2) == 2);
            }
        } else {
            hasEnclosingMethod = hasNesting = true;
        }

        DotName enclosingClass = null;
        ClassInfo.EnclosingMethodInfo enclosingMethod = null;
        String simpleName = null;

        if (hasNesting) {
            enclosingClass = nameTable[stream.readPackedU32()];
            simpleName = stringTable[stream.readPackedU32()];
            enclosingMethod = hasEnclosingMethod ? readEnclosingMethod(stream, version) : null;
        }

        int size = stream.readPackedU32();

        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<DotName, List<AnnotationInstance>>(size);
        ClassInfo clazz = new ClassInfo(name, superType, flags, interfaceTypes, annotations);
        clazz.setTypeParameters(typeParameters);

        if (hasNesting) {
            clazz.setEnclosingMethod(enclosingMethod);
            // Version 8 and earlier records inner type info regardless of
            // whether or not it is an inner type
            clazz.setInnerClassInfo(enclosingClass, simpleName, version >= 9);
        }

        FieldInternal[] fields = readClassFields(stream, clazz);
        clazz.setFieldArray(fields);

        MethodInternal[] methods = readClassMethods(stream, clazz);
        clazz.setMethodArray(methods);

        for (int i = 0; i < size; i++) {
            List<AnnotationInstance> instances = convertToList(readAnnotations(stream, clazz));
            if (instances.size() > 0) {
                DotName annotationName = instances.get(0).name();
                annotations.put(annotationName, instances);
                addToMaster(masterAnnotations, annotationName, instances);
            }
        }



        return clazz;
    }

    private void addToMaster(Map<DotName, List<AnnotationInstance>> masterAnnotations, DotName name,
                             List<AnnotationInstance> annotations) {
        List<AnnotationInstance> entry = masterAnnotations.get(name);
        if (entry == null) {
            masterAnnotations.put(name, new ArrayList<AnnotationInstance>(annotations));
            return;
        }

        entry.addAll(annotations);
    }

    private List<AnnotationInstance> convertToList(AnnotationInstance[] annotationInstances) {
        if (annotationInstances.length == 0) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(Arrays.asList(annotationInstances));
    }

    private void addClassToMap(HashMap<DotName, List<ClassInfo>> map, DotName name, ClassInfo currentClass) {
        List<ClassInfo> list = map.get(name);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            map.put(name, list);
        }

        list.add(currentClass);
    }

    private FieldInternal[] readClassFields(PackedDataInputStream stream, ClassInfo clazz) throws IOException {
        int len = stream.readPackedU32();
        FieldInternal[] fields = new FieldInternal[len];
        for (int i = 0; i < len; i++) {
            FieldInternal field = fieldTable[stream.readPackedU32()];
            updateAnnotationTargetInfo(field.annotationArray(), clazz);
            fields[i] = field;
        }
        return fields;
    }

    private MethodInternal[] readClassMethods(PackedDataInputStream stream, ClassInfo clazz) throws IOException {
        int len = stream.readPackedU32();
        MethodInternal[] methods = new MethodInternal[len];
        for (int i = 0; i < len; i++) {
            MethodInternal method = methodTable[stream.readPackedU32()];
            updateAnnotationTargetInfo(method.annotationArray(), clazz);
            methods[i] = method;

            if (method.parameterArray().length == 0 && Arrays.equals(INIT_METHOD_NAME, method.nameBytes())) {
                clazz.setHasNoArgsConstructor(true);
            }
        }
        return methods;
    }

    private void updateAnnotationTargetInfo(AnnotationInstance[] annotations, ClassInfo clazz) {
        // Update a method or field internals annotations to reference the class.
        // This update is possible since annotations on a non-null target are unique and not shared
        for (AnnotationInstance annotation : annotations) {
            AnnotationTarget target = annotation.target();
            if (target instanceof MethodInfo) {
                ((MethodInfo)target).setClassInfo(clazz);
            } else if (target instanceof FieldInfo) {
                ((FieldInfo)target).setClassInfo(clazz);
            }
        }
    }

    private ClassInfo.EnclosingMethodInfo readEnclosingMethod(PackedDataInputStream stream, int version) throws IOException {
        if (version < 9 && stream.readUnsignedByte() != HAS_ENCLOSING_METHOD) {
            return null;
        }

        String eName = stringTable[stream.readPackedU32()];
        DotName eClass = nameTable[stream.readPackedU32()];
        Type returnType = typeTable[stream.readPackedU32()];
        Type[] parameters = typeListTable[stream.readPackedU32()];
        return new ClassInfo.EnclosingMethodInfo(eName, returnType, parameters, eClass);
    }

    private Index readClasses(PackedDataInputStream stream,
                              int annotationsSize, int implementorsSize, int subclassesSize, int version) throws IOException {
        int classesSize = stream.readPackedU32();
        HashMap<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>(classesSize);
        HashMap<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>(subclassesSize);
        HashMap<DotName, List<ClassInfo>> implementors = new HashMap<DotName, List<ClassInfo>>(implementorsSize);
        HashMap<DotName, List<AnnotationInstance>> masterAnnotations =
                new HashMap<DotName, List<AnnotationInstance>>(annotationsSize);

        for (int i = 0; i < classesSize; i++) {
            ClassInfo clazz = readClassEntry(stream, masterAnnotations, version);
            addClassToMap(subclasses, clazz.superName(), clazz);
            for (Type interfaceType : clazz.interfaceTypeArray()) {
                addClassToMap(implementors, interfaceType.name(), clazz);
            }
            classes.put(clazz.name(), clazz);
        }


        return new Index(masterAnnotations, subclasses, implementors, classes);
    }

    int toDataVersion(int version) {
        return MAX_DATA_VERSION;
    }
}
