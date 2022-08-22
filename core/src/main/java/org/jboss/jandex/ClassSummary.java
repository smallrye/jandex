package org.jboss.jandex;

import java.util.Set;

public final class ClassSummary {
    private final DotName name;
    private final DotName superclassName;
    private final Set<DotName> annotations;

    ClassSummary(DotName name, DotName superclassName, Set<DotName> annotations) {
        this.name = name;
        this.superclassName = superclassName;
        this.annotations = annotations;
    }

    public DotName name() {
        return name;
    }

    public DotName superclassName() {
        return superclassName;
    }

    public Set<DotName> annotations() {
        return annotations;
    }

    public int annotationsCount() {
        return annotations.size();
    }
}
