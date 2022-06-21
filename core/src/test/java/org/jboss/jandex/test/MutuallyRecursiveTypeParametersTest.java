package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.TypeVariableReference;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class MutuallyRecursiveTypeParametersTest {
    static class IndirectRecursion<A extends List<B>, B extends List<A>> {
        A a;
        B b;
    }

    static class Graph<G extends Graph<G, E, V>, E extends Edge<G, E, V>, V extends Vertex<G, E, V>> {
        Set<E> edges;
        Set<V> vertices;

        Set<E> getEdges() {
            return edges;
        }

        Set<V> getVertices() {
            return vertices;
        }
    }

    static class Edge<G extends Graph<G, E, V>, E extends Edge<G, E, V>, V extends Vertex<G, E, V>> {
        G graph;
        V source;
        V target;

        G getGraph() {
            return graph;
        }

        V getSource() {
            return source;
        }

        V getTarget() {
            return target;
        }
    }

    static class Vertex<G extends Graph<G, E, V>, E extends Edge<G, E, V>, V extends Vertex<G, E, V>> {
        G graph;
        Set<E> incoming;
        Set<E> outgoing;

        G getGraph() {
            return graph;
        }

        Set<E> getIncoming() {
            return incoming;
        }

        Set<E> getOutgoing() {
            return outgoing;
        }
    }

    @Test
    public void indirectRecursion() throws IOException {
        Index index = Index.of(IndirectRecursion.class);
        indirectRecursion(index);
        indirectRecursion(IndexingUtil.roundtrip(index));
    }

    private void indirectRecursion(Index index) {
        ClassInfo indirectlyRecursive = index.getClassByName(IndirectRecursion.class);
        assertEquals(2, indirectlyRecursive.typeParameters().size());
        assertA(indirectlyRecursive.typeParameters().get(0));
        assertB(indirectlyRecursive.typeParameters().get(1));
        assertA(indirectlyRecursive.field("a").type());
        assertB(indirectlyRecursive.field("b").type());
    }

    private void assertA(Type a) {
        assertTrue(a.kind() == Type.Kind.TYPE_VARIABLE || a.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (a.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable aVar = a.asTypeVariable();
            assertEquals("A", aVar.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), aVar.name());
            assertEquals(1, aVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, aVar.bounds().get(0).kind());
            ParameterizedType aBound = aVar.bounds().get(0).asParameterizedType();
            assertEquals(1, aBound.arguments().size());
            assertB(aBound.arguments().get(0));
            //assertSame(a, aBound.arguments().get(0).asTypeVariableReference().follow());
        } else {
            TypeVariableReference aRef = a.asTypeVariableReference();
            assertEquals("A", aRef.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), aRef.name());
        }
    }

    private void assertB(Type b) {
        assertTrue(b.kind() == Type.Kind.TYPE_VARIABLE || b.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (b.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable bVar = b.asTypeVariable();
            assertEquals("B", bVar.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), bVar.name());
            assertEquals(1, bVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, bVar.bounds().get(0).kind());
            ParameterizedType bBound = bVar.bounds().get(0).asParameterizedType();
            assertEquals(1, bBound.arguments().size());
            assertA(bBound.arguments().get(0));
            //assertSame(b, bBound.arguments().get(0).asTypeVariableReference().follow());
        } else {
            TypeVariableReference bRef = b.asTypeVariableReference();
            assertEquals("B", bRef.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), bRef.name());
        }
    }

    @Test
    public void typeFamily() throws IOException {
        Index index = Index.of(Graph.class, Edge.class, Vertex.class);
        typeFamily(index);
        typeFamily(IndexingUtil.roundtrip(index));
    }

    private void typeFamily(Index index) {
        ClassInfo graph = index.getClassByName(Graph.class);
        assertClassInTypeFamily(graph);
        assertE(graph.field("edges").type().asParameterizedType().arguments().get(0));
        assertV(graph.field("vertices").type().asParameterizedType().arguments().get(0));
        assertE(graph.method("getEdges").returnType().asParameterizedType().arguments().get(0));
        assertV(graph.method("getVertices").returnType().asParameterizedType().arguments().get(0));

        ClassInfo edge = index.getClassByName(Edge.class);
        assertClassInTypeFamily(edge);
        assertG(edge.field("graph").type());
        assertV(edge.field("source").type());
        assertV(edge.field("target").type());
        assertG(edge.method("getGraph").returnType());
        assertV(edge.method("getSource").returnType());
        assertV(edge.method("getTarget").returnType());

        ClassInfo vertex = index.getClassByName(Vertex.class);
        assertClassInTypeFamily(vertex);
        assertG(vertex.field("graph").type());
        assertE(vertex.field("incoming").type().asParameterizedType().arguments().get(0));
        assertE(vertex.field("outgoing").type().asParameterizedType().arguments().get(0));
        assertG(vertex.method("getGraph").returnType());
        assertE(vertex.method("getIncoming").returnType().asParameterizedType().arguments().get(0));
        assertE(vertex.method("getOutgoing").returnType().asParameterizedType().arguments().get(0));
    }

    private void assertClassInTypeFamily(ClassInfo clazz) {
        List<TypeVariable> typeParameters = clazz.typeParameters();
        assertEquals(3, typeParameters.size());
        assertG(typeParameters.get(0));
        assertE(typeParameters.get(1));
        assertV(typeParameters.get(2));
    }

    private void assertG(Type g) {
        assertTrue(g.kind() == Type.Kind.TYPE_VARIABLE || g.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (g.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable gVar = g.asTypeVariable();
            assertEquals("G", gVar.identifier());
            assertEquals(DotName.createSimple(Graph.class.getName()), gVar.name());
            assertEquals(1, gVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, gVar.bounds().get(0).kind());
            ParameterizedType gBound = gVar.bounds().get(0).asParameterizedType();
            assertEquals(3, gBound.arguments().size());
            assertG(gBound.arguments().get(0));
            assertSame(g, gBound.arguments().get(0).asTypeVariableReference().follow());
            assertE(gBound.arguments().get(1));
            assertV(gBound.arguments().get(2));
        } else {
            TypeVariableReference gRef = g.asTypeVariableReference();
            assertEquals("G", gRef.identifier());
            assertEquals(DotName.createSimple(Graph.class.getName()), gRef.name());
        }
    }

    private void assertE(Type e) {
        assertTrue(e.kind() == Type.Kind.TYPE_VARIABLE || e.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (e.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable eVar = e.asTypeVariable();
            assertEquals("E", eVar.identifier());
            assertEquals(DotName.createSimple(Edge.class.getName()), eVar.name());
            assertEquals(1, eVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, eVar.bounds().get(0).kind());
            ParameterizedType eBound = eVar.bounds().get(0).asParameterizedType();
            assertEquals(3, eBound.arguments().size());
            assertG(eBound.arguments().get(0));
            assertE(eBound.arguments().get(1));
            assertSame(e, eBound.arguments().get(1).asTypeVariableReference().follow());
            assertV(eBound.arguments().get(2));
        } else {
            TypeVariableReference eRef = e.asTypeVariableReference();
            assertEquals("E", eRef.identifier());
            assertEquals(DotName.createSimple(Edge.class.getName()), eRef.name());
        }
    }

    private void assertV(Type v) {
        assertTrue(v.kind() == Type.Kind.TYPE_VARIABLE || v.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (v.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable vVar = v.asTypeVariable();
            assertEquals("V", vVar.identifier());
            assertEquals(DotName.createSimple(Vertex.class.getName()), vVar.name());
            assertEquals(1, vVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, vVar.bounds().get(0).kind());
            ParameterizedType vBound = vVar.bounds().get(0).asParameterizedType();
            assertEquals(3, vBound.arguments().size());
            assertG(vBound.arguments().get(0));
            assertE(vBound.arguments().get(1));
            assertV(vBound.arguments().get(2));
            assertSame(v, vBound.arguments().get(2).asTypeVariableReference().follow());
        } else {
            TypeVariableReference vRef = v.asTypeVariableReference();
            assertEquals("V", vRef.identifier());
            assertEquals(DotName.createSimple(Vertex.class.getName()), vRef.name());
        }
    }
}
