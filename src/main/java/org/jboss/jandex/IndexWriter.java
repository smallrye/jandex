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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes a Jandex index file to a stream. The write process is somewhat more
 * expensive to allow for fast reads and a compact size. For more information on
 * the index content, see the documentation on {@link Indexer}.
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
 * @see Indexer
 * @see Index
 * @author Jason T. Greene 
 * 
 */
public final class IndexWriter {
    // babelfish (no h)
    private static final int MAGIC = 0xBABE1F15;
    private static final byte VERSION = 1;
    private static final byte FIELD_TAG = 1;
    private static final byte METHOD_TAG = 2;
    private static final byte METHOD_PARAMATER_TAG = 3;
    private static final byte CLASS_TAG = 4; 
    
    private final OutputStream out;
    private StrongInternPool<String> pool;
    StrongInternPool<String>.Index poolIndex;
    private TreeMap<DotName, Integer> classTable;
    
    /**
     * Constructs an IndexWriter using the specified stream
     * 
     * @param out a stream to write an index to
     */
    public IndexWriter(OutputStream out) {
        this.out = out;
    }
    
    /**
     * Writes the specified index to the associated output stream. This may be called multiple times in order
     * to write multiple indexes.
     * 
     * @param index the index to write to the stream
     * @return the number of bytes written to the stream
     * @throws IOException if any i/o error occurs
     */
    public int write(Index index) throws IOException {
        PackedDataOutputStream stream = new PackedDataOutputStream(new BufferedOutputStream(out));
        stream.writeInt(MAGIC);
        stream.writeByte(VERSION);
        
        buildTables(index);
        writeClassTable(stream);
        writeStringTable(stream);
        writeClasses(stream, index);
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
        for (Map.Entry<DotName, Integer> entry : classTable.entrySet()) {
            entry.setValue(pos++);
            DotName name = entry.getKey();
            assert !name.isComponentized(); 
            
            int nameDepth = 0;
            for (DotName prefix = name.prefix(); prefix != null; prefix = prefix.prefix())
                nameDepth++;
            
            stream.writePackedU32(nameDepth);
            stream.writeUTF(name.local());
        }
    }
    
    private int positionOf(String string) {
        int i = poolIndex.positionOf(string);
        if (i < 0)
            throw new IllegalStateException();
        
        return i;
    }
    
    private int positionOf(DotName className) {
        Integer i = classTable.get(className);
        if (i == null)
            throw new IllegalStateException("Class not found in class table:" + className);
        
        return i.intValue();
    }
    
    
    private void writeClasses(PackedDataOutputStream stream, Index index) throws IOException {
        Collection<ClassInfo> classes = index.getKnownClasses();
        stream.writePackedU32(classes.size());
        for (ClassInfo clazz: classes) {
            stream.writePackedU32(positionOf(clazz.name()));
            stream.writePackedU32(clazz.superName() == null ? 0 : positionOf(clazz.superName()));
            stream.writeShort(clazz.flags());
            DotName[] interfaces = clazz.interfaces();
            stream.writePackedU32(interfaces.length);
            for (DotName intf: interfaces)
                stream.writePackedU32(positionOf(intf));
            
            Set<Entry<DotName, List<AnnotationTarget>>> entrySet = clazz.annotations().entrySet();
            stream.writePackedU32(entrySet.size());
            for (Entry<DotName, List<AnnotationTarget>> entry :  entrySet) {
                stream.writePackedU32(positionOf(entry.getKey()));
                
                List<AnnotationTarget> targets = entry.getValue();
                stream.writePackedU32(targets.size());
                for (AnnotationTarget target: targets) {
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
                }
            }
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
            
            for (Entry<DotName, List<AnnotationTarget>> entry :  clazz.annotations().entrySet()) {
                addClassName(entry.getKey());
                
                for (AnnotationTarget target: entry.getValue()) {
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
                }
            }       
        }
        
        poolIndex = pool.index();
    }

    private String intern(String name) {
        if (name == null)
            throw new IllegalArgumentException();
        
        return pool.intern(name);
    }
    
    private void addClassName(DotName name) {
        if (! classTable.containsKey(name))
            classTable.put(name, null);
        
        DotName prefix = name.prefix();
        if (prefix != null)
            addClassName(prefix);
    }

}
