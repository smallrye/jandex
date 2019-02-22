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
 * A DotName represents a dot separated name, typically a Java package or a Java class.
 * It has two possible variants. A simple wrapper based variant allows for fast construction
 * (it simply wraps the specified name string). Whereas, a componentized variant represents
 * one or more String methodInternal that when combined with a dot character, assemble the full
 * name. The intention of the componentized variant is that the String methodInternal can be reused
 * to offer memory efficiency. This reuse is common in Java where packages and classes follow
 * a tree structure.
 *
 * <p>Both the simple and componentized variants are considered semantically equivalent if they
 * refer to the same logical name. More specifically the equals and hashCode methods return the
 * same values for the same semantic name regardless of the variant used. Which variant to use
 * when depends on the specific performance and overhead objectives of the specific use pattern.
 *
 * <p>Simple names are cheap to construct (just a an additional wrapper object), so are ideal for
 * temporary use, like looking for an entry in a Map. Componentized names however require that
 * they be split in advance, and so require some additional time to construct. However the memory
 * benefits of reusing component strings make them desirable when stored in a longer term area
 * such as in a Java data structure.
 *
 * @author Jason T. Greene
 *
 */
public final class DotName implements Comparable<DotName> {
    static final DotName JAVA_NAME;
    static final DotName JAVA_LANG_NAME;
    static final DotName OBJECT_NAME;

    private final DotName prefix;
    private final String local;
    private int hash;
    private final boolean componentized;
    private final boolean innerClass;

    static {
        JAVA_NAME = new DotName(null, "java", true, false);
        JAVA_LANG_NAME = new DotName(JAVA_NAME, "lang", true, false);
        OBJECT_NAME = new DotName(JAVA_LANG_NAME, "Object", true, false);
    }

    /**
     * Constructs a simple DotName which stores the string in it's entirety. This variant is ideal
     * for temporary usage, such as looking up an entry in a Map.
     *
     * @param name A fully qualified non-null name (with dots)
     * @return a simple DotName that wraps name
     */
    public static DotName createSimple(String name) {
       return new DotName(null, name, false, false);
    }

    /**
     * Constructs a componentized DotName. Each DotName refers to a parent
     * prefix (or null if there is no further prefix) in addition to a local
     * name that has no dot separator. The fully qualified name this DotName
     * represents is constructed by recursing all parent prefixes and joining all
     * local name values with the '.' character.
     *
     * @param prefix Another DotName that is the portion to the left of
     *        localName, this may be null if there is not one
     * @param localName the local non-null portion of this name, which does not contain
     *        '.'
     * @return a componentized DotName.
     */
    public static DotName createComponentized(DotName prefix, String localName) {
        if (localName.indexOf('.') != -1)
            throw new IllegalArgumentException("A componentized DotName can not contain '.' characters in a local name");

        return new DotName(prefix, localName, true, false);
    }

    /**
     * Constructs a componentized DotName. Each DotName refers to a parent
     * prefix (or null if there is no further prefix) in addition to a local
     * name that has no dot separator. The fully qualified name this DotName
     * represents is consructed by recursing all parent prefixes and joining all
     * local name values with the '.' character.
     *
     * @param prefix Another DotName that is the portion to the left of
     *        localName, this may be null if there is not one
     * @param localName the local non-null portion of this name, which does not contain
     *        '.'
     * @param innerClass whether or not this localName is an inner class style name, requiring '$' vs '.'
     * @return a componentized DotName.
     */
    public static DotName createComponentized(DotName prefix, String localName, boolean innerClass) {
        if (localName.indexOf('.') != -1)
            throw new IllegalArgumentException("A componentized DotName can not contain '.' characters in a local name");

        return new DotName(prefix, localName, true, innerClass);
    }

    DotName(DotName prefix, String local, boolean noDots, boolean innerClass) {
        if (local == null) {
            throw new IllegalArgumentException("Local string can not be null");
        }

        if (prefix != null && !prefix.componentized) {
            throw new IllegalArgumentException("A componentized DotName must have a componentized prefix, or null");
        }

        this.prefix = prefix;
        this.local = local;
        this.componentized = noDots;
        this.innerClass = innerClass;
    }

    /**
     * Returns the parent prefix for this DotName or null if there is none.
     * Simple DotName variants never have a prefix.
     *
     * @return the parent prefix for this DotName
     */
    public DotName prefix() {
        return prefix;
    }

    /**
     * Returns the local portion of this DotName. In simple variants, the entire
     * fully qualified string is returned. In componentized variants, just the
     * right most portion not including a separator (either . or $) is returned.
     *
     * <p>Use {@link #withoutPackagePrefix()} instead of this method if the
     * desired value is simply the right most portion (including dollar signs if
     * present) after a '.' delimiter.
     * </p>
     *
     * @return the non-null local portion of this DotName
     */
    public String local() {
        return local;
    }

    /**
     * Returns the portion of this DotName that does not contain a package prefix.
     * In the case of an inner class syntax name, the $ portion is included in
     * the return value.
     *
     * @return the portion of the name that is not package prefixed
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
     * Returns whether this DotName is a componentized variant.
     *
     * @return true if it is componentized, false if it is a simple DotName
     */
    public boolean isComponentized() {
        return componentized;
    }

