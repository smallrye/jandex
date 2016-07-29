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

import static org.jboss.jandex.ClassInfo.EnclosingMethodInfo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes and indexes the annotation and key structural information of a set
 * of classes. The indexer will purposefully skip any class that is not Java 5
 * or later. It will also do a basic/quick structural scan on any class it
 * determines does not have annotations.
 *
 * <p>
 * The Indexer operates on input streams that point to class file data. Input
 * streams do not need to be buffered, as the indexer already does this. There
 * is also no limit to the number of class file streams the indexer can process,
 * other than available memory.
 *
 * <p>
 * The Indexer attempts to minimize the final memory state of the index, but to
 * do this it must maintain additional in-process state (intern tables etc)
 * until the index is complete.
 *
 * <p>
 * Numerous optimizations are taken during indexing to attempt to minimize the
 * CPU and I/O cost, however, the Java class file format was not designed for
 * partial searching, which ultimately limits the efficiency of processing them.
 *
 * <p>
 * <b>Thread-Safety</b> This class is not thread-safe can <b>not</b> be
 * shared between threads. The index it produces however is thread-safe.
 *
 * @author Jason T. Greene
 *
 */
public final class Indexer {

    private final static int CONSTANT_CLASS = 7;
    private final static int CONSTANT_FIELDREF = 9;
    private final static int CONSTANT_METHODREF = 10;
    private final static int CONSTANT_INTERFACEMETHODREF = 11;
    private final static int CONSTANT_STRING = 8;
    private final static int CONSTANT_INTEGER = 3;
    private final static int CONSTANT_FLOAT = 4;
    private final static int CONSTANT_LONG = 5;
    private final static int CONSTANT_DOUBLE = 6;
    private final static int CONSTANT_NAMEANDTYPE = 12;
    private final static int CONSTANT_UTF8 = 1;
    private final static int CONSTANT_INVOKEDYNAMIC = 18;
    private final static int CONSTANT_METHODHANDLE = 15;
    private final static int CONSTANT_METHODTYPE = 16;

    // "RuntimeVisibleAnnotations"
    private final static byte[] RUNTIME_ANNOTATIONS = new byte[] {
        0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65, 0x56, 0x69, 0x73, 0x69, 0x62,
        0x6c, 0x65, 0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e,
        0x73
    };

    // "RuntimeVisibleParameterAnnotations"
    private final static byte[] RUNTIME_PARAM_ANNOTATIONS = new byte[] {
        0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65, 0x56, 0x69, 0x73, 0x69, 0x62,
        0x6c, 0x65, 0x50, 0x61, 0x72, 0x61, 0x6d, 0x65, 0x74, 0x65, 0x72, 0x41,
        0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x73
    };


    // "RuntimeTypeVisibleAnnotations"
    private final static byte[] RUNTIME_TYPE_ANNOTATIONS = new byte[] {
        0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65, 0x56, 0x69, 0x73, 0x69, 0x62,
        0x6c, 0x65, 0x54, 0x79, 0x70, 0x65, 0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61,
        0x74, 0x69, 0x6f, 0x6e, 0x73
    };

    // "Signature"
    private final static byte[] SIGNATURE = new byte[] {
        0x53, 0x69, 0x67, 0x6e, 0x61, 0x74, 0x75, 0x72, 0x65
    };

    // "Exceptions"
    private final static byte[] EXCEPTIONS = new byte[] {
        0x45, 0x78, 0x63, 0x65, 0x70, 0x74, 0x69, 0x6f, 0x6e, 0x73
    };

    // "InnerClasses"
    private final static byte[] INNER_CLASSES = new byte[] {
        0x49, 0x6e, 0x6e, 0x65, 0x72, 0x43, 0x6c, 0x61, 0x73, 0x73, 0x65, 0x73
    };

    // "EnclosingMethod"
    private final static byte[] ENCLOSING_METHOD = new byte[] {
        0x45, 0x6e, 0x63, 0x6c, 0x6f, 0x73, 0x69, 0x6e, 0x67, 0x4d, 0x65, 0x74, 0x68, 0x6f, 0x64
    };

    private final static int RUNTIME_ANNOTATIONS_LEN = RUNTIME_ANNOTATIONS.length;
    private final static int RUNTIME_PARAM_ANNOTATIONS_LEN = RUNTIME_PARAM_ANNOTATIONS.length;
    private final static int RUNTIME_TYPE_ANNOTATIONS_LEN = RUNTIME_TYPE_ANNOTATIONS.length;
    private final static int SIGNATURE_LEN = SIGNATURE.length;
    private final static int EXCEPTIONS_LEN = EXCEPTIONS.length;
    private final static int INNER_CLASSES_LEN = INNER_CLASSES.length;
    private final static int ENCLOSING_METHOD_LEN = ENCLOSING_METHOD.length;

    private final static int HAS_RUNTIME_ANNOTATION = 1;
    private final static int HAS_RUNTIME_PARAM_ANNOTATION = 2;
    private final static int HAS_RUNTIME_TYPE_ANNOTATION = 3;
    private final static int HAS_SIGNATURE = 4;
    private final static int HAS_EXCEPTIONS = 5;
    private final static int HAS_INNER_CLASSES = 6;
    private final static int HAS_ENCLOSING_METHOD = 7;

    private final static byte[] INIT_METHOD_NAME = Utils.toUTF8("<init>");

    private IdentityHashMap<AnnotationTarget, Object> signaturePresent;

    private static class InnerClassInfo {
        private InnerClassInfo(DotName innerClass, DotName enclosingClass, String simpleName, int flags) {
            this.innnerClass = innerClass;
            this.enclosingClass = enclosingClass;
            this.simpleName = simpleName;
            this.flags = flags;
        }

        private final DotName innnerClass;
        private DotName enclosingClass;
        private String simpleName;
        private int flags;

    }

    private static boolean match(byte[] target, int offset, byte[] expected) {
        if (target.length - offset < expected.length)
            return false;

        for (int i = 0; i < expected.length; i++)
            if (target[offset + i] != expected[i])
                return false;

        return true;
    }

    private static byte[] sizeToFit(byte[] buf, int needed, int offset, int remainingEntries) {
        if (offset + needed > buf.length) {
            buf = Arrays.copyOf(buf, buf.length + Math.max(needed, (remainingEntries + 1) * 20));
        }
        return buf;
    }

    private static void skipFully(InputStream s, long n) throws IOException {
        long skipped;
        long total = 0;

        while (total < n) {
            skipped = s.skip(n - total);
            if (skipped < 0)
                throw new EOFException();
            total += skipped;

            // Skip is not guaranteed to distinguish between EOF and nothing-read
            if (skipped == 0) {
                if (s.read() < 0) {
                    throw new EOFException();
                }
                total++;
            }
        }
    }


