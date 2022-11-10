package org.jboss.jandex;

import java.io.InputStream;
import java.util.Set;

/**
 * Summary of a just-indexed class, returned by {@link Indexer#indexWithSummary(InputStream)}.
 * Contains overview information for reporting progress in batch indexers, as well as
 * some structural information that can safely be exposed to callers during indexing.
 * <p>
 * This class must not expose information that could possibly change during post-processing,
 * when completing the index.
 */
public final class ClassSummary {
    private final DotName name;
    private final DotName superclassName;
    private final Set<DotName> annotations;

    ClassSummary(DotName name, DotName superclassName, Set<DotName> annotations) {
        this.name = name;
        this.superclassName = superclassName;
        this.annotations = annotations;
    }

    /**
     * Returns the name of this class.
     */
    public DotName name() {
        return name;
    }

    /**
     * Returns the name of this class's superclass.
     */
    public DotName superclassName() {
        return superclassName;
    }

    /**
     * Returns the names of annotations present in this class.
     */
    public Set<DotName> annotations() {
        return annotations;
    }

    /**
     * Returns the number of types of annotations present in this class.
     */
    public int annotationsCount() {
        return annotations.size();
    }
}
