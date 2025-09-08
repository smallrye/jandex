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
import java.util.function.Consumer;

final class SerializedIndexScannerV2 {
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

    SerializedIndexScannerV2(PackedDataInputStream input, int version) {
        this.input = input;
        this.version = version;
    }

    Index read(Consumer<String> messageConsumer) throws IOException {
        try {
            PackedDataInputStream stream = this.input;
            int annotationsSize = stream.readPackedU32();
            messageConsumer.accept("annotationsSize:" + annotationsSize);
            int implementorsSize = stream.readPackedU32();
            messageConsumer.accept("implementorsSize:" + implementorsSize);
            int subinterfacesSize = 0;
            if (version >= 11) {
                subinterfacesSize = stream.readPackedU32();
                messageConsumer.accept("subinterfacesSize:" + subinterfacesSize);
            }
            int subclassesSize = stream.readPackedU32();
            messageConsumer.accept("subclassesSize:" + subclassesSize);
            int usersSize = 0;
            if (version >= 10) {
                usersSize = stream.readPackedU32();
                messageConsumer.accept("usersSize:" + usersSize);
                users = new HashMap<DotName, Set<DotName>>(usersSize);
            }

            readByteTable(stream, messageConsumer);
            readStringTable(stream, messageConsumer);
            readNameTable(stream, messageConsumer);

            typeTable = new Type[stream.readPackedU32() + 1];
            typeListTable = new Type[stream.readPackedU32() + 1][];
            annotationTable = new AnnotationInstance[stream.readPackedU32() + 1];

            readTypeTable(stream, messageConsumer);
            readTypeListTable(stream, messageConsumer);
            if (version >= 10) {
                readUsers(stream, usersSize, messageConsumer);
            }
            readMethodTable(stream, messageConsumer);
            readFieldTable(stream, messageConsumer);
            if (version >= 10) {
                readRecordComponentTable(stream, messageConsumer);
            }
            return readClasses(stream, annotationsSize, implementorsSize, subinterfacesSize, subclassesSize, messageConsumer);
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

    private void readUsers(PackedDataInputStream stream, int usersSize, Consumer<String> messageConsumer) throws IOException {
        for (int i = 0; i < usersSize; i++) {
            String pre = "user[" + i + "]";
            int userIndex = stream.readPackedU32();
            messageConsumer.accept(pre + ".userIndex=" + userIndex);
            DotName user = nameTable[userIndex];
            int usesCount = stream.readPackedU32();
            messageConsumer.accept(pre + ".usesCount=" + usesCount);
            Set<DotName> uses = new HashSet<DotName>(usesCount);
            for (int j = 0; j < usesCount; j++) {
                int nameIndex = stream.readPackedU32();
                messageConsumer.accept(pre + ".uses[" + j + "]=" + nameIndex + ":" + nameTable[nameIndex]);
                uses.add(nameTable[nameIndex]);
            }
            users.put(user, uses);
        }
    }

    private void readByteTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null is the implicit first entry
        int size = stream.readPackedU32() + 1;
        messageConsumer.accept("byteTableSize:" + size);
        byte[][] byteTable = this.byteTable = new byte[size][];
        for (int i = 1; i < size; i++) {
            int len = stream.readPackedU32();
            byteTable[i] = new byte[len];
            stream.readFully(byteTable[i], 0, len);
            messageConsumer.accept("byteTable[" + i + "].length=" + byteTable[i].length);
        }
    }

    private void readStringTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null is the implicit first entry
        int size = stream.readPackedU32() + 1;
        messageConsumer.accept("stringTableSize:" + size);
        String[] stringTable = this.stringTable = new String[size];
        for (int i = 1; i < size; i++) {
            stringTable[i] = stream.readUTF();
            messageConsumer.accept("stringTable[" + i + "]=" + stringTable[i]);
        }
    }

    private void readNameTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null is the implicit first entry
        int entries = stream.readPackedU32() + 1;
        messageConsumer.accept("nameTableSize:" + entries);
        int lastDepth = -1;
        DotName curr = null;

        nameTable = new DotName[entries];
        for (int i = 1; i < entries; i++) {
            // see IndexWriterV2.writeNameTable
            if (version >= 11) {
                int prefixOffset = stream.readPackedU32();
                messageConsumer.accept("nameTable[" + i + "].prefixOffset=" + prefixOffset);
                boolean inner = (prefixOffset & 1) == 1;
                prefixOffset >>= 1;

                int prefixPosition = prefixOffset == 0 ? 0 : i - prefixOffset;
                DotName prefix = nameTable[prefixPosition];
                int localIndex = stream.readPackedU32();
                messageConsumer.accept("nameTable[" + i + "].localIndex=" + localIndex);
                String local = stringTable[localIndex];
                nameTable[i] = new DotName(prefix, local, true, inner);
                messageConsumer.accept("nameTable[" + i + "]=" + nameTable[i]);
            } else {
                int depth = stream.readPackedU32();
                messageConsumer.accept("nameTable[" + i + "].depth=" + depth);
                boolean inner = (depth & 1) == 1;
                depth >>= 1;

                int localIndex = stream.readPackedU32();
                messageConsumer.accept("nameTable[" + i + "].localIndex=" + localIndex);
                String local = stringTable[localIndex];

                if (depth <= lastDepth) {
                    while (lastDepth-- >= depth) {
                        assert curr != null;
                        curr = curr.prefix();
                    }
                }

                nameTable[i] = curr = new DotName(curr, local, true, inner);
                lastDepth = depth;
                messageConsumer.accept("nameTable[" + i + "]=" + nameTable[i]);
            }
        }
    }

    private void readTypeTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        Map<TypeVariableReference, Integer> references = new IdentityHashMap<>();

        // Null is the implicit first entry
        for (int i = 1; i < typeTable.length; i++) {
            typeTable[i] = readTypeEntry(stream, references, "type[" + i + "]", messageConsumer);
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

    private void readTypeListTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null is the implicit first entry
        Type[][] typeListTable = this.typeListTable;
        // Already emitted entries are omitted as gaps in the table portion
        for (int i = findNextNull(typeListTable, 1); i < typeListTable.length; i = findNextNull(typeListTable, i)) {
            typeListTable[i] = readTypeListEntry(stream, "typeList[" + i + "]", messageConsumer);
        }
    }

    private AnnotationInstance[] readAnnotations(PackedDataInputStream stream, AnnotationTarget target, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int size = stream.readPackedU32();
        if (size == 0) {
            return AnnotationInstance.EMPTY_ARRAY;
        }

        AnnotationInstance[] annotations = new AnnotationInstance[size];
        for (int i = 0; i < size; i++) {
            String pre = prefix + ".annotations[" + i + "]";
            int reference = stream.readPackedU32();
            messageConsumer.accept(pre + ".reference=" + reference);
            if (annotationTable[reference] == null) {
                annotationTable[reference] = readAnnotationEntry(stream, target, pre, messageConsumer);
            }

            annotations[i] = annotationTable[reference];
        }
        return annotations;
    }

    private AnnotationValue[] readAnnotationValues(PackedDataInputStream stream, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int numValues = stream.readPackedU32();
        messageConsumer.accept(prefix + ".annotationValuesSize=" + numValues);
        AnnotationValue[] values = numValues > 0 ? new AnnotationValue[numValues] : AnnotationValue.EMPTY_ARRAY;

        for (int i = 0; i < numValues; i++) {
            AnnotationValue value = readAnnotationValue(stream, prefix + ".annotationValues[" + i + "]", messageConsumer);
            values[i] = value;
        }

        return values;
    }

    private AnnotationValue readAnnotationValue(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int nameIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".nameIndex=" + nameIndex);
        String name = stringTable[nameIndex];
        int tag = stream.readByte();
        messageConsumer.accept(prefix + ".tag=" + tag);
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
                nameIndex = stream.readPackedU32();
                value = new AnnotationValue.StringValue(name, stringTable[nameIndex]);
                messageConsumer.accept(prefix + ".AVALUE_STRING.nameIndex=" + nameIndex + ":" + stringTable[nameIndex]);
                break;
            case AVALUE_CLASS:
                int typeIndex = stream.readPackedU32();
                value = new AnnotationValue.ClassValue(name, typeTable[typeIndex]);
                messageConsumer.accept(prefix + ".AVALUE_CLASS.typeIndex=" + typeIndex + ":" + typeTable[typeIndex]);
                break;
            case AVALUE_ENUM:
                nameIndex = stream.readPackedU32();
                messageConsumer.accept(prefix + ".AVALUE_STRING.nameIndex=" + nameIndex + ":" + nameTable[nameIndex]);
                int stringIndex = stream.readPackedU32();
                messageConsumer.accept(prefix + ".AVALUE_STRING.stringIndex=" + stringIndex + ":" + stringTable[stringIndex]);
                value = new AnnotationValue.EnumValue(name, nameTable[nameIndex],
                        stringTable[stringIndex]);
                break;
            case AVALUE_ARRAY:
                value = new AnnotationValue.ArrayValue(name,
                        readAnnotationValues(stream, prefix + ".AVALUE_ARRAY", messageConsumer));
                break;
            case AVALUE_NESTED: {
                int reference = stream.readPackedU32();
                AnnotationInstance nestedInstance = annotationTable[reference];
                if (nestedInstance == null) {
                    nestedInstance = annotationTable[reference] = readAnnotationEntry(stream, null, prefix + ".AVALUE_NESTED",
                            messageConsumer);
                }

                value = new AnnotationValue.NestedAnnotation(name, nestedInstance);
                break;
            }
            default:
                throw new IllegalStateException("Invalid annotation value tag:" + tag);
        }
        return value;
    }

    private AnnotationInstance readAnnotationEntry(PackedDataInputStream stream, AnnotationTarget caller, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        DotName name = nameTable[stream.readPackedU32()];
        AnnotationTarget target = readAnnotationTarget(stream, caller, prefix + ".target", messageConsumer);
        AnnotationValue[] values = readAnnotationValues(stream, prefix + ".values", messageConsumer);
        boolean visible = true;
        if (version >= 11) {
            visible = stream.readBoolean();
            messageConsumer.accept(prefix + ".visible=" + visible);
        }
        return AnnotationInstance.create(name, visible, target, values);
    }

    private Type[] readTypeListReference(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int reference = stream.readPackedU32();
        String pre = prefix + ".typeListReference";
        messageConsumer.accept(pre + "s=" + reference);
        Type[] types = typeListTable[reference];
        if (types != null) {
            return types;
        }

        return typeListTable[reference] = readTypeListEntry(stream, pre, messageConsumer);
    }

    private Type[] readTypeListEntry(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int size = stream.readPackedU32();
        String pre = prefix + ".typeList";
        messageConsumer.accept(pre + "Size=" + size);
        if (size == 0) {
            return Type.EMPTY_ARRAY;
        }

        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            int typeIndex = stream.readPackedU32();
            types[i] = typeTable[typeIndex];
            messageConsumer.accept(pre + "[" + i + "].typeIndex=" + typeIndex + ":" + types[i]);
        }

        return types;
    }

    private Type readTypeEntry(PackedDataInputStream stream, Map<TypeVariableReference, Integer> references, String prefix,
            Consumer<String> messageConsumer)
            throws IOException {
        Type.Kind kind = Type.Kind.fromOrdinal(stream.readUnsignedByte());
        messageConsumer.accept(prefix + ".kind=" + kind);

        switch (kind) {
            case CLASS: {
                int nameIndex = stream.readPackedU32();
                DotName name = nameTable[nameIndex];
                messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + name);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new ClassType(name, annotations);
            }
            case ARRAY: {
                int dimensions = stream.readPackedU32();
                messageConsumer.accept(prefix + ".dimensions=" + dimensions);
                int componentIndex = stream.readPackedU32();
                Type component = typeTable[componentIndex];
                messageConsumer.accept(prefix + ".componentIndex=" + componentIndex + ":" + component);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new ArrayType(component, dimensions, annotations);
            }
            case PRIMITIVE: {
                int primitive = stream.readUnsignedByte();
                Type type = PrimitiveType.fromOridinal(primitive);
                messageConsumer.accept(prefix + ".primitive=" + primitive + ":" + type);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return annotations.length > 0 ? type.copyType(annotations) : type;
            }
            case VOID: {
                Type type = VoidType.VOID;
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return annotations.length > 0 ? type.copyType(annotations) : type;
            }
            case TYPE_VARIABLE: {
                int identifierIndex = stream.readPackedU32();
                String identifier = stringTable[identifierIndex];
                messageConsumer.accept(prefix + ".identifierIndex=" + identifierIndex + ":" + identifier);
                Type[] bounds = readTypeListReference(stream, prefix, messageConsumer);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new TypeVariable(identifier, bounds, annotations);
            }
            case UNRESOLVED_TYPE_VARIABLE: {
                int identifierIndex = stream.readPackedU32();
                String identifier = stringTable[identifierIndex];
                messageConsumer.accept(prefix + ".identifierIndex=" + identifierIndex + ":" + identifier);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new UnresolvedTypeVariable(identifier, annotations);
            }
            case WILDCARD_TYPE: {
                boolean isExtends = stream.readPackedU32() == 1;
                messageConsumer.accept(prefix + ".isExtends=" + isExtends);
                int boundIndex = stream.readPackedU32();
                Type bound = typeTable[boundIndex]; // may be null in case of an unbounded wildcard
                messageConsumer.accept(prefix + ".boundIndex=" + boundIndex + ":" + bound);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new WildcardType(bound, isExtends, annotations);

            }
            case PARAMETERIZED_TYPE: {
                int nameIndex = stream.readPackedU32();
                DotName name = nameTable[nameIndex];
                messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + name);
                int reference = stream.readPackedU32();
                messageConsumer.accept(prefix + ".reference=" + reference);
                Type owner = typeTable[reference];
                Type[] parameters = readTypeListReference(stream, prefix, messageConsumer);
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                return new ParameterizedType(name, parameters, owner, annotations);
            }
            case TYPE_VARIABLE_REFERENCE: {
                int identifierIndex = stream.readPackedU32();
                String identifier = stringTable[identifierIndex];
                messageConsumer.accept(prefix + ".identifierIndex=" + identifierIndex + ":" + identifier);
                int position = stream.readPackedU32();
                messageConsumer.accept(prefix + ".position=" + position);
                DotName className = null;
                if (version >= 12) {
                    int nameIndex = stream.readPackedU32();
                    className = nameTable[nameIndex];
                    messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + className);
                }
                AnnotationInstance[] annotations = readAnnotations(stream, null, prefix, messageConsumer);
                TypeVariableReference reference = new TypeVariableReference(identifier, null, annotations, className);
                references.put(reference, position);
                return reference;
            }
        }

        throw new IllegalStateException("Unrecognized type: " + kind);
    }

    private AnnotationTarget readAnnotationTarget(PackedDataInputStream stream, AnnotationTarget caller, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        byte tag = stream.readByte();
        messageConsumer.accept(prefix + ".tag=" + tag);
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
                messageConsumer.accept(prefix + ".parameter=" + parameter);
                return new MethodParameterInfo((MethodInfo) caller, parameter);
            }
            case EMPTY_TYPE_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                boolean isReceiver = stream.readPackedU32() == 1;
                messageConsumer.accept(prefix + ".isReceiver=" + isReceiver);
                return new EmptyTypeTarget(caller, target, isReceiver);
            }
            case CLASS_EXTENDS_TYPE_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                int pos = stream.readPackedU32();
                messageConsumer.accept(prefix + ".pos=" + pos);
                return new ClassExtendsTypeTarget(caller, target, pos);
            }
            case TYPE_PARAMETER_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                int pos = stream.readPackedU32();
                messageConsumer.accept(prefix + ".pos=" + pos);
                return new TypeParameterTypeTarget(caller, target, pos);
            }
            case TYPE_PARAMETER_BOUND_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                int pos = stream.readPackedU32();
                messageConsumer.accept(prefix + ".pos=" + pos);
                int bound = stream.readPackedU32();
                messageConsumer.accept(prefix + ".bound=" + bound);
                return new TypeParameterBoundTypeTarget(caller, target, pos, bound);
            }
            case METHOD_PARAMETER_TYPE_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                int pos = stream.readPackedU32();
                messageConsumer.accept(prefix + ".pos=" + pos);
                return new MethodParameterTypeTarget(caller, target, pos);
            }
            case THROWS_TYPE_TAG: {
                int targetIndex = stream.readPackedU32();
                Type target = typeTable[targetIndex];
                messageConsumer.accept(prefix + ".targetIndex=" + targetIndex + ":" + target);
                int pos = stream.readPackedU32();
                messageConsumer.accept(prefix + ".pos=" + pos);
                return new ThrowsTypeTarget(caller, target, pos);
            }
        }

        throw new IllegalStateException("Invalid tag: " + tag);
    }

    private void readMethodTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        messageConsumer.accept("methodTableSize=" + size);
        methodTable = new MethodInternal[size];
        for (int i = 1; i < size; i++) {
            String prefix = "methodTable[" + i + "]";
            methodTable[i] = readMethodEntry(stream, prefix, messageConsumer);
        }

    }

    private void readFieldTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        messageConsumer.accept("nameTableSize=" + size);
        fieldTable = new FieldInternal[size];
        for (int i = 1; i < size; i++) {
            String prefix = "fieldTable[" + i + "]";
            fieldTable[i] = readFieldEntry(stream, prefix, messageConsumer);
        }
    }

    private void readRecordComponentTable(PackedDataInputStream stream, Consumer<String> messageConsumer) throws IOException {
        // Null holds the first slot
        int size = stream.readPackedU32() + 1;
        messageConsumer.accept("recordComponentTableSize=" + size);
        recordComponentTable = new RecordComponentInternal[size];
        for (int i = 1; i < size; i++) {
            String prefix = "recordComponentTable[" + i + "]";
            recordComponentTable[i] = readRecordComponentEntry(stream, prefix, messageConsumer);
        }
    }

    private MethodInternal readMethodEntry(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int nameIndex = stream.readPackedU32();
        byte[] name = byteTable[nameIndex];
        messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + new String(name));
        short flags = (short) stream.readPackedU32();
        messageConsumer.accept(prefix + ".flags=" + flags);
        int typeParametersIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".typeParametersIndex=" + typeParametersIndex);
        Type[] typeParameters = typeListTable[typeParametersIndex];
        int reference = stream.readPackedU32();
        Type receiverType = typeTable[reference];
        messageConsumer.accept(prefix + ".reference=" + reference + ":" + receiverType);
        int returnTypeIndex = stream.readPackedU32();
        Type returnType = typeTable[returnTypeIndex];
        messageConsumer.accept(prefix + ".returnTypeIndex=" + returnTypeIndex + ":" + returnType);
        int parametersIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".parametersIndex=" + parametersIndex);
        Type[] parameters = typeListTable[parametersIndex];
        Type[] descriptorParameters = parameters;
        if (version >= 11) {
            int descriptorParamertersIndex = stream.readPackedU32();
            messageConsumer.accept(prefix + ".descriptorParametersIndex=" + descriptorParamertersIndex);
            descriptorParameters = typeListTable[descriptorParamertersIndex];
        }
        int exceptionsIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".exceptionsIndex=" + exceptionsIndex);
        Type[] exceptions = typeListTable[exceptionsIndex];
        AnnotationValue defaultValue = null;
        if (version >= 7) {
            boolean hasDefaultValue = stream.readByte() > 0;
            messageConsumer.accept(prefix + ".hasDefaultValue=" + hasDefaultValue);
            if (hasDefaultValue) {
                defaultValue = readAnnotationValue(stream, prefix, messageConsumer);
            }
        }
        byte[][] methodParameterBytes = MethodInternal.EMPTY_PARAMETER_NAMES;
        if (version >= 8) {
            int size = stream.readPackedU32();
            messageConsumer.accept(prefix + ".methodParameters.length=" + size);
            if (size > 0) {
                methodParameterBytes = new byte[size][];
                for (int i = 0; i < size; i++) {
                    methodParameterBytes[i] = byteTable[stream.readPackedU32()];
                }
            }
        }

        MethodInfo methodInfo = new MethodInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, methodInfo, prefix, messageConsumer);
        MethodInternal methodInternal = new MethodInternal(name, methodParameterBytes, descriptorParameters, returnType, flags,
                receiverType, typeParameters,
                exceptions, annotations, defaultValue);
        methodInfo.setMethodInternal(methodInternal);
        methodInfo.setParameters(parameters);
        return methodInternal;
    }

    private FieldInternal readFieldEntry(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int nameIndex = stream.readPackedU32();
        byte[] name = byteTable[nameIndex];
        messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + new String(name));
        short flags = (short) stream.readPackedU32();
        messageConsumer.accept(prefix + ".flags=" + flags);
        int typeIndex = stream.readPackedU32();
        Type type = typeTable[typeIndex];
        messageConsumer.accept(prefix + ".typeIndex=" + typeIndex + ":" + type);

        FieldInfo fieldInfo = new FieldInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, fieldInfo, prefix, messageConsumer);
        FieldInternal fieldInternal = new FieldInternal(name, type, flags, annotations);
        fieldInfo.setFieldInternal(fieldInternal);
        return fieldInternal;
    }

    private RecordComponentInternal readRecordComponentEntry(PackedDataInputStream stream, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int nameIndex = stream.readPackedU32();
        byte[] name = byteTable[nameIndex];
        messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + new String(name));
        int typeIndex = stream.readPackedU32();
        Type type = typeTable[typeIndex];
        messageConsumer.accept(prefix + ".typeIndex=" + typeIndex + ":" + type);

        RecordComponentInfo recordComponentInfo = new RecordComponentInfo();
        AnnotationInstance[] annotations = readAnnotations(stream, recordComponentInfo, prefix, messageConsumer);
        RecordComponentInternal recordComponentInternal = new RecordComponentInternal(name, type, annotations);
        recordComponentInfo.setRecordComponentInternal(recordComponentInternal);
        return recordComponentInternal;
    }

    private ClassInfo readClassEntry(PackedDataInputStream stream,
            Map<DotName, List<AnnotationInstance>> masterAnnotations, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int nameIndex = stream.readPackedU32();
        DotName name = nameTable[nameIndex];
        messageConsumer.accept(prefix + ".nameIndex=" + nameIndex + ":" + name);
        short flags = (short) stream.readPackedU32();
        messageConsumer.accept(prefix + ".flags=" + flags);
        boolean hasNoArgsConstructor = false;
        if (version >= 11) {
            hasNoArgsConstructor = stream.readBoolean();
            messageConsumer.accept(prefix + ".hasNoArgsConstructor=" + hasNoArgsConstructor);
        }
        int superTypeIndex = stream.readPackedU32();
        Type superType = typeTable[superTypeIndex];
        messageConsumer.accept(prefix + ".superTypeIndex=" + superTypeIndex + ":" + superType);
        int typeParametersIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".typeParametersIndex=" + typeParametersIndex);
        Type[] typeParameters = typeListTable[typeParametersIndex];
        int interfaceTypesIndex = stream.readPackedU32();
        messageConsumer.accept(prefix + ".interfaceTypesIndex=" + interfaceTypesIndex);
        Type[] interfaceTypes = typeListTable[interfaceTypesIndex];

        boolean hasEnclosingMethod = false;
        boolean hasNesting = false;
        if (version >= 9) {
            int nestingMask = stream.readUnsignedByte();
            messageConsumer.accept(prefix + ".nestingMask=" + nestingMask);
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
            int enclosingClassIndex = stream.readPackedU32();
            enclosingClass = nameTable[enclosingClassIndex];
            messageConsumer.accept(prefix + ".enclosingClassIndex=" + enclosingClassIndex + ":" + enclosingClass);
            int simpleNameIndex = stream.readPackedU32();
            simpleName = stringTable[simpleNameIndex];
            messageConsumer.accept(prefix + ".simpleNameIndex=" + simpleNameIndex + ":" + simpleName);
            if (version >= 13) {
                int enclosingClassInitializerIndex = stream.readPackedU32();
                messageConsumer.accept(prefix + ".enclosingClassInInitializer=" + enclosingClassInitializerIndex);
                enclosingClassInInitializer = nameTable[enclosingClassInitializerIndex];
            }
            enclosingMethod = hasEnclosingMethod ? readEnclosingMethod(stream, prefix + ".enclosingMethod", messageConsumer)
                    : null;
        }

        Set<DotName> memberClasses = null;
        if (version >= 11) {
            int memberClassesCount = stream.readPackedU32();
            messageConsumer.accept(prefix + ".memberClassesCount=" + memberClassesCount);
            if (memberClassesCount > 0) {
                memberClasses = new HashSet<>(memberClassesCount);
                for (int i = 0; i < memberClassesCount; i++) {
                    int memberClassIndex = stream.readPackedU32();
                    messageConsumer
                            .accept(prefix + ".memberClassIndex=" + memberClassIndex + ":" + nameTable[memberClassIndex]);
                    memberClasses.add(nameTable[memberClassIndex]);
                }
            }
        }

        Set<DotName> permittedSubclasses = null;
        if (version >= 12) {
            int permittedSubclassesCount = stream.readPackedU32();
            messageConsumer.accept(prefix + ".permittedSubclassesCount=" + permittedSubclassesCount);
            if (permittedSubclassesCount > 0) {
                permittedSubclasses = new HashSet<>(permittedSubclassesCount);
                for (int i = 0; i < permittedSubclassesCount; i++) {
                    int permittedSubclassNameIndex = stream.readPackedU32();
                    messageConsumer.accept(prefix + ".permittedSubclassNameIndex=" + permittedSubclassNameIndex + ":"
                            + nameTable[permittedSubclassNameIndex]);
                    permittedSubclasses.add(nameTable[permittedSubclassNameIndex]);
                }
            }
        }

        int size = stream.readPackedU32();
        messageConsumer.accept(prefix + ".size=" + size);

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

        FieldInternal[] fields = readClassFields(stream, clazz, prefix, messageConsumer);
        clazz.setFieldArray(fields);

        if (version >= 10) {
            clazz.setFieldPositionArray(byteTable[stream.readPackedU32()]);
        }

        MethodInternal[] methods = readClassMethods(stream, clazz, prefix, messageConsumer);
        clazz.setMethodArray(methods);

        if (version >= 10) {
            clazz.setMethodPositionArray(byteTable[stream.readPackedU32()]);
        }

        if (version >= 10) {
            RecordComponentInternal[] recordComponents = readClassRecordComponents(stream, clazz, prefix, messageConsumer);
            clazz.setRecordComponentArray(recordComponents);
            clazz.setRecordComponentPositionArray(byteTable[stream.readPackedU32()]);
        }

        for (int i = 0; i < size; i++) {
            List<AnnotationInstance> instances = convertToList(readAnnotations(stream, clazz, prefix, messageConsumer));
            if (instances.size() > 0) {
                DotName annotationName = instances.get(0).name();
                annotations.put(annotationName, instances);
                addToMaster(masterAnnotations, annotationName, instances);
            }
        }

        clazz.setAnnotations(annotations);

        return clazz;
    }

    private ModuleInfo readModuleEntry(PackedDataInputStream stream, ClassInfo moduleInfoClass, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int moduleNameIndex = stream.readPackedU32();
        DotName moduleName = nameTable[moduleNameIndex];
        messageConsumer.accept(prefix + ".moduleNameIndex=" + moduleNameIndex + ":" + moduleName);
        short moduleFlags = (short) stream.readPackedU32();
        messageConsumer.accept(prefix + ".moduleFlags=" + moduleFlags);
        int moduleVersionIndex = stream.readPackedU32();
        String moduleVersion = stringTable[moduleVersionIndex];
        messageConsumer.accept(prefix + ".moduleVersionIndex=" + moduleVersionIndex + ":" + moduleVersion);
        int mainClassIndex = stream.readPackedU32();
        DotName mainClass = nameTable[mainClassIndex];
        messageConsumer.accept(prefix + ".mainClassIndex=" + mainClassIndex + ":" + mainClass);

        ModuleInfo module = new ModuleInfo(moduleInfoClass, moduleName, moduleFlags, moduleVersion);
        module.setMainClass(mainClass);

        // requires
        int requiredCount = stream.readPackedU32();
        List<ModuleInfo.RequiredModuleInfo> requires = Utils.listOfCapacity(requiredCount);

        for (int i = 0; i < requiredCount; i++) {
            int nameIndex = stream.readPackedU32();
            DotName name = nameTable[nameIndex];
            messageConsumer.accept(prefix + ".requires[" + i + "].nameIndex=" + nameIndex + ":" + name);
            short flags = (short) stream.readPackedU32();
            messageConsumer.accept(prefix + ".requires[" + i + "].flags=" + flags);
            int versionIndex = stream.readPackedU32();
            String version = stringTable[versionIndex];
            messageConsumer.accept(prefix + ".requires[" + i + "].versionIndex=" + versionIndex + ":" + version);
            requires.add(new ModuleInfo.RequiredModuleInfo(name, flags, version));
        }

        module.setRequires(requires);

        // exports
        int exportedCount = stream.readPackedU32();
        messageConsumer.accept(prefix + ".exports.length=" + exportedCount);
        List<ModuleInfo.ExportedPackageInfo> exports = Utils.listOfCapacity(exportedCount);

        for (int i = 0; i < exportedCount; i++) {
            String pre = prefix + ".exports[" + i + "]";
            int sourceIndex = stream.readPackedU32();
            DotName source = nameTable[sourceIndex];
            messageConsumer.accept(pre + ".sourceIndex=" + sourceIndex + ":" + source);
            short flags = (short) stream.readPackedU32();
            messageConsumer.accept(pre + ".flags=" + flags);
            List<DotName> targets = readDotNames(stream, pre, messageConsumer);
            exports.add(new ModuleInfo.ExportedPackageInfo(source, flags, targets));
        }

        module.setExports(exports);

        // uses
        module.setUses(readDotNames(stream, prefix, messageConsumer));

        // opens
        int openedCount = stream.readPackedU32();
        messageConsumer.accept(prefix + ".opens.length=" + openedCount);
        List<ModuleInfo.OpenedPackageInfo> opens = Utils.listOfCapacity(openedCount);

        for (int i = 0; i < openedCount; i++) {
            int sourceIndex = stream.readPackedU32();
            String pre = prefix + ".opens[" + i + "]";
            DotName source = nameTable[sourceIndex];
            messageConsumer.accept(pre + ".sourceIndex=" + sourceIndex + ":" + source);
            short flags = (short) stream.readPackedU32();
            messageConsumer.accept(pre + ".flags=" + flags);
            List<DotName> targets = readDotNames(stream, prefix, messageConsumer);
            opens.add(new ModuleInfo.OpenedPackageInfo(source, flags, targets));
        }

        module.setOpens(opens);

        // provides
        int providedCount = stream.readPackedU32();
        messageConsumer.accept(prefix + ".provides.length=" + providedCount);
        List<ModuleInfo.ProvidedServiceInfo> provides = Utils.listOfCapacity(providedCount);

        for (int i = 0; i < providedCount; i++) {
            String pre = prefix + ".provides[" + i + "]";
            int serviceIndex = stream.readPackedU32();
            DotName service = nameTable[serviceIndex];
            messageConsumer.accept(pre + ".serviceIndex=" + serviceIndex + ":" + service);
            List<DotName> providers = readDotNames(stream, pre, messageConsumer);
            provides.add(new ModuleInfo.ProvidedServiceInfo(service, providers));
        }

        module.setProvides(provides);

        // packages
        module.setPackages(readDotNames(stream, prefix, messageConsumer));

        return module;
    }

    private List<DotName> readDotNames(PackedDataInputStream stream, String prefix, Consumer<String> messageConsumer)
            throws IOException {
        int size = stream.readPackedU32();
        messageConsumer.accept(prefix + ".size=" + size);
        List<DotName> names = Utils.listOfCapacity(size);

        for (int i = 0; i < size; i++) {
            int nameIndex = stream.readPackedU32();
            messageConsumer.accept(prefix + ".names[" + i + "].nameIndex=" + nameIndex + ":" + nameTable[nameIndex]);
            names.add(nameTable[nameIndex]);
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

    private FieldInternal[] readClassFields(PackedDataInputStream stream, ClassInfo clazz, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int len = stream.readPackedU32();
        messageConsumer.accept(prefix + ".fields.len=" + len);
        FieldInternal[] fields = len > 0 ? new FieldInternal[len] : FieldInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            int fieldIndex = stream.readPackedU32();
            FieldInternal field = fieldTable[fieldIndex];
            messageConsumer.accept(prefix + ".fields[" + i + "].index=" + fieldIndex + ":" + field);
            updateAnnotationTargetInfo(field.annotationArray(), clazz);
            fields[i] = field;
        }
        return fields;
    }

    private RecordComponentInternal[] readClassRecordComponents(PackedDataInputStream stream, ClassInfo clazz, String prefix,
            Consumer<String> messageConsumer)
            throws IOException {
        int len = stream.readPackedU32();
        messageConsumer.accept(prefix + ".recordComponents.len=" + len);
        RecordComponentInternal[] recordComponents = len > 0 ? new RecordComponentInternal[len]
                : RecordComponentInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            int recordComponentIndex = stream.readPackedU32();
            RecordComponentInternal recordComponent = recordComponentTable[recordComponentIndex];
            messageConsumer
                    .accept(prefix + ".recordComponents[" + i + "].index=" + recordComponentIndex + ":" + recordComponent);
            updateAnnotationTargetInfo(recordComponent.annotationArray(), clazz);
            recordComponents[i] = recordComponent;
        }
        return recordComponents;
    }

    private MethodInternal[] readClassMethods(PackedDataInputStream stream, ClassInfo clazz, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        int len = stream.readPackedU32();
        messageConsumer.accept(prefix + ".classMethods.len=" + len);
        MethodInternal[] methods = len > 0 ? new MethodInternal[len] : MethodInternal.EMPTY_ARRAY;
        for (int i = 0; i < len; i++) {
            int methodIndex = stream.readPackedU32();
            MethodInternal method = methodTable[methodIndex];
            messageConsumer.accept(prefix + ".classMethods[" + i + "].index=" + methodIndex + ":" + method);
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

    private ClassInfo.EnclosingMethodInfo readEnclosingMethod(PackedDataInputStream stream, String prefix,
            Consumer<String> messageConsumer) throws IOException {
        if (version < 9 && stream.readUnsignedByte() != HAS_ENCLOSING_METHOD) {
            return null;
        }

        int eNameIndex = stream.readPackedU32();
        String eName = stringTable[eNameIndex];
        messageConsumer.accept(prefix + ".enclosingMethod.eNameIndex=" + eNameIndex + ":" + eName);
        int eClassIndex = stream.readPackedU32();
        DotName eClass = nameTable[eClassIndex];
        messageConsumer.accept(prefix + ".enclosingMethod.eClassIndex=" + eClassIndex + ":" + eClass);
        int returnTypeIndex = stream.readPackedU32();
        Type returnType = typeTable[returnTypeIndex];
        messageConsumer.accept(prefix + ".enclosingMethod.returnTypeIndex=" + returnTypeIndex + ":" + returnType);
        int parametersIndex = stream.readPackedU32();
        Type[] parameters = typeListTable[parametersIndex];
        messageConsumer.accept(prefix + ".enclosingMethod.parametersIndex=" + parametersIndex);
        return new ClassInfo.EnclosingMethodInfo(eName, returnType, parameters, eClass);
    }

    private Index readClasses(PackedDataInputStream stream,
            int annotationsSize, int implementorsSize, int subinterfacesSize, int subclassesSize,
            Consumer<String> messageConsumer) throws IOException {
        int classesSize = stream.readPackedU32();
        messageConsumer.accept("classesSize=" + classesSize);
        HashMap<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>(classesSize);
        HashMap<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>(subclassesSize);
        HashMap<DotName, List<ClassInfo>> subinterfaces = new HashMap<DotName, List<ClassInfo>>(subinterfacesSize);
        HashMap<DotName, List<ClassInfo>> implementors = new HashMap<DotName, List<ClassInfo>>(implementorsSize);
        HashMap<DotName, List<AnnotationInstance>> masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>(
                annotationsSize);

        for (int i = 0; i < classesSize; i++) {
            String prefix = "classes[" + i + "]";
            ClassInfo clazz = readClassEntry(stream, masterAnnotations, prefix, messageConsumer);
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

        Map<DotName, ModuleInfo> modules = (version >= 10) ? readModules(stream, masterAnnotations, messageConsumer)
                : Collections.<DotName, ModuleInfo> emptyMap();

        return Index.create(masterAnnotations, subclasses, subinterfaces, implementors, classes, modules, users);
    }

    private Map<DotName, ModuleInfo> readModules(PackedDataInputStream stream,
            Map<DotName, List<AnnotationInstance>> masterAnnotations, Consumer<String> messageConsumer) throws IOException {

        int modulesSize = stream.readPackedU32();
        Map<DotName, ModuleInfo> modules = modulesSize > 0 ? new HashMap<DotName, ModuleInfo>(modulesSize)
                : Collections.<DotName, ModuleInfo> emptyMap();

        for (int i = 0; i < modulesSize; i++) {
            String prefix = "module[" + i + "]";
            ClassInfo clazz = readClassEntry(stream, masterAnnotations, prefix, messageConsumer);
            ModuleInfo module = readModuleEntry(stream, clazz, prefix, messageConsumer);
            modules.put(module.name(), module);
        }

        return modules;
    }
}
