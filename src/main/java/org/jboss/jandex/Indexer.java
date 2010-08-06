/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
 * <b>Thread-Safety</b>>/p> This class is not thread-safe can <b>not<b> be
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
    
    private final static int RUNTIME_ANNOTATIONS_LEN = RUNTIME_ANNOTATIONS.length;
    private final static int RUNTIME_PARAM_ANNOTATIONS_LEN = RUNTIME_PARAM_ANNOTATIONS.length;

    private final static int HAS_RUNTIME_ANNOTATION = 1;
    private final static int HAS_RUNTIME_PARAM_ANNOTATION = 2;
    
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
        } 
    }
    
    // Class lifespan fields
    private byte[] constantPool;
    private int[] constantPoolOffsets;
    private byte[] constantPoolAnnoAttrributes;
    private ClassInfo currentClass;
    private volatile ClassInfo publishClass;
    private HashMap<DotName, List<AnnotationTarget>> classAnnotations;
    private StrongInternPool<String> internPool;
    
    // Index lifespan fields
    private Map<DotName, List<AnnotationTarget>> masterAnnotations;
    private Map<DotName, List<ClassInfo>> subclasses;
    private Map<DotName, ClassInfo> classes;
    private Map<String, DotName> names;
    
    private void initIndexMaps() {
        if (masterAnnotations == null)
            masterAnnotations = new HashMap<DotName, List<AnnotationTarget>>();
        
        if (subclasses == null)
            subclasses = new HashMap<DotName, List<ClassInfo>>();
        
        if (classes == null) 
            classes = new HashMap<DotName, ClassInfo>();
        
        if (names == null)
            names = new HashMap<String, DotName>();
    }
    
    
    private DotName convertToName(String name) {
        return convertToName(name, '.');
    }
    
    private DotName convertToName(String name, char delim) {
        DotName result = names.get(name);
        if (result != null)
            return result;
        
        int loc = name.lastIndexOf(delim);
        String local = intern(name.substring(loc + 1));
        DotName prefix = loc < 1 ? null : convertToName(intern(name.substring(0, loc)), delim);
        result = new DotName(prefix, local, true);
        
        names.put(name, result);
        
        return result;
    }
      
    private void processMethodInfo(DataInputStream data) throws IOException {
        int numMethods = data.readUnsignedShort();
        
        for (int i = 0; i < numMethods; i++) {
            short flags = (short) data.readUnsignedShort();
            String name = intern(decodeUtf8Entry(data.readUnsignedShort()));
            String descriptor = decodeUtf8Entry(data.readUnsignedShort());
            
            IntegerHolder pos = new IntegerHolder();
            Type[] args = parseMethodArgs(descriptor, pos); 
            pos.i++;
            Type returnType = parseType(descriptor, pos);
            
            MethodInfo method = new MethodInfo(currentClass, name, args, returnType, flags);
            
            processAttributes(data, method);
        }
    }

    private void processFieldInfo(DataInputStream data) throws IOException {
        int numFields = data.readUnsignedShort();
        
        for (int i = 0; i < numFields; i++) {
            short flags = (short) data.readUnsignedShort();
            String name = intern(decodeUtf8Entry(data.readUnsignedShort()));
            Type type = parseType(decodeUtf8Entry(data.readUnsignedShort()));
            FieldInfo field = new FieldInfo(currentClass, name, type, flags);
            
            processAttributes(data, field);
        }
    }

    private void processAttributes(DataInputStream data, AnnotationTarget target) throws IOException {
        int numAttrs = data.readUnsignedShort();
        for (int a = 0; a < numAttrs; a++) {
            int index = data.readUnsignedShort();
            long attributeLen = data.readInt() & 0xFFFFFFFFL;
            byte annotationAttribute = constantPoolAnnoAttrributes[index - 1];
            if (annotationAttribute == HAS_RUNTIME_ANNOTATION) {
                int numAnnotations = data.readUnsignedShort();
                while (numAnnotations-- > 0)
                    processAnnotation(data, target);
            } else if (annotationAttribute == HAS_RUNTIME_PARAM_ANNOTATION){
                if (!(target instanceof MethodInfo))
                    throw new IllegalStateException("RuntimeVisibleParameterAnnotaitons appeared on a non-method");
                int numParameters = data.readUnsignedByte();
                for (short p = 0; p < numParameters; p++) {
                    int numAnnotations = data.readUnsignedShort();
                    while (numAnnotations-- > 0)
                        processAnnotation(data, new MethodParameterInfo((MethodInfo)target, p));
                }
            } else {
                skipFully(data, attributeLen);
            }
        }
    }

    private void processAnnotation(DataInputStream data, AnnotationTarget target) throws IOException {
        String annotation = convertClassFieldDescriptor(decodeUtf8Entry(data.readUnsignedShort()));
        int valuePairs = data.readUnsignedShort();
        for (int v = 0; v < valuePairs; v++) {
            data.skipBytes(2); // Name
            processAnnotationElementValue(data);
        }
        
        // Don't record nested annotations
        if (target == null)
            return;
        
        DotName annotationName = convertToName(annotation);
        
        recordAnnotation(classAnnotations, annotationName, target);
        recordAnnotation(masterAnnotations, annotationName, target);
        
    }

    private void recordAnnotation(Map<DotName, List<AnnotationTarget>> classAnnotations2, DotName annotation, AnnotationTarget target) {
        List<AnnotationTarget> list = classAnnotations2.get(annotation);
        if (list == null) {
            list = new ArrayList<AnnotationTarget>(); 
            classAnnotations2.put(annotation, list);
        }
        
        list.add(target);
    }

    private String intern(String string) {
        return internPool.intern(string);
    }

    private void processAnnotationElementValue(DataInputStream data) throws IOException {
        int tag = data.readUnsignedByte();
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
            case 'c':
                data.skipBytes(2);
                break;
            case 'e':
                data.skipBytes(4);
                break;
            case '@':
                processAnnotation(data, null);
                break;
            case '[':
                int numValues = data.readUnsignedShort();
                for (int i = 0; i < numValues; i++)
                    processAnnotationElementValue(data);
                break;

        }
    }
  
    
    private void processClassInfo(DataInputStream data) throws IOException {
        short flags = (short) data.readUnsignedShort();
        DotName thisName = decodeClassEntry(data.readUnsignedShort());
        int superIndex = data.readUnsignedShort();
        DotName superName = (superIndex != 0) ? decodeClassEntry(superIndex) : null;
        
        int numInterfaces = data.readUnsignedShort();
        DotName[] interfaces = new DotName[numInterfaces];
        
        for (int i = 0; i < numInterfaces; i++) {
            interfaces[i] = decodeClassEntry(data.readUnsignedShort());
        }
        
        this.classAnnotations = new HashMap<DotName, List<AnnotationTarget>>();
        this.currentClass = new ClassInfo(thisName, superName, flags, interfaces, classAnnotations);
        
        if (superName != null)
            addSubclass(superName, currentClass);
        
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

    private boolean isJDK5OrNewer(DataInputStream stream) throws IOException {
        byte[] buf = new byte[4];
        
        stream.readFully(buf);
        return (buf[2] > 0 || buf[3] > (byte)48);
    }

    private void verifyMagic(DataInputStream stream) throws IOException {
        byte[] buf = new byte[4];
        
        stream.readFully(buf);
        if (buf[0] != (byte)0xCA || buf[1] != (byte)0xFE || buf[2] != (byte)0xBA || buf[3] != (byte)0xBE)
            throw new RuntimeException("Invalid Magic");
        
    }
    
    private DotName decodeClassEntry(int classInfoIndex) {
        byte[] pool = constantPool;
        int[] offsets = constantPoolOffsets;
        
        int pos = offsets[classInfoIndex - 1];
        if (pool[pos] != CONSTANT_CLASS)
            throw new IllegalStateException("Constant pool entry is not a class info type: " + classInfoIndex + ":" + pos);
        
        int nameIndex = (pool[++pos] & 0xFF) << 8 | (pool[++pos] & 0xFF);
        return convertToName(decodeUtf8Entry(nameIndex), '/');
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
        
        return types.toArray(new Type[0]);
    }
    
    private Type parseType(String descriptor) {
        return parseType(descriptor, new IntegerHolder());
    }
    
    private Type parseType(String descriptor, IntegerHolder pos) {
        int start = pos.i;
        
        char c = descriptor.charAt(start);
        DotName name;
        Type.Kind kind = Type.Kind.PRIMITIVE;
        switch (c) {
            case 'B': name = new DotName(null, "byte", true); break;
            case 'C': name = new DotName(null, "char", true); break;
            case 'D': name = new DotName(null, "double", true); break;
            case 'F': name = new DotName(null, "float", true); break;
            case 'I': name = new DotName(null, "int", true); break;
            case 'J': name = new DotName(null, "long", true); break;
            case 'S': name = new DotName(null, "short", true); break;
            case 'Z': name = new DotName(null, "boolean", true); break;
            
            case 'V': 
                name = new DotName(null, "void", true); 
                kind = Type.Kind.VOID; 
                break;
            
            case 'L': {
                int end = start;
                while (descriptor.charAt(++end) != ';');
                name = convertToName(descriptor.substring(start + 1, end), '/');
                kind = Type.Kind.CLASS;
                pos.i = end;
                break;
            }
            case '[': {
                int end = start;
                while (descriptor.charAt(++end) == '[');
                if (descriptor.charAt(end) == 'L') {
                    while (descriptor.charAt(++end) != ';');
                } 
                
                name = new DotName(null, descriptor.substring(start, end + 1), true);
                kind = Type.Kind.ARRAY;
                pos.i = end;
                break;
            }
            default: throw new IllegalArgumentException("Invalid descriptor: " + descriptor + " pos " + start);
        }
        
        return new Type(name, kind);
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
     * @throws IOException
     */
    public ClassInfo index(InputStream stream) throws IOException {
        try
        {
            DataInputStream data = new DataInputStream(new BufferedInputStream(stream));
            verifyMagic(data);
            if (!isJDK5OrNewer(data))
                return null;
           
            initIndexMaps();
            internPool = new StrongInternPool<String>();
            
            boolean hasAnnotations = processConstantPool(data);
            
            processClassInfo(data);
            if (!hasAnnotations)
                return currentClass;
            
            processFieldInfo(data);
            processMethodInfo(data);
            processAttributes(data, currentClass);
            
            // Trigger a happens-before edge since the annotation map is populated 
            // AFTER the class is constructed
            publishClass = currentClass;
            
            return publishClass;
        } finally {
            constantPool = null;
            constantPoolOffsets = null;
            constantPoolAnnoAttrributes = null;
            currentClass = null;
            classAnnotations = null;
            internPool = null;
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
            return new Index(masterAnnotations, subclasses, classes);
        } finally {
            masterAnnotations = null;
            subclasses = null;
            classes = null;
        }
    }    
}