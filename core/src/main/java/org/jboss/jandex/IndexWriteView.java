package org.jboss.jandex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class IndexWriteView {
    final int annotationCount;
    final int subclassCount;
    final int subinterfaceCount;
    final int implementorCount; // note this also includes direct subinterfaces!

    final List<ClassInfo> classes;
    final List<ModuleInfo> modules;
    final List<User> users;

    IndexWriteView(Index index) {
        annotationCount = index.annotations.size();
        subclassCount = index.subclasses.size();
        subinterfaceCount = index.subinterfaces.size();
        implementorCount = index.implementors.size();

        classes = new ArrayList<>(index.classes.values());
        classes.sort(Comparator.comparing(ClassInfo::name));
        modules = new ArrayList<>(index.modules.values());
        modules.sort(Comparator.comparing(ModuleInfo::name));
        if (index.users != null) {
            users = new ArrayList<>(index.users.size());
            for (Map.Entry<DotName, ClassInfo[]> user : index.users.entrySet()) {
                ClassInfo[] array = Arrays.copyOf(user.getValue(), user.getValue().length);
                Arrays.sort(array, Comparator.comparing(ClassInfo::name));
                users.add(new User(user.getKey(), array));
            }
            users.sort(Comparator.comparing(u -> u.name));
        } else {
            users = Collections.emptyList();
        }
    }

    static final class User {
        final DotName name;
        final ClassInfo[] uses;

        User(DotName name, ClassInfo[] uses) {
            this.name = name;
            this.uses = uses;
        }
    }
}
