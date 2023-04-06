/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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

/**
 * A {@code DotName} represents a dot separated name, typically a Java package or a Java class.
 * It has two possible variants. A simple wrapper based variant allows for fast construction
 * (it simply wraps the specified name string). Whereas, a componentized variant represents
 * one or more {@code String} components that, when combined with a dot character, assemble the
 * full name. The intention of the componentized variant is that the {@code String} components
 * can be reused to offer memory efficiency. This reuse is common in Java where packages
 * and classes follow a tree structure.
 * <p>
 * Both the simple and componentized variants are considered semantically equivalent if they
 * refer to the same logical name. More specifically, the {@code equals} and {@code hashCode}
 * methods return the same values for the same semantic name regardless of the variant used.
 * Which variant to use when depends on the specific performance and overhead objectives
 * of the specific use pattern.
 * <p>
 * Simple names are cheap to construct (just an additional wrapper object), so are ideal for
 * temporary use, like looking for an entry in a {@code Map}. Componentized names however require
 * that they be split in advance, and so require some additional time to construct. However, the memory
 * benefits of reusing component strings make them desirable when stored in a longer term area
 * such as in a Java data structure.
 *
 * @author Jason T. Greene
 */
public final class DotName implements Comparable<DotName> {
    static final DotName JAVA_NAME;
    static final DotName JAVA_LANG_NAME;
    static final DotName JAVA_LANG_ANNOTATION_NAME;
    public static final DotName OBJECT_NAME;
    public static final DotName ENUM_NAME;
    public static final DotName RECORD_NAME;
    public static final DotName STRING_NAME;

    private final DotName prefix;
    private final String local;
    private int hash;
    private final boolean componentized;
    private final boolean innerClass;

    static {
        JAVA_NAME = new DotName(null, "java", true, false);
        JAVA_LANG_NAME = new DotName(JAVA_NAME, "lang", true, false);
        JAVA_LANG_ANNOTATION_NAME = new DotName(JAVA_LANG_NAME, "annotation", true, false);
        OBJECT_NAME = new DotName(JAVA_LANG_NAME, "Object", true, false);
        ENUM_NAME = new DotName(JAVA_LANG_NAME, "Enum", true, false);
        RECORD_NAME = new DotName(JAVA_LANG_NAME, "Record", true, false);
        STRING_NAME = new DotName(JAVA_LANG_NAME, "String", true, false);
    }

    /**
     * Constructs a simple {@link DotName} which stores the string in its entirety. This variant is ideal
     * for temporary usage, such as looking up an entry in a {@code Map} or an {@linkplain Index index}.
     *
     * @param name a fully qualified name (with dots); must not be {@code null}
     * @return a simple {@code DotName} that wraps given {@code name}; never {@code null}
     */
    public static DotName createSimple(String name) {
        return new DotName(null, name, false, false);
    }

    /**
     * Constructs a simple {@link DotName} which stores the name of given class in its entirety.
     * This variant is ideal for temporary usage, such as looking up an entry in a {@code Map}
     * or an {@linkplain Index index}.
     * <p>
     * This method is a shortcut for {@code DotName.createSimple(clazz.getName())}.
     *
     * @param clazz a class whose fully qualified name is returned; must not be {@code null}
     * @return a simple {@code DotName} that wraps the name of given {@code clazz}; never {@code null}
     */
    public static DotName createSimple(Class<?> clazz) {
        return createSimple(clazz.getName());
    }

    /**
     * Constructs a componentized {@link DotName}. Such {@code DotName} refers to
     * a parent prefix (or {@code null} if there is no further prefix) in addition
     * to a local name that has no dot separator. The fully qualified name this
     * {@code DotName} represents is constructed by recursing all parent prefixes
     * and joining all local names with the {@code '.'} character.
     *
     * @param prefix another {@code DotName} that is the portion of the final name to the left of {@code localName}; may be
     *        {@code null} if there is no prefix
     * @param localName the local portion of this name; must not be {@code null} and must not contain {@code '.'}
     * @return a componentized {@code DotName}; never {@code null}
     */
    public static DotName createComponentized(DotName prefix, String localName) {
        if (localName.indexOf('.') != -1)
            throw new IllegalArgumentException("A componentized DotName must not contain '.' characters in a local name");

        return new DotName(prefix, localName, true, false);
    }

