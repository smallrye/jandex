package org.jboss.jandex.maven;

import java.io.File;
import java.util.List;

public class FileSet {

    private File directory;

    private List<String> includes;

    private List<String> excludes;

    private boolean useDefaultExcludes;

    public File getDirectory() {
        return directory;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public boolean isUseDefaultExcludes() {
        return useDefaultExcludes;
    }

    public void setUseDefaultExcludes(boolean useDefaultExcludes) {
        this.useDefaultExcludes = useDefaultExcludes;
    }
}