    /**
     * Returns whether the local portion of a componentized DotName is separated
     * by an inner class style delimiter ('$"). This should not be used to test
     * whether the name truly refers to an inner class, only that the dollar
     * sign delimits the value. Java class names are allowed to contain dollar
     * signs, so the local value could simply be a fragment of a class name, and
     * not an actual inner class. The correct way to determine whether or not a
     * name refers to an actual inner class is to lookup a ClassInfo in the
     * index and call and examine the nesting type like so:
     *
     * <code><pre>
     *    index.get(name).nestingType() != TOP_LEVEL;
     * </pre></code>
     *
     *
     * @return true if local is an inner class style delimited name, false otherwise
     */
    public boolean isInner() {
        return innerClass;
    }

    /**
     * Returns the regular fully qualifier class name.
     *
     * @return The fully qualified class name
     */
    public String toString() {
        return toString('.');
    }

    public String toString(char delim) {
        String string = local;
        if (prefix != null) {
            StringBuilder builder = new StringBuilder();
            buildString(delim, builder);
            string = builder.toString();
        }

        return string;
    }

    private void buildString(char delim, StringBuilder builder) {
        if (prefix != null) {
            prefix.buildString(delim, builder);
            builder.append(innerClass ? '$' : delim);
        }
        builder.append(local);
    }

    /**
     * Returns a hash code which is based on the semantic representation of this <code>DotName</code>.
     * Whether or not a <code>DotName</code> is componentized has no impact on the calculated hash code.
     *
     * @return a hash code representing this object
     * @see Object#hashCode()
     */
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0)
            return hash;

        if (prefix != null) {
            hash = prefix.hashCode() * 31 + (innerClass ? '$' : '.');

            // Luckily String.hashCode documents the algorithm it follows
            for (int i = 0; i < local.length(); i++) {
                hash = 31 * hash + local.charAt(i);
            }
        } else {
            hash = local.hashCode();
        }

        return this.hash = hash;
    }

    /**
     * Compares a <code>DotName</code> to another <code>DotName</code> and returns whether this DotName
     * is lesser than, greater than, or equal to the specified DotName. If this <code>DotName</code> is lesser,
     * a negative value is returned. If greater, a positive value is returned. If equal, zero is returned.
     *
     * @param other the DotName to compare to
     * @return a negative number if this is less than the specified object, a positive if greater, and zero if equal
     *
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(DotName other) {
        IndexState s1 = new IndexState();
        IndexState s2 = new IndexState();

        for (;;) {
            int c1 = nextChar(s1, this);
            int c2 = nextChar(s2, other);

            if (c1 == -1) {
                return c2 == -1 ? 0 : -1;
            }

            if (c2 == -1) {
                return 1;
            }

            if (c1 != c2) {
                return c1 - c2;
            }
        }
    }

    /**
     * Compares a DotName to another DotName and returns true if the represent
     * the same underlying semantic name. In other words, whether or not a
     * name is componentized or simple has no bearing on the comparison.
     *
     * @param o the DotName object to compare to
     * @return true if equal, false if not
     *
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (! (o instanceof DotName))
            return false;

        DotName other = (DotName)o;
        if (other.prefix == null && prefix == null)
            return local.equals(other.local) && innerClass == other.innerClass;

        if (!other.componentized && componentized)
            return crossEquals(other, this);

        if (other.componentized && !componentized)
            return crossEquals(this, other);

        return prefix != null && innerClass == other.innerClass && local.equals(other.local) && prefix.equals(other.prefix);
    }

    private static boolean crossEquals(final DotName simple, final DotName comp) {
        final String exactToMatch = simple.local;
        // We start matching from the end, as that's what we have in componentized mode:
        int cursor = 0;
        final int len = exactToMatch.length();
        for (DotName d = comp; d != null && cursor - 1 <= len; d = d.prefix) {
            cursor += 1 + d.local.length();
        }

        if (--cursor != len) {
            return false;
        }

        DotName current = comp;
        while (current!=null) {
            final String nextFragment = current.local;
            final int fragLength = nextFragment.length();
            if (! exactToMatch.regionMatches(cursor-fragLength, nextFragment, 0, fragLength)) {
                return false;
            }
            //Jump by fragment match, +1 for the separator symbol:
            cursor = cursor - fragLength - 1;
            if (cursor==-1) {
                //Our exactToMatch reference is finished; just verify we consumed comp completely as well::
                return current.prefix == null;
            }
            final char expectNext = current.innerClass ? '$' : '.';
            if (exactToMatch.charAt(cursor) != expectNext) {
                return false;
            }
            
            current=current.prefix;
        }
        //And finally, verify we consumed it all:
        return cursor == -1;
    }

    private static class IndexState {
        DotName currentPrefix;
        int offset;
    }

    private int nextChar(IndexState state, DotName name) {
        if (state.offset == -1) {
            return -1;
        }

        if (!name.componentized) {
            if (state.offset > name.local.length() - 1) {
                state.offset = -1;
                return -1;
            }
            return name.local.charAt(state.offset++);
        }

        DotName p = name, n = name;
        while (n.prefix != state.currentPrefix) {
            p = n;
            n = n.prefix;
        }

        if (state.offset > n.local.length() - 1) {
            if (n == name) {
                state.offset = -1;
                return -1;
            } else {
                state.offset = 0;
                state.currentPrefix = n;
                return p.isInner() ? '$' : '.';
            }
        }

        return n.local.charAt(state.offset++);
    }

}