    /**
     * Constructs a componentized {@link DotName}. Such {@code DotName} refers to
     * a parent prefix (or {@code null} if there is no further prefix) in addition
     * to a local name that has no dot separator. The fully qualified name this
     * {@code DotName} represents is constructed by recursing all parent prefixes
     * and joining all local names with the {@code '.'} character.
     *
     * @param prefix another {@code DotName} that is the portion of the final name to the left of {@code localName}; may be
     *        {@code null} if there is no prefix
     * @param localName the local portion of this name; must not be {@code null} and must not contain {@code '.'}
     * @param innerClass whether the {@code localName} is an inner class style name, which is joined to the prefix using
     *        {@code '$'} instead of {@code '.'}
     * @return a componentized {@code DotName}; never {@code null}
     */
    public static DotName createComponentized(DotName prefix, String localName, boolean innerClass) {
        if (localName.indexOf('.') != -1) {
            throw new IllegalArgumentException("A componentized DotName must not contain '.' characters in a local name");
        }

        return new DotName(prefix, localName, true, innerClass);
    }

    DotName(DotName prefix, String local, boolean noDots, boolean innerClass) {
        if (local == null) {
            throw new IllegalArgumentException("Local name must not be null");
        }

        if (prefix != null && !prefix.componentized) {
            throw new IllegalArgumentException("A componentized DotName must not have a non-componentized prefix");
        }

        this.prefix = prefix;
        this.local = local;
        this.componentized = noDots;
        this.innerClass = innerClass;
    }

    /**
     * Returns the parent prefix for this {@link DotName} or {@code null} if there is none.
     * Simple {@code DotName} variants never have a prefix.
     *
     * @return the parent prefix for this {@code DotName}; may be {@code null}
     */
    public DotName prefix() {
        return prefix;
    }

    /**
     * Returns the local portion of this {@link DotName}. In simple variants,
     * the entire fully qualified string is returned. In componentized variants,
     * just the rightmost portion not including a separator (either {@code '.'}
     * or {@code '$'}) is returned.
     * <p>
     * Use {@link #withoutPackagePrefix()} instead of this method if the
     * desired value is the part of the string (including {@code '$'} signs
     * if present) after the rightmost {@code '.'} delimiter.
     *
     * @return the local portion of this {@code DotName}; never {@code null}
     */
    public String local() {
        return local;
    }

    /**
     * Returns the portion of this {@link DotName} that does not contain a package prefix.
     * In the case of an inner class syntax name, the {@code '$'} portion is included in
     * the return value.
     *
     * @return the portion of the fully qualified name that does not include a package name
     * @since 2.1.1
     */
    public String withoutPackagePrefix() {
        if (componentized) {
            StringBuilder builder = new StringBuilder();
            stripPackage(builder);
            return builder.toString();
        } else {
            int index = local.lastIndexOf('.');
            return index == -1 ? local : index < local.length() - 1 ? local.substring(index + 1) : "";
        }
    }

    private void stripPackage(StringBuilder builder) {
        if (innerClass) {
            prefix.stripPackage(builder);
            builder.append('$');
        }
        builder.append(local);
    }

    /**
     * Returns the package portion of this {@link DotName}.
     *
     * @return the package name or {@code null} if this {@link DotName} has no package prefix
     * @since 2.4
     */
    public String packagePrefix() {
        if (componentized) {
            if (prefix == null) {
                return null;
            }
            if (innerClass) {
                return prefix.packagePrefix();
            }
            return prefix.toString();
        } else {
            int index = local.lastIndexOf('.');
            return index == -1 ? null : local.substring(0, index);
        }
    }

    /**
     * Returns the package portion of this {@link DotName}. This is a {@code DotName}-returning
     * variant of {@link #packagePrefix()}.
     *
     * @return the package name or {@code null} if this {@link DotName} has no package prefix
     * @since 3.0
     */
    public DotName packagePrefixName() {
        if (componentized) {
            if (prefix == null) {
                return null;
            }
            if (innerClass) {
                return prefix.packagePrefixName();
            }
            return prefix;
        } else {
            int index = local.lastIndexOf('.');
            return index == -1 ? null : DotName.createSimple(local.substring(0, index));
        }
    }

    /**
     * Returns whether this {@link DotName} is a componentized variant.
     *
     * @return {@code true} if it is componentized, {@code false} if it is a simple {@code DotName}
     */
    public boolean isComponentized() {
        return componentized;
    }

