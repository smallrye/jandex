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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    private final static int CONSTANT_MODULE = 19;
    private final static int CONSTANT_PACKAGE = 20;
    private final static int CONSTANT_DYNAMIC = 17;

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

    // "AnnotationDefault"
    private final static byte[] ANNOTATION_DEFAULT = new byte[] {
            0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x44, 0x65,
            0x66, 0x61, 0x75, 0x6c, 0x74
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

    // "MethodParameters"
    private final static byte[] METHOD_PARAMETERS = new byte[] {
            0x4d, 0x65, 0x74, 0x68, 0x6f, 0x64, 0x50, 0x61, 0x72, 0x61, 0x6d, 0x65, 0x74, 0x65, 0x72, 0x73
    };

    // "LocalVariableTable"
    private final static byte[] LOCAL_VARIABLE_TABLE = new byte[] {
            0x4c, 0x6f, 0x63, 0x61, 0x6c, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x54, 0x61, 0x62, 0x6c, 0x65
    };

    // "Code"
    private final static byte[] CODE = new byte[] {
            0x43, 0x6f, 0x64, 0x65
    };

    // "Module"
    private final static byte[] MODULE = new byte[] {
            // M     o     d     u     l     e
            0x4d, 0x6f, 0x64, 0x75, 0x6c, 0x65
    };

    // "ModulePackages"
    private final static byte[] MODULE_PACKAGES = new byte[] {
            // M     o     d     u     l     e     P     a     c     k     a     g     e     s
            0x4d, 0x6f, 0x64, 0x75, 0x6c, 0x65, 0x50, 0x61, 0x63, 0x6b, 0x61, 0x67, 0x65, 0x73
    };

    // "ModuleMainClass"
    private final static byte[] MODULE_MAIN_CLASS = new byte[] {
            // M     o     d     u     l     e     M     a     i     n     C     l     a     s     s
            0x4d, 0x6f, 0x64, 0x75, 0x6c, 0x65, 0x4d, 0x61, 0x69, 0x6e, 0x43, 0x6c, 0x61, 0x73, 0x73
    };

    // "Record"
    private final static byte[] RECORD = new byte[] {
            // R     e     c     o     r     d
            0x52, 0x65, 0x63, 0x6f, 0x72, 0x64
    };

    // "RuntimeInvisibleAnnotations"
    private final static byte[] RUNTIME_INVISIBLE_ANNOTATIONS = new byte[] {
            // R     u     n     t     i     m     e
            0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65,
            // I     n     v     i     s     i     b     l     e
            0x49, 0x6e, 0x76, 0x69, 0x73, 0x69, 0x62, 0x6c, 0x65,
            // A     n     n     o     t     a     t     i     o     n     s
            0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x73
    };

    // "RuntimeInvisibleParameterAnnotations"
    private final static byte[] RUNTIME_INVISIBLE_PARAM_ANNOTATIONS = new byte[] {
            // R     u     n     t     i     m     e
            0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65,
            // I     n     v     i     s     i     b     l     e
            0x49, 0x6e, 0x76, 0x69, 0x73, 0x69, 0x62, 0x6c, 0x65,
            // P     a     r     a     m     e     t     e     r
            0x50, 0x61, 0x72, 0x61, 0x6d, 0x65, 0x74, 0x65, 0x72,
            // A     n     n     o     t     a     t     i     o     n     s
            0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x73
    };

    // "RuntimeInvisibleTypeAnnotations"
    private final static byte[] RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = new byte[] {
            // R     u     n     t     i     m     e
            0x52, 0x75, 0x6e, 0x74, 0x69, 0x6d, 0x65,
            // I     n     v     i     s     i     b     l     e
            0x49, 0x6e, 0x76, 0x69, 0x73, 0x69, 0x62, 0x6c, 0x65,
            // T     y     p     e
            0x54, 0x79, 0x70, 0x65,
            // A     n     n     o     t     a     t     i     o     n     s
            0x41, 0x6e, 0x6e, 0x6f, 0x74, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x73
    };

    // "PermittedSubclasses"
    private final static byte[] PERMITTED_SUBCLASSES = new byte[] {
            // P     e     r     m     i     t     t     e     d
            0x50, 0x65, 0x72, 0x6d, 0x69, 0x74, 0x74, 0x65, 0x64,
            // S     u     b     c     l     a     s     s     e     s
            0x53, 0x75, 0x62, 0x63, 0x6c, 0x61, 0x73, 0x73, 0x65, 0x73
    };

    private final static int RUNTIME_ANNOTATIONS_LEN = RUNTIME_ANNOTATIONS.length;
    private final static int RUNTIME_PARAM_ANNOTATIONS_LEN = RUNTIME_PARAM_ANNOTATIONS.length;
    private final static int RUNTIME_TYPE_ANNOTATIONS_LEN = RUNTIME_TYPE_ANNOTATIONS.length;
    private final static int ANNOTATION_DEFAULT_LEN = ANNOTATION_DEFAULT.length;
    private final static int SIGNATURE_LEN = SIGNATURE.length;
    private final static int EXCEPTIONS_LEN = EXCEPTIONS.length;
    private final static int INNER_CLASSES_LEN = INNER_CLASSES.length;
    private final static int ENCLOSING_METHOD_LEN = ENCLOSING_METHOD.length;
    private final static int METHOD_PARAMETERS_LEN = METHOD_PARAMETERS.length;
    private final static int LOCAL_VARIABLE_TABLE_LEN = LOCAL_VARIABLE_TABLE.length;
    private final static int CODE_LEN = CODE.length;
    private final static int MODULE_LEN = MODULE.length;
    private final static int MODULE_PACKAGES_LEN = MODULE_PACKAGES.length;
    private final static int MODULE_MAIN_CLASS_LEN = MODULE_MAIN_CLASS.length;
    private final static int RECORD_LEN = RECORD.length;
    private final static int RUNTIME_INVISIBLE_ANNOTATIONS_LEN = RUNTIME_INVISIBLE_ANNOTATIONS.length;
    private final static int RUNTIME_INVISIBLE_PARAM_ANNOTATIONS_LEN = RUNTIME_INVISIBLE_PARAM_ANNOTATIONS.length;
    private final static int RUNTIME_INVISIBLE_TYPE_ANNOTATIONS_LEN = RUNTIME_INVISIBLE_TYPE_ANNOTATIONS.length;
    private final static int PERMITTED_SUBCLASSES_LEN = PERMITTED_SUBCLASSES.length;

    private final static int HAS_RUNTIME_ANNOTATION = 1;
    private final static int HAS_RUNTIME_PARAM_ANNOTATION = 2;
    private final static int HAS_RUNTIME_TYPE_ANNOTATION = 3;
    private final static int HAS_SIGNATURE = 4;
    private final static int HAS_EXCEPTIONS = 5;
    private final static int HAS_INNER_CLASSES = 6;
    private final static int HAS_ENCLOSING_METHOD = 7;
    private final static int HAS_ANNOTATION_DEFAULT = 8;
    private final static int HAS_METHOD_PARAMETERS = 9;
    private final static int HAS_LOCAL_VARIABLE_TABLE = 10;
    private final static int HAS_CODE = 11;
    private final static int HAS_MODULE = 12;
    private final static int HAS_MODULE_PACKAGES = 13;
    private final static int HAS_MODULE_MAIN_CLASS = 14;
    private final static int HAS_RECORD = 15;
    private final static int HAS_RUNTIME_INVISIBLE_ANNOTATION = 16;
    private final static int HAS_RUNTIME_INVISIBLE_PARAM_ANNOTATION = 17;
    private final static int HAS_RUNTIME_INVISIBLE_TYPE_ANNOTATION = 18;
    private final static int HAS_PERMITTED_SUBCLASSES = 19;

    private static class InnerClassInfo {
        private InnerClassInfo(DotName innerClass, DotName enclosingClass, String simpleName, int flags) {
            this.innerClass = innerClass;
            this.enclosingClass = enclosingClass;
            this.simpleName = simpleName;
            this.flags = flags;
        }

        private final DotName innerClass;
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
        int oldLength = buf.length;
        if (offset + needed > oldLength) {
            int newLength = newLength(oldLength, needed, oldLength >> 1);
            buf = Arrays.copyOf(buf, newLength);
        }
        return buf;
    }

    private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        int prefLength = oldLength + Math.max(minGrowth, prefGrowth);
        return prefLength > 0 ? prefLength : minLength(oldLength, minGrowth);
    }

    private static int minLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) {
            throw new OutOfMemoryError("Cannot allocate a large enough array: " +
                    oldLength + " + " + minGrowth + " is too large");
        }
        return minLength;
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

    private static final class TmpObjects {
        private Utils.ReusableBufferedDataInputStream dataInputStream;

        private byte[] constantPool;
        private int[] constantPoolOffsets;
        private byte[] constantPoolAnnoAttributes;

        DataInputStream dataInputStreamOf(InputStream inputStream) {
            Utils.ReusableBufferedDataInputStream stream = dataInputStream;
            if (stream == null) {
                stream = new Utils.ReusableBufferedDataInputStream();
                this.dataInputStream = stream;
            }
            stream.setInputStream(inputStream);
            return stream;
        }

        byte[] borrowConstantPool(int poolSize) {
            byte[] buf = this.constantPool;
            if (buf == null || buf.length < (20 * poolSize)) {
                buf = new byte[20 * poolSize]; // Guess
            } else {
                Arrays.fill(buf, 0, poolSize, (byte) 0);
            }
            this.constantPool = null;
            return buf;
        }

        void returnConstantPool(byte[] buf) {
            this.constantPool = buf;
        }

        int[] borrowConstantPoolOffsets(int poolSize) {
            int[] buf = this.constantPoolOffsets;
            if (buf == null || buf.length < poolSize) {
                buf = new int[poolSize];
            } else {
                Arrays.fill(buf, 0, poolSize, 0);
            }
            this.constantPoolOffsets = null;
            return buf;
        }

        void returnConstantPoolOffsets(int[] offsets) {
            this.constantPoolOffsets = offsets;
        }

        byte[] borrowConstantPoolAnnoAttributes(int poolSize) {
            byte[] buf = this.constantPoolAnnoAttributes;
            if (buf == null || buf.length < poolSize) {
                buf = new byte[poolSize];
            } else {
                Arrays.fill(buf, 0, poolSize, (byte) 0);
            }
            this.constantPoolAnnoAttributes = null;
            return buf;
        }

        void returnConstantAnnoAttributes(byte[] attributes) {
            this.constantPoolAnnoAttributes = attributes;
        }
    }

    // Class lifespan fields
    private int constantPoolSize;
    private byte[] constantPool;
    private int[] constantPoolOffsets;
    private byte[] constantPoolAnnoAttrributes;

    // note that for reproducibility, we have to establish a predictable iteration order for all `Map`s
    // below that we iterate upon (fortunately, that's not too many)
    // this is either by using keys with predictable `equals`/`hashCode` (such as `DotName`),
    // or by storing the keys on the side in a list and iterate on that (needed for `IdentityHashMap`s)

    private ClassInfo currentClass;
    private HashMap<DotName, List<AnnotationInstance>> classAnnotations;
    private ArrayList<AnnotationInstance> elementAnnotations;
    private IdentityHashMap<AnnotationTarget, Object> signaturePresent;
    private List<Object> signatures;
    private int classSignatureIndex = -1;
    private Map<DotName, InnerClassInfo> innerClasses;
    // iteration: `typeAnnotationsKeys` is used for predictable iteration order, we never iterate on `typeAnnotations`
    private IdentityHashMap<AnnotationTarget, List<TypeAnnotationState>> typeAnnotations;
    private List<AnnotationTarget> typeAnnotationsKeys;
    private List<MethodInfo> methods;
    private List<FieldInfo> fields;
    private List<RecordComponentInfo> recordComponents;
    private IdentityHashMap<MethodInfo, MethodParamList> methodParams;
    private List<DotName> modulePackages;
    private DotName moduleMainClass;

    // Index lifespan fields
    private Map<DotName, List<AnnotationInstance>> masterAnnotations;
    private Map<DotName, List<ClassInfo>> subclasses;
    private Map<DotName, List<ClassInfo>> subinterfaces;
    private Map<DotName, List<ClassInfo>> implementors;
    // iteration: `DotName` has predictable `equals`/`hashCode`, which implies predictable iteration order
    private Map<DotName, ClassInfo> classes;
    private Map<DotName, ModuleInfo> modules;
    // iteration: `DotName` has predictable `equals`/`hashCode`, which implies predictable iteration order
    // iteration: the `Set`s in map values must be linked sets for predictable iteration order
    private Map<DotName, Set<ClassInfo>> users;
    private NameTable names;
    private GenericSignatureParser signatureParser;
    private final TmpObjects tmpObjects = new TmpObjects();

    private void initIndexMaps() {
        if (masterAnnotations == null)
            masterAnnotations = new HashMap<DotName, List<AnnotationInstance>>();

        if (subclasses == null)
            subclasses = new HashMap<DotName, List<ClassInfo>>();

        if (subinterfaces == null)
            subinterfaces = new HashMap<DotName, List<ClassInfo>>();

        if (implementors == null)
            implementors = new HashMap<DotName, List<ClassInfo>>();

        if (classes == null)
            classes = new HashMap<DotName, ClassInfo>();

        if (modules == null)
            modules = new HashMap<DotName, ModuleInfo>();

        if (users == null)
            users = new HashMap<DotName, Set<ClassInfo>>();

        if (names == null)
            names = new NameTable();

        if (signatureParser == null) {
            signatureParser = new GenericSignatureParser(names);
        }
    }

    private void initClassFields() {
        classAnnotations = new HashMap<DotName, List<AnnotationInstance>>();
        elementAnnotations = new ArrayList<AnnotationInstance>();
        signaturePresent = new IdentityHashMap<AnnotationTarget, Object>();
        signatures = new ArrayList<Object>();
        typeAnnotations = new IdentityHashMap<AnnotationTarget, List<TypeAnnotationState>>();
        typeAnnotationsKeys = new ArrayList<>();

        // in bytecode, record components are stored as class attributes,
        // and if the attribute is missing, processRecordComponents isn't called at all
        recordComponents = new ArrayList<RecordComponentInfo>();

        methodParams = new IdentityHashMap<>();
    }

    private void processMethodInfo(DataInputStream data) throws IOException {
        int numMethods = data.readUnsignedShort();
        List<MethodInfo> methods = numMethods > 0 ? new ArrayList<MethodInfo>(numMethods)
                : Collections.<MethodInfo> emptyList();

        for (int i = 0; i < numMethods; i++) {
            short flags = (short) data.readUnsignedShort();
            byte[] name = intern(decodeUtf8EntryAsBytes(data.readUnsignedShort()));
            String descriptor = decodeUtf8Entry(data.readUnsignedShort());

            IntegerHolder pos = new IntegerHolder();
            Type[] parameters = intern(parseMethodArgs(descriptor, pos));
            Type returnType = parseType(descriptor, pos);

            MethodInfo method = new MethodInfo(currentClass, name, MethodInternal.EMPTY_PARAMETER_NAMES, parameters, returnType,
                    flags);

            if (parameters.length == 0 && Arrays.equals(Utils.INIT_METHOD_NAME, name)) {
                currentClass.setHasNoArgsConstructor(true);
            }
            MethodParamList parameterList = new MethodParamList(method);
            methodParams.put(method, parameterList);
            processAttributes(data, method);
            method.setAnnotations(elementAnnotations);
            elementAnnotations.clear();
            parameterList.finish();

            byte[][] parameterNames = parameterList.getNames();
            if (parameterNames != null) {
                method.methodInternal().setParameterNames(parameterNames);
            }

            methods.add(method);
        }

        this.methods = methods;
    }

    private void processFieldInfo(DataInputStream data) throws IOException {
        int numFields = data.readUnsignedShort();
        List<FieldInfo> fields = numFields > 0 ? new ArrayList<FieldInfo>(numFields) : Collections.<FieldInfo> emptyList();
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

    private void processRecordComponents(DataInputStream data) throws IOException {
        int numComponents = data.readUnsignedShort();
        List<RecordComponentInfo> recordComponents = numComponents > 0 ? new ArrayList<RecordComponentInfo>(numComponents)
                : Collections.<RecordComponentInfo> emptyList();
        for (int i = 0; i < numComponents; i++) {
            byte[] name = intern(decodeUtf8EntryAsBytes(data.readUnsignedShort()));
            Type type = intern(parseType(decodeUtf8Entry(data.readUnsignedShort())));
            RecordComponentInfo component = new RecordComponentInfo(currentClass, name, type);

            processAttributes(data, component);
            component.setAnnotations(elementAnnotations);
            elementAnnotations.clear();
            recordComponents.add(component);
        }
        this.recordComponents = recordComponents;
    }

    private void processPermittedSubclasses(DataInputStream data, ClassInfo target) throws IOException {
        int numPermittedSubclasses = data.readUnsignedShort();
        if (numPermittedSubclasses > 0) {
            Set<DotName> permittedSubclasses = new HashSet<>(numPermittedSubclasses);
            for (int i = 0; i < numPermittedSubclasses; i++) {
                DotName name = decodeClassEntry(data.readUnsignedShort());
                permittedSubclasses.add(name);
            }
            target.setPermittedSubclasses(permittedSubclasses);
        }
    }

    private void processAttributes(DataInputStream data, AnnotationTarget target) throws IOException {
        int numAttrs = data.readUnsignedShort();
        byte[] constantPoolAnnoAttrributes = this.constantPoolAnnoAttrributes;
        for (int a = 0; a < numAttrs; a++) {
            int index = data.readUnsignedShort();
            long attributeLen = data.readInt() & 0xFFFFFFFFL;
            byte annotationAttribute = constantPoolAnnoAttrributes[index - 1];
            if (annotationAttribute == HAS_RUNTIME_ANNOTATION || annotationAttribute == HAS_RUNTIME_INVISIBLE_ANNOTATION) {
                processAnnotations(data, target, annotationAttribute == HAS_RUNTIME_ANNOTATION);
            } else if (annotationAttribute == HAS_RUNTIME_PARAM_ANNOTATION
                    || annotationAttribute == HAS_RUNTIME_INVISIBLE_PARAM_ANNOTATION) {
                if (!(target instanceof MethodInfo)) {
                    if (annotationAttribute == HAS_RUNTIME_PARAM_ANNOTATION) {
                        throw new IllegalStateException("RuntimeVisibleParameterAnnotations appeared on a non-method");
                    } else {
                        throw new IllegalStateException("RuntimeInvisibleParameterAnnotations appeared on a non-method");
                    }
                }
                int numParameters = data.readUnsignedByte();
                if (target.asMethod().parametersCount() > 255) {
                    // the Kotlin compiler happily generates methods with more than 255 parameters,
                    // even if the JVM specification prohibits them, so if the method descriptor
                    // indicates that so many parameters are present, let's use that count instead of
                    // the one we just read (for extra safety, do NOT do this for compliant methods)
                    numParameters = target.asMethod().parametersCount();
                }
                for (short p = 0; p < numParameters; p++) {
                    processAnnotations(data, new MethodParameterInfo((MethodInfo) target, p),
                            annotationAttribute == HAS_RUNTIME_PARAM_ANNOTATION);
                }
            } else if (annotationAttribute == HAS_RUNTIME_TYPE_ANNOTATION
                    || annotationAttribute == HAS_RUNTIME_INVISIBLE_TYPE_ANNOTATION) {
                processTypeAnnotations(data, target, annotationAttribute == HAS_RUNTIME_TYPE_ANNOTATION);
            } else if (annotationAttribute == HAS_SIGNATURE) {
                processSignature(data, target);
            } else if (annotationAttribute == HAS_EXCEPTIONS && target instanceof MethodInfo) {
                processExceptions(data, (MethodInfo) target);
            } else if (annotationAttribute == HAS_INNER_CLASSES && target instanceof ClassInfo) {
                processInnerClasses(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_ENCLOSING_METHOD && target instanceof ClassInfo) {
                processEnclosingMethod(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_ANNOTATION_DEFAULT && target instanceof MethodInfo) {
                processAnnotationDefault(data, (MethodInfo) target);
            } else if (annotationAttribute == HAS_METHOD_PARAMETERS && target instanceof MethodInfo) {
                processMethodParameters(data, (MethodInfo) target);
            } else if (annotationAttribute == HAS_CODE && target instanceof MethodInfo) {
                processCode(data, (MethodInfo) target);
            } else if (annotationAttribute == HAS_MODULE && target instanceof ClassInfo) {
                processModule(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_MODULE_PACKAGES && target instanceof ClassInfo) {
                processModulePackages(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_MODULE_MAIN_CLASS && target instanceof ClassInfo) {
                processModuleMainClass(data, (ClassInfo) target);
            } else if (annotationAttribute == HAS_RECORD && target instanceof ClassInfo) {
                processRecordComponents(data);
            } else if (annotationAttribute == HAS_PERMITTED_SUBCLASSES && target instanceof ClassInfo) {
                processPermittedSubclasses(data, (ClassInfo) target);
            } else {
                skipFully(data, attributeLen);
            }
        }
    }

    private void processModule(DataInputStream data, ClassInfo target) throws IOException {
        if (!target.isModule()) {
            throw new IllegalStateException("Module attribute appeared in a non-module class file");
        }

        DotName moduleName = decodeModuleEntry(data.readUnsignedShort());
        int flags = data.readUnsignedShort();
        String version = decodeOptionalUtf8Entry(data.readUnsignedShort());

        ModuleInfo module = new ModuleInfo(target, moduleName, (short) flags, version);
        module.setRequires(processModuleRequires(data));
        module.setExports(processModuleExports(data));
        module.setOpens(processModuleOpens(data));
        module.setUses(processModuleUses(data));
        module.setProvides(processModuleProvides(data));

        // Store the owning ClassInfo using the module name instead of `module-info`
        modules.put(moduleName, module);
    }

    private List<ModuleInfo.RequiredModuleInfo> processModuleRequires(DataInputStream data) throws IOException {
        int requiresCount = data.readUnsignedShort();
        List<ModuleInfo.RequiredModuleInfo> requires = Utils.listOfCapacity(requiresCount);

        for (int i = 0; i < requiresCount; i++) {
            DotName name = decodeModuleEntry(data.readUnsignedShort());
            int flags = data.readUnsignedShort();
            String version = decodeOptionalUtf8Entry(data.readUnsignedShort());
            requires.add(new ModuleInfo.RequiredModuleInfo(name, flags, version));
        }

        return requires;
    }

    private List<ModuleInfo.ExportedPackageInfo> processModuleExports(DataInputStream data) throws IOException {
        int exportsCount = data.readUnsignedShort();
        List<ModuleInfo.ExportedPackageInfo> exports = Utils.listOfCapacity(exportsCount);

        for (int i = 0; i < exportsCount; i++) {
            DotName source = decodePackageEntry(data.readUnsignedShort());
            int flags = data.readUnsignedShort();
            int targetCount = data.readUnsignedShort();
            List<DotName> targets = Utils.listOfCapacity(targetCount);

            for (int j = 0; j < targetCount; j++) {
                targets.add(decodeModuleEntry(data.readUnsignedShort()));
            }

            exports.add(new ModuleInfo.ExportedPackageInfo(source, flags, targets));
        }

        return exports;
    }

    private List<ModuleInfo.OpenedPackageInfo> processModuleOpens(DataInputStream data) throws IOException {
        int opensCount = data.readUnsignedShort();
        List<ModuleInfo.OpenedPackageInfo> opens = Utils.listOfCapacity(opensCount);

        for (int i = 0; i < opensCount; i++) {
            DotName source = decodePackageEntry(data.readUnsignedShort());
            int flags = data.readUnsignedShort();
            int targetCount = data.readUnsignedShort();
            List<DotName> targets = Utils.listOfCapacity(targetCount);

            for (int j = 0; j < targetCount; j++) {
                targets.add(decodeModuleEntry(data.readUnsignedShort()));
            }

            opens.add(new ModuleInfo.OpenedPackageInfo(source, flags, targets));
        }

        return opens;
    }

    private List<DotName> processModuleUses(DataInputStream data) throws IOException {
        int usesCount = data.readUnsignedShort();
        List<DotName> usesServices = Utils.listOfCapacity(usesCount);

        for (int j = 0; j < usesCount; j++) {
            usesServices.add(decodeClassEntry(data.readUnsignedShort()));
        }

        return usesServices;
    }

    private List<ModuleInfo.ProvidedServiceInfo> processModuleProvides(DataInputStream data) throws IOException {
        int providesCount = data.readUnsignedShort();
        List<ModuleInfo.ProvidedServiceInfo> provides = Utils.listOfCapacity(providesCount);

        for (int i = 0; i < providesCount; i++) {
            DotName service = decodeClassEntry(data.readUnsignedShort());
            int providerCount = data.readUnsignedShort();
            List<DotName> providers = Utils.listOfCapacity(providerCount);

            for (int j = 0; j < providerCount; j++) {
                providers.add(decodeClassEntry(data.readUnsignedShort()));
            }

            provides.add(new ModuleInfo.ProvidedServiceInfo(service, providers));
        }

        return provides;
    }

    private void processModulePackages(DataInputStream data, ClassInfo target) throws IOException {
        if (!target.isModule()) {
            throw new IllegalStateException("ModulePackages attribute appeared in a non-module class file");
        }

        int packagesCount = data.readUnsignedShort();
        List<DotName> packages = Utils.listOfCapacity(packagesCount);

        for (int j = 0; j < packagesCount; j++) {
            packages.add(decodePackageEntry(data.readUnsignedShort()));
        }

        this.modulePackages = packages;
    }

    private void processModuleMainClass(DataInputStream data, ClassInfo target) throws IOException {
        if (!target.isModule()) {
            throw new IllegalStateException("ModuleMainClass attribute appeared in a non-module class file");
        }

        this.moduleMainClass = decodeClassEntry(data.readUnsignedShort());
    }

    private void processCode(DataInputStream data, MethodInfo target) throws IOException {
        data.skipBytes(4); // 2 bytes for `maxStack` + 2 bytes for `maxLocals`
        long h = data.readUnsignedShort();
        long l = data.readUnsignedShort();
        long codeLength = (h << 16) | l;
        skipFully(data, codeLength);
        int exceptionTableLength = data.readUnsignedShort();
        skipFully(data, exceptionTableLength * (2 + 2 + 2 + 2));
        byte[] constantPoolAnnoAttrributes = this.constantPoolAnnoAttrributes;
        int numAttrs = data.readUnsignedShort();
        for (int a = 0; a < numAttrs; a++) {
            int index = data.readUnsignedShort();
            long attributeLen = data.readInt() & 0xFFFFFFFFL;
            byte annotationAttribute = constantPoolAnnoAttrributes[index - 1];
            if (annotationAttribute == HAS_LOCAL_VARIABLE_TABLE && target instanceof MethodInfo) {
                processLocalVariableTable(data, target);
            } else {
                skipFully(data, attributeLen);
            }
        }
    }

    private void processAnnotationDefault(DataInputStream data, MethodInfo target) throws IOException {
        target.setDefaultValue(processAnnotationElementValue(target.name(), data));
    }

    private void processAnnotations(DataInputStream data, AnnotationTarget target, boolean visible) throws IOException {
        int numAnnotations = data.readUnsignedShort();
        while (numAnnotations-- > 0)
            processAnnotation(data, target, visible);
    }

    private void processInnerClasses(DataInputStream data, ClassInfo target) throws IOException {
        int numClasses = data.readUnsignedShort();
        innerClasses = numClasses > 0 ? new HashMap<>(numClasses) : Collections.emptyMap();
        Set<DotName> memberClasses = new HashSet<>();
        for (int i = 0; i < numClasses; i++) {
            DotName innerClass = decodeClassEntry(data.readUnsignedShort());
            int outerIndex = data.readUnsignedShort();
            DotName outerClass = outerIndex == 0 ? null : decodeClassEntry(outerIndex);
            int simpleIndex = data.readUnsignedShort();
            String simpleName = simpleIndex == 0 ? null : decodeUtf8Entry(simpleIndex);
            int flags = data.readUnsignedShort();

            if (innerClass.equals(target.name())) {
                target.setInnerClassInfo(outerClass, simpleName, true);
                target.setFlags((short) flags);
            }
            if (outerClass != null && outerClass.equals(target.name())) {
                memberClasses.add(innerClass);
            }

            innerClasses.put(innerClass, new InnerClassInfo(innerClass, outerClass, simpleName, flags));
        }
        if (!memberClasses.isEmpty()) {
            target.setMemberClasses(memberClasses);
        }
    }

    private void processMethodParameters(DataInputStream data, MethodInfo target) throws IOException {
        int numParameters = data.readUnsignedByte();
        if (target.parametersCount() > 255) {
            // the Kotlin compiler happily generates methods with more than 255 parameters,
            // even if the JVM specification prohibits them, so if the method descriptor
            // indicates that so many parameters are present, let's use that count instead of
            // the one we just read (for extra safety, do NOT do this for compliant methods)
            numParameters = target.parametersCount();
        }
        for (int i = 0; i < numParameters; i++) {
            int nameIndex = data.readUnsignedShort();
            byte[] parameterName = nameIndex == 0 ? null : decodeUtf8EntryAsBytes(nameIndex);
            int flags = data.readUnsignedShort();
            boolean syntheticOrMandated = (flags & (Modifiers.SYNTHETIC | Modifiers.MANDATED)) != 0;

            if (methodParams.containsKey(target)) { // should always be there, just being extra careful
                methodParams.get(target).appendProper(parameterName, syntheticOrMandated);
            }
        }
    }

    private void processLocalVariableTable(DataInputStream data, MethodInfo target) throws IOException {
        int numVariables = data.readUnsignedShort();
        int numParameters = 0;
        for (int i = 0; i < numVariables; i++) {
            int startPc = data.readUnsignedShort();
            int length = data.readUnsignedShort();
            int nameIndex = data.readUnsignedShort();
            int descriptorIndex = data.readUnsignedShort();
            int index = data.readUnsignedShort();

            // parameters have startPc == 0
            if (startPc != 0) {
                continue;
            }

            byte[] parameterName = nameIndex == 0 ? null : decodeUtf8EntryAsBytes(nameIndex);

            // ignore "this"
            if (numParameters == 0 && parameterName != null && parameterName.length == 4
                    && parameterName[0] == 0x74
                    && parameterName[1] == 0x68
                    && parameterName[2] == 0x69
                    && parameterName[3] == 0x73) {
                continue;
            }

            // treat "this$*" that javac adds (not ecj) as synthetic (or mandated)
            boolean synthetic = false;
            if (numParameters == 0 && parameterName != null && parameterName.length > 5
                    && parameterName[0] == 0x74
                    && parameterName[1] == 0x68
                    && parameterName[2] == 0x69
                    && parameterName[3] == 0x73
                    && parameterName[4] == 0x24) {
                synthetic = true;
            }

            if (methodParams.containsKey(target)) { // should always be there, just being extra careful
                methodParams.get(target).appendDebug(parameterName, synthetic);
            }

            numParameters++;
        }
    }

    private void processEnclosingMethod(DataInputStream data, ClassInfo target) throws IOException {
        int classIndex = data.readUnsignedShort();
        int index = data.readUnsignedShort();

        DotName enclosingClass = decodeClassEntry(classIndex);

        if (index == 0) {
            // enclosed in a static/instance/field initializer
            target.setEnclosingClassInInitializer(enclosingClass);
            return;
        }

        NameAndType nameAndType = decodeNameAndTypeEntry(index);

        IntegerHolder pos = new IntegerHolder();
        Type[] parameters = intern(parseMethodArgs(nameAndType.descriptor, pos));
        Type returnType = parseType(nameAndType.descriptor, pos);

        EnclosingMethodInfo method = new EnclosingMethodInfo(nameAndType.name, returnType, parameters, enclosingClass);
        target.setEnclosingMethod(method);
    }

    private void processTypeAnnotations(DataInputStream data, AnnotationTarget target, boolean visible) throws IOException {
        int numAnnotations = data.readUnsignedShort();
        List<TypeAnnotationState> annotations = new ArrayList<>(numAnnotations);

        for (int i = 0; i < numAnnotations; i++) {
            TypeAnnotationState annotation = processTypeAnnotation(data, target, visible);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }

        if (typeAnnotations.containsKey(target)) {
            typeAnnotations.get(target).addAll(annotations);
        } else {
            typeAnnotations.put(target, annotations);
            typeAnnotationsKeys.add(target);
        }
    }

    private TypeAnnotationState processTypeAnnotation(DataInputStream data, AnnotationTarget target, boolean visible)
            throws IOException {
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
                int position = data.readUnsignedShort();

                // Skip invalid usage (observed bad bytecode on method attributes)
                if (target instanceof ClassInfo) {
                    typeTarget = new ClassExtendsTypeTarget((ClassInfo) target, position);
                }
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
                // javac has a bug in case of compact record constructors, where it targets a type annotation
                // on a constructor parameter type to a field; in such case, just skip the annotation
                if (target.kind() == AnnotationTarget.Kind.METHOD && targetType == 0x13) {
                    break;
                }
                typeTarget = new EmptyTypeTarget(target, targetType == 0x15);
                break;
            case 0x16: // METHOD_FORMAL_PARAMETER
            {
                int position = data.readUnsignedByte();
                if (target instanceof MethodInfo) {
                    typeTarget = new MethodParameterTypeTarget((MethodInfo) target, position);
                }
                break;
            }
            case 0x17: // THROWS
            {
                int position = data.readUnsignedShort();
                if (target instanceof MethodInfo) {
                    typeTarget = new ThrowsTypeTarget((MethodInfo) target, position);
                }
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
            skipTargetPath(data);
            // eat
            // TODO - introduce an allocation free annotation skip
            processAnnotation(data, null, visible);
            return null;
        }

        BooleanHolder genericsRequired = new BooleanHolder();
        BooleanHolder bridgeIncompatible = new BooleanHolder();

        if (typeTarget.usage() == TypeTarget.Usage.TYPE_PARAMETER
                || typeTarget.usage() == TypeTarget.Usage.TYPE_PARAMETER_BOUND) {
            genericsRequired.bool = true;
        }

        ArrayList<PathElement> pathElements = processTargetPath(data, genericsRequired, bridgeIncompatible);
        AnnotationInstance annotation = processAnnotation(data, typeTarget, visible);
        return new TypeAnnotationState(typeTarget, annotation, pathElements, genericsRequired.bool, bridgeIncompatible.bool);
    }

    private void adjustMethodParameters() {
        IdentityHashMap<MethodInfo, Object> alreadyProcessedMethods = new IdentityHashMap<>();
        for (MethodInfo method : methods) {
            if (alreadyProcessedMethods.containsKey(method)) {
                continue;
            }
            alreadyProcessedMethods.put(method, null);

            if (signaturePresent.containsKey(method)) {
                // if the method has a signature, we derived the parameter list from it,
                // and it never includes mandated or synthetic parameters
                continue;
            }

            if (isInnerConstructor(method)) {
                // inner class constructor, descriptor includes 1 mandated or synthetic parameter at the beginning
                DotName enclosingClass = null;
                if (method.declaringClass().enclosingClass() != null) {
                    enclosingClass = method.declaringClass().enclosingClass();
                } else if (method.declaringClass().enclosingMethod() != null) {
                    enclosingClass = method.declaringClass().enclosingMethod().enclosingClass();
                }

                Type[] parameterTypes = method.methodInternal().parameterTypesArray();
                if (parameterTypes.length > 0 && parameterTypes[0].name().equals(enclosingClass)) {
                    Type[] newParameterTypes = new Type[parameterTypes.length - 1];
                    System.arraycopy(parameterTypes, 1, newParameterTypes, 0, parameterTypes.length - 1);
                    method.setParameters(intern(newParameterTypes));
                }
            } else if (isEnumConstructor(method)) {
                // enum constructor, descriptor includes 2 synthetic parameters at the beginning
                Type[] parameterTypes = method.methodInternal().parameterTypesArray();
                if (parameterTypes.length >= 2
                        && parameterTypes[0].kind() == Type.Kind.CLASS
                        && parameterTypes[0].name().equals(DotName.STRING_NAME)
                        && parameterTypes[1].kind() == Type.Kind.PRIMITIVE
                        && parameterTypes[1].asPrimitiveType().primitive() == PrimitiveType.Primitive.INT) {
                    Type[] newParameterTypes;
                    if (parameterTypes.length == 2) {
                        newParameterTypes = Type.EMPTY_ARRAY;
                    } else {
                        newParameterTypes = new Type[parameterTypes.length - 2];
                        System.arraycopy(parameterTypes, 2, newParameterTypes, 0, parameterTypes.length - 2);
                    }
                    method.setParameters(intern(newParameterTypes));
                }
            }
        }
    }

    private boolean isInnerConstructor(MethodInfo method) {
        if (!method.isConstructor()) {
            return false;
        }

        ClassInfo klass = method.declaringClass();

        // there's no indication in the class file whether a local or anonymous class was declared in static context,
        // so we use some heuristics here
        if (klass.nestingType() == ClassInfo.NestingType.LOCAL
                || klass.nestingType() == ClassInfo.NestingType.ANONYMOUS) {

            // synthetic or mandated parameter at the beginning of the parameter list is the enclosing instance,
            // the constructor belongs to a "non-static" class
            //
            // this works for:
            // - javac with `-g` or `-parameters`
            // - javac since JDK 21 (see https://bugs.openjdk.org/browse/JDK-8292275)
            // - ecj with `-parameters` (see also `processLocalVariableTable`)
            MethodParamList parameters = methodParams.get(method);
            if (parameters != null && parameters.firstIsEnclosingInstance()) {
                return true;
            }

            // synthetic field named `this$*` is the enclosing instance,
            // the constructor belongs to a "non-static" class
            //
            // this works for:
            // - javac until JDK 18 (see https://bugs.openjdk.org/browse/JDK-8271623)
            // - ecj
            for (FieldInfo field : fields) {
                if (Modifiers.isSynthetic(field.flags()) && field.name().startsWith("this$")) {
                    return true;
                }
            }

            return false;
        }

        return klass.nestingType() == ClassInfo.NestingType.INNER && !Modifier.isStatic(klass.flags());
    }

    private static boolean isEnumConstructor(MethodInfo method) {
        return method.declaringClass().isEnum() && method.isConstructor();
    }

    private void resolveTypeAnnotations() {
        for (AnnotationTarget key : typeAnnotationsKeys) {
            List<TypeAnnotationState> annotations = typeAnnotations.get(key);

            for (TypeAnnotationState annotation : annotations) {
                resolveTypeAnnotation(key, annotation);
            }
        }
    }

    private void resolveUsers() throws IOException {
        // class references in constant pool
        int poolSize = constantPoolSize;
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        for (int i = 0; i < poolSize; i++) {
            int offset = offsets[i];
            if (pool[offset] == CONSTANT_CLASS) {
                int nameIndex = (pool[++offset] & 0xFF) << 8 | (pool[++offset] & 0xFF);
                DotName usedClass = names.convertToName(decodeUtf8Entry(nameIndex), '/');
                recordUsedClass(usedClass);
            }
        }

        // class declaration
        for (TypeVariable typeParameter : currentClass.typeParameters()) {
            recordUsedType(typeParameter);
        }
        recordUsedType(currentClass.superClassType());
        for (Type interfaceType : currentClass.interfaceTypes()) {
            recordUsedType(interfaceType);
        }
        for (DotName permittedSubclass : currentClass.permittedSubclasses()) {
            recordUsedClass(permittedSubclass);
        }
        // field declarations
        for (FieldInfo field : fields) {
            recordUsedType(field.type());
        }
        // method declarations (ignoring receiver types, they are always the current class)
        for (MethodInfo method : methods) {
            for (TypeVariable typeParameter : method.typeParameters()) {
                recordUsedType(typeParameter);
            }
            recordUsedType(method.returnType());
            for (Type parameterType : method.parameterTypes()) {
                recordUsedType(parameterType);
            }
            for (Type exceptionType : method.exceptions()) {
                recordUsedType(exceptionType);
            }
        }
        // record component declarations
        for (RecordComponentInfo recordComponent : recordComponents) {
            recordUsedType(recordComponent.type());
        }
    }

    private void recordUsedType(Type type) {
        if (type == null) {
            return;
        }

        switch (type.kind()) {
            case CLASS:
                recordUsedClass(type.asClassType().name());
                break;
            case PARAMETERIZED_TYPE:
                recordUsedClass(type.asParameterizedType().name());
                for (Type typeArgument : type.asParameterizedType().arguments()) {
                    recordUsedType(typeArgument);
                }
                break;
            case ARRAY:
                recordUsedType(type.asArrayType().elementType());
                break;
            case WILDCARD_TYPE:
                recordUsedType(type.asWildcardType().bound());
                break;
            case TYPE_VARIABLE:
                for (Type bound : type.asTypeVariable().boundArray()) {
                    recordUsedType(bound);
                }
                break;
        }
    }

    private void recordUsedClass(DotName usedClass) {
        Set<ClassInfo> usersOfClass = users.get(usedClass);
        if (usersOfClass == null) {
            usersOfClass = new LinkedHashSet<>();
            users.put(usedClass, usersOfClass);
        }
        usersOfClass.add(this.currentClass);
    }

    private void updateTypeTargets() {
        for (AnnotationTarget key : typeAnnotationsKeys) {
            List<TypeAnnotationState> annotations = typeAnnotations.get(key);

            for (TypeAnnotationState annotation : annotations) {
                updateTypeTarget(key, annotation);
            }
        }
    }

    private static Type[] getTypeParameters(AnnotationTarget target) {
        if (target instanceof ClassInfo) {
            return ((ClassInfo) target).typeParameterArray();
        } else if (target instanceof MethodInfo) {
            return ((MethodInfo) target).typeParameterArray();
        }

        throw new IllegalStateException("Type annotation referred to type parameters on an invalid target: " + target);
    }

    private static Type[] copyTypeParameters(AnnotationTarget target) {
        if (target instanceof ClassInfo) {
            return ((ClassInfo) target).typeParameterArray().clone();
        } else if (target instanceof MethodInfo) {
            return ((MethodInfo) target).typeParameterArray().clone();
        }

        throw new IllegalStateException("Type annotation referred to type parameters on an invalid target: " + target);
    }

    private static void setTypeParameters(AnnotationTarget target, Type[] typeParameters) {
        if (target instanceof ClassInfo) {
            ((ClassInfo) target).setTypeParameters(typeParameters);
            return;
        } else if (target instanceof MethodInfo) {
            ((MethodInfo) target).setTypeParameters(typeParameters);
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
            if (type.hasImplicitObjectBound()) {
                bound.adjustBoundDown();
            }
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
            if (skipBridge(typeAnnotationState, method)) {
                return;
            }

            MethodParameterTypeTarget parameter = (MethodParameterTypeTarget) typeTarget;
            int index = parameter.position();
            Type[] types = method.copyParameters();
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
                Type returnType = method.returnType();
                if (skipBridge(typeAnnotationState, method)) {
                    return;
                }
                if (!method.isConstructor()) {
                    method.setReturnType(resolveTypePath(returnType, typeAnnotationState));
                } else {
                    // create a synthetic `ClassType` for the purpose of resolving the type path,
                    // which would fail on a `VoidType` if the path points to a nested type
                    // (this happens on inner class constructors with type annotations)
                    Type newType = ClassType.create(method.declaringClass().name());
                    newType = resolveTypePath(newType, typeAnnotationState);
                    returnType = returnType.copyType(newType.annotationArray());
                    // fixup, `resolveTypePath` sets `typeAnnotationState.target` to the synthetic `ClassType`
                    typeAnnotationState.target.setTarget(returnType);
                    method.setReturnType(returnType);
                }
            }
        } else if (typeTarget.usage() == TypeTarget.Usage.EMPTY && target instanceof RecordComponentInfo) {
            RecordComponentInfo recordComponent = (RecordComponentInfo) target;
            recordComponent.setType(resolveTypePath(recordComponent.type(), typeAnnotationState));
        } else if (typeTarget.usage() == TypeTarget.Usage.THROWS && target instanceof MethodInfo) {
            MethodInfo method = (MethodInfo) target;
            int position = ((ThrowsTypeTarget) typeTarget).position();
            Type[] exceptions = method.copyExceptions();

            if (position >= exceptions.length) {
                return;
            }

            exceptions[position] = resolveTypePath(exceptions[position], typeAnnotationState);
            method.setExceptions(intern(exceptions));
        }
    }

    private boolean skipBridge(TypeAnnotationState typeAnnotationState, MethodInfo method) {
        // javac copies annotations to bridge methods (which is good), however type annotations
        // might become invalid. For instance, the bridge signature for @Nullable Object[] is
        // Object (non-array), so usage=ARRAY signature is invalid. Other cases include annotated
        // inner classes.
        //
        // We ignore those annotations for the bridge methods.
        // See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6695379
        return typeAnnotationState.bridgeIncompatible && isBridge(method);
    }

    private boolean isBridge(MethodInfo methodInfo) {
        int bridgeModifiers = Modifiers.SYNTHETIC | Modifiers.BRIDGE;
        return (methodInfo.flags() & bridgeModifiers) == bridgeModifiers;
    }

    private Type resolveTypePath(Type type, TypeAnnotationState typeAnnotationState) {
        PathElementStack elements = typeAnnotationState.pathElements;
        PathElement element = elements.pop();
        if (element == null) {
            boolean canBeNested = type.kind() == Type.Kind.CLASS || type.kind() == Type.Kind.PARAMETERIZED_TYPE;
            boolean isNestedType = canBeNested && innerClasses != null && innerClasses.containsKey(type.name());
            if (isNestedType && elements.emptyOrNoNestedAfterLastParameterized()) {
                // the annotation targets the outermost type where type annotations are admissible
                ArrayDeque<InnerClassInfo> innerClasses = buildClassesQueue(type);
                InnerClassInfo outermostInfo = innerClasses.getFirst();
                if (Modifier.isStatic(outermostInfo.flags)) {
                    // we'll handle this type immediately, hence `rebuildNestedType` shouldn't see it
                    //
                    // we must not do this for a non-static class, because in that case, the outermost
                    // annotable type is an _enclosing_ type of the first type in the `innerClasses` list,
                    // which `rebuildNestedType` never looks at
                    innerClasses.pollFirst();
                }
                DotName outermostName = Modifier.isStatic(outermostInfo.flags) ? outermostInfo.innerClass
                        : outermostInfo.enclosingClass;

                Type outermost = null;
                if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    Type candidate = type;
                    while (candidate != null) {
                        if (outermostName.equals(candidate.name())) {
                            outermost = candidate;
                            break;
                        }
                        if (candidate.kind() != Type.Kind.PARAMETERIZED_TYPE) {
                            break;
                        }
                        candidate = candidate.asParameterizedType().owner();
                    }
                }
                if (outermost == null) {
                    outermost = outermostName.equals(type.name()) ? type : intern(ClassType.create(outermostName));
                }

                outermost = intern(outermost.addAnnotation(AnnotationInstance.create(typeAnnotationState.annotation, null)));
                return rebuildNestedType(outermost, innerClasses, type, 0, typeAnnotationState);
            }

            // the annotation targets the type itself
            // clone the annotation instance with a null target so that it can be interned
            type = intern(type.addAnnotation(AnnotationInstance.create(typeAnnotationState.annotation, null)));
            typeAnnotationState.target.setTarget(type);
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
                // hack for Kotlin which emits a wrong type annotation path
                // (see KotlinTypeAnnotationWrongTypePathTest)
                if (type.kind() == Type.Kind.WILDCARD_TYPE
                        && type.asWildcardType().bound() != null
                        && type.asWildcardType().bound().kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    return type;
                }

                ParameterizedType parameterizedType = type.asParameterizedType();

                if (elements.noNestedBeforeThisParameterizedAfterPreviousParameterized()) {
                    // we need the _outermost_ parameterized type
                    return rebuildOutermostParameterized(parameterizedType, typeAnnotationState, element.pos);
                }

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
                return rebuildNestedType(null, buildClassesQueue(type), type, depth, typeAnnotationState);
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
                    type = ((FieldInfo) enclosingTarget).type();
                } else if (enclosingTarget instanceof RecordComponentInfo) {
                    type = ((RecordComponentInfo) enclosingTarget).type();
                } else {
                    MethodInfo method = (MethodInfo) enclosingTarget;
                    type = target.asEmpty().isReceiver() ? method.receiverType() : method.returnType();
                    if (skipBridge(typeAnnotationState, method)) {
                        return;
                    }
                    if (method.isConstructor()) {
                        return;
                    }
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
                if (skipBridge(typeAnnotationState, method)) {
                    return;
                }
                type = method.methodInternal().parameterTypesArray()[target.asMethodParameterType().position()];

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
                type = ((MethodInfo) enclosingTarget).methodInternal().exceptionArray()[target.asThrows().position()];
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
            // need to check:
            // 1. if the type path is empty (and since `searchNestedType` calls `searchTypePath`
            // with the path element stack depleted, `element == null` is not enough);
            // 2. and if so, if the type can possibly be nested;
            // 3. and if so, if the type is nested or not
            boolean isTypePathEmpty = elements.pathElements.isEmpty();
            boolean canBeNested = type.kind() == Type.Kind.CLASS
                    || type.kind() == Type.Kind.PARAMETERIZED_TYPE;
            boolean isNestedType = canBeNested && innerClasses != null && innerClasses.containsKey(type.name());
            if (isTypePathEmpty && isNestedType) {
                // the annotation targets the outermost type where type annotations are admissible
                DotName outermostName = outermostAnnotableTypeName(type);
                return buildOwnerMap(type).get(outermostName);
            }

            // the annotation targets the type itself
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
                // hack for Kotlin which emits a wrong type annotation path
                // (see KotlinTypeAnnotationWrongTypePathTest)
                if (type.kind() == Type.Kind.WILDCARD_TYPE
                        && type.asWildcardType().bound() != null
                        && type.asWildcardType().bound().kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    return type;
                }

                ParameterizedType parameterizedType = type.asParameterizedType();
                if (elements.noNestedBeforeThisParameterizedAfterPreviousParameterized()) {
                    // we need the _outermost_ parameterized type
                    while (parameterizedType.owner() instanceof ParameterizedType) {
                        parameterizedType = parameterizedType.owner().asParameterizedType();
                    }
                }
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

    private Type rebuildNestedType(Type last, ArrayDeque<InnerClassInfo> classes, Type type, int depth,
            TypeAnnotationState typeAnnotationState) {
        Map<DotName, Type> ownerMap = buildOwnerMap(type);

        // the first type in the list of enclosing types may be static
        if (!classes.isEmpty() && Modifier.isStatic(classes.getFirst().flags)) {
            depth++;
        }

        for (InnerClassInfo current : classes) {
            DotName currentName = current.innerClass;
            Type oType = ownerMap.get(currentName);
            depth--;

            if (last != null) {
                last = intern(oType != null ? convertParameterized(oType).copyType(last)
                        : new ParameterizedType(currentName, null, last));
            } else if (oType != null) {
                last = oType;
            }

            if (depth == 0) {
                if (last == null) {
                    last = intern(ClassType.create(currentName));
                }

                last = resolveTypePath(last, typeAnnotationState);

                // Assignment to -1 messes up IDEA data-flow, use -- instead
                depth--;
            }
        }

        // UGLY HACK - Java 8 has a compiler bug that inserts bogus extra NESTED values
        // when the enclosing type is an anonymous class (potentially multiple if there
        // is multiple nested anonymous classes). Java 11 does not suffer from this issue
        if (depth > 0 && hasAnonymousEncloser(typeAnnotationState)) {
            return resolveTypePath(type, typeAnnotationState);
        }
        // UGLY HACK -- javac emits a type path containing a "nested" instruction for local
        // classes, even though the enclosing classes can't be denoted or annotated
        if (depth > 0 && hasLocalEncloser(typeAnnotationState)) {
            return resolveTypePath(type, typeAnnotationState);
        }

        if (last == null) {
            throw new IllegalStateException("Required class information is missing on: "
                    + typeAnnotationState.target.enclosingTarget().asClass().name().toString());
        }

        return last;
    }

    private ParameterizedType rebuildOutermostParameterized(ParameterizedType type, TypeAnnotationState typeAnnotationState,
            int typeArgIndex) {
        if (type.owner() == null || type.owner().kind() != Type.Kind.PARAMETERIZED_TYPE) {
            Type[] arguments = type.argumentsArray().clone();
            if (typeArgIndex >= arguments.length) {
                throw new IllegalStateException("Type annotation referred to a type argument that does not exist");
            }

            arguments[typeArgIndex] = resolveTypePath(arguments[typeArgIndex], typeAnnotationState);
            return (ParameterizedType) intern(type.copyType(arguments));
        }

        return (ParameterizedType) intern(type.copyType(rebuildOutermostParameterized(type.owner().asParameterizedType(),
                typeAnnotationState, typeArgIndex)));
    }

    private ParameterizedType convertParameterized(Type oType) {
        return oType instanceof ClassType ? oType.asClassType().toParameterizedType() : oType.asParameterizedType();
    }

    private Type searchNestedType(Type type, int depth, TypeAnnotationState typeAnnotationState) {
        Map<DotName, Type> ownerMap = buildOwnerMap(type);
        ArrayDeque<InnerClassInfo> classes = buildClassesQueue(type);

        // the first type in the list of enclosing types may be static
        if (!classes.isEmpty() && Modifier.isStatic(classes.getFirst().flags)) {
            depth++;
        }

        Type last = null;
        for (InnerClassInfo current : classes) {
            DotName currentName = current.innerClass;
            depth--;

            if (ownerMap.containsKey(currentName)) {
                last = ownerMap.get(currentName);
            }

            if (depth == 0) {
                return searchTypePath(last == null ? type : last, typeAnnotationState);
            }
        }

        // UGLY HACKS, see comment in rebuildNestedType
        if (hasAnonymousEncloser(typeAnnotationState)) {
            return searchTypePath(type, typeAnnotationState);
        }
        if (depth > 0 && hasLocalEncloser(typeAnnotationState)) {
            return searchTypePath(type, typeAnnotationState);
        }

        if (last == null) {
            throw new IllegalStateException("Required class information is missing");
        }

        return last;
    }

    private boolean hasAnonymousEncloser(TypeAnnotationState typeAnnotationState) {
        return typeAnnotationState.target instanceof ClassExtendsTypeTarget
                && typeAnnotationState.target.enclosingTarget().asClass().nestingType() == ClassInfo.NestingType.ANONYMOUS;
    }

    private boolean hasLocalEncloser(TypeAnnotationState typeAnnotationState) {
        if (typeAnnotationState.target instanceof ClassExtendsTypeTarget
                && typeAnnotationState.target.enclosingTarget().asClass().nestingType() == ClassInfo.NestingType.LOCAL) {
            return true;
        }

        AnnotationTarget enclosingTarget = typeAnnotationState.target.enclosingTarget();

        ClassInfo enclosingClass = null;
        if (enclosingTarget.kind() == AnnotationTarget.Kind.FIELD) {
            enclosingClass = enclosingTarget.asField().declaringClass();
        } else if (enclosingTarget.kind() == AnnotationTarget.Kind.METHOD) {
            enclosingClass = enclosingTarget.asMethod().declaringClass();
        } else if (enclosingTarget.kind() == AnnotationTarget.Kind.METHOD_PARAMETER) {
            enclosingClass = enclosingTarget.asMethodParameter().method().declaringClass();
        } else if (enclosingTarget.kind() == AnnotationTarget.Kind.RECORD_COMPONENT) {
            enclosingClass = enclosingTarget.asRecordComponent().declaringClass();
        }

        return enclosingClass != null && enclosingClass.nestingType() == ClassInfo.NestingType.LOCAL;
    }

    /**
     * Returns the name of the outermost type that encloses given {@code type} and on which
     * type annotations are admissible. This is either the nearest enclosing {@code static}
     * nested class, or the enclosing top-level class. If {@code type} is not a nested type,
     * returs its name.
     * <p>
     * This could easily be implemented by calling {@link #buildClassesQueue(Type)} and looking
     * at the first element. The only difference is that the present implementation doesn't
     * allocate and is probably a little faster.
     */
    private DotName outermostAnnotableTypeName(Type type) {
        DotName name = type.name();
        if (!innerClasses.containsKey(name)) {
            return name;
        }

        while (true) {
            InnerClassInfo info = innerClasses.get(name);
            if (Modifier.isStatic(info.flags)) {
                return info.innerClass;
            }

            name = info.enclosingClass;
            if (!innerClasses.containsKey(name)) {
                return name;
            }
        }
    }

    /**
     * Returns a list of {@link InnerClassInfo}s representing types enclosing given {@code type}.
     * Only types on which type annotations are admissible are present in the result. That is,
     * the first element of the list represents the outermost type on which type annotations are admissible,
     * and the last element of the list is an {@code InnerClassInfo} representing {@code type} itself.
     * Returns an empty list if {@code type} is not a nested type.
     */
    private ArrayDeque<InnerClassInfo> buildClassesQueue(Type type) {
        ArrayDeque<InnerClassInfo> result = new ArrayDeque<>();

        InnerClassInfo info = innerClasses.get(type.name());
        while (info != null) {
            result.addFirst(info);
            if (Modifier.isStatic(info.flags)) {
                // this inner class is static, so even if an enclosing class existed,
                // type annotations would not be admissible on it
                break;
            }

            DotName name = info.enclosingClass;
            info = name != null ? innerClasses.get(name) : null;
        }

        return result;
    }

    private Map<DotName, Type> buildOwnerMap(Type type) {
        Map<DotName, Type> owners = new HashMap<>();
        do {
            owners.put(type.name(), type);
            type = type instanceof ParameterizedType ? type.asParameterizedType().owner() : null;
        } while (type != null);
        return owners;
    }

    private static class PathElement {
        private enum Kind {
            ARRAY,
            NESTED,
            WILDCARD_BOUND,
            PARAMETERIZED
        }

        private static final Kind[] KINDS = Kind.values();
        private final Kind kind;
        private final int pos;

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

        boolean noNestedBeforeThisParameterizedAfterPreviousParameterized() {
            // `elementPos` is after the last seen element
            // `elementPos - 1` is the last seen element, which is `PARAMETERIZED`
            assert pathElements.get(elementPos - 1).kind == PathElement.Kind.PARAMETERIZED;
            // `elementPos - 2` is where we start searching
            for (int i = elementPos - 2; i >= 0; i--) {
                if (pathElements.get(i).kind == PathElement.Kind.NESTED) {
                    return false;
                }
                if (pathElements.get(i).kind == PathElement.Kind.PARAMETERIZED) {
                    return true;
                }
            }
            return true;
        }

        boolean emptyOrNoNestedAfterLastParameterized() {
            if (pathElements.isEmpty()) {
                return true;
            }

            // "last" is used in an absolute sense, but since this method is only called after
            // the element stack is depleted, we could start from `elementPos - 1` too
            int idx = pathElements.size() - 1;
            while (idx >= 0) {
                if (pathElements.get(idx).kind == PathElement.Kind.NESTED) {
                    return false;
                }
                if (pathElements.get(idx).kind == PathElement.Kind.PARAMETERIZED) {
                    return true;
                }
                idx--;
            }

            // no `NESTED`, but also no `PARAMETERIZED`
            return false;
        }
    }

    private static class TypeAnnotationState {
        private final TypeTarget target;
        private final AnnotationInstance annotation;
        private final boolean genericsRequired;
        private final boolean bridgeIncompatible;
        private final PathElementStack pathElements;

        TypeAnnotationState(TypeTarget target, AnnotationInstance annotation, ArrayList<PathElement> pathElements,
                boolean genericsRequired, boolean bridgeIncompatible) {
            this.target = target;
            this.annotation = annotation;
            this.pathElements = new PathElementStack(pathElements);
            this.genericsRequired = genericsRequired;
            this.bridgeIncompatible = bridgeIncompatible;
        }
    }

    private static class BooleanHolder {
        boolean bool;
    }

    private ArrayList<PathElement> processTargetPath(DataInputStream data, BooleanHolder genericsRequired,
            BooleanHolder bridgeIncompatible) throws IOException {
        int numElements = data.readUnsignedByte();

        ArrayList<PathElement> elements = new ArrayList<PathElement>(numElements);
        for (int i = 0; i < numElements; i++) {
            int kindIndex = data.readUnsignedByte();
            int pos = data.readUnsignedByte();
            PathElement.Kind kind = PathElement.KINDS[kindIndex];
            if (kind == PathElement.Kind.WILDCARD_BOUND || kind == PathElement.Kind.PARAMETERIZED) {
                genericsRequired.bool = true;
            } else if (kind == PathElement.Kind.ARRAY || kind == PathElement.Kind.NESTED) {
                bridgeIncompatible.bool = true;
            }
            elements.add(new PathElement(kind, pos));
        }

        return elements;
    }

    private void skipTargetPath(DataInputStream data) throws IOException {
        int numElements = data.readUnsignedByte();
        skipFully(data, numElements * 2);
    }

    private void processExceptions(DataInputStream data, MethodInfo target) throws IOException {
        int numExceptions = data.readUnsignedShort();

        Type[] exceptions = numExceptions <= 0 ? Type.EMPTY_ARRAY : new Type[numExceptions];
        for (int i = 0; i < numExceptions; i++) {
            exceptions[i] = intern(ClassType.create(decodeClassEntry(data.readUnsignedShort())));
        }

        // Do not overwrite a signature exception
        if (numExceptions > 0 && target.exceptions().size() == 0) {
            target.setExceptions(exceptions);
        }
    }

    private void processSignature(DataInputStream data, AnnotationTarget target) throws IOException {
        String signature = decodeUtf8Entry(data.readUnsignedShort());
        if (target instanceof ClassInfo) {
            classSignatureIndex = signatures.size();
        }
        signatures.add(signature);
        signatures.add(target);
        signaturePresent.put(target, null);
    }

    private void applySignatures() {
        int end = signatures.size();

        // Class signature should be processed first to establish class type parameters
        signatureParser.beforeNewClass(currentClass.name());
        if (classSignatureIndex >= 0) {
            String elementSignature = (String) signatures.get(classSignatureIndex);
            Object element = signatures.get(classSignatureIndex + 1);
            parseClassSignature(elementSignature, (ClassInfo) element);
        }

        for (int i = 0; i < end; i += 2) {
            if (i == classSignatureIndex) {
                continue;
            }

            signatureParser.beforeNewElement();

            String elementSignature = (String) signatures.get(i);
            Object element = signatures.get(i + 1);

            if (element instanceof FieldInfo) {
                parseFieldSignature(elementSignature, (FieldInfo) element);
            } else if (element instanceof MethodInfo) {
                parseMethodSignature(elementSignature, (MethodInfo) element);
            } else if (element instanceof RecordComponentInfo) {
                parseRecordComponentSignature(elementSignature, (RecordComponentInfo) element);
            }
        }
    }

    private void parseClassSignature(String signature, ClassInfo clazz) {
        GenericSignatureParser.ClassSignature classSignature;
        try {
            classSignature = signatureParser.parseClassSignature(signature, clazz.name());
        } catch (Exception e) {
            // invalid generic signature
            // let's just pretend that no signature exists
            return;
        }

        clazz.setInterfaceTypes(classSignature.interfaces());
        clazz.setSuperClassType(classSignature.superClass());
        clazz.setTypeParameters(classSignature.parameters());
    }

    private void parseFieldSignature(String signature, FieldInfo field) {
        Type type;
        try {
            type = signatureParser.parseFieldSignature(signature);
        } catch (Exception e) {
            // invalid generic signature
            // let's just pretend that no signature exists
            return;
        }

        field.setType(type);
    }

    private void parseMethodSignature(String signature, MethodInfo method) {
        GenericSignatureParser.MethodSignature methodSignature;
        try {
            methodSignature = signatureParser.parseMethodSignature(signature);
        } catch (Exception e) {
            // invalid generic signature
            // let's just pretend that no signature exists
            return;
        }

        method.setParameters(methodSignature.methodParameters());
        method.setReturnType(methodSignature.returnType());
        method.setTypeParameters(methodSignature.typeParameters());
        if (methodSignature.throwables().length > 0) {
            method.setExceptions(methodSignature.throwables());
        }
    }

    private void parseRecordComponentSignature(String signature, RecordComponentInfo recordComponent) {
        Type type = null;
        try {
            // per JVM Specification, signatures stored for records must be field signatures
            type = signatureParser.parseFieldSignature(signature);
        } catch (Exception e) {
            // invalid generic signature
            // let's just pretend that no signature exists
            return;
        }

        recordComponent.setType(type);
    }

    private AnnotationInstance processAnnotation(DataInputStream data, AnnotationTarget target, boolean visible)
            throws IOException {
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
        AnnotationInstance instance = AnnotationInstance.create(annotationName, visible, target, values);

        // Don't record nested annotations in index
        if (target != null) {
            recordAnnotation(classAnnotations, annotationName, instance);
            recordAnnotation(masterAnnotations, annotationName, instance);

            if (target instanceof FieldInfo || target instanceof MethodInfo || target instanceof MethodParameterInfo
                    || target instanceof RecordComponentInfo
                    || target instanceof TypeTarget
                            && ((TypeTarget) target).enclosingTarget().kind() != AnnotationTarget.Kind.CLASS) {
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
                return new AnnotationValue.ByteValue(name, (byte) decodeIntegerEntry(data.readUnsignedShort()));
            case 'C':
                return new AnnotationValue.CharacterValue(name, (char) decodeIntegerEntry(data.readUnsignedShort()));
            case 'I':
                return new AnnotationValue.IntegerValue(name, decodeIntegerEntry(data.readUnsignedShort()));
            case 'S':
                return new AnnotationValue.ShortValue(name, (short) decodeIntegerEntry(data.readUnsignedShort()));

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
                return new AnnotationValue.NestedAnnotation(name, processAnnotation(data, null, true));
            case '[': {
                int numValues = data.readUnsignedShort();
                AnnotationValue[] values = new AnnotationValue[numValues];
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
            interfaces.add(intern(ClassType.create(decodeClassEntry(data.readUnsignedShort()))));
        }
        Type[] interfaceTypes = intern(interfaces.toArray(new Type[interfaces.size()]));
        Type superClassType = superName == null ? null : intern(ClassType.create(superName));

        this.currentClass = new ClassInfo(thisName, superClassType, flags, interfaceTypes);

        if (superName != null)
            addSubclass(superName, currentClass);

        for (int i = 0; i < numInterfaces; i++) {
            DotName superInterface = interfaces.get(i).name();
            // interfaces are intentionally added to implementors
            // it is counter-intuitive, but we keep it to maintain behavioral compatibility
            addImplementor(superInterface, currentClass);
            if (Modifier.isInterface(currentClass.flags())) {
                addSubinterface(superInterface, currentClass);
            }
        }

        if (!currentClass.isModule()) {
            classes.put(currentClass.name(), currentClass);
        }
    }

    private void addSubclass(DotName superName, ClassInfo currentClass) {
        List<ClassInfo> list = subclasses.get(superName);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            subclasses.put(superName, list);
        }

        list.add(currentClass);
    }

    private void addSubinterface(DotName superName, ClassInfo currentClass) {
        List<ClassInfo> list = subinterfaces.get(superName);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            subinterfaces.put(superName, list);
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
        final int magic;
        try {
            magic = stream.readInt();
        } catch (EOFException e) {
            throw new EOFException("Input is not a valid class file; must begin with a 4-byte integer 0xCAFEBABE");
        }
        if (magic != 0xCA_FE_BA_BE) {
            throw new IOException("Input is not a valid class file; must begin with a 4-byte integer 0xCAFEBABE, "
                    + "but seen 0x" + Integer.toHexString(magic).toUpperCase());
        }
    }

    private DotName decodeClassEntry(int index) throws IOException {
        return index == 0 ? null : decodeDotNameEntry(index, CONSTANT_CLASS, "Class_info", '/');
    }

    private DotName decodeModuleEntry(int index) throws IOException {
        return index == 0 ? null : decodeDotNameEntry(index, CONSTANT_MODULE, "Module_info", '.');
    }

    private DotName decodePackageEntry(int index) throws IOException {
        return index == 0 ? null : decodeDotNameEntry(index, CONSTANT_PACKAGE, "Package_info", '/');
    }

    private DotName decodeDotNameEntry(int index, int constantType, String typeName, char delim) throws IOException {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];

        if (pool[pos] != constantType) {
            throw new IllegalStateException(
                    String.format(Locale.ROOT, "Constant pool entry is not a %s type: %d:%d", typeName, index, pos));
        }

        int nameIndex = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
        return names.convertToName(decodeUtf8Entry(nameIndex), delim);
    }

    private String decodeOptionalUtf8Entry(int index) throws IOException {
        return index == 0 ? null : decodeUtf8Entry(index);
    }

    private String decodeUtf8Entry(int index) throws IOException {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;

        int pos = offsets[index - 1];
        if (pool[pos] != CONSTANT_UTF8)
            throw new IllegalStateException("Constant pool entry is not a utf8 info type: " + index + ":" + pos);

        pos++;

        int len = (pool[pos] & 0xFF) << 8 | (pool[pos + 1] & 0xFF);
        if (BitTricks.isAsciiOnly(pool, pos + 2, len)) {
            // see also https://bugs.openjdk.org/browse/JDK-8295496
            return new String(pool, 0, pos + 2, len);
        }

        // slow path
        // DataInputStream needs to read the length again
        return new DataInputStream(new ByteArrayInputStream(pool, pos, len + 2)).readUTF();
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

    private NameAndType decodeNameAndTypeEntry(int index) throws IOException {
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
        return (pool[++pos] & 0xFF) << 24 | (pool[++pos] & 0xFF) << 16 | (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
    }

    private long bitsToLong(byte[] pool, int pos) {
        return ((long) (pool[++pos] & 0xFF)) << 56 |
                ((long) (pool[++pos] & 0xFF)) << 48 |
                ((long) (pool[++pos] & 0xFF)) << 40 |
                ((long) (pool[++pos] & 0xFF)) << 32 |
                ((long) (pool[++pos] & 0xFF)) << 24 |
                ((long) (pool[++pos] & 0xFF)) << 16 |
                ((long) (pool[++pos] & 0xFF)) << 8 |
                ((long) (pool[++pos] & 0xFF));
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
        return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
    }

    private static class IntegerHolder {
        private int i;
    };

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
            case 'V':
                return VoidType.VOID;
            case 'L': {
                int end = start;
                while (descriptor.charAt(++end) != ';')
                    ;
                name = names.convertToName(descriptor.substring(start + 1, end), '/');
                pos.i = end;
                return names.intern(ClassType.create(name));
            }
            case '[': {
                int end = start;
                while (descriptor.charAt(++end) == '[')
                    ;
                int depth = end - start;
                pos.i = end;
                type = parseType(descriptor, pos);
                return names.intern(new ArrayType(type, depth));
            }
            default:
                throw new IllegalArgumentException("Invalid descriptor: " + descriptor + " pos " + start);
        }
    }

    private boolean processConstantPool(DataInputStream stream) throws IOException {
        int size = stream.readUnsignedShort() - 1;
        byte[] buf = tmpObjects.borrowConstantPool(size);
        byte[] annoAttributes = tmpObjects.borrowConstantPoolAnnoAttributes(size);
        int[] offsets = tmpObjects.borrowConstantPoolOffsets(size);
        boolean hasAnnotations = false;

        for (int pos = 0, offset = 0; pos < size; pos++) {
            int tag = stream.readUnsignedByte();
            offsets[pos] = offset;
            switch (tag) {
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                case CONSTANT_METHODTYPE:
                case CONSTANT_MODULE:
                case CONSTANT_PACKAGE:
                    buf = sizeToFit(buf, 3, offset, size - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 2);
                    offset += 2;
                    break;
                case CONSTANT_FIELDREF:
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                case CONSTANT_INTEGER:
                case CONSTANT_INVOKEDYNAMIC:
                case CONSTANT_DYNAMIC:
                case CONSTANT_FLOAT:
                case CONSTANT_NAMEANDTYPE:
                    buf = sizeToFit(buf, 5, offset, size - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 4);
                    offset += 4;
                    break;
                case CONSTANT_LONG:
                case CONSTANT_DOUBLE:
                    buf = sizeToFit(buf, 9, offset, size - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 8);
                    offset += 8;
                    pos++; // 8 byte constant pool entries take two "virtual" slots for some reason
                    break;
                case CONSTANT_METHODHANDLE:
                    buf = sizeToFit(buf, 4, offset, size - pos);
                    buf[offset++] = (byte) tag;
                    stream.readFully(buf, offset, 3);
                    offset += 3;
                    break;
                case CONSTANT_UTF8:
                    int len = stream.readUnsignedShort();
                    buf = sizeToFit(buf, len + 3, offset, size - pos);
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
                    } else if (len == ANNOTATION_DEFAULT_LEN && match(buf, offset, ANNOTATION_DEFAULT)) {
                        annoAttributes[pos] = HAS_ANNOTATION_DEFAULT;
                    } else if (len == METHOD_PARAMETERS_LEN && match(buf, offset, METHOD_PARAMETERS)) {
                        annoAttributes[pos] = HAS_METHOD_PARAMETERS;
                    } else if (len == LOCAL_VARIABLE_TABLE_LEN && match(buf, offset, LOCAL_VARIABLE_TABLE)) {
                        annoAttributes[pos] = HAS_LOCAL_VARIABLE_TABLE;
                    } else if (len == CODE_LEN && match(buf, offset, CODE)) {
                        annoAttributes[pos] = HAS_CODE;
                    } else if (len == MODULE_LEN && match(buf, offset, MODULE)) {
                        annoAttributes[pos] = HAS_MODULE;
                    } else if (len == MODULE_PACKAGES_LEN && match(buf, offset, MODULE_PACKAGES)) {
                        annoAttributes[pos] = HAS_MODULE_PACKAGES;
                    } else if (len == MODULE_MAIN_CLASS_LEN && match(buf, offset, MODULE_MAIN_CLASS)) {
                        annoAttributes[pos] = HAS_MODULE_MAIN_CLASS;
                    } else if (len == RECORD_LEN && match(buf, offset, RECORD)) {
                        annoAttributes[pos] = HAS_RECORD;
                    } else if (len == RUNTIME_INVISIBLE_ANNOTATIONS_LEN && match(buf, offset, RUNTIME_INVISIBLE_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_INVISIBLE_ANNOTATION;
                    } else if (len == RUNTIME_INVISIBLE_PARAM_ANNOTATIONS_LEN
                            && match(buf, offset, RUNTIME_INVISIBLE_PARAM_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_INVISIBLE_PARAM_ANNOTATION;
                    } else if (len == RUNTIME_INVISIBLE_TYPE_ANNOTATIONS_LEN
                            && match(buf, offset, RUNTIME_INVISIBLE_TYPE_ANNOTATIONS)) {
                        annoAttributes[pos] = HAS_RUNTIME_INVISIBLE_TYPE_ANNOTATION;
                    } else if (len == PERMITTED_SUBCLASSES_LEN && match(buf, offset, PERMITTED_SUBCLASSES)) {
                        annoAttributes[pos] = HAS_PERMITTED_SUBCLASSES;
                    }
                    offset += len;
                    break;
                default:
                    throw new IllegalStateException(
                            String.format(Locale.ROOT, "Unknown tag %s! pos = %s poolSize = %s", tag, pos, size));
            }
        }

        constantPoolSize = size;
        constantPool = buf;
        constantPoolOffsets = offsets;
        constantPoolAnnoAttrributes = annoAttributes;

        return hasAnnotations;
    }

    /**
     * Analyze and index the class file data of given {@code clazz}.
     * Each call adds information to the final complete index.
     *
     * @param clazz a previously-loaded class
     * @throws IOException if the class file data is corrupt or the underlying stream fails
     * @throws IllegalArgumentException if {@code clazz} is {@code null}
     */
    public void indexClass(Class<?> clazz) throws IOException {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        String resourceName = '/' + clazz.getName().replace('.', '/') + ".class";
        try (InputStream resource = clazz.getResourceAsStream(resourceName)) {
            index(resource);
        }
    }

    // maintain binary compatibility with Jandex 2.4
    public ClassInfo indexClass$$bridge(Class<?> clazz) throws IOException {
        indexClass(clazz);
        return null;
    }

    /**
     * Analyze and index the class file data present in given input {@code stream}.
     * Each call adds information to the final complete index. Closing the input stream
     * is the caller's responsibility.
     *
     * @param stream the class bytecode to index, must not be {@code null}
     * @throws IOException if the class file data is corrupt or the stream fails
     * @throws IllegalArgumentException if {@code stream} is {@code null}
     */
    public void index(InputStream stream) throws IOException {
        indexWithSummary(stream);
    }

    // maintain binary compatibility with Jandex 2.4
    public ClassInfo index$$bridge(InputStream stream) throws IOException {
        index(stream);
        return null;
    }

    /**
     * Analyze and index the class file data present in given input {@code stream}.
     * Each call adds information to the final complete index. Closing the input stream
     * is the caller's responsibility.
     * <p>
     * For reporting progress in batch indexers, this variant of {@code index} returns
     * a summary of the just-indexed class.
     *
     * @param stream the class bytecode to index, must not be {@code null}
     * @return a summary of the just-indexed class
     * @throws IOException if the class file data is corrupt or the stream fails
     * @throws IllegalArgumentException if {@code stream} is {@code null}
     */
    public ClassSummary indexWithSummary(InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream cannot be null");
        }
        try (DataInputStream data = tmpObjects.dataInputStreamOf(stream)) {
            verifyMagic(data);

            // Retroweaved classes may contain annotations
            // Also, hierarchy info is needed regardless
            if (!isJDK11OrNewer(data)) // refers to JDK 1.1, not JDK 11
                return null;

            initIndexMaps();
            initClassFields();

            processConstantPool(data);
            processClassInfo(data);
            processFieldInfo(data);
            processMethodInfo(data);
            processAttributes(data, currentClass);

            applySignatures();
            adjustMethodParameters(); // must be called _after_ applying signatures and _before_ fixing type annotations
            resolveTypeAnnotations();
            updateTypeTargets();
            resolveUsers();

            currentClass.setMethods(methods, names);
            currentClass.setFields(fields, names);
            currentClass.setRecordComponents(recordComponents, names);
            currentClass.setAnnotations(classAnnotations);
            if (currentClass.isModule() && currentClass.module() != null) {
                if (modulePackages != null) {
                    currentClass.module().setPackages(modulePackages);
                }
                currentClass.module().setMainClass(moduleMainClass);
            }

            return new ClassSummary(currentClass.name(), currentClass.superName(), currentClass.annotationsMap().keySet());
        } finally {
            constantPoolSize = 0;
            tmpObjects.returnConstantPool(constantPool);
            constantPool = null;
            tmpObjects.returnConstantPoolOffsets(constantPoolOffsets);
            constantPoolOffsets = null;
            tmpObjects.returnConstantAnnoAttributes(constantPoolAnnoAttrributes);
            constantPoolAnnoAttrributes = null;

            currentClass = null;
            classAnnotations = null;
            elementAnnotations = null;
            signaturePresent = null;
            signatures = null;
            classSignatureIndex = -1;
            innerClasses = null;
            typeAnnotations = null;
            typeAnnotationsKeys = null;
            methods = null;
            fields = null;
            recordComponents = null;
            methodParams = null;
            modulePackages = null;
            moduleMainClass = null;
        }
    }

    /**
     * Completes, finalizes, and returns the index after zero or more calls to
     * {@code index()}. Future calls to {@code index()} will result in a new index.
     *
     * @return the master index for all scanned class streams
     */
    public Index complete() {
        initIndexMaps(); // if no class was indexed before calling `complete()`

        // these 2 post-processing steps are separate so that when propagating type variables,
        // all type parameters are already fully propagated
        propagateTypeParameterBounds();
        propagateTypeVariables();

        try {
            Map<DotName, List<ClassInfo>> userLists = new HashMap<>();
            for (Map.Entry<DotName, Set<ClassInfo>> entry : users.entrySet()) {
                userLists.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return Index.create(masterAnnotations, subclasses, subinterfaces, implementors, classes, modules, userLists);
        } finally {
            masterAnnotations = null;
            subclasses = null;
            subinterfaces = null;
            implementors = null;
            classes = null;
            modules = null;
            users = null;
            names = null;
            signatureParser = null;
        }
    }

    private void propagateTypeParameterBounds() {
        // we need to process indexed classes such that class A is processed before class B
        // when B is enclosed in A (potentially indirectly)
        //
        // we construct a two-level total order which provides this guarantee:
        // 1. for each class, compute its nesting level (top-level classes have nesting level 0,
        //    classes nested in top-level classes have nesting level 1, etc.) and sort the classes
        //    in ascending nesting level order
        // 2. for equal nesting levels, sort by class name
        // (see also `TotalOrderChecker`)
        Map<DotName, Integer> nestingLevels = new HashMap<>();
        for (ClassInfo clazz : classes.values()) {
            DotName name = clazz.name();
            int nestingLevel = 0;
            while (clazz != null) {
                if (clazz.enclosingClass() != null) {
                    clazz = classes.get(clazz.enclosingClass());
                    nestingLevel++;
                } else if (clazz.enclosingMethod() != null) {
                    clazz = classes.get(clazz.enclosingMethod().enclosingClass());
                    nestingLevel++;
                } else {
                    clazz = null;
                }
            }
            nestingLevels.put(name, nestingLevel);
        }

        List<ClassInfo> classes = new ArrayList<>(this.classes.values());
        classes.sort(new Comparator<ClassInfo>() {
            @Override
            public int compare(ClassInfo c1, ClassInfo c2) {
                int diff = Integer.compare(nestingLevels.get(c1.name()), nestingLevels.get(c2.name()));
                if (diff != 0) {
                    return diff;
                }
                return c1.name().compareTo(c2.name());
            }
        });

        // single shared instance to avoid needless allocations
        Deque<TypeVariable> sharedTypeVarStack = new ArrayDeque<>();

        for (ClassInfo clazz : classes) {
            propagateTypeParameterBounds(clazz, sharedTypeVarStack);
            for (MethodInfo method : clazz.methods()) {
                propagateTypeParameterBounds(method, sharedTypeVarStack);
            }
        }
    }

    private void propagateTypeParameterBounds(AnnotationTarget target, Deque<TypeVariable> sharedTypeVarStack) {
        Type[] typeParameters = copyTypeParameters(target);

        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable typeParameter = (TypeVariable) typeParameters[i];
            Type[] typeParameterBounds = typeParameter.boundArray().clone();
            for (int j = 0; j < typeParameterBounds.length; j++) {
                Type typeParameterBound = typeParameterBounds[j];

                Type newTypeParameterBound = propagateOneTypeParameterBound(typeParameterBound, typeParameters, target);
                if (newTypeParameterBound != typeParameterBound) {
                    Type newTypeParameter = intern(typeParameter.copyType(j, newTypeParameterBound));
                    typeParameters[i] = newTypeParameter;

                    retargetTypeAnnotations(target, typeParameter, newTypeParameter);
                }
            }
        }

        // must be set before patching type variable references, because the patching process
        // sometimes needs to look up type variables from `target`, and those must be the already
        // propagated ones
        setTypeParameters(target, intern(typeParameters));

        // interspersing type annotation propagation (above) with patching would lead to type variable references
        // pointing to stale type variables, hence patching must be an extra editing pass
        for (int i = 0; i < typeParameters.length; i++) {
            sharedTypeVarStack.clear();
            patchTypeVariableReferences(typeParameters[i], sharedTypeVarStack, target);
        }
    }

    // when called from outside, `type` must be a type parameter bound
    private Type propagateOneTypeParameterBound(Type type, Type[] allTypeParams, AnnotationTarget target) {
        if (type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE || type.kind() == Type.Kind.TYPE_VARIABLE) {
            String identifier = getTypeVariableIdentifier(type);
            TypeVariable resolved = findTypeParameter(allTypeParams, identifier);
            if (resolved == null) {
                resolved = resolveTypeParameter(target, identifier);
            }
            if (resolved != null) {
                // can't use resolved.copyType(), because that returns a shallow copy
                // (and if the type variable bounds contain type variable references,
                // they would end up shared incorrectly)
                Type newTypeVariable = intern(deepCopyTypeIfNeeded(resolved).copyType(type.annotationArray()));
                retargetTypeAnnotations(target, type, newTypeVariable);
                return newTypeVariable;
            }
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType parameterized = type.asParameterizedType();
            if (parameterized.owner() != null) {
                Type newOwner = propagateOneTypeParameterBound(parameterized.owner(), allTypeParams, target);
                if (parameterized.owner() != newOwner) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(newOwner));
                }
            }
            Type[] typeArguments = parameterized.argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                Type newTypeArgument = propagateOneTypeParameterBound(typeArguments[i], allTypeParams, target);
                if (newTypeArgument != typeArguments[i]) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(i, newTypeArgument));
                }
            }
            if (parameterized != type) {
                retargetTypeAnnotations(target, type, parameterized);
                return parameterized;
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            WildcardType wildcard = type.asWildcardType();
            Type newBound = propagateOneTypeParameterBound(wildcard.bound(), allTypeParams, target);
            if (newBound != wildcard.bound()) {
                Type newWildcard = intern(wildcard.copyType(newBound));
                retargetTypeAnnotations(target, type, newWildcard);
                return newWildcard;
            }
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            Type newComponent = propagateOneTypeParameterBound(array.component(), allTypeParams, target);
            if (newComponent != array.component()) {
                Type newArray = intern(array.copyType(newComponent, array.dimensions()));
                retargetTypeAnnotations(target, type, newArray);
                return newArray;
            }
        }

        return type;
    }

    /**
     * When {@code type} contains no type variable references, returns {@code type}. When {@code type}
     * does contain type variable references, returns a deep copy with each reference replaced by a new one.
     * In that case, the newly created references must be patched by the caller.
     * <p>
     * When called from outside, {@code type} must be a type variable.
     */
    private Type deepCopyTypeIfNeeded(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE) {
            // type variable references must be patched by the caller, so no need to set target here
            return new TypeVariableReference(type.asTypeVariableReference().identifier(), null, type.annotationArray(),
                    type.asTypeVariableReference().internalClassName());
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable typeVariable = type.asTypeVariable();
            Type[] bounds = typeVariable.boundArray();
            for (int i = 0; i < bounds.length; i++) {
                Type newBound = deepCopyTypeIfNeeded(bounds[i]);
                if (newBound != bounds[i]) {
                    typeVariable = (TypeVariable) intern(typeVariable.copyType(i, newBound));
                }
            }
            if (typeVariable != type) {
                return typeVariable;
            }
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType parameterized = type.asParameterizedType();
            if (parameterized.owner() != null) {
                Type newOwner = deepCopyTypeIfNeeded(parameterized.owner());
                if (parameterized.owner() != newOwner) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(newOwner));
                }
            }
            Type[] typeArguments = parameterized.argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                Type newTypeArgument = deepCopyTypeIfNeeded(typeArguments[i]);
                if (newTypeArgument != typeArguments[i]) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(i, newTypeArgument));
                }
            }
            if (parameterized != type) {
                return parameterized;
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            WildcardType wildcard = type.asWildcardType();
            Type newBound = deepCopyTypeIfNeeded(wildcard.bound());
            if (newBound != wildcard.bound()) {
                return intern(wildcard.copyType(newBound));
            }
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            Type newComponent = deepCopyTypeIfNeeded(array.component());
            if (newComponent != array.component()) {
                return intern(array.copyType(newComponent, array.dimensions()));
            }
        }

        return type;
    }

    /**
     * Patches all type variable references contained in given {@code type}. The {@code typeVarStack} is used
     * to track enclosing type variables when traversing the structure of the type; when called from outside,
     * it must be empty and must not be used anywhere else. The {@code parametricEncloser} is the {@code type}'s
     * nearest enclosing annotation target that may have type parameters, that is, the nearest enclosing method or class.
     */
    private void patchTypeVariableReferences(Type type, Deque<TypeVariable> typeVarStack, AnnotationTarget parametricEncloser) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE) {
            String identifier = type.asTypeVariableReference().identifier();

            for (TypeVariable typeVariable : typeVarStack) {
                if (identifier.equals(typeVariable.identifier())) {
                    type.asTypeVariableReference().setTarget(typeVariable);
                    return;
                }
            }

            TypeVariable typeParameter = resolveTypeParameter(parametricEncloser, identifier);
            if (typeParameter != null) {
                type.asTypeVariableReference().setTarget(typeParameter);
            }
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            typeVarStack.push(type.asTypeVariable());
            for (Type bound : type.asTypeVariable().boundArray()) {
                patchTypeVariableReferences(bound, typeVarStack, parametricEncloser);
            }
            typeVarStack.pop();
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            if (type.asParameterizedType().owner() != null) {
                patchTypeVariableReferences(type.asParameterizedType().owner(), typeVarStack, parametricEncloser);
            }
            for (Type typeArg : type.asParameterizedType().argumentsArray()) {
                patchTypeVariableReferences(typeArg, typeVarStack, parametricEncloser);
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            patchTypeVariableReferences(type.asWildcardType().bound(), typeVarStack, parametricEncloser);
        } else if (type.kind() == Type.Kind.ARRAY) {
            patchTypeVariableReferences(type.asArrayType().component(), typeVarStack, parametricEncloser);
        }
    }

    /**
     * Finds and returns a type variable with given {@code identifier} among given {@code typeParameters}.
     * Returns {@code null} when none exists.
     */
    private TypeVariable findTypeParameter(Type[] typeParameters, String identifier) {
        for (Type typeParameter : typeParameters) {
            if (typeParameter.kind() == Type.Kind.TYPE_VARIABLE) {
                if (typeParameter.asTypeVariable().identifier().equals(identifier)) {
                    return typeParameter.asTypeVariable();
                }
            }
        }
        return null;
    }

    /**
     * Resolves a given type variable {@code identifier} against given parametric {@code target}
     * (either a method or a class). That is, if the {@code target} has a type parameter with matching
     * identifier, returns it; otherwise, resolves {@code identifier} against {@code target}'s nearest
     * enclosing method or class. Returns {@code null} if the identifier can't be resolved.
     */
    private TypeVariable resolveTypeParameter(AnnotationTarget target, String identifier) {
        if (target.kind() == AnnotationTarget.Kind.CLASS) {
            ClassInfo clazz = target.asClass();
            TypeVariable found = findTypeParameter(clazz.typeParameterArray(), identifier);
            if (found != null) {
                return found;
            }
            if (!Modifier.isStatic(clazz.flags())) {
                if (clazz.enclosingClass() != null) {
                    ClassInfo enclosingClass = this.classes.get(clazz.enclosingClass());
                    if (enclosingClass != null) {
                        return resolveTypeParameter(enclosingClass, identifier);
                    }
                } else if (clazz.enclosingMethod() != null) {
                    ClassInfo enclosingClass = this.classes.get(clazz.enclosingMethod().enclosingClass());
                    if (enclosingClass != null) {
                        MethodInfo enclosingMethod = enclosingClass.method(clazz.enclosingMethod().name(),
                                clazz.enclosingMethod().parametersArray());
                        if (enclosingMethod != null) {
                            return resolveTypeParameter(enclosingMethod, identifier);
                        }
                    }
                }
            }
        } else if (target.kind() == AnnotationTarget.Kind.METHOD) {
            MethodInfo method = target.asMethod();
            TypeVariable found = findTypeParameter(method.typeParameterArray(), identifier);
            if (found != null) {
                return found;
            }
            if (!Modifier.isStatic(method.flags())) {
                return resolveTypeParameter(method.declaringClass(), identifier);
            }
        }
        return null;
    }

    private String getTypeVariableIdentifier(Type typeVariable) {
        if (typeVariable.kind() == Type.Kind.TYPE_VARIABLE) {
            return typeVariable.asTypeVariable().identifier();
        } else if (typeVariable.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
            return typeVariable.asUnresolvedTypeVariable().identifier();
        } else {
            return null;
        }
    }

    private void propagateTypeVariables() {
        for (ClassInfo clazz : classes.values()) {
            if (clazz.superClassType() != null) {
                clazz.setSuperClassType(propagateTypeVariables(clazz.superClassType(), clazz));
            }

            Type[] interfaces = clazz.interfaceTypeArray().clone();
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = propagateTypeVariables(interfaces[i], clazz);
            }
            clazz.setInterfaceTypes(intern(interfaces));

            for (FieldInternal field : clazz.fieldArray()) {
                field.setType(propagateTypeVariables(field.type(), clazz));
            }

            for (MethodInternal method : clazz.methodArray()) {
                MethodInfo m = new MethodInfo(clazz, method);

                // no need to propagate type parameters, those were handled before (see `propagateTypeParameterBounds`)

                method.setReturnType(propagateTypeVariables(method.returnType(), m));

                if (method.receiverTypeField() != null) {
                    method.setReceiverType(propagateTypeVariables(method.receiverTypeField(), m));
                }

                Type[] parameterTypes = method.parameterTypesArray().clone();
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterTypes[i] = propagateTypeVariables(parameterTypes[i], m);
                }
                method.setParameterTypes(intern(parameterTypes));

                Type[] exceptionTypes = method.exceptionArray().clone();
                for (int i = 0; i < exceptionTypes.length; i++) {
                    exceptionTypes[i] = propagateTypeVariables(exceptionTypes[i], m);
                }
                method.setExceptions(intern(exceptionTypes));
            }

            for (RecordComponentInternal recordComponent : clazz.recordComponentArray()) {
                recordComponent.setType(propagateTypeVariables(recordComponent.type(), clazz));
            }
        }
    }

    // `parametricEncloser` is `type`'s nearest enclosing annotation target that can have type parameters,
    // that is, the nearest enclosing method or class
    private Type propagateTypeVariables(Type type, AnnotationTarget parametricEncloser) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE || type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
            String identifier = getTypeVariableIdentifier(type);
            TypeVariable resolved = resolveTypeParameter(parametricEncloser, identifier);
            if (resolved != null) {
                // can't use resolved.copyType(), because that returns a shallow copy
                // (and if the type variable bounds contain type variable references,
                // they would end up shared incorrectly)
                Type newTypeVariable = intern(deepCopyTypeIfNeeded(resolved).copyType(type.annotationArray()));
                patchTypeVariableReferences(newTypeVariable, new ArrayDeque<>(), parametricEncloser);
                retargetTypeAnnotations(parametricEncloser, type, newTypeVariable);
                return newTypeVariable;
            }
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType parameterized = type.asParameterizedType();
            if (parameterized.owner() != null) {
                Type newOwner = propagateTypeVariables(parameterized.owner(), parametricEncloser);
                if (parameterized.owner() != newOwner) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(newOwner));
                }
            }
            Type[] typeArguments = parameterized.argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                Type newTypeArgument = propagateTypeVariables(typeArguments[i], parametricEncloser);
                if (newTypeArgument != typeArguments[i]) {
                    parameterized = (ParameterizedType) intern(parameterized.copyType(i, newTypeArgument));
                }
            }
            if (parameterized != type) {
                retargetTypeAnnotations(parametricEncloser, type, parameterized);
                return parameterized;
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            WildcardType wildcard = type.asWildcardType();
            Type newBound = propagateTypeVariables(wildcard.bound(), parametricEncloser);
            if (newBound != wildcard.bound()) {
                Type newWildcard = intern(wildcard.copyType(newBound));
                retargetTypeAnnotations(parametricEncloser, type, newWildcard);
                return newWildcard;
            }
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            Type newComponent = propagateTypeVariables(array.component(), parametricEncloser);
            if (newComponent != array.component()) {
                Type newArray = intern(array.copyType(newComponent, array.dimensions()));
                retargetTypeAnnotations(parametricEncloser, type, newArray);
                return newArray;
            }
        }

        return type;
    }

    private void retargetTypeAnnotations(AnnotationTarget parametricEncloser, Type oldType, Type newType) {
        ClassInfo clazz;
        if (parametricEncloser instanceof ClassInfo) {
            clazz = (ClassInfo) parametricEncloser;
        } else if (parametricEncloser instanceof MethodInfo) {
            clazz = ((MethodInfo) parametricEncloser).declaringClass();
        } else {
            throw new IllegalArgumentException("Expected class or method: " + parametricEncloser);
        }

        for (List<AnnotationInstance> annotationsList : clazz.annotationsMap().values()) {
            for (AnnotationInstance annotation : annotationsList) {
                if (annotation.target().kind() != AnnotationTarget.Kind.TYPE) {
                    continue;
                }

                TypeTarget typeTarget = annotation.target().asType();
                if (typeTarget.target() == oldType) {
                    typeTarget.setTarget(newType);
                }
            }
        }
    }
}
