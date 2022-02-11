package org.jboss.jandex;

public final class ClassSummary {
    private final String name;
    private final int annotationsCount;

    ClassSummary(String name, int annotationsCount) {
        this.name = name;
        this.annotationsCount = annotationsCount;
    }

    public String name() {
        return name;
    }

    public int annotationsCount() {
        return annotationsCount;
    }
}