    /**
     * Returns whether the local portion of a componentized {@link DotName} is separated
     * by an inner class style delimiter ({@code '$'}). The result is undefined when this
     * {@code DotName} is not componentized.
     * <p>
     * This should not be used to test whether the name truly refers to an inner class,
     * only that the dollar sign delimits the value. Java class names are allowed to contain
     * {@code '$'} signs, so the local value could simply be a fragment of a class name,
     * and not an actual inner class. The correct way to determine whether a name refers
     * to an actual inner class is to look up a {@link ClassInfo} in the index and examine
     * the nesting type like so:
     *
     * <pre class="brush:java">
     * index.getClassByName(name).nestingType() != TOP_LEVEL;
     * </pre>
     *
     * @return {@code true} if local is an inner class style delimited name, {@code false} otherwise
     */
    public boolean isInner() {
        return innerClass;
    }

    /**
     * Returns the regular binary class name.
     *
     * @return the binary class name
     */
    public String toString() {
        return toString('.');
    }

    /**
     * Returns the regular binary class name where {@code delim} is used as a package separator.
     *
     * @param delim the package separator; typically {@code .}, but may be e.g. {@code /}
     *        to construct a bytecode descriptor
     * @return the binary class name with given character used as a package separator
     */
    public String toString(char delim) {
        if (componentized) {
            StringBuilder builder = new StringBuilder();
            buildString(delim, builder);
            return builder.toString();
        } else {
            return delim == '.' ? local : local.replace('.', delim);
        }
    }

    private void buildString(char delim, StringBuilder builder) {
        if (prefix != null) {
            prefix.buildString(delim, builder);
            builder.append(innerClass ? '$' : delim);
        }
        builder.append(local);
    }

    /**
     * Returns a hash code which is based on the semantic representation of this {@link DotName}.
     * <p>
     * Whether a {@code DotName} is componentized has no impact on the calculated hash code.
     * In other words, a componentized {@code DotName} and a simple {@code DotName} that
     * represent the same fully qualified name have the same hash code.
     *
     * @return a hash code representing this object
     * @see Object#hashCode()
     */
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        if (prefix != null) {
            hash = prefix.hashCode() * 31 + (innerClass ? '$' : '.');

            // luckily String.hashCode documents the algorithm it follows
            for (int i = 0; i < local.length(); i++) {
                hash = 31 * hash + local.charAt(i);
            }
        } else {
            hash = local.hashCode();
        }

