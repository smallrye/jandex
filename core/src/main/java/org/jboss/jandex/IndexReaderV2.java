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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    static final int MAX_VERSION = 13;
    private static final byte NULL_TARGET_TAG = 0;
    private static final byte FIELD_TAG = 1;
    private static final byte METHOD_TAG = 2;
    private static final byte METHOD_PARAMETER_TAG = 3;
    private static final byte CLASS_TAG = 4;
    private static final byte EMPTY_TYPE_TAG = 5;
    private static final byte CLASS_EXTENDS_TYPE_TAG = 6;
    private static final byte TYPE_PARAMETER_TAG = 7;
    private static final byte TYPE_PARAMETER_BOUND_TAG = 8;
    private static final byte METHOD_PARAMETER_TYPE_TAG = 9;
    private static final byte THROWS_TYPE_TAG = 10;
    private static final byte RECORD_COMPONENT_TAG = 11;
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

    private final PackedDataInputStream input;
    private final int version;
    private byte[][] byteTable;
    private String[] stringTable;
    private DotName[] nameTable;
    private Type[] typeTable;
    private Type[][] typeListTable;
    private AnnotationInstance[] annotationTable;
    private MethodInternal[] methodTable;
    private FieldInternal[] fieldTable;
    private RecordComponentInternal[] recordComponentTable;
    private HashMap<DotName, Set<DotName>> users;

    IndexReaderV2(PackedDataInputStream input, int version) {
        this.input = input;
        this.version = version;
    }

    Index read() throws IOException {
        try {
            PackedDataInputStream stream = this.input;
            int annotationsSize = stream.readPackedU32();
            int implementorsSize = stream.readPackedU32();
            int subinterfacesSize = 0;
            if (version >= 11) {
                subinterfacesSize = stream.readPackedU32();
            }
            int subclassesSize = stream.readPackedU32();
            int usersSize = 0;
            if (version >= 10) {
                usersSize = stream.readPackedU32();
                users = new HashMap<DotName, Set<DotName>>(usersSize);
            }

            readByteTable(stream);
            readStringTable(stream);
            readNameTable(stream);

            typeTable = new Type[stream.readPackedU32() + 1];
            typeListTable = new Type[stream.readPackedU32() + 1][];
            annotationTable = new AnnotationInstance[stream.readPackedU32() + 1];

            readTypeTable(stream);
            readTypeListTable(stream);
            if (version >= 10) {
                readUsers(stream, usersSize);
            }
            readMethodTable(stream);
            readFieldTable(stream);
            if (version >= 10) {
                readRecordComponentTable(stream);
            }
            return readClasses(stream, annotationsSize, implementorsSize, subinterfacesSize, subclassesSize);
        } finally {
            byteTable = null;
            stringTable = null;
            nameTable = null;
            typeTable = null;
            typeListTable = null;
            annotationTable = null;
            methodTable = null;
            fieldTable = null;
            recordComponentTable = null;
            users = null;
        }
    }

    private void readUsers(PackedDataInputStream stream, int usersSize) throws IOException {
        for (int i = 0; i < usersSize; i++) {
            DotName user = nameTable[stream.readPackedU32()];
            int usesCount = stream.readPackedU32();
            Set<DotName> uses = new HashSet<DotName>(usesCount);
            for (int j = 0; j < usesCount; j++) {
                uses.add(nameTable[stream.readPackedU32()]);
            }
            users.put(user, uses);
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
            // see IndexWriterV2.writeNameTable
            if (version >= 11) {
                int prefixOffset = stream.readPackedU32();
                boolean inner = (prefixOffset & 1) == 1;
                prefixOffset >>= 1;

                int prefixPosition = prefixOffset == 0 ? 0 : i - prefixOffset;
                DotName prefix = nameTable[prefixPosition];
                String local = stringTable[stream.readPackedU32()];
                nameTable[i] = new DotName(prefix, local, true, inner);
            } else {
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
    }

    private void readTypeTable(PackedDataInputStream stream) throws IOException {
        Map<TypeVariableReference, Integer> references = new IdentityHashMap<>();

        // Null is the implicit first entry
        for (int i = 1; i < typeTable.length; i++) {
            typeTable[i] = readTypeEntry(stream, references);
        }

        // patch type variable references (see IndexWriterV2#addType)
        for (Entry<TypeVariableReference, Integer> entry : references.entrySet()) {
            TypeVariableReference reference = entry.getKey();
            Integer position = entry.getValue();
            assert position != null;
            assert typeTable[position] instanceof TypeVariable;
            reference.setTarget((TypeVariable) typeTable[position]);
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
        AnnotationValue[] values = numValues > 0 ? new AnnotationValue[numValues] : AnnotationValue.EMPTY_ARRAY;

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
                value = new AnnotationValue.EnumValue(name, nameTable[stream.readPackedU32()],
                        stringTable[stream.readPackedU32()]);
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
        boolean visible = true;
        if (version >= 11) {
            visible = stream.readBoolean();
        }
        return AnnotationInstance.create(name, visible, target, values);
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

    private Type readTypeEntry(PackedDataInputStream stream, Map<TypeVariableReference, Integer> references)
            throws IOException {
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
                Type bound = typeTable[stream.readPackedU32()]; // may be null in case of an unbounded wildcard
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
            case TYPE_VARIABLE_REFERENCE: {
                String identifier = stringTable[stream.readPackedU32()];
                int position = stream.readPackedU32();
                DotName className = null;
                if (version >= 12) {
                    className = nameTable[stream.readPackedU32()];
                }
                AnnotationInstance[] annotations = readAnnotations(stream, null);
                TypeVariableReference reference = new TypeVariableReference(identifier, null, annotations, className);
                references.put(reference, position);
                return reference;
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
            case RECORD_COMPONENT_TAG:
                return caller;
            case METHOD_PARAMETER_TAG: {
                short parameter = (short) stream.readPackedU32();
                return new MethodParameterInfo((MethodInfo) caller, parameter);
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

    private void readMethodTable(PackedDataInputStream stream) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        methodTable = new MethodInternal[size];
        for (int i = 1; i < size; i++) {
            methodTable[i] = readMethodEntry(stream);
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

    private void readRecordComponentTable(PackedDataInputStream stream) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        recordComponentTable = new RecordComponentInternal[size];
        for (int i = 1; i < size; i++) {
            recordComponentTable[i] = readRecordComponentEntry(stream);
        }
    }

    private MethodInternal readMethodEntry(PackedDataInputStream stream) throws IOException {
        byte[] name = byteTable[stream.readPackedU32()];
        short flags = (short) stream.readPackedU32();
        Type[] typeParameters = typeListTable[stream.readPackedU32()];
        int reference = stream.readPackedU32();
        Type receiverType = typeTable[reference];
        Type returnType = typeTable[stream.readPackedU32()];
        Type[] parameters = typeListTable[stream.readPackedU32()];
        Type[] descriptorParameters = parameters;
        if (version >= 11) {
            descriptorParameters = typeListTable[stream.readPackedU32()];
        }
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
            if (size > 0) {
                methodParameterBytes = new byte[size][];
                for (int i = 0; i < size; i++) {
                    methodParameterBytes[i] = byteTable[stream.readPackedU32()];
                }
            }
        }

        MethodInfo methodInfo = new MethodInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, methodInfo);
        MethodInternal methodInternal = new MethodInternal(name, methodParameterBytes, descriptorParameters, returnType, flags,
                receiverType, typeParameters,
                exceptions, annotations, defaultValue);
        methodInfo.setMethodInternal(methodInternal);
        methodInfo.setParameters(parameters);
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

    private RecordComponentInternal readRecordComponentEntry(PackedDataInputStream stream) throws IOException {
        byte[] name = byteTable[stream.readPackedU32()];
        Type type = typeTable[stream.readPackedU32()];

        RecordComponentInfo recordComponentInfo = new RecordComponentInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, recordComponentInfo);
        RecordComponentInternal recordComponentInternal = new RecordComponentInternal(name, type, annotations);
        recordComponentInfo.setRecordComponentInternal(recordComponentInternal);
        return recordComponentInternal;
    }

    private ClassInfo readClassEntry(PackedDataInputStream stream,
            Map<DotName, List<AnnotationInstance>> masterAnnotations) throws IOException {
        DotName name = nameTable[stream.readPackedU32()];
        short flags = (short) stream.readPackedU32();
        boolean hasNoArgsConstructor = false;
        if (version >= 11) {
            hasNoArgsConstructor = stream.readBoolean();
        }
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
        String simpleName = null;
        DotName enclosingClassInInitializer = null;
        ClassInfo.EnclosingMethodInfo enclosingMethod = null;

        if (hasNesting) {
            enclosingClass = nameTable[stream.readPackedU32()];
            simpleName = stringTable[stream.readPackedU32()];
            if (version >= 13) {
                enclosingClassInInitializer = nameTable[stream.readPackedU32()];
            }
            enclosingMethod = hasEnclosingMethod ? readEnclosingMethod(stream) : null;
        }

        Set<DotName> memberClasses = null;
        if (version >= 11) {
            int memberClassesCount = stream.readPackedU32();
            if (memberClassesCount > 0) {
                memberClasses = new HashSet<>(memberClassesCount);
                for (int i = 0; i < memberClassesCount; i++) {
                    memberClasses.add(nameTable[stream.readPackedU32()]);
                }
            }
        }

        Set<DotName> permittedSubclasses = null;
        if (version >= 12) {
            int permittedSubclassesCount = stream.readPackedU32();
            if (permittedSubclassesCount > 0) {
                permittedSubclasses = new HashSet<>(permittedSubclassesCount);
                for (int i = 0; i < permittedSubclassesCount; i++) {
                    permittedSubclasses.add(nameTable[stream.readPackedU32()]);
                }
            }
        }

        int size = stream.readPackedU32();

        Map<DotName, List<AnnotationInstance>> annotations = size > 0
                ? new HashMap<DotName, List<AnnotationInstance>>(size)
                : Collections.<DotName, List<AnnotationInstance>> emptyMap();
        ClassInfo clazz = new ClassInfo(name, superType, flags, interfaceTypes);
        clazz.setHasNoArgsConstructor(hasNoArgsConstructor);
        clazz.setTypeParameters(typeParameters);

        if (hasNesting) {
            clazz.setEnclosingMethod(enclosingMethod);
            // Version 8 and earlier records inner type info regardless of
            // whether or not it is an inner type
            clazz.setInnerClassInfo(enclosingClass, simpleName, version >= 9);
            clazz.setEnclosingClassInInitializer(enclosingClassInInitializer);
        }
        if (memberClasses != null) {
            clazz.setMemberClasses(memberClasses);
        }
        if (permittedSubclasses != null) {
            clazz.setPermittedSubclasses(permittedSubclasses);
        }

        FieldInternal[] fields = readClassFields(stream, clazz);
        clazz.setFieldArray(fields);

        if (version >= 10) {
            clazz.setFieldPositionArray(byteTable[stream.readPackedU32()]);
        }

        MethodInternal[] methods = readClassMethods(stream, clazz);
        clazz.setMethodArray(methods);

        if (version >= 10) {
            clazz.setMethodPositionArray(byteTable[stream.readPackedU32()]);
        }

        if (version >= 10) {
            RecordComponentInternal[] recordComponents = readClassRecordComponents(stream, clazz);
            clazz.setRecordComponentArray(recordComponents);
            clazz.setRecordComponentPositionArray(byteTable[stream.readPackedU32()]);
        }

        for (int i = 0; i < size; i++) {
            List<AnnotationInstance> instances = convertToList(readAnnotations(stream, clazz));
            if (instances.size() > 0) {
                DotName annotationName = instances.get(0).name();
                annotations.put(annotationName, instances);
                addToMaster(masterAnnotations, annotationName, instances);
            }
        }

        clazz.setAnnotations(annotations);

        return clazz;
    }

    private ModuleInfo readModuleEntry(PackedDataInputStream stream, ClassInfo moduleInfoClass) throws IOException {
        DotName moduleName = nameTable[stream.readPackedU32()];
        short moduleFlags = (short) stream.readPackedU32();
        String moduleVersion = stringTable[stream.readPackedU32()];
        DotName mainClass = nameTable[stream.readPackedU32()];

        ModuleInfo module = new ModuleInfo(moduleInfoClass, moduleName, moduleFlags, moduleVersion);
        module.setMainClass(mainClass);

        // requires
        int requiredCount = stream.readPackedU32();
        List<ModuleInfo.RequiredModuleInfo> requires = Utils.listOfCapacity(requiredCount);

        for (int i = 0; i < requiredCount; i++) {
            DotName name = nameTable[stream.readPackedU32()];
            short flags = (short) stream.readPackedU32();
            String version = stringTable[stream.readPackedU32()];
            requires.add(new ModuleInfo.RequiredModuleInfo(name, flags, version));
        }

        module.setRequires(requires);

        // exports
        int exportedCount = stream.readPackedU32();
        List<ModuleInfo.ExportedPackageInfo> exports = Utils.listOfCapacity(exportedCount);

        for (int i = 0; i < exportedCount; i++) {
            DotName source = nameTable[stream.readPackedU32()];
            short flags = (short) stream.readPackedU32();
            List<DotName> targets = readDotNames(stream);
            exports.add(new ModuleInfo.ExportedPackageInfo(source, flags, targets));
        }

        module.setExports(exports);

        // uses
        module.setUses(readDotNames(stream));

        // opens
        int openedCount = stream.readPackedU32();
        List<ModuleInfo.OpenedPackageInfo> opens = Utils.listOfCapacity(openedCount);

        for (int i = 0; i < openedCount; i++) {
            DotName source = nameTable[stream.readPackedU32()];
            short flags = (short) stream.readPackedU32();
            List<DotName> targets = readDotNames(stream);
            opens.add(new ModuleInfo.OpenedPackageInfo(source, flags, targets));
        }

        module.setOpens(opens);

        // provides
        int providedCount = stream.readPackedU32();
        List<ModuleInfo.ProvidedServiceInfo> provides = Utils.listOfCapacity(providedCount);

        for (int i = 0; i < providedCount; i++) {
            DotName service = nameTable[stream.readPackedU32()];
            List<DotName> providers = readDotNames(stream);
            provides.add(new ModuleInfo.ProvidedServiceInfo(service, providers));
        }

        module.setProvides(provides);

        // packages
        module.setPackages(readDotNames(stream));

        return module;
    }

    private List<DotName> readDotNames(PackedDataInputStream stream) throws IOException {
        int size = stream.readPackedU32();
        List<DotName> names = Utils.listOfCapacity(size);

        for (int i = 0; i < size; i++) {
            names.add(nameTable[stream.readPackedU32()]);
        }

        return names;
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

        return new ImmutableArrayList<>(annotationInstances);
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
        FieldInternal[] fields = len > 0 ? new FieldInternal[len] : FieldInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            FieldInternal field = fieldTable[stream.readPackedU32()];
            updateAnnotationTargetInfo(field.annotationArray(), clazz);
            fields[i] = field;
        }
        return fields;
    }

    private RecordComponentInternal[] readClassRecordComponents(PackedDataInputStream stream, ClassInfo clazz)
            throws IOException {
        int len = stream.readPackedU32();
        RecordComponentInternal[] recordComponents = len > 0 ? new RecordComponentInternal[len]
                : RecordComponentInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            RecordComponentInternal recordComponent = recordComponentTable[stream.readPackedU32()];
            updateAnnotationTargetInfo(recordComponent.annotationArray(), clazz);
            recordComponents[i] = recordComponent;
        }
        return recordComponents;
    }

    private MethodInternal[] readClassMethods(PackedDataInputStream stream, ClassInfo clazz) throws IOException {
        int len = stream.readPackedU32();
        MethodInternal[] methods = len > 0 ? new MethodInternal[len] : MethodInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            MethodInternal method = methodTable[stream.readPackedU32()];
            updateAnnotationTargetInfo(method.annotationArray(), clazz);
            methods[i] = method;

            if (version < 11 && method.parameterTypesArray().length == 0
                    && Arrays.equals(Utils.INIT_METHOD_NAME, method.nameBytes())) {
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
            if (target instanceof TypeTarget) {
                target = ((TypeTarget) target).enclosingTarget();
            }
            if (target instanceof MethodInfo) {
                ((MethodInfo) target).setClassInfo(clazz);
            } else if (target instanceof MethodParameterInfo) {
                ((MethodParameterInfo) target).method().setClassInfo(clazz);
            } else if (target instanceof FieldInfo) {
                ((FieldInfo) target).setClassInfo(clazz);
            } else if (target instanceof RecordComponentInfo) {
                ((RecordComponentInfo) target).setClassInfo(clazz);
            }
        }
    }

    private ClassInfo.EnclosingMethodInfo readEnclosingMethod(PackedDataInputStream stream) throws IOException {
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
            int annotationsSize, int implementorsSize, int subinterfacesSize, int subclassesSize) throws IOException {
        int classesSize = stream.readPackedU32();
        HashMap<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>(classesSize);
        HashMap<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>(subclassesSize);
        HashMap<DotName, List<ClassInfo>> subinterfaces = new HashMap<DotName, List<ClassInfo>>(subinterfacesSize);
        HashMap<DotName, List<ClassInfo>> implementors = new HashMap<DotName, List<ClassInfo>>(implementorsSize);
        HashMap<DotName, List<AnnotationInstance>> masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>(
                annotationsSize);

        for (int i = 0; i < classesSize; i++) {
            ClassInfo clazz = readClassEntry(stream, masterAnnotations);
            addClassToMap(subclasses, clazz.superName(), clazz);
            for (Type interfaceType : clazz.interfaceTypeArray()) {
                if (Modifier.isInterface(clazz.flags())) {
                    addClassToMap(subinterfaces, interfaceType.name(), clazz);
                }
                // interfaces are intentionally added to implementors
                // it is counter-intuitive, but we keep it to maintain behavioral compatibility
                addClassToMap(implementors, interfaceType.name(), clazz);
            }
            classes.put(clazz.name(), clazz);
        }
        Map<DotName, List<ClassInfo>> users = null;
        if (version >= 10) {
            users = new HashMap<DotName, List<ClassInfo>>(this.users.size());
            for (Entry<DotName, Set<DotName>> entry : this.users.entrySet()) {
                List<ClassInfo> usedBy = new ArrayList<ClassInfo>(entry.getValue().size());
                users.put(entry.getKey(), usedBy);
                for (DotName usedByName : entry.getValue()) {
                    usedBy.add(classes.get(usedByName));
                }
            }
        } else {
            users = Collections.emptyMap();
        }

        Map<DotName, ModuleInfo> modules = (version >= 10) ? readModules(stream, masterAnnotations)
                : Collections.<DotName, ModuleInfo> emptyMap();

        return Index.create(masterAnnotations, subclasses, subinterfaces, implementors, classes, modules, users);
    }

    private Map<DotName, ModuleInfo> readModules(PackedDataInputStream stream,
            Map<DotName, List<AnnotationInstance>> masterAnnotations) throws IOException {

        int modulesSize = stream.readPackedU32();
        Map<DotName, ModuleInfo> modules = modulesSize > 0 ? new HashMap<DotName, ModuleInfo>(modulesSize)
                : Collections.<DotName, ModuleInfo> emptyMap();

        for (int i = 0; i < modulesSize; i++) {
            ClassInfo clazz = readClassEntry(stream, masterAnnotations);
            ModuleInfo module = readModuleEntry(stream, clazz);
            modules.put(module.name(), module);
        }

        return modules;
    }
}