    // Class lifespan fields
    private byte[] constantPool;
    private int[] constantPoolOffsets;
    private byte[] constantPoolAnnoAttrributes;
    private ClassInfo currentClass;
    private HashMap<DotName, List<AnnotationInstance>> classAnnotations;
    private ArrayList<AnnotationInstance> elementAnnotations;
    private List<Object> signatures;
    private Map<DotName, InnerClassInfo> innerClasses;
    private IdentityHashMap<AnnotationTarget, List<TypeAnnotationState>> typeAnnotations;
    private List<MethodInfo> methods;
    private List<FieldInfo> fields;

    // Index lifespan fields
    private Map<DotName, List<AnnotationInstance>> masterAnnotations;
    private Map<DotName, List<ClassInfo>> subclasses;
    private Map<DotName, List<ClassInfo>> implementors;
    private Map<DotName, ClassInfo> classes;
    private NameTable names;
    private GenericSignatureParser signatureParser;


    private void initIndexMaps() {
        if (masterAnnotations == null)
            masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>();

        if (subclasses == null)
            subclasses = new HashMap<DotName, List<ClassInfo>>();

        if (implementors == null)
            implementors = new HashMap<DotName, List<ClassInfo>>();

        if (classes == null)
            classes = new HashMap<DotName, ClassInfo>();

        if (names == null)
            names = new NameTable();

        if (signatureParser == null) {
            signatureParser = new GenericSignatureParser(names);
        }
    }

    private void initClassFields() {
        elementAnnotations = new ArrayList<AnnotationInstance>();
        signaturePresent = new IdentityHashMap<AnnotationTarget, Object>();
        signatures = new ArrayList<Object>();
        typeAnnotations = new IdentityHashMap<AnnotationTarget, List<TypeAnnotationState>>();
    }


    private void processMethodInfo(DataInputStream data) throws IOException {
        int numMethods = data.readUnsignedShort();
        List<MethodInfo> methods = numMethods > 0 ? new ArrayList<MethodInfo>(numMethods) : Collections.<MethodInfo>emptyList();

        for (int i = 0; i < numMethods; i++) {
            short flags = (short) data.readUnsignedShort();
            byte[] name = intern(decodeUtf8EntryAsBytes(data.readUnsignedShort()));
            String descriptor = decodeUtf8Entry(data.readUnsignedShort());

            IntegerHolder pos = new IntegerHolder();
            Type[] parameters = intern(parseMethodArgs(descriptor, pos));
            Type returnType = parseType(descriptor, pos);

            MethodInfo method = new MethodInfo(currentClass, name, parameters, returnType, flags);

            if (parameters.length == 0 && Arrays.equals(INIT_METHOD_NAME, name)) {
                currentClass.setHasNoArgsConstructor(true);
            }
            processAttributes(data, method);
            method.setAnnotations(elementAnnotations);
            elementAnnotations.clear();

            methods.add(method);
        }

        this.methods = methods;
    }

    private void processFieldInfo(DataInputStream data) throws IOException {
        int numFields = data.readUnsignedShort();
        List<FieldInfo> fields = numFields > 0 ? new ArrayList<FieldInfo>(numFields) : Collections.<FieldInfo>emptyList();
        for (int i = 0; i < numFields; i++) {
            short flags = (short) data.readUnsignedShort();
            byte[] name = intern(decodeUtf8EntryAsBytes(data.readUnsignedShort()));
            Type type = parseType(decodeUtf8Entry(data.readUnsignedShort()));
            FieldInfo field = new FieldInfo(currentClass, name, type, flags);

            processAttributes(data, field);
            field.setAnnotations(elementAnnotations);
            elementAnnotations.clear();
            fields.add(field);
        }
        this.fields = fields;
    }

