/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;

import java.util.ArrayDeque;

/**
 * A DotName represents a dot separated name, typically a Java package or a Java class.
 * It has two possible variants. A simple wrapper based variant allows for fast construction
 * (it simply wraps the specified name string). Whereas, a componentized variant represents
 * one or more String components that when combined with a dot character, assemble the full
 * name. The intention of the componentized variant is that the String components can be reused
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
    private final DotName prefix;
    private final String local;
    private int hash;
    private boolean componentized = false;

    /**
     * Constructs a simple DotName which stores the string in it's entirety. This variant is ideal
     * for temporary usage, such as looking up an entry in a Map.
     *
     * @param name A fully qualified non-null name (with dots)
     * @return a simple DotName that wraps name
     */
    public static DotName createSimple(String name) {
       return new DotName(null, name, false);
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
     * @return a componentized DotName.
     */
    public static DotName createComponentized(DotName prefix, String localName) {
        if (localName.indexOf('.') != -1)
            throw new IllegalArgumentException("A componentized DotName can not contain '.' characters in a local name");

        return new DotName(prefix, localName, true);
    }

    DotName(DotName prefix, String local, boolean noDots) {
        if (local == null)
            throw new IllegalArgumentException("Local string can not be null");

        this.prefix = prefix;
        this.local = local;
        this.componentized = (prefix == null || prefix.componentized) && noDots;
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
     * Returns the local portion of this DotName. In simple variants, the entire fully qualified
     * string is returned. In componentized variants, just the right most portion not including a separator
     * is returned.
     *
     * @return the non-null local portion of this DotName
     */
    public String local() {
        return local;
    }

    /**
     * Returns whether this DotName is a componentized variant.
     *
     * @return true if it is compponentized, false if it is a simple DotName
     */
    public boolean isComponentized() {
        return !componentized;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        if (prefix != null)
            string.append(prefix).append(".");

        string.append(local);

        return string.toString();
    }

    public int hashCode() {
        int hash = this.hash;
        if (hash > 0)
            return hash;

        if (prefix != null) {
            hash = prefix.hashCode() * 31 + '.';

            // Luckily String.hashCode documents the algorithm it follows
            for (int i = 0; i < local.length(); i++) {
                hash = 31 * hash + local.charAt(i);
            }
        } else {
            hash = local.hashCode();
        }

        return this.hash = hash;
    }

    @Override
    public int compareTo(DotName other) {

        if (componentized && other.componentized) {
            ArrayDeque<DotName> thisStack = new ArrayDeque<DotName>();
            ArrayDeque<DotName> otherStack = new ArrayDeque<DotName>();

            DotName curr = this;
            while (curr != null) {
                thisStack.push(curr);
                curr = curr.prefix();
            }

            curr = other;
            while (curr != null) {
                otherStack.push(curr);
                curr = curr.prefix();
            }

            int thisSize = thisStack.size();
            int otherSize = otherStack.size();
            int stop = Math.min(thisSize, otherSize);

            for (int i = 0; i < stop; i++) {
                DotName thisComp = thisStack.pop();
                DotName otherComp = otherStack.pop();

                int comp = thisComp.local.compareTo(otherComp.local);
                if (comp != 0)
                    return comp;
            }

            int diff = thisSize - otherSize;
            if (diff != 0)
                return diff;
        }

        // Fallback to string comparison
        return toString().compareTo(other.toString());
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (! (o instanceof DotName))
            return false;

        DotName other = (DotName)o;
        if (other.prefix == null && prefix == null)
            return local.equals(other.local);

        if (other.prefix == null && prefix != null)
            return toString().equals(other.local);

        if (other.prefix != null && prefix == null)
            return other.toString().equals(local);


        return local.equals(other.local) && prefix.equals(other.prefix);
    }
}
