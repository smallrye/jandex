/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021 Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a module descriptor entry in an index.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 */
public final class ModuleInfo {

    private static final int OPEN = 0x0020;

    private final ClassInfo moduleInfoClass;
    private final DotName name;
    private final short flags;
    private final String version;

    private DotName mainClass;
    private List<RequiredModuleInfo> requires;
    private List<ExportedPackageInfo> exports;
    private List<OpenedPackageInfo> opens;
    private List<DotName> uses;
    private List<ProvidedServiceInfo> provides;
    private List<DotName> packages;

    ModuleInfo(ClassInfo moduleInfoClass, DotName name, short flags, String version) {
        this.moduleInfoClass = moduleInfoClass;
        this.name = name;
        this.flags = flags;
        this.version = version;
        this.packages = Collections.emptyList(); // Initialize
        moduleInfoClass.setModule(this);
    }

    public String toString() {
        return name.toString();
    }

    public ClassInfo moduleInfoClass() {
        return moduleInfoClass;
    }

    /**
     * Returns the name of the class
     *
     * @return the name of the class
     */
    public DotName name() {
        return name;
    }

    /**
     * Returns the access flags for this class. The standard {@link java.lang.reflect.Modifier}
     * can be used to decode the value.
     *
     * @return the access flags
     */
    public short flags() {
        return flags;
    }

    public boolean isOpen() {
        return (flags & OPEN) != 0;
    }

    public String version() {
        return version;
    }

    public DotName mainClass() {
        return mainClass;
    }

    List<RequiredModuleInfo> requiresList() {
        return requires;
    }

    public List<RequiredModuleInfo> requires() {
        return Collections.unmodifiableList(requires);
    }

    List<ExportedPackageInfo> exportsList() {
        return exports;
    }

    public List<ExportedPackageInfo> exports() {
        return Collections.unmodifiableList(exports);
    }

    List<OpenedPackageInfo> opensList() {
        return opens;
    }

    public List<OpenedPackageInfo> opens() {
        return Collections.unmodifiableList(opens);
    }

    List<DotName> usesList() {
        return uses;
    }

    public List<DotName> uses() {
        return Collections.unmodifiableList(uses);
    }

    List<ProvidedServiceInfo> providesList() {
        return provides;
    }

    public List<ProvidedServiceInfo> provides() {
        return Collections.unmodifiableList(provides);
    }

    List<DotName> packagesList() {
        return packages;
    }

    public List<DotName> packages() {
        return Collections.unmodifiableList(packages);
    }

    public final boolean hasAnnotation(DotName name) {
        return moduleInfoClass.hasDeclaredAnnotation(name);
    }

    public final AnnotationInstance annotation(DotName name) {
        return moduleInfoClass.declaredAnnotation(name);
    }

    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        return moduleInfoClass.declaredAnnotationsWithRepeatable(name, index);
    }

    public final Collection<AnnotationInstance> annotations() {
        return moduleInfoClass.declaredAnnotations();
    }

    void setMainClass(DotName mainClass) {
        this.mainClass = mainClass;
    }

    void setRequires(List<RequiredModuleInfo> requires) {
        this.requires = requires;
    }

    void setExports(List<ExportedPackageInfo> exports) {
        this.exports = exports;
    }

    void setOpens(List<OpenedPackageInfo> opens) {
        this.opens = opens;
    }

    void setUses(List<DotName> uses) {
        this.uses = uses;
    }

    void setProvides(List<ProvidedServiceInfo> provides) {
        this.provides = provides;
    }

    void setPackages(List<DotName> packages) {
        this.packages = packages;
    }

    public static final class RequiredModuleInfo {
        private static final int TRANSITIVE = 0x0020;
        private static final int STATIC = 0x0040;

        private final DotName name;
        private final int flags;
        private final String version;

        RequiredModuleInfo(DotName name, int flags, String version) {
            this.name = name;
            this.flags = flags;
            this.version = version;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("requires ");

            if (isStatic()) {
                result.append("static ");
            }

            if (isTransitive()) {
                result.append("transitive ");
            }

            result.append(name.toString());

            if (version != null) {
                result.append('@');
                result.append(version);
            }

            return result.toString();
        }

        public DotName name() {
            return name;
        }

        public int flags() {
            return flags;
        }

        public String version() {
            return version;
        }

        public boolean isStatic() {
            return (flags & STATIC) != 0;
        }

        public boolean isTransitive() {
            return (flags & TRANSITIVE) != 0;
        }
    }

    public static final class ExportedPackageInfo {
        private final DotName source;
        private final int flags;
        private final List<DotName> targets;

        ExportedPackageInfo(DotName source, int flags, List<DotName> targets) {
            this.source = source;
            this.flags = flags;
            this.targets = targets;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("exports ");

            result.append(source.toString());

            if (!targets.isEmpty()) {
                result.append(" to ");

                for (int i = 0, m = targets.size(); i < m; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    result.append(targets.get(i));
                }
            }

            return result.toString();
        }

        public DotName source() {
            return source;
        }

        public int flags() {
            return flags;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        List<DotName> targetsList() {
            return targets;
        }

        public List<DotName> targets() {
            return Collections.unmodifiableList(targets);
        }
    }

    public static final class OpenedPackageInfo {
        private final DotName source;
        private final int flags;
        private final List<DotName> targets;

        OpenedPackageInfo(DotName source, int flags, List<DotName> targets) {
            this.source = source;
            this.flags = flags;
            this.targets = targets;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("opens ");

            result.append(source.toString());

            if (!targets.isEmpty()) {
                result.append(" to ");

                for (int i = 0, m = targets.size(); i < m; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    result.append(targets.get(i));
                }
            }

            return result.toString();
        }

        public DotName source() {
            return source;
        }

        public int flags() {
            return flags;
        }

        public boolean isQualified() {
            return !targets.isEmpty();
        }

        List<DotName> targetsList() {
            return targets;
        }

        public List<DotName> targets() {
            return Collections.unmodifiableList(targets);
        }
    }

    public static final class ProvidedServiceInfo {
        private final DotName service;
        private final List<DotName> providers;

        ProvidedServiceInfo(DotName name, List<DotName> providers) {
            this.service = name;
            this.providers = providers;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("provides ");

            result.append(service.toString());

            if (!providers.isEmpty()) {
                result.append(" with ");

                for (int i = 0, m = providers.size(); i < m; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }

                    result.append(providers.get(i));
                }
            }

            return result.toString();
        }

        public DotName service() {
            return service;
        }

        List<DotName> providersList() {
            return providers;
        }

        public List<DotName> providers() {
            return Collections.unmodifiableList(providers);
        }
    }
}