        return this.hash = hash;
    }

    /**
     * Compares a {@link DotName} to another {@code DotName} and returns {@code true}
     * if they represent the same underlying semantic name. In other words, whether a
     * name is componentized or simple has no bearing on the comparison.
     *
     * @param o the {@code DotName} object to compare to
     * @return true if equal, false if not
     *
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof DotName))
            return false;

        DotName other = (DotName) o;
        if (this.prefix == null && other.prefix == null)
            return local.equals(other.local) && innerClass == other.innerClass;

        if (this.hash != 0 && other.hash != 0 && this.hash != other.hash)
            return false;

        return componentizedEquals(this, other);
    }

    private static boolean componentizedEquals(DotName a, DotName b) {
        // fast path for identical componentizations
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.innerClass == b.innerClass && a.local.equals(b.local)) {
            return componentizedEquals(a.prefix, b.prefix);
        }

        // this algorithm simply goes from the end towards the beginning, both `DotName`s in parallel,
        // and checks that they match on each position; whenever there is a mismatch, the result is `false`
        //
        // positions range from -1 to <local name length - 1>, where values >= 0 are indices into
        // the string and the -1 value is used for a separator (either '$' or '.', depending on
        // whether given name has the `innerClass` flag set)
        //
        // an interesting situation occurs at the position -1 of the farthest `DotName` (one that has
        // no prefix): we still need to compare whether the `innerClass` flags match, but there's no need
        // to write a special case for it, we just treat a prefix-less `DotName` that is _not_ flagged `innerClass`
        // as having an extra '.' character at the beginning

        String aLocal = a.local;
        String bLocal = b.local;
        int aPos = aLocal.length() - 1;
        int bPos = bLocal.length() - 1;
        while (a != null && b != null) {
            char aChar = aPos >= 0 ? aLocal.charAt(aPos) : (a.innerClass ? '$' : '.');
            char bChar = bPos >= 0 ? bLocal.charAt(bPos) : (b.innerClass ? '$' : '.');

            if (aChar != bChar) {
                return false;
            }

            aPos--;
            if (aPos < -1) {
                a = a.prefix;
                if (a != null) {
                    aLocal = a.local;
                    aPos = aLocal.length() - 1;
                }
            }

            bPos--;
            if (bPos < -1) {
                b = b.prefix;
                if (b != null) {
                    bLocal = b.local;
                    bPos = bLocal.length() - 1;
                }
            }
        }
        return a == null && b == null;
    }

    /**
     * Compares a {@link DotName} to another {@code DotName} and returns whether this {@code DotName}
     * is lesser than, greater than, or equal to the specified DotName. If this {@code DotName} is lesser,
     * a negative value is returned. If greater, a positive value is returned. If equal, zero is returned.
     *
     * @param other the {@code DotName} to compare to
     * @return a negative number if this is less than the specified object, a positive if greater, and zero if equal
     *
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(DotName other) {
        // fast path for simple names
        if (this == other) {
            return 0;
        }
        if (this.prefix == null && other.prefix == null) {
            if (this.innerClass != other.innerClass) {
                return this.innerClass ? -1 : 1; // '$' is lesser than '.'
            }
            return this.local.compareTo(other.local);
        }

        return componentizedCompare(flatten(this), flatten(other));
    }

    // note that `a` and `b` have one extra `null` element at the end, see the `flatten` method
    private static int componentizedCompare(DotName[] a, DotName[] b) {
        int aPos = 0; // current position in `a`
        DotName aCur = a[aPos]; // always `a[aPos]`

        int bPos = 0; // current position in `b`
        DotName bCur = b[bPos]; // always `b[bPos]`

        // skip shared components; after the loop, `aCur` and `bCur` may be `null`
        while (aCur == bCur) {
            aPos++;
            aCur = a[aPos];

            bPos++;
            bCur = b[bPos];
        }

        String aCurLocal = null; // always `aCur.local`
        int aCurLocalLength = 0; // always `aCurLocal.length()`
        int aCharPos = -1; // current position in `aCurLocal`
        if (aCur != null) {
            aCurLocal = aCur.local;
            aCurLocalLength = aCurLocal.length();
        }

        String bCurLocal = null; // always `bCur.local`
        int bCurLocalLength = 0; // always `bCurLocal.length()`
        int bCharPos = -1; // current position in `bCurLocal`
        if (bCur != null) {
            bCurLocal = bCur.local;
            bCurLocalLength = bCurLocal.length();
        }

        // compare char by char until the end of the shorter name is reached
        while (aCur != null && bCur != null) {
            char aChar = aCharPos >= 0 ? aCurLocal.charAt(aCharPos) : (aCur.innerClass ? '$' : '.');
            char bChar = bCharPos >= 0 ? bCurLocal.charAt(bCharPos) : (bCur.innerClass ? '$' : '.');
            if (aChar != bChar) {
                return aChar - bChar;
            }

            aCharPos++;
            if (aCharPos == aCurLocalLength) {
                aPos++;
                aCharPos = -1;
                aCur = a[aPos];
                if (aCur != null) {
                    aCurLocal = aCur.local;
                    aCurLocalLength = aCurLocal.length();
                }
            }

            bCharPos++;
            if (bCharPos == bCurLocalLength) {
                bPos++;
                bCharPos = -1;
                bCur = b[bPos];
                if (bCur != null) {
                    bCurLocal = bCur.local;
                    bCurLocalLength = bCurLocal.length();
                }
            }
        }

        // all chars up to the end of the shorter name are equal
        if (aCur == null && bCur == null) {
            // `a` is same length as `b`, so they are equal
            return 0;
        } else if (aCur != null) {
            // `a` is longer than `b`
            return 1;
        } else /* bCur != null */ {
            // `a` is shorter than `b`
            return -1;
        }
    }

    private static DotName[] flatten(DotName name) {
        int count = 0;
        {
            DotName tmp = name;
            while (tmp != null) {
                count++;
                tmp = tmp.prefix;
            }
        }

        DotName[] result = new DotName[count + 1];
        {
            result[count] = null; // sentinel to prevent reaching after the end of array

            DotName tmp = name;
            int index = count - 1;
            while (tmp != null) {
                result[index] = tmp;
                index--;
                tmp = tmp.prefix;
            }
        }

        return result;
    }
}
