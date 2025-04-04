package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.StackedIndex;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class IndexNavigationTest {
    // IGrandparent   <----------------------------   CGrandparent
    //      |                                              |
    //      |                                              |
    //   IParent   <---------------------------------   CParent
    //      |                                              |
    //      +--------+                                     +--------+
    //      |        |                                     |        |
    //    IChild  ISibling   <------------------------   CChild  CSibling
    //      |                                              |
    //      +-------------+                                +-------------+
    //      |             |                                |             |
    // IGrandchild1  IGrandchild2   <--------------   CGrandchild1  CGrandchild2

    interface IGrandparent {
    }

    interface IParent extends IGrandparent {
    }

    interface IChild extends IParent {
    }

    interface ISibling extends IParent {
    }

    interface IGrandchild1 extends IChild {
    }

    interface IGrandchild2 extends IChild {
    }

    static class CGrandparent implements IGrandparent {
    }

    static class CParent extends CGrandparent implements IParent {
    }

    static class CChild extends CParent implements IChild {
    }

    static class CSibling extends CParent implements ISibling {
    }

    static class CGrandchild1 extends CChild implements IGrandchild1 {
    }

    static class CGrandchild2 extends CChild implements IGrandchild2 {
    }

    @Test
    public void testSingleIndex() throws IOException {
        Index index = Index.of(IGrandparent.class, IParent.class, IChild.class, ISibling.class, IGrandchild1.class,
                IGrandchild2.class, CGrandparent.class, CParent.class, CChild.class, CSibling.class, CGrandchild1.class,
                CGrandchild2.class);
        testIndex(index);
        testIndex(CompositeIndex.create(index));
        testIndex(StackedIndex.create(index));

        index = IndexingUtil.roundtrip(index);
        testIndex(index);
        testIndex(CompositeIndex.create(index));
        testIndex(StackedIndex.create(index));
    }

    @Test
    public void testCompositeIndex() throws IOException {
        Index index1 = Index.of(IGrandparent.class, IParent.class, IChild.class, IGrandchild1.class, CGrandparent.class,
                CParent.class, CChild.class, CGrandchild1.class);
        Index index2 = Index.of(ISibling.class, IGrandchild2.class, CSibling.class, CGrandchild2.class);

        IndexView index = CompositeIndex.create(index1, index2);
        testIndex(index);

        index = CompositeIndex.create(IndexingUtil.roundtrip(index1), IndexingUtil.roundtrip(index2));
        testIndex(index);
    }

    @Test
    public void testOverlappingCompositeIndex() throws IOException {
        Index index1 = Index.of(IGrandparent.class, IParent.class, IChild.class, IGrandchild1.class, CGrandparent.class,
                CParent.class, CChild.class, CGrandchild1.class);
        Index index2 = Index.of(IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);

        IndexView index = CompositeIndex.create(index1, index2);
        doTestOverlappingCompositeIndex(index);

        index = CompositeIndex.create(IndexingUtil.roundtrip(index1), IndexingUtil.roundtrip(index2));
        doTestOverlappingCompositeIndex(index);
    }

    private void doTestOverlappingCompositeIndex(IndexView index) {
        assertCollection(index.getKnownDirectSubclasses(Object.class), IGrandparent.class, IParent.class, IChild.class,
                IGrandchild1.class, CGrandparent.class, IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getKnownDirectSubclasses(CParent.class), CChild.class, CChild.class, CSibling.class);
        assertCollection(index.getKnownDirectSubclasses(CChild.class), CGrandchild1.class, CGrandchild1.class,
                CGrandchild2.class);

        assertCollection(index.getAllKnownSubclasses(Object.class), IGrandparent.class, IParent.class, IChild.class,
                IGrandchild1.class, CGrandparent.class, CParent.class, CChild.class, CGrandchild1.class, IChild.class,
                ISibling.class, IGrandchild1.class, IGrandchild2.class, CChild.class, CSibling.class, CGrandchild1.class,
                CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CGrandparent.class), CParent.class, CChild.class, CGrandchild1.class,
                CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CParent.class), CChild.class, CGrandchild1.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CChild.class), CGrandchild1.class, CGrandchild1.class, CGrandchild2.class);

        assertCollection(index.getKnownDirectSubinterfaces(IGrandparent.class), IParent.class);
        assertCollection(index.getKnownDirectSubinterfaces(IParent.class), IChild.class, IChild.class, ISibling.class);
        assertCollection(index.getKnownDirectSubinterfaces(IChild.class), IGrandchild1.class, IGrandchild1.class,
                IGrandchild2.class);

        assertCollection(index.getAllKnownSubinterfaces(IGrandparent.class), IParent.class, IChild.class, IGrandchild1.class,
                IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getAllKnownSubinterfaces(IParent.class), IChild.class, IGrandchild1.class, IChild.class,
                ISibling.class, IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getAllKnownSubinterfaces(IChild.class), IGrandchild1.class, IGrandchild1.class,
                IGrandchild2.class);

        assertCollection(index.getKnownDirectImplementations(IGrandparent.class), CGrandparent.class);
        assertCollection(index.getKnownDirectImplementations(IParent.class), CParent.class);
        assertCollection(index.getKnownDirectImplementations(IChild.class), CChild.class, CChild.class);
        assertCollection(index.getKnownDirectImplementations(ISibling.class), CSibling.class);
        assertCollection(index.getKnownDirectImplementations(IGrandchild1.class), CGrandchild1.class, CGrandchild1.class);
        assertCollection(index.getKnownDirectImplementations(IGrandchild2.class), CGrandchild2.class);

        assertCollection(index.getAllKnownImplementations(IGrandparent.class), CGrandparent.class, CParent.class, CChild.class,
                CGrandchild1.class, CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(IParent.class), CParent.class, CChild.class, CGrandchild1.class,
                CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
        // doesn't behave as expected, but the behavior is actually not defined
        //assertCollection(index.getAllKnownImplementations(IChild.class), CChild.class, CGrandchild1.class, CChild.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(ISibling.class), CSibling.class);
        assertCollection(index.getAllKnownImplementations(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getAllKnownImplementations(IGrandchild2.class), CGrandchild2.class);

        assertCollection(index.getKnownDirectImplementors(IGrandparent.class), CGrandparent.class, IParent.class);
        assertCollection(index.getKnownDirectImplementors(IParent.class), IChild.class, CParent.class, IChild.class,
                ISibling.class);
        assertCollection(index.getKnownDirectImplementors(IChild.class), IGrandchild1.class, CChild.class, IGrandchild1.class,
                IGrandchild2.class, CChild.class);
        assertCollection(index.getKnownDirectImplementors(ISibling.class), CSibling.class);
        assertCollection(index.getKnownDirectImplementors(IGrandchild1.class), CGrandchild1.class, CGrandchild1.class);
        assertCollection(index.getKnownDirectImplementors(IGrandchild2.class), CGrandchild2.class);

        assertCollection(index.getAllKnownImplementors(IGrandparent.class), CGrandparent.class, CParent.class, CChild.class,
                CGrandchild1.class, CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(IParent.class), CParent.class, CChild.class, CGrandchild1.class,
                CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
        // doesn't behave as expected, but the behavior is actually not defined
        //assertCollection(index.getAllKnownImplementors(IChild.class), CChild.class, CGrandchild1.class, CChild.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(ISibling.class), CSibling.class);
        assertCollection(index.getAllKnownImplementors(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getAllKnownImplementors(IGrandchild2.class), CGrandchild2.class);

        // doesn't contain duplicates, though it probably should (again, the behavior is not defined)
        assertCollection(index.getClassesInPackage(IndexNavigationTest.class.getPackage().getName()), IGrandparent.class,
                IParent.class, IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class, CGrandparent.class,
                CParent.class, CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
    }

    @Test
    public void testStackedIndex() throws IOException {
        Index index1 = Index.of(IGrandparent.class, IParent.class, IChild.class, IGrandchild1.class, CGrandparent.class,
                CParent.class, CChild.class, CGrandchild1.class);
        Index index2 = Index.of(ISibling.class, IGrandchild2.class, CSibling.class, CGrandchild2.class);

        IndexView index = StackedIndex.create(index1, index2);
        testIndex(index);

        index = StackedIndex.create(IndexingUtil.roundtrip(index1), IndexingUtil.roundtrip(index2));
        testIndex(index);
    }

    @Test
    public void testOverlappingStackedIndex() throws IOException {
        Index index1 = Index.of(IGrandparent.class, IParent.class, IChild.class, IGrandchild1.class, CGrandparent.class,
                CParent.class, CChild.class, CGrandchild1.class);
        Index index2 = Index.of(IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);

        IndexView index = StackedIndex.create(index1, index2);
        testIndex(index);

        index = StackedIndex.create(IndexingUtil.roundtrip(index1), IndexingUtil.roundtrip(index2));
        testIndex(index);
    }

    private void testIndex(IndexView index) {
        testDirectSubclasses(index);
        testAllSubclasses(index);
        testDirectSubinterfaces(index);
        testAllSubinterfaces(index);
        testDirectImplementations(index);
        testAllImplementations(index);
        testDirectImplementors(index);
        testAllImplementors(index);
        testClassesInPackage(index);
    }

    private void testDirectSubclasses(IndexView index) {
        assertCollection(index.getKnownDirectSubclasses(Object.class), IGrandparent.class, IParent.class, IChild.class,
                ISibling.class, IGrandchild1.class, IGrandchild2.class, CGrandparent.class);
        assertCollection(index.getKnownDirectSubclasses(IGrandparent.class));
        assertCollection(index.getKnownDirectSubclasses(IParent.class));
        assertCollection(index.getKnownDirectSubclasses(IChild.class));
        assertCollection(index.getKnownDirectSubclasses(ISibling.class));
        assertCollection(index.getKnownDirectSubclasses(IGrandchild1.class));
        assertCollection(index.getKnownDirectSubclasses(IGrandchild2.class));
        assertCollection(index.getKnownDirectSubclasses(CGrandparent.class), CParent.class);
        assertCollection(index.getKnownDirectSubclasses(CParent.class), CChild.class, CSibling.class);
        assertCollection(index.getKnownDirectSubclasses(CChild.class), CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getKnownDirectSubclasses(CSibling.class));
        assertCollection(index.getKnownDirectSubclasses(CGrandchild1.class));
        assertCollection(index.getKnownDirectSubclasses(CGrandchild2.class));
    }

    private void testAllSubclasses(IndexView index) {
        assertCollection(index.getAllKnownSubclasses(Object.class), IGrandparent.class, IParent.class, IChild.class,
                ISibling.class, IGrandchild1.class, IGrandchild2.class, CGrandparent.class, CParent.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(IGrandparent.class));
        assertCollection(index.getAllKnownSubclasses(IParent.class));
        assertCollection(index.getAllKnownSubclasses(IChild.class));
        assertCollection(index.getAllKnownSubclasses(ISibling.class));
        assertCollection(index.getAllKnownSubclasses(IGrandchild1.class));
        assertCollection(index.getAllKnownSubclasses(IGrandchild2.class));
        assertCollection(index.getAllKnownSubclasses(CGrandparent.class), CParent.class, CChild.class, CSibling.class,
                CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CParent.class), CChild.class, CSibling.class, CGrandchild1.class,
                CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CChild.class), CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownSubclasses(CSibling.class));
        assertCollection(index.getAllKnownSubclasses(CGrandchild1.class));
        assertCollection(index.getAllKnownSubclasses(CGrandchild2.class));
    }

    private void testDirectSubinterfaces(IndexView index) {
        assertCollection(index.getKnownDirectSubinterfaces(Object.class));
        assertCollection(index.getKnownDirectSubinterfaces(IGrandparent.class), IParent.class);
        assertCollection(index.getKnownDirectSubinterfaces(IParent.class), IChild.class, ISibling.class);
        assertCollection(index.getKnownDirectSubinterfaces(IChild.class), IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getKnownDirectSubinterfaces(ISibling.class));
        assertCollection(index.getKnownDirectSubinterfaces(IGrandchild1.class));
        assertCollection(index.getKnownDirectSubinterfaces(IGrandchild2.class));
        assertCollection(index.getKnownDirectSubinterfaces(CGrandparent.class));
        assertCollection(index.getKnownDirectSubinterfaces(CParent.class));
        assertCollection(index.getKnownDirectSubinterfaces(CChild.class));
        assertCollection(index.getKnownDirectSubinterfaces(CSibling.class));
        assertCollection(index.getKnownDirectSubinterfaces(CGrandchild1.class));
        assertCollection(index.getKnownDirectSubinterfaces(CGrandchild2.class));
    }

    private void testAllSubinterfaces(IndexView index) {
        assertCollection(index.getAllKnownSubinterfaces(Object.class));
        assertCollection(index.getAllKnownSubinterfaces(IGrandparent.class), IParent.class, IChild.class, ISibling.class,
                IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getAllKnownSubinterfaces(IParent.class), IChild.class, ISibling.class, IGrandchild1.class,
                IGrandchild2.class);
        assertCollection(index.getAllKnownSubinterfaces(IChild.class), IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getAllKnownSubinterfaces(ISibling.class));
        assertCollection(index.getAllKnownSubinterfaces(IGrandchild1.class));
        assertCollection(index.getAllKnownSubinterfaces(IGrandchild2.class));
        assertCollection(index.getAllKnownSubinterfaces(CGrandparent.class));
        assertCollection(index.getAllKnownSubinterfaces(CParent.class));
        assertCollection(index.getAllKnownSubinterfaces(CChild.class));
        assertCollection(index.getAllKnownSubinterfaces(CSibling.class));
        assertCollection(index.getAllKnownSubinterfaces(CGrandchild1.class));
        assertCollection(index.getAllKnownSubinterfaces(CGrandchild2.class));
    }

    private void testDirectImplementations(IndexView index) {
        assertCollection(index.getKnownDirectImplementations(Object.class));
        assertCollection(index.getKnownDirectImplementations(IGrandparent.class), CGrandparent.class);
        assertCollection(index.getKnownDirectImplementations(IParent.class), CParent.class);
        assertCollection(index.getKnownDirectImplementations(IChild.class), CChild.class);
        assertCollection(index.getKnownDirectImplementations(ISibling.class), CSibling.class);
        assertCollection(index.getKnownDirectImplementations(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getKnownDirectImplementations(IGrandchild2.class), CGrandchild2.class);
        assertCollection(index.getKnownDirectImplementations(CGrandparent.class));
        assertCollection(index.getKnownDirectImplementations(CParent.class));
        assertCollection(index.getKnownDirectImplementations(CChild.class));
        assertCollection(index.getKnownDirectImplementations(CSibling.class));
        assertCollection(index.getKnownDirectImplementations(CGrandchild1.class));
        assertCollection(index.getKnownDirectImplementations(CGrandchild2.class));
    }

    private void testAllImplementations(IndexView index) {
        assertCollection(index.getAllKnownImplementations(Object.class));
        assertCollection(index.getAllKnownImplementations(IGrandparent.class), CGrandparent.class, CParent.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(IParent.class), CParent.class, CChild.class, CSibling.class,
                CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(IChild.class), CChild.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(ISibling.class), CSibling.class);
        assertCollection(index.getAllKnownImplementations(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getAllKnownImplementations(IGrandchild2.class), CGrandchild2.class);
        assertCollection(index.getAllKnownImplementations(CGrandparent.class));
        assertCollection(index.getAllKnownImplementations(CParent.class));
        assertCollection(index.getAllKnownImplementations(CChild.class));
        assertCollection(index.getAllKnownImplementations(CSibling.class));
        assertCollection(index.getAllKnownImplementations(CGrandchild1.class));
        assertCollection(index.getAllKnownImplementations(CGrandchild2.class));
    }

    private void testDirectImplementors(IndexView index) {
        assertCollection(index.getKnownDirectImplementors(Object.class));
        assertCollection(index.getKnownDirectImplementors(IGrandparent.class), CGrandparent.class, IParent.class);
        assertCollection(index.getKnownDirectImplementors(IParent.class), CParent.class, IChild.class, ISibling.class);
        assertCollection(index.getKnownDirectImplementors(IChild.class), CChild.class, IGrandchild1.class, IGrandchild2.class);
        assertCollection(index.getKnownDirectImplementors(ISibling.class), CSibling.class);
        assertCollection(index.getKnownDirectImplementors(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getKnownDirectImplementors(IGrandchild2.class), CGrandchild2.class);
        assertCollection(index.getKnownDirectImplementors(CGrandparent.class));
        assertCollection(index.getKnownDirectImplementors(CParent.class));
        assertCollection(index.getKnownDirectImplementors(CChild.class));
        assertCollection(index.getKnownDirectImplementors(CSibling.class));
        assertCollection(index.getKnownDirectImplementors(CGrandchild1.class));
        assertCollection(index.getKnownDirectImplementors(CGrandchild2.class));
    }

    private void testAllImplementors(IndexView index) {
        assertCollection(index.getAllKnownImplementors(Object.class));
        assertCollection(index.getAllKnownImplementors(IGrandparent.class), CGrandparent.class, CParent.class, CChild.class,
                CSibling.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(IParent.class), CParent.class, CChild.class, CSibling.class,
                CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(IChild.class), CChild.class, CGrandchild1.class, CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(ISibling.class), CSibling.class);
        assertCollection(index.getAllKnownImplementors(IGrandchild1.class), CGrandchild1.class);
        assertCollection(index.getAllKnownImplementors(IGrandchild2.class), CGrandchild2.class);
        assertCollection(index.getAllKnownImplementors(CGrandparent.class));
        assertCollection(index.getAllKnownImplementors(CParent.class));
        assertCollection(index.getAllKnownImplementors(CChild.class));
        assertCollection(index.getAllKnownImplementors(CSibling.class));
        assertCollection(index.getAllKnownImplementors(CGrandchild1.class));
        assertCollection(index.getAllKnownImplementors(CGrandchild2.class));
    }

    private void testClassesInPackage(IndexView index) {
        assertCollection(index.getClassesInPackage(IndexNavigationTest.class.getPackage().getName()), IGrandparent.class,
                IParent.class, IChild.class, ISibling.class, IGrandchild1.class, IGrandchild2.class, CGrandparent.class,
                CParent.class, CChild.class, CSibling.class, CGrandchild1.class, CGrandchild2.class);
    }

    private static void assertCollection(Collection<ClassInfo> collection, Class<?>... expectation) {
        assertEquals(expectation.length, collection.size(),
                "Expected " + expectation.length + " items in " + collection);
        for (Class<?> clazz : expectation) {
            DotName name = DotName.createSimple(clazz);
            assertTrue(collection.stream().anyMatch(it -> name.equals(it.name())),
                    "Expected " + name + " in " + collection);
        }
    }
}
