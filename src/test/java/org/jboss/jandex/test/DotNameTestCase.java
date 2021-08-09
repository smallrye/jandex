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
package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.Assert;
import org.junit.Test;

/**
 * Since DotName is often used as a key in collections and implements Comparable,
 * make sure the #compareTo, hashCode, equals and toString are strictly consistent.
 * Not actually that trivial since DotName can use multiple different representations
 * for the same "value".
 * The implementation is also meant to be efficient, in particular we aim at very low
 * memory impact.
 *
 * @author Sanne Grinovero
 */
public class DotNameTestCase {

    // Using a fixed seed for test reproducibility:
    private static final Random r = new Random(13);

    public static final class DotsContainer {
        private final TreeSet<DotName> dotnames = new TreeSet<DotName>();
        private final TreeSet<String> strings = new TreeSet<String>();

        void add(DotName d) {
            dotnames.add(d);
            strings.add(d.toString());
            if (dotnames.size() != strings.size()) {
                throw new AssertionError("Expectation violated");
            }
        }

        int size() {
            return dotnames.size();
        }

        void verifyAll() {
            Assert.assertEquals(strings.size(), dotnames.size());
            Iterator<DotName> dots = dotnames.iterator();
            Iterator<String> names = strings.iterator();
            while (dots.hasNext()) {
                Assert.assertTrue(names.hasNext());
                final String fullname = names.next();
                DotName instance = dots.next();
                Assert.assertEquals(fullname, instance.toString());
                DotName asSimple = DotName.createSimple(fullname);
                Assert.assertTrue(dotnames.contains(asSimple));
                sameHashCode(instance, asSimple);
                Assert.assertEquals(0, instance.compareTo(asSimple));
                Assert.assertEquals(0, asSimple.compareTo(instance));
            }
            Assert.assertEquals(dotnames.toString(), strings.toString());
        }
    }

    @Test
    public void testIsComponentized() {
        assertTrue(DotName.createComponentized(DotName.createComponentized(null, "jboss"), "Foo").isComponentized());
        assertFalse(DotName.createSimple("org.jboss.Foo").isComponentized());
    }

    @Test
    public void testWithoutPackgePrefix() {
        DotName foo = DotName.createComponentized(DotName.createComponentized(null, "root"), "thefoo");
        foo = DotName.createComponentized(foo, "Foo");
        assertEquals("Foo", foo.withoutPackagePrefix());
        DotName inner = DotName.createComponentized(foo, "Inner", true);
        DotName inner2 = DotName.createComponentized(inner, "Inner2", true);
        assertEquals("Foo$Inner", inner.withoutPackagePrefix());
        assertEquals("Foo$Inner$Inner2", inner2.withoutPackagePrefix());
        assertEquals("Inner", inner.local());
        assertEquals("Inner2", inner2.local());
        assertEquals("root.thefoo.Foo$Inner$Inner2", inner2.toString());
        assertEquals("Foo", DotName.createSimple("root.Foo").withoutPackagePrefix());
    }

    @Test
    public void testpackgePrefix() {
        DotName foo = DotName.createComponentized(DotName.createComponentized(null, "root"), "thefoo");
        foo = DotName.createComponentized(foo, "Foo");
        assertEquals("root.thefoo", foo.packagePrefix());
        DotName inner = DotName.createComponentized(foo, "Inner", true);
        DotName inner2 = DotName.createComponentized(inner, "Inner2", true);
        assertEquals("root.thefoo", inner.packagePrefix());
        assertEquals("root.thefoo", inner2.packagePrefix());
        assertEquals("foo.bar.baz", DotName.createSimple("foo.bar.baz.Foo").packagePrefix());
        assertNull(DotName.createSimple("Foo").packagePrefix());
    }


    @Test
    public void testForNaturalComparator() {
        DotsContainer c = new DotsContainer();
        while (c.size() < 200) {
            c.add(createRandomDotName());
        }
        // Throw in a special case:
        c.add(DotName.createSimple("a"));
        // "a" being the simplest case it must come first:
        Assert.assertEquals("a", c.strings.iterator().next());
        Assert.assertEquals("a", c.dotnames.iterator().next().toString());
        // Simple way to verify the comparator of DotName implements
        c.verifyAll();
    }

    @Test
    public void testCollectionsProperties() {
        for (int i=0; i<500; i++) {
            DotName componentised = createRandomComponentised();
            DotName simple = DotName.createSimple(componentised.toString());
            Assert.assertEquals(simple.hashCode(), componentised.hashCode());
            Assert.assertEquals(0, simple.compareTo(componentised));
            Assert.assertEquals(0, componentised.compareTo(simple));
            Assert.assertTrue(simple.equals(componentised));
            Assert.assertTrue(componentised.equals(simple));
            Assert.assertFalse(componentised == simple);
            Assert.assertFalse(simple.equals(null));
            Assert.assertFalse(componentised.equals(null));
            Assert.assertTrue(componentised.toString().equals(simple.toString()));
            Assert.assertEquals(0, componentised.compareTo(simple));
            Assert.assertEquals(0, simple.compareTo(componentised));
        }
    }

