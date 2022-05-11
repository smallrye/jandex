package org.jboss.jandex.maven;

public class Dependency {
    private String groupId;

    private String artifactId;

    private String classifier;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    // ---

    @Override
    public String toString() {
        return groupId + ":" + artifactId + (classifier != null ? ":" + classifier : "");
    }
}
