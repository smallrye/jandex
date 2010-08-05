package org.jboss.jandex;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An index useful for quickly processing annotations. 
 * 
 * <p>It contains the following information:
 * <ol>
 * <li>All annotations and a collection of targets they refer to </li>
 * <li>All classes (including interfaces) scanned during the indexing process (typicall all classess in a jar)</li>
 * <li>All subclasses indexed by super class known to this index</li>
 < </ol>
 * 
 * @author Jason T. Greene
 *
 */
public class Index {
    final Map<DotName, List<AnnotationTarget>> annotations;
    final Map<DotName, List<ClassInfo>> subclasses;
    final Map<DotName, ClassInfo> classes;
    
    Index (Map<DotName, List<AnnotationTarget>> annotations,  Map<DotName, List<ClassInfo>> subclasses,  Map<DotName, ClassInfo> classes) {
        this.annotations = Collections.unmodifiableMap(annotations);
        this.classes = Collections.unmodifiableMap(classes);
        this.subclasses = Collections.unmodifiableMap(subclasses);
    }
    
    /**
     * Obtains a list of targets for the specified annotation. 
     * This is done using an O(1) lookup. Valid targets include 
     * field, method, parameter, and class.
     *
     * @param annotationName the name of the annotation to look for
     * @return a list of annotation targets
     */
    public List<AnnotationTarget> getAnnotationTargets(DotName annotationName) {
        return Collections.unmodifiableList(annotations.get(annotationName));
    }
    
    /**
     * Gets all known subclasses of the specified class name. A known subclass is
     * one which was found during the scanning process; however, this is often not 
     * the complete universe of subclasses, since typically indexes are constructed 
     * per jar. It is expected that several indexes will need to be searched when 
     * analyzing a jar that is a part of a complex multi-module/classloader 
     * environment (like an EE application server).
     * 
     * @param className the super class of the desired subclasses
     * @return all known subclasses of className
     */
    public List<ClassInfo> getKnownSubclasses(DotName className) {
        return Collections.unmodifiableList(subclasses.get(className));
    }
    
    /**
     * Gets the class (or interface, or annotation) that was scanned during the 
     * indexing phase.
     * 
     * @param className the name of the class 
     * @return information about the class
     */
    public ClassInfo getClassByName(DotName className) {
        return classes.get(className);
    }
    
    /**
     * Gets all known classes by this index (those which were scanned). 
     * 
     * @return a collection of known classes
     */
    public Collection<ClassInfo> getKnownClasses() {
        return Collections.unmodifiableCollection(classes.values());
    }
    
    /**
     * Print all annotations known by this index to stdout.
     */
    public void printAnnotations()
    {
        System.out.println("Annotations:");
        for (Map.Entry<DotName, List<AnnotationTarget>> e : annotations.entrySet()) {
            System.out.println(e.getKey() + ":");
            for (AnnotationTarget target : e.getValue()) {
                if (target instanceof ClassInfo) {
                    System.out.println("    Class: " + target);
                } else if (target instanceof FieldInfo) {
                    System.out.println("    Field: " + target);
                } else if (target instanceof MethodInfo) {
                    System.out.println("    Method: " + target);
                } else if (target instanceof MethodParameterInfo) {
                    System.out.println("    Parameter: " + target);
                }
            }
        }
    }
    
    /**
     * Print all classes that have known subclasses, and all their subclasses
     */
    public void printSubclasses()
    {
        System.out.println("Subclasses:");
        for (Map.Entry<DotName, List<ClassInfo>> entry : subclasses.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (ClassInfo clazz : entry.getValue())
                System.out.println("    " + clazz.name());
        }
    }
}