    @Test
    public void crossTypeDifferences() {
        DotName simple_perfectmatch = DotName.createSimple("some.case.case.Name");
        DotName comp_1 = DotName.createComponentized(null, "some");
        DotName comp_2 = DotName.createComponentized(comp_1, "case");
        DotName comp_3 = DotName.createComponentized(comp_2, "case");
        DotName compo_perfectmatch = DotName.createComponentized(comp_3, "Name");
        definitelyEquals(compo_perfectmatch, simple_perfectmatch);
        DotName comp_longer = DotName.createComponentized(compo_perfectmatch, "More", true);
        DotName simple_longer = DotName.createSimple("some.case.case.Name$More");
        definitelyNotEquals(comp_longer, compo_perfectmatch);
        definitelyNotEquals(comp_longer, simple_perfectmatch);
        definitelyNotEquals(simple_longer, simple_perfectmatch);
        definitelyEquals(simple_longer, comp_longer);
        definitelyNotEquals(simple_longer, compo_perfectmatch);
        DotName simple_shorter = DotName.createSimple("case.case.Name");
        DotName comp_s_1 = DotName.createComponentized(null, "case");
        DotName comp_s_2 = DotName.createComponentized(comp_s_1, "case");
        DotName comp_shorter = DotName.createComponentized(comp_s_2, "Name");
        definitelyEquals(simple_shorter, comp_shorter);
        definitelyNotEquals(comp_shorter, compo_perfectmatch);
        definitelyNotEquals(comp_shorter, simple_perfectmatch);
        definitelyNotEquals(simple_shorter, compo_perfectmatch);
        definitelyNotEquals(simple_shorter, simple_perfectmatch);
    }

    private void definitelyEquals(DotName a, DotName b) {
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(a.compareTo(b),0);
        Assert.assertEquals(b.compareTo(a),0);
        sameHashCode(a, b);
    }

    private static void sameHashCode(DotName a, DotName b) {
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    private void definitelyNotEquals(DotName a, DotName b) {
        Assert.assertFalse("should not be equals", a.equals(b));
        Assert.assertFalse("should not be equals", b.equals(a));
        Assert.assertFalse(a.compareTo(b) == 0);
        Assert.assertFalse(b.compareTo(a) == 0);
        Assert.assertEquals(b.compareTo(a), -1 * a.compareTo(b));
    }

    @Test
    public void testSpecialCase() {
        // This specific sequence was highlighting a bug
        DotsContainer c = new DotsContainer();
        c.add(DotName.createSimple("c.ae.dceebebea.dbbbee.cddd$cccaa"));
        c.add(DotName.createSimple("c.aebecacddd.ea.eeebcdc.bceaaa$ddabbabccc"));
        c.add(DotName.createSimple("cacbaa.bcbeacdcc"));
        c.add(DotName.createSimple(""));

        c.add(DotName.createComponentized(DotName.createComponentized(null, "c", false), "b", true));
        c.add(DotName.createComponentized(DotName.createComponentized(null, "c", false), "e", true));
        c.add(DotName.createComponentized(DotName.createComponentized(null, "c", false), "ae", false));
        c.verifyAll();
    }


    @Test
    public void testTrailingDelimiter() throws IOException {
        DotName org = DotName.createComponentized(null, "org");
        DotName jboss = DotName.createComponentized(org, "jboss");
        DotName jandex = DotName.createComponentized(jboss, "jandex");
        DotName test = DotName.createComponentized(jandex, "test");
        DotName indexerTestCase = DotName.createComponentized(test, getClass().getSimpleName());
        DotName testName = DotName.createComponentized(indexerTestCase, Test$.class.getSimpleName(), true);

        Index index = Index.of(Test$.class);
        assertEquals(testName, index.getKnownClasses().iterator().next().name());
        assertNotNull(index.getClassByName(DotName.createSimple(Test$.class.getName())));
        assertNotNull(index.getClassByName(testName));
    }

    public static class Test$ {

    }

    private static DotName createRandomDotName() {
        return r.nextBoolean() ? createRandomComponentised() : createRandomSimple();
    }

    private static DotName createRandomSimple() {
        int partsToAppend = r.nextInt(5);
        final StringBuilder sb = new StringBuilder();
        sb.append(someRandomChars());
        while (partsToAppend > 0) {
            sb.append('.');
            sb.append(someRandomChars());
            partsToAppend--;
        }
        // Occasionally make it a nested class:
        if (r.nextBoolean()) {
            sb.append('$');
            sb.append(someRandomChars());
        }
        return DotName.createSimple(sb.toString());
    }

    private static String someRandomChars() {
        final int chars = r.nextInt(10) + 1;
        final char[] buf = new char[chars];
        // Good enough for our purposes, and want to avoid symbols:
        final char[] validOptions = new char[] { 'a', 'b', 'c', 'd', 'e' };
        for (int i = 0; i < chars; i++) {
            buf[i] = validOptions[r.nextInt(validOptions.length)];
        }
        return new String(buf);
    }

    private static DotName createRandomComponentised() {
        int partsToAppend = r.nextInt(5) + 1;
        DotName current = null;
        while (partsToAppend > 0) {
            current = DotName.createComponentized(current, someRandomChars());
            partsToAppend--;
        }
        // Occasionally make it a nested class:
        if (r.nextBoolean()) {
            current = DotName.createComponentized(current, someRandomChars(), true);
        }
        return current;
    }

}
