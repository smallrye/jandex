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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
public final class IndexReader {
    private static final int MAGIC = 0xBABE1F15;
    private static final byte VERSION = 1;
    private static final byte FIELD_TAG = 1;
    private static final byte METHOD_TAG = 2;
    private static final byte METHOD_PARAMATER_TAG = 3;
    private static final byte CLASS_TAG = 4;
    
    private InputStream input;
    private DotName[] classTable;
    private String[] stringTable;
    private HashMap<DotName, List<AnnotationTarget>> masterAnnotations;

   
    /**
     * Constructs a new IndedReader using the passed stream. The stream is not
     * read from until the read method is called.
     * 
     * @param input a stream which points to a jandex index file
     */
    public IndexReader(InputStream input) {
        this.input = input;
    }

    /**
     * Read the index at the associated stream of this reader. This method can be called multiple 
     * times if the stream contains multiple index files.
     * 
     * @return the Index contained in the stream
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the stream does not point to Jandex index data
     * @throws UnsupportedVersion if the index data is tagged with a version not known to this reader
     */
    public Index read() throws IOException {
        PackedDataInputStream stream = new PackedDataInputStream(new BufferedInputStream(input));
        if (stream.readInt() != MAGIC)
            throw new IllegalArgumentException("Not a jandex index");
        byte version = stream.readByte();
        
        if (version != VERSION)
            throw new UnsupportedVersion("Version: " + version);

        try {
            masterAnnotations = new HashMap<DotName, List<AnnotationTarget>>();
            readClassTable(stream);
            readStringTable(stream);
            return readClasses(stream);
        } finally {
            classTable = null;
            stringTable = null;
            masterAnnotations = null;
        }
    }


    private Index readClasses(PackedDataInputStream stream) throws IOException {
        int entries = stream.readPackedU32();
        HashMap<DotName, List<ClassInfo>> subclasses = new HashMap<DotName, List<ClassInfo>>();
        HashMap<DotName, ClassInfo> classes = new HashMap<DotName, ClassInfo>();
        masterAnnotations = new HashMap<DotName, List<AnnotationTarget>>();
        
        for (int i = 0; i < entries; i++) {
            DotName name = classTable[stream.readPackedU32()];
            DotName superName = classTable[stream.readPackedU32()];
            short flags = stream.readShort();
            int numIntfs = stream.readPackedU32();
            DotName[] interfaces = new DotName[numIntfs];
            for (int j = 0; j < numIntfs; j++) {
                interfaces[j] = classTable[stream.readPackedU32()];
            }
            
            Map<DotName, List<AnnotationTarget>> annotations = new HashMap<DotName, List<AnnotationTarget>>();
            ClassInfo clazz = new ClassInfo(name, superName, flags, interfaces, annotations);
            classes.put(name, clazz);
            addSubclass(subclasses, superName, clazz);
            readAnnotations(stream, annotations, clazz); 
        }
        
        return new Index(masterAnnotations, subclasses, classes);
    }


    private void readAnnotations(PackedDataInputStream stream, Map<DotName, List<AnnotationTarget>> annotations, ClassInfo clazz)
            throws IOException {
        int numAnnotations = stream.readPackedU32();
        for (int j = 0; j < numAnnotations; j++) {
            DotName annotation = classTable[stream.readPackedU32()];
            
            int numTargets = stream.readPackedU32();
            for (int k = 0; k < numTargets; k++) {
                int tag = stream.readPackedU32();
                
                AnnotationTarget target;
                switch (tag) {
                    case FIELD_TAG: {
                        String name = stringTable[stream.readPackedU32()];
                        Type type = readType(stream);
                        short flags = stream.readShort();
                        target = new FieldInfo(clazz, name, type, flags);
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
                
                recordAnnotation(masterAnnotations, annotation, target);
                recordAnnotation(annotations, annotation, target);
                
            }
        }
    }


    private MethodInfo readMethod(ClassInfo clazz, PackedDataInputStream stream) throws IOException {
        String name = stringTable[stream.readPackedU32()];
        int numArgs = stream.readPackedU32();
        Type args[] = new Type[numArgs];
        for (int i = 0; i < numArgs; i ++) {
            args[i] = readType(stream);
        }
        Type returnType = readType(stream);
        short flags = stream.readShort();
        return  new MethodInfo(clazz, name, args, returnType, flags);
    }
    
    private void recordAnnotation(Map<DotName, List<AnnotationTarget>> annotations, DotName annotation, AnnotationTarget target) {
        List<AnnotationTarget> list = annotations.get(annotation);
        if (list == null) {
            list = new ArrayList<AnnotationTarget>(); 
            annotations.put(annotation, list);
        }
        
        list.add(target);
    }
    
    private void addSubclass(HashMap<DotName, List<ClassInfo>> subclasses, DotName superName, ClassInfo currentClass) {
        List<ClassInfo> list = subclasses.get(superName);
        if (list == null) {
            list = new ArrayList<ClassInfo>();
            subclasses.put(superName, list);
        }
        
        list.add(currentClass);
    }


    private Type readType(PackedDataInputStream stream) throws IOException {
        Type.Kind kind = Type.Kind.fromOrdinal(stream.readByte());
        DotName name = classTable[stream.readPackedU32()];
        return new Type(name, kind);
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
            
            classTable[i] = curr = new DotName(curr, local, true);
            lastDepth = depth;
        }
    }

}