    private void processAttributes(DataInputStream data, AnnotationTarget target) throws IOException {
        int numAttrs = data.readUnsignedShort();
        for (int a = 0; a < numAttrs; a++) {
            int index = data.readUnsignedShort();
            long attributeLen = data.readInt() & 0xFFFFFFFFL;
            byte annotationAttribute = constantPoolAnnoAttrributes[index - 1];
            if (annotationAttribute == HAS_RUNTIME_ANNOTATION) {
                processAnnotations(data, target);
            } else if (annotationAttribute == HAS_RUNTIME_PARAM_ANNOTATION) {
                if (!(target instanceof MethodInfo))
                    throw new IllegalStateException("RuntimeVisibleParameterAnnotations appeared on a non-method");
                int numParameters = data.readUnsignedByte();
                for (short p = 0; p < numParameters; p++) {
                    processAnnotations(data, new MethodParameterInfo((MethodInfo) target, p));
                }
            } else if (annotationAttribute == HAS_RUNTIME_TYPE_ANNOTATION) {
                processTypeAnnotations(data, target);
            } else if (annotationAttribute == HAS_SIGNATURE) {
                processSignature(data, target);
            } else if (annotationAttribute == HAS_EXCEPTIONS && target instanceof MethodInfo) {
                processExceptions(data, (MethodInfo) target);
            } else if (annotationAttribute == HAS_INNER_CLASSES && target instanceof ClassInfo) {
                processInnerClasses(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_ENCLOSING_METHOD && target instanceof ClassInfo) {
                processEnclosingMethod(data, (ClassInfo) target);
            } else {
                skipFully(data, attributeLen);
            }
        }
    }

    private void processAnnotations(DataInputStream data, AnnotationTarget target) throws IOException {
        int numAnnotations = data.readUnsignedShort();
        while (numAnnotations-- > 0)
            processAnnotation(data, target);
    }

    private void processInnerClasses(DataInputStream data, ClassInfo target) throws IOException {
        int numClasses = data.readUnsignedShort();
        innerClasses = numClasses > 0 ? new HashMap<DotName, InnerClassInfo>(numClasses)
                                      : Collections.<DotName, InnerClassInfo>emptyMap();
        for (int i = 0; i < numClasses; i++) {
            DotName innerClass = decodeClassEntry(data.readUnsignedShort());
            int outerIndex = data.readUnsignedShort();
            DotName outerClass = outerIndex == 0 ? null : decodeClassEntry(outerIndex);
            int simpleIndex = data.readUnsignedShort();
            String simpleName = simpleIndex == 0 ? null : decodeUtf8Entry(simpleIndex);
            int flags = data.readUnsignedShort();

            if (innerClass.equals(target.name())) {
                target.setInnerClassInfo(outerClass, simpleName);
                target.setFlags((short)flags);
            }

            innerClasses.put(innerClass, new InnerClassInfo(innerClass, outerClass, simpleName, flags));
        }
    }

    private void processEnclosingMethod(DataInputStream data, ClassInfo target) throws IOException {
        int classIndex = data.readUnsignedShort();
        int index = data.readUnsignedShort();
        if (index == 0) {
            return; // Enclosed in a static or an instance variable
        }

        DotName enclosingClass = decodeClassEntry(classIndex);
        NameAndType nameAndType = decodeNameAndTypeEntry(index);

        IntegerHolder pos = new IntegerHolder();
        Type[] parameters = intern(parseMethodArgs(nameAndType.descriptor, pos));
        Type returnType = parseType(nameAndType.descriptor, pos);

        EnclosingMethodInfo method = new EnclosingMethodInfo(nameAndType.name, returnType, parameters, enclosingClass);
        target.setEnclosingMethod(method);
    }


    private void processTypeAnnotations(DataInputStream data, AnnotationTarget target) throws IOException {
        int numAnnotations = data.readUnsignedShort();
        List<TypeAnnotationState> annotations = new ArrayList<TypeAnnotationState>(numAnnotations);

        for (int i = 0; i < numAnnotations; i++) {
            TypeAnnotationState annotation = processTypeAnnotation(data, target);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }

        typeAnnotations.put(target, annotations);
    }


    private TypeAnnotationState processTypeAnnotation(DataInputStream data, AnnotationTarget target) throws IOException {
        int targetType = data.readUnsignedByte();
        TypeTarget typeTarget = null;
        switch (targetType) {
            case 0x00: // CLASS_TYPE_PARAMETER
            case 0x01: // METHOD_TYPE_PARAMETER
            {
                typeTarget = new TypeParameterTypeTarget(target, data.readUnsignedByte());
                break;
            }
            case 0x10: // CLASS_EXTENDS
            {
                if (!(target instanceof ClassInfo)) {
                    throw new IllegalStateException("Class extends type annotation appeared on a non class target");
                }
                typeTarget = new ClassExtendsTypeTarget((ClassInfo)target, data.readUnsignedShort());
                break;
            }
            case 0x11: // CLASS_TYPE_PARAMETER_BOUND
            case 0x12: // METHOD_TYPE_PARAMETER_BOUND
            {
                typeTarget = new TypeParameterBoundTypeTarget(target, data.readUnsignedByte(), data.readUnsignedByte());
                break;
            }
            case 0x13: // FIELD
            case 0x14: // METHOD_RETURN
            case 0x15: // METHOD_RECEIVER
                typeTarget = new EmptyTypeTarget(target, targetType == 0x15);
                break;
            case 0x16: // METHOD_FORMAL_PARAMETER
            {
                if (!(target instanceof MethodInfo)) {
                    throw new IllegalStateException("Method parameter type annotation appeared on a non-method target");
                }
                typeTarget = new MethodParameterTypeTarget((MethodInfo)target, data.readUnsignedByte());
                break;
            }
            case 0x17: // THROWS
            {
                if (!(target instanceof MethodInfo)) {
                    throw new IllegalStateException("Throws type annotation appeared on a non-method target");
                }
                typeTarget = new ThrowsTypeTarget((MethodInfo) target, data.readUnsignedShort());
                break;
            }
            // Skip code attribute values, which shouldn't be present

            case 0x40: // LOCAL_VARIABLE
            case 0x41: // RESOURCE_VARIABLE
                skipFully(data, data.readUnsignedShort() * 6);
                break;
            case 0x42: // EXCEPTION_PARAMETER
                skipFully(data, 2);
                break;
            case 0x43: // INSTANCEOF
            case 0x44: // NEW
            case 0x45: // CONSTRUCTOR_REFERENCE
            case 0x46: // METHOD_REFERENCE
                skipFully(data, 2);
                break;
            case 0x47: // CAST
            case 0x48: // CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT
            case 0x49: // METHOD_INVOCATION_TYPE_ARGUMENT
            case 0x4A: // CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT
            case 0x4B: // METHOD_REFERENCE_TYPE_ARGUMENT
                skipFully(data, 3);
                break;
            default:
                throw new IllegalStateException("Invalid type annotation target type");

        }

        if (typeTarget == null) {
            return null;
        }

        BooleanHolder genericsRequired = new BooleanHolder();
        ArrayList<PathElement> pathElements = processTargetPath(data, genericsRequired);
        AnnotationInstance annotation = processAnnotation(data, typeTarget);
        return new TypeAnnotationState(typeTarget, annotation, pathElements, genericsRequired.bool);
    }

    private void resolveTypeAnnotations() {
        for (Map.Entry<AnnotationTarget, List<TypeAnnotationState>> entry : typeAnnotations.entrySet()) {
            AnnotationTarget key = entry.getKey();
            List<TypeAnnotationState> annotations = entry.getValue();

            for (TypeAnnotationState annotation : annotations) {
                resolveTypeAnnotation(key, annotation);
            }
        }
    }

    private void updateTypeTargets() {
        for (Map.Entry<AnnotationTarget, List<TypeAnnotationState>> entry : typeAnnotations.entrySet()) {
            AnnotationTarget key = entry.getKey();
            List<TypeAnnotationState> annotations = entry.getValue();

            for (TypeAnnotationState annotation : annotations) {
                updateTypeTarget(key, annotation);
            }
        }
    }

    private static Type[] getTypeParameters(AnnotationTarget target) {
        if (target instanceof ClassInfo) {
            return ((ClassInfo)target).typeParameterArray();
        } else if (target instanceof MethodInfo) {
            return ((MethodInfo)target).typeParameterArray();
        }

        throw new IllegalStateException("Type annotation referred to type parameters on an invalid target: " + target);
    }

    private static Type[] copyTypeParameters(AnnotationTarget target) {
        if (target instanceof ClassInfo) {
            return ((ClassInfo)target).typeParameterArray().clone();
        } else if (target instanceof MethodInfo) {
            return ((MethodInfo)target).typeParameterArray().clone();
        }

        throw new IllegalStateException("Type annotation referred to type parameters on an invalid target: " + target);
    }

    private void setTypeParameters(AnnotationTarget target, Type[] typeParameters) {
        if (target instanceof ClassInfo) {
            ((ClassInfo)target).setTypeParameters(typeParameters);
            return;
        } else if (target instanceof MethodInfo) {
            ((MethodInfo)target).setTypeParameters(typeParameters);
            return;
        }

        throw new IllegalStateException("Type annotation referred to type parameters on an invalid target: " + target);

    }

    private void resolveTypeAnnotation(AnnotationTarget target, TypeAnnotationState typeAnnotationState) {
        // Signature is erroneously omitted from bridge methods with generic type annotations
        if (typeAnnotationState.genericsRequired && !signaturePresent.containsKey(target)) {
            typeAnnotationState.target.setTarget(VoidType.VOID);
            return;
        }

        TypeTarget typeTarget = typeAnnotationState.target;

        if (typeTarget.usage() == TypeTarget.Usage.TYPE_PARAMETER_BOUND) {
            TypeParameterBoundTypeTarget bound = (TypeParameterBoundTypeTarget) typeTarget;
            Type[] types = copyTypeParameters(target);
            int index = bound.position();
            if (index >= types.length) {
                return;
            }

            TypeVariable type = types[index].asTypeVariable();
            int boundIndex = bound.boundPosition();
            if (boundIndex >= type.boundArray().length) {
                return;
            }
            type = type.copyType(boundIndex, resolveTypePath(type.boundArray()[boundIndex], typeAnnotationState));
            types[index] = intern(type);
            setTypeParameters(target, intern(types));
        } else if (typeTarget.usage() == TypeTarget.Usage.TYPE_PARAMETER) {
            TypeParameterTypeTarget parameter = (TypeParameterTypeTarget) typeTarget;
            Type[] types = copyTypeParameters(target);
            int index = parameter.position();
            if (index >= types.length) {
                return;
            }

            types[index] = resolveTypePath(types[index], typeAnnotationState);
            setTypeParameters(target, intern(types));
        } else if (typeTarget.usage() == TypeTarget.Usage.CLASS_EXTENDS) {
            ClassInfo clazz = (ClassInfo) target;
            ClassExtendsTypeTarget extendsTarget = (ClassExtendsTypeTarget) typeTarget;
            int index = extendsTarget.position();
            if (index == 65535) {
                clazz.setSuperClassType(resolveTypePath(clazz.superClassType(), typeAnnotationState));
            } else if (index < clazz.interfaceTypes().size()) {
                Type[] types = clazz.copyInterfaceTypes();
                types[index] = resolveTypePath(types[index], typeAnnotationState);
                clazz.setInterfaceTypes(intern(types));
            }
        } else if (typeTarget.usage() == TypeTarget.Usage.METHOD_PARAMETER) {
            MethodInfo method = (MethodInfo) target;
            MethodParameterTypeTarget parameter = (MethodParameterTypeTarget) typeTarget;
            int index = parameter.position();
            Type[] types = method.copyParameters();

            if (index >= types.length) {
                return;
            }

            types[index] = resolveTypePath(types[index], typeAnnotationState);
            method.setParameters(intern(types));
        } else if (typeTarget.usage() == TypeTarget.Usage.EMPTY && target instanceof FieldInfo) {
            FieldInfo field = (FieldInfo) target;
            field.setType(resolveTypePath(field.type(), typeAnnotationState));
        } else if (typeTarget.usage() == TypeTarget.Usage.EMPTY && target instanceof MethodInfo) {
            MethodInfo method = (MethodInfo) target;
            if (((EmptyTypeTarget) typeTarget).isReceiver()) {
                method.setReceiverType(resolveTypePath(method.receiverType(), typeAnnotationState));
            } else {
                method.setReturnType(resolveTypePath(method.returnType(), typeAnnotationState));
            }
        } else if (typeTarget.usage() == TypeTarget.Usage.THROWS  && target instanceof MethodInfo) {
            MethodInfo method = (MethodInfo) target;
            int position = ((ThrowsTypeTarget)typeTarget).position();
            Type[] exceptions = method.copyExceptions();

            if (position >= exceptions.length) {
                return;
            }

            exceptions[position] = resolveTypePath(exceptions[position], typeAnnotationState);
            method.setExceptions(intern(exceptions));
        }
    }

    private Type resolveTypePath(Type type, TypeAnnotationState typeAnnotationState) {
        PathElementStack elements = typeAnnotationState.pathElements;
        PathElement element = elements.pop();
        if (element == null) {
            type = intern(type.addAnnotation(new AnnotationInstance(typeAnnotationState.annotation, null)));
            typeAnnotationState.target.setTarget(type);       // FIXME
            // Clone the instance with a null target so that it can be interned
            return type;
        }

        switch (element.kind) {
            case ARRAY: {
                ArrayType arrayType = type.asArrayType();
                int dimensions = arrayType.dimensions();
                while (--dimensions > 0 && elements.size() > 0 && elements.peek().kind == PathElement.Kind.ARRAY) {
                    elements.pop();
                }

                Type nested = dimensions > 0 ? new ArrayType(arrayType.component(), dimensions) : arrayType.component();
                nested = resolveTypePath(nested, typeAnnotationState);

                return intern(arrayType.copyType(nested, arrayType.dimensions() - dimensions));
            }
            case PARAMETERIZED: {
                ParameterizedType parameterizedType = type.asParameterizedType();
                Type[] arguments = parameterizedType.argumentsArray().clone();
                int pos = element.pos;
                if (pos >= arguments.length) {
                    throw new IllegalStateException("Type annotation referred to a type argument that does not exist");
                }

                arguments[pos] = resolveTypePath(arguments[pos], typeAnnotationState);
                return intern(parameterizedType.copyType(arguments));
            }
            case WILDCARD_BOUND: {
                WildcardType wildcardType = type.asWildcardType();
                Type bound = resolveTypePath(wildcardType.bound(), typeAnnotationState);
                return intern(wildcardType.copyType(bound));
            }
            case NESTED: {
                int depth = popNestedDepth(elements);

                return rebuildNestedType(type, depth, typeAnnotationState);
            }
        }

        throw new IllegalStateException("Unknown path element");
    }

    private int popNestedDepth(PathElementStack elements) {
        int depth = 1;
        while (elements.size() > 0 && elements.peek().kind == PathElement.Kind.NESTED) {
            elements.pop();
            depth++;
        }
        return depth;
    }


    private void updateTypeTarget(AnnotationTarget enclosingTarget, TypeAnnotationState typeAnnotationState) {
        // Signature is erroneously omitted from bridge methods with generic type annotations
        if (typeAnnotationState.genericsRequired && !signaturePresent.containsKey(enclosingTarget)) {
            return;
        }

        typeAnnotationState.pathElements.reset();

        TypeTarget target = typeAnnotationState.target;
        Type type;
        switch (target.usage()) {
            case EMPTY: {
                if (enclosingTarget instanceof FieldInfo) {
                    type = ((FieldInfo)enclosingTarget).type();
                } else {
                    MethodInfo method = (MethodInfo) enclosingTarget;
                    type = target.asEmpty().isReceiver() ? method.receiverType() : method.returnType();
                }
                break;
            }
            case CLASS_EXTENDS: {
                ClassInfo clazz = (ClassInfo) enclosingTarget;
                int position = target.asClassExtends().position();
                type = position == 65535 ? clazz.superClassType() : clazz.interfaceTypeArray()[position];
                break;
            }
            case METHOD_PARAMETER: {
                MethodInfo method = (MethodInfo) enclosingTarget;
                type = method.methodInternal().parameterArray()[target.asMethodParameterType().position()];
                break;
            }
            case TYPE_PARAMETER: {
                type = getTypeParameters(enclosingTarget)[target.asTypeParameter().position()];
                break;
            }
            case TYPE_PARAMETER_BOUND: {
                TypeParameterBoundTypeTarget boundTarget = target.asTypeParameterBound();
                type = getTypeParameters(enclosingTarget)[boundTarget.position()]
                           .asTypeVariable().boundArray()[boundTarget.boundPosition()];
                break;
            }
            case THROWS: {
                type = ((MethodInfo)enclosingTarget).methodInternal().exceptionArray()[target.asThrows().position()];
                break;
            }
            default:
                throw new IllegalStateException("Unknown type target: " + target.usage());
        }

        type = searchTypePath(type, typeAnnotationState);
        target.setTarget(type);
    }

    private Type searchTypePath(Type type, TypeAnnotationState typeAnnotationState) {
        PathElementStack elements = typeAnnotationState.pathElements;
        PathElement element = elements.pop();
        if (element == null) {
            return type;
        }

        switch (element.kind) {
            case ARRAY: {
                ArrayType arrayType = type.asArrayType();
                int dimensions = arrayType.dimensions();
                while (--dimensions > 0 && elements.size() > 0 && elements.peek().kind == PathElement.Kind.ARRAY) {
                    elements.pop();
                }
                assert dimensions == 0;
                return searchTypePath(arrayType.component(), typeAnnotationState);
            }
            case PARAMETERIZED: {
                ParameterizedType parameterizedType = type.asParameterizedType();
                return searchTypePath(parameterizedType.argumentsArray()[element.pos], typeAnnotationState);
            }
            case WILDCARD_BOUND: {
                return searchTypePath(type.asWildcardType().bound(), typeAnnotationState);
            }
            case NESTED: {
                int depth = popNestedDepth(elements);
                return searchNestedType(type, depth, typeAnnotationState);
            }
        }

        throw new IllegalStateException("Unknown path element");
    }


    private Type rebuildNestedType(Type type, int depth, TypeAnnotationState typeAnnotationState) {
        DotName name = type.name();
        Map<DotName, Type> ownerMap = buildOwnerMap(type);
        ArrayDeque<InnerClassInfo> classes = buildClassesQueue(name);

        Type last = null;
        for (InnerClassInfo current : classes) {
            DotName currentName = current.innnerClass;
            Type oType = ownerMap.get(currentName);

            // Static classes do not count for NESTED path elements
            if (depth > 0 && !Modifier.isStatic(current.flags)) {
                --depth;
            }

            if (last != null) {
                last = intern(oType != null ? convertParameterized(oType).copyType(last)
                                            : new ParameterizedType(currentName, null, last));
            } else if (oType != null) {
                last = oType;
            }


            if (depth == 0) {
                if (last == null) {
                    last = intern(new ClassType(currentName));
                }

                last = resolveTypePath(last, typeAnnotationState);

                // Assignment to -1 messes up IDEA data-flow, use -- instead
                depth--;
            }
        }

        if (last == null) {
            throw new IllegalStateException("Required class information is missing");
        }

        return last;
    }

    private ParameterizedType convertParameterized(Type oType) {
        return oType instanceof ClassType ? oType.asClassType().toParameterizedType() : oType.asParameterizedType();
    }

    private Type searchNestedType(Type type, int depth, TypeAnnotationState typeAnnotationState) {
        DotName name = type.name();
        Map<DotName, Type> ownerMap = buildOwnerMap(type);
        ArrayDeque<InnerClassInfo> classes = buildClassesQueue(name);

        for (InnerClassInfo current : classes) {
            DotName currentName = current.innnerClass;

            // Static classes do not count for NESTED path elements
            if (depth > 0 && !Modifier.isStatic(current.flags)) {
                --depth;
            }

            if (depth == 0) {
                Type owner = ownerMap.get(currentName);
                return searchTypePath(owner == null ? type : owner, typeAnnotationState);
            }
        }

        throw new IllegalStateException("Required class information is missing");
    }

    private ArrayDeque<InnerClassInfo> buildClassesQueue(DotName name) {
        ArrayDeque<InnerClassInfo> classes = new ArrayDeque<InnerClassInfo>();

        InnerClassInfo info = innerClasses.get(name);
        while (info != null) {
            classes.addFirst(info);
            name = info.enclosingClass;
            info = name != null ? innerClasses.get(name) : null;
        }
        return classes;
    }

    private Map<DotName, Type> buildOwnerMap(Type type) {
        Map<DotName, Type> pTypeTree = new HashMap<DotName, Type>();

        Type nextType = type;
        do {
            pTypeTree.put(nextType.name(), nextType);
            nextType = nextType instanceof ParameterizedType ? nextType.asParameterizedType().owner() : null;
        } while (nextType != null);
        return pTypeTree;
    }

    private static class PathElement {
        private static enum Kind {ARRAY, NESTED, WILDCARD_BOUND, PARAMETERIZED}
        private static Kind[] KINDS = Kind.values();
        private Kind kind;
        private int pos;

        private PathElement(Kind kind, int pos) {
            this.kind = kind;
            this.pos = pos;
        }
    }

    private static class PathElementStack {
        private int elementPos;
        private final ArrayList<PathElement> pathElements;

        PathElementStack(ArrayList<PathElement> pathElements) {
            this.pathElements = pathElements;
        }

        PathElement pop() {
            if (elementPos >= pathElements.size()) {
                return null;
            }

            return pathElements.get(elementPos++);
        }

        PathElement peek() {
            return pathElements.get(elementPos);
        }

        int size() {
            return pathElements.size() - elementPos;
        }

        void reset() {
            elementPos = 0;
        }
    }

    private static class TypeAnnotationState {
        private final TypeTarget target;
        private final AnnotationInstance annotation;
        private final boolean genericsRequired;
        private final PathElementStack pathElements;


        public TypeAnnotationState(TypeTarget target, AnnotationInstance annotation, ArrayList<PathElement> pathElements, boolean genericsRequired) {
            this.target = target;
            this.annotation = annotation;
            this.pathElements = new PathElementStack(pathElements);
            this.genericsRequired = genericsRequired;
        }
    }

    private static class BooleanHolder {
        boolean bool;
    }

    private ArrayList<PathElement> processTargetPath(DataInputStream data, BooleanHolder genericsRequired) throws IOException {
        int numElements = data.readUnsignedByte();

        ArrayList<PathElement> elements = new ArrayList<PathElement>(numElements);
        for (int i = 0; i < numElements; i++) {
            int kindIndex = data.readUnsignedByte();
            int pos = data.readUnsignedByte();
            PathElement.Kind kind = PathElement.KINDS[kindIndex];
            if (kind == PathElement.Kind.WILDCARD_BOUND || kind == PathElement.Kind.PARAMETERIZED) {
                genericsRequired.bool = true;
            }
            elements.add(new PathElement(kind, pos));
        }

        return elements;
    }

    private void processExceptions(DataInputStream data, MethodInfo target) throws IOException {
        int numExceptions = data.readUnsignedShort();

        Type[] exceptions = numExceptions <= 0 ? Type.EMPTY_ARRAY : new Type[numExceptions];
        for (int i = 0; i < numExceptions; i++) {
            exceptions[i] = intern(new ClassType(decodeClassEntry(data.readUnsignedShort())));
        }

        // Do not overwrite a signature exception
        if (numExceptions > 0 && target.exceptions().size() == 0) {
            target.setExceptions(exceptions);
        }
    }

    private void processSignature(DataInputStream data, AnnotationTarget target) throws IOException {
        String signature =  decodeUtf8Entry(data.readUnsignedShort());
        signatures.add(signature);
        signatures.add(target);
        signaturePresent.put(target, null);
    }

    private void parseClassSignature(String signature, ClassInfo clazz) {
        GenericSignatureParser.ClassSignature classSignature = signatureParser.parseClassSignature(signature);
        clazz.setInterfaceTypes(classSignature.interfaces());
        clazz.setSuperClassType(classSignature.superClass());
        clazz.setTypeParameters(classSignature.parameters());
    }

    private void applySignatures() {
        int end = signatures.size();

        // Class signature is always the last element and should be processed first
        Object last = end > 1 ? signatures.get(end - 1) : null;
        if (last instanceof ClassInfo) {
            parseClassSignature((String)signatures.get(end - 2), (ClassInfo)last);
            end -= 2;
        }

        for (int i = 0; i < end; i += 2) {
            String elementSignature = (String) signatures.get(i);
            Object element = signatures.get(i + 1);

            if (element instanceof FieldInfo) {
                parseFieldSignature(elementSignature, (FieldInfo)element);
            } else if (element instanceof MethodInfo) {
                parseMethodSignature(elementSignature, (MethodInfo) element);
            }
        }
    }

    private void parseFieldSignature(String signature, FieldInfo field) {
        Type type = signatureParser.parseFieldSignature(signature);
        field.setType(type);
    }

    private void parseMethodSignature(String signature, MethodInfo method) {
        GenericSignatureParser.MethodSignature methodSignature = signatureParser.parseMethodSignature(signature);
        method.setParameters(methodSignature.methodParameters());
        method.setReturnType(methodSignature.returnType());
        method.setTypeParameters(methodSignature.typeParameters());
        if (methodSignature.throwables().length > 0) {
            method.setExceptions(methodSignature.throwables());
        }
    }

    private AnnotationInstance processAnnotation(DataInputStream data, AnnotationTarget target) throws IOException {
        String annotation = convertClassFieldDescriptor(decodeUtf8Entry(data.readUnsignedShort()));
        int valuePairs = data.readUnsignedShort();

        AnnotationValue[] values = new AnnotationValue[valuePairs];
        for (int v = 0; v < valuePairs; v++) {
            String name = intern(decodeUtf8Entry(data.readUnsignedShort()));
            values[v] = processAnnotationElementValue(name, data);
        }

        // Sort entries so they can be binary searched
        Arrays.sort(values, new Comparator<AnnotationValue>() {
            public int compare(AnnotationValue o1, AnnotationValue o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        DotName annotationName = names.convertToName(annotation);
        AnnotationInstance instance = new AnnotationInstance(annotationName, target, values);

        // Don't record nested annotations in index
        if (target != null) {
            recordAnnotation(classAnnotations, annotationName, instance);
            recordAnnotation(masterAnnotations, annotationName, instance);

            if (target instanceof FieldInfo || target instanceof MethodInfo || target instanceof MethodParameterInfo) {
                elementAnnotations.add(instance);
            }
        }

        return instance;

    }

    private void recordAnnotation(Map<DotName, List<AnnotationInstance>> classAnnotations, DotName annotation,
                                  AnnotationInstance instance) {
        List<AnnotationInstance> list = classAnnotations.get(annotation);
        if (list == null) {
            list = new ArrayList<AnnotationInstance>();
            classAnnotations.put(annotation, list);
        }

        list.add(instance);
    }

    private String intern(String string) {
        return names.intern(string);
    }

    private byte[] intern(byte[] bytes) {
        return names.intern(bytes);
    }

    private Type intern(Type type) {
        return names.intern(type);
    }

    private Type[] intern(Type[] type) {
        return names.intern(type);
    }

    private AnnotationValue processAnnotationElementValue(String name, DataInputStream data) throws IOException {
        int tag = data.readUnsignedByte();
        switch (tag) {
            case 'B':
                return new AnnotationValue.ByteValue(name, (byte)decodeIntegerEntry(data.readUnsignedShort()));
            case 'C':
                return new AnnotationValue.CharacterValue(name, (char)decodeIntegerEntry(data.readUnsignedShort()));
            case 'I':
                return new AnnotationValue.IntegerValue(name, decodeIntegerEntry(data.readUnsignedShort()));
            case 'S':
                return new AnnotationValue.ShortValue(name, (short)decodeIntegerEntry(data.readUnsignedShort()));

            case 'Z':
                return new AnnotationValue.BooleanValue(name, decodeIntegerEntry(data.readUnsignedShort()) > 0);

            case 'F':
                return new AnnotationValue.FloatValue(name, decodeFloatEntry(data.readUnsignedShort()));

            case 'D':
                return new AnnotationValue.DoubleValue(name, decodeDoubleEntry(data.readUnsignedShort()));
            case 'J':
                return new AnnotationValue.LongValue(name, decodeLongEntry(data.readUnsignedShort()));

            case 's':
                return new AnnotationValue.StringValue(name, decodeUtf8Entry(data.readUnsignedShort()));
            case 'c':
                return new AnnotationValue.ClassValue(name, parseType(decodeUtf8Entry(data.readUnsignedShort())));
            case 'e': {
                DotName type = parseType(decodeUtf8Entry(data.readUnsignedShort())).name();
                String value = decodeUtf8Entry(data.readUnsignedShort());
                return new AnnotationValue.EnumValue(name, type, value);
            }
            case '@':
                return new AnnotationValue.NestedAnnotation(name, processAnnotation(data, null));
            case '[': {
                int numValues = data.readUnsignedShort();
                AnnotationValue values[] = new AnnotationValue[numValues];
                for (int i = 0; i < numValues; i++)
                    values[i] = processAnnotationElementValue("", data);
                return new AnnotationValue.ArrayValue(name, values);
            }
            default:
                throw new IllegalStateException("Invalid tag value: " + tag);
        }

    }


    private void processClassInfo(DataInputStream data) throws IOException {
        short flags = (short) data.readUnsignedShort();
        DotName thisName = decodeClassEntry(data.readUnsignedShort());
        int superIndex = data.readUnsignedShort();
        DotName superName = (superIndex != 0) ? decodeClassEntry(superIndex) : null;

        int numInterfaces = data.readUnsignedShort();
        List<Type> interfaces = new ArrayList<Type>(numInterfaces);

        for (int i = 0; i < numInterfaces; i++) {
            interfaces.add(intern(new ClassType(decodeClassEntry(data.readUnsignedShort()))));
        }
        Type[] interfaceTypes = intern(interfaces.toArray(new Type[interfaces.size()]));
        Type superClassType = superName == null ? null : intern(new ClassType(superName));

        this.classAnnotations = new HashMap<DotName, List<AnnotationInstance>>();
        this.currentClass = new ClassInfo(thisName, superClassType, flags, interfaceTypes, classAnnotations);

        if (superName != null)
            addSubclass(superName, currentClass);

        for (int i = 0; i < numInterfaces; i++) {
            addImplementor(interfaces.get(i).name(), currentClass);
        }

        classes.put(currentClass.name(), currentClass);
    }

    private void addSubclass(DotName superName, ClassInfo currentClass) {
        List<ClassInfo> list = subclasses.get(superName);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            subclasses.put(superName, list);
        }

        list.add(currentClass);
    }

    private void addImplementor(DotName interfaceName, ClassInfo currentClass) {
        List<ClassInfo> list = implementors.get(interfaceName);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            implementors.put(interfaceName, list);
        }

        list.add(currentClass);
    }

    private boolean isJDK11OrNewer(DataInputStream stream) throws IOException {
        int minor = stream.readUnsignedShort();
        int major = stream.readUnsignedShort();
        return major > 45 || (major == 45 && minor >= 3);
    }

    private void verifyMagic(DataInputStream stream) throws IOException {
        byte[] buf = new byte[4];

        stream.readFully(buf);
        if (buf[0] != (byte)0xCA || buf[1] != (byte)0xFE || buf[2] != (byte)0xBA || buf[3] != (byte)0xBE)
            throw new IOException("Invalid Magic");

    }

    private DotName decodeClassEntry(int classInfoIndex) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[classInfoIndex - 1];
        if (pool[pos] != CONSTANT_CLASS)
            throw new IllegalStateException("Constant pool entry is not a class info type: " + classInfoIndex + ":" + pos);

        int nameIndex = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
        return names.convertToName(decodeUtf8Entry(nameIndex), '/');
    }

    private String decodeUtf8Entry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_UTF8)
            throw new IllegalStateException("Constant pool entry is not a utf8 info type: " + index + ":" + pos);

        int len = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
        return new String(pool, ++pos, len, Charset.forName("UTF-8"));
    }

    private byte[] decodeUtf8EntryAsBytes(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_UTF8)
            throw new IllegalStateException("Constant pool entry is not a utf8 info type: " + index + ":" + pos);

        int len = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);

        return Arrays.copyOfRange(pool, ++pos, len + pos);
    }

    private static class NameAndType {
        private String name;
        private String descriptor;

        private NameAndType(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }
    }

    private NameAndType decodeNameAndTypeEntry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_NAMEANDTYPE)
            throw new IllegalStateException("Constant pool entry is not a name and type type: " + index + ":" + pos);

        int nameIndex = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
        int descriptorIndex = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);

        return new NameAndType(intern(decodeUtf8Entry(nameIndex)), decodeUtf8Entry(descriptorIndex));
    }

    private int bitsToInt(byte[] pool, int pos) {
        return (pool[++pos] & 0xFF) << 24 | (pool[++pos] & 0xFF) << 16 | (pool[++pos] & 0xFF) << 8  | (pool[++pos] & 0xFF);
    }

    private long bitsToLong(byte[] pool, int pos) {
        return ((long)pool[++pos] & 0xFF) << 56 |
               ((long)pool[++pos] & 0xFF) << 48 |
               ((long)pool[++pos] & 0xFF) << 40 |
               ((long)pool[++pos] & 0xFF) << 32 |
                     (pool[++pos] & 0xFF) << 24 |
                     (pool[++pos] & 0xFF) << 16 |
                     (pool[++pos] & 0xFF) << 8  |
                     (pool[++pos] & 0xFF);
    }

    private int decodeIntegerEntry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_INTEGER)
            throw new IllegalStateException("Constant pool entry is not an integer info type: " + index + ":" + pos);

        return bitsToInt(pool, pos);
    }


    private long decodeLongEntry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_LONG)
            throw new IllegalStateException("Constant pool entry is not an long info type: " + index + ":" + pos);

        return bitsToLong(pool, pos);
    }


    private float decodeFloatEntry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_FLOAT)
            throw new IllegalStateException("Constant pool entry is not an float info type: " + index + ":" + pos);

        return Float.intBitsToFloat(bitsToInt(pool, pos));
    }

    private double decodeDoubleEntry(int index) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_DOUBLE)
            throw new IllegalStateException("Constant pool entry is not an double info type: " + index + ":" + pos);

        return Double.longBitsToDouble(bitsToLong(pool, pos));
    }

    private static String convertClassFieldDescriptor(String descriptor) {
        if (descriptor.charAt(0) != 'L')
            throw new IllegalArgumentException("Non class descriptor: " + descriptor);
        return descriptor.substring(1, descriptor.length() -1 ).replace('/', '.');
    }

    private static class IntegerHolder { private int i; };

    private Type[] parseMethodArgs(String descriptor, IntegerHolder pos) {
        if (descriptor.charAt(pos.i) != '(')
            throw new IllegalArgumentException("Invalid descriptor: " + descriptor);

        ArrayList<Type> types = new ArrayList<Type>();
        while (descriptor.charAt(++pos.i) != ')') {
            types.add(parseType(descriptor, pos));
        }

        pos.i++;
        return types.toArray(new Type[types.size()]);
    }

    private Type parseType(String descriptor) {
        return parseType(descriptor, new IntegerHolder());
    }

    private Type parseType(String descriptor, IntegerHolder pos) {
        int start = pos.i;

        char c = descriptor.charAt(start);

        Type type = PrimitiveType.decode(c);
        if (type != null) {
            return type;
        }

        DotName name;
        switch (c) {
            case 'V': return VoidType.VOID;
            case 'L': {
                int end = start;
                while (descriptor.charAt(++end) != ';');
                name = names.convertToName(descriptor.substring(start + 1, end), '/');
                pos.i = end;
                return names.intern(new ClassType(name));
            }
            case '[': {
                int end = start;
                while (descriptor.charAt(++end) == '[');
                int depth = end - start;
                pos.i = end;
                type = parseType(descriptor, pos);
                return names.intern(new ArrayType(type, depth));
            }
            default: throw new IllegalArgumentException("Invalid descriptor: " + descriptor + " pos " + start);
        }
    }

    private boolean processConstantPool(DataInputStream stream) throws IOException {
        int poolCount = stream.readUnsignedShort() - 1;
        byte[] buf = new byte[20 * poolCount]; // Guess
        byte[] annoAttributes = new byte[poolCount];
        int[] offsets = new int[poolCount];
        boolean hasAnnotations = false;

        for (int pos = 0, offset = 0; pos < poolCount; pos++) {
            int tag = stream.readUnsignedByte();
            offsets[pos] = offset;
            switch (tag) {
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                    buf = sizeToFit(buf, 3, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 2);
                    offset += 2;
                    break;
                case CONSTANT_FIELDREF:
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                case CONSTANT_INTEGER:
                case CONSTANT_INVOKEDYNAMIC:
                case CONSTANT_FLOAT:
                case CONSTANT_NAMEANDTYPE:
                    buf = sizeToFit(buf, 5, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 4);
                    offset += 4;
                    break;
                case CONSTANT_LONG:
                case CONSTANT_DOUBLE:
                    buf = sizeToFit(buf, 9, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 8);
                    offset += 8;
                    pos++; // 8 byte constant pool entries take two "virtual" slots for some reason
                    break;
                case CONSTANT_METHODHANDLE:
                    buf = sizeToFit(buf, 4, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 3);
                    offset += 3;
                    break;
                case CONSTANT_METHODTYPE:
                    buf = sizeToFit(buf, 3, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 2);
                    offset += 2;
                    break;
                case CONSTANT_UTF8:
                    int len = stream.readUnsignedShort();
                    buf = sizeToFit(buf, len + 3, offset, poolCount - pos);
                    buf[offset++] = (byte) tag;
                    buf[offset++] = (byte) (len >>> 8);
                    buf[offset++] = (byte) len;

                    stream.readFully(buf, offset, len);
                    if (len == RUNTIME_ANNOTATIONS_LEN && match(buf, offset, RUNTIME_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_ANNOTATION;
                        hasAnnotations = true;
                    } else if (len == RUNTIME_PARAM_ANNOTATIONS_LEN && match(buf, offset, RUNTIME_PARAM_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_PARAM_ANNOTATION;
                        hasAnnotations = true;
                    } else if (len == RUNTIME_TYPE_ANNOTATIONS_LEN && match(buf, offset, RUNTIME_TYPE_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_TYPE_ANNOTATION;
                    } else if (len == SIGNATURE_LEN && match(buf, offset, SIGNATURE)) {
                        annoAttributes[pos] = HAS_SIGNATURE;
                    } else if (len == EXCEPTIONS_LEN && match(buf, offset, EXCEPTIONS)) {
                        annoAttributes[pos] = HAS_EXCEPTIONS;
                    } else if (len == INNER_CLASSES_LEN && match(buf, offset, INNER_CLASSES)) {
                        annoAttributes[pos] = HAS_INNER_CLASSES;
                    } else if (len == ENCLOSING_METHOD_LEN && match(buf, offset, ENCLOSING_METHOD)) {
                        annoAttributes[pos] = HAS_ENCLOSING_METHOD;
                    }
                    offset += len;
                    break;
               default:
                   throw new IllegalStateException("Unknown tag! pos=" + pos + " poolCount = " + poolCount);
            }
        }

        constantPool = buf;
        constantPoolOffsets = offsets;
        constantPoolAnnoAttrributes = annoAttributes;

        return hasAnnotations;
    }

    /**
     * Analyze and index the class file data present in the passed input stream.
     * Each call adds information to the final complete index; however, to aid in
     * processing a per-class index (ClassInfo) is returned on each call.
     *
     * @param stream a stream pointing to class file data
     * @return a class index containing all annotations on the passed class stream
     * @throws IOException if the class file data is corrupt or the underlying stream fails
     */
    public ClassInfo index(InputStream stream) throws IOException {
        try
        {
            DataInputStream data = new DataInputStream(new BufferedInputStream(stream));
            verifyMagic(data);

            // Retroweaved classes may contain annotations
            // Also, hierarchy info is needed regardless
            if (!isJDK11OrNewer(data))
                return null;

            initIndexMaps();
            initClassFields();

            processConstantPool(data);
            processClassInfo(data);
            processFieldInfo(data);
            processMethodInfo(data);
            processAttributes(data, currentClass);

            applySignatures();
            resolveTypeAnnotations();
            updateTypeTargets();

            currentClass.setMethods(methods, names);
            currentClass.setFields(fields, names);

            return currentClass;
        } finally {
            constantPool = null;
            constantPoolOffsets = null;
            constantPoolAnnoAttrributes = null;
            currentClass = null;
            classAnnotations = null;
            elementAnnotations = null;
            innerClasses = null;
            signatures = null;
            signaturePresent = null;
        }
    }

    /**
     * Completes, finalizes, and returns the index after zero or more calls to
     * index. Future calls to index will result in a new index.
     *
     * @return the master index for all scanned class streams
     */
    public Index complete() {
        initIndexMaps();
        try {
            return Index.create(masterAnnotations, subclasses, implementors, classes);
        } finally {
            masterAnnotations = null;
            subclasses = null;
            classes = null;
            signatureParser = null;
            names = null;
        }
    }
}
