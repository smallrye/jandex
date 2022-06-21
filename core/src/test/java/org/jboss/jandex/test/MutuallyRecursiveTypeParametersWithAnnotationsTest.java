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

public class MutuallyRecursiveTypeParametersWithAnnotationsTest {
    static class IndirectRecursion<@MyAnnotation("class:var:a") A extends List<@MyAnnotation("a:ref:b") B>, @MyAnnotation("class:var:b") B extends List<@MyAnnotation("b:var:a") A>> {
        @MyAnnotation("field a:var:a")
        A a;

        @MyAnnotation("field b:var:b")
        B b;
    }

    static class Graph<@MyAnnotation("class Graph:var:g") G extends Graph<@MyAnnotation("g:ref:g") G, @MyAnnotation("g:ref:e") E, @MyAnnotation("g:ref:v") V>, @MyAnnotation("class Graph:var:e") E extends Edge<@MyAnnotation("e:var:g") G, @MyAnnotation("e:ref:e") E, @MyAnnotation("e:ref:v") V>, @MyAnnotation("class Graph:var:v") V extends Vertex<@MyAnnotation("v:var:g") G, @MyAnnotation("v:var:e") E, @MyAnnotation("v:ref:v") V>> {
        Set<@MyAnnotation("field Graph.edges:var:e") E> edges;

        Set<@MyAnnotation("field Graph.vertices:var:v") V> vertices;

        Set<@MyAnnotation("method Graph.getEdges:var:e") E> getEdges() {
            return edges;
        }

        Set<@MyAnnotation("method Graph.getVertices:var:v") V> getVertices() {
            return vertices;
        }
    }

    static class Edge<@MyAnnotation("class Edge:var:g") G extends Graph<@MyAnnotation("g:ref:g") G, @MyAnnotation("g:ref:e") E, @MyAnnotation("g:ref:v") V>, @MyAnnotation("class Edge:var:e") E extends Edge<@MyAnnotation("e:var:g") G, @MyAnnotation("e:ref:e") E, @MyAnnotation("e:ref:v") V>, @MyAnnotation("class Edge:var:v") V extends Vertex<@MyAnnotation("v:var:g") G, @MyAnnotation("v:var:e") E, @MyAnnotation("v:ref:v") V>> {
        @MyAnnotation("field Edge.graph:var:g")
        G graph;

        @MyAnnotation("field Edge.source:var:v")
        V source;

        @MyAnnotation("field Edge.target:var:v")
        V target;

        @MyAnnotation("method Edge.getGraph:var:g")
        G getGraph() {
            return graph;
        }

        @MyAnnotation("method Edge.getSource:var:v")
        V getSource() {
            return source;
        }

        @MyAnnotation("method Edge.getTarget:var:v")
        V getTarget() {
            return target;
        }
    }

    static class Vertex<@MyAnnotation("class Vertex:var:g") G extends Graph<@MyAnnotation("g:ref:g") G, @MyAnnotation("g:ref:e") E, @MyAnnotation("g:ref:v") V>, @MyAnnotation("class Vertex:var:e") E extends Edge<@MyAnnotation("e:var:g") G, @MyAnnotation("e:ref:e") E, @MyAnnotation("e:ref:v") V>, @MyAnnotation("class Vertex:var:v") V extends Vertex<@MyAnnotation("v:var:g") G, @MyAnnotation("v:var:e") E, @MyAnnotation("v:ref:v") V>> {
        @MyAnnotation("field Vertex.graph:var:g")
        G graph;

        Set<@MyAnnotation("field Vertex.incoming:var:e") E> incoming;

        Set<@MyAnnotation("field Vertex.outgoing:var:e") E> outgoing;

        @MyAnnotation("method Vertex.getGraph:var:g")
        G getGraph() {
            return graph;
        }

        Set<@MyAnnotation("method Vertex.getIncoming:var:e") E> getIncoming() {
            return incoming;
        }

        Set<@MyAnnotation("method Vertex.getOutgoing:var:e") E> getOutgoing() {
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
        assertA(indirectlyRecursive.typeParameters().get(0), "class");
        assertB(indirectlyRecursive.typeParameters().get(1), "class");
        assertA(indirectlyRecursive.field("a").type(), "field a");
        assertB(indirectlyRecursive.field("b").type(), "field b");
    }

    private void assertA(Type a, String origin) {
        assertTrue(a.kind() == Type.Kind.TYPE_VARIABLE || a.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (a.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable aVar = a.asTypeVariable();
            assertEquals("A", aVar.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), aVar.name());
            assertTrue(aVar.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":var:a", aVar.annotation(MyAnnotation.DOT_NAME).value().asString());
            assertEquals(1, aVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, aVar.bounds().get(0).kind());
            ParameterizedType aBound = aVar.bounds().get(0).asParameterizedType();
            assertEquals(1, aBound.arguments().size());
            assertB(aBound.arguments().get(0), "a");
        } else {
            TypeVariableReference aRef = a.asTypeVariableReference();
            assertEquals("A", aRef.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), aRef.name());
            assertTrue(aRef.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":ref:a", aRef.annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    private void assertB(Type b, String origin) {
        assertTrue(b.kind() == Type.Kind.TYPE_VARIABLE || b.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (b.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable bVar = b.asTypeVariable();
            assertEquals("B", bVar.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), bVar.name());
            assertTrue(bVar.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":var:b", bVar.annotation(MyAnnotation.DOT_NAME).value().asString());
            assertEquals(1, bVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, bVar.bounds().get(0).kind());
            ParameterizedType bBound = bVar.bounds().get(0).asParameterizedType();
            assertEquals(1, bBound.arguments().size());
            assertA(bBound.arguments().get(0), "b");
        } else {
            TypeVariableReference bRef = b.asTypeVariableReference();
            assertEquals("B", bRef.identifier());
            assertEquals(DotName.createSimple(List.class.getName()), bRef.name());
            assertTrue(bRef.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":ref:b", bRef.annotation(MyAnnotation.DOT_NAME).value().asString());
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
        assertClassInTypeFamily(graph, "class Graph");
        assertE(graph.field("edges").type().asParameterizedType().arguments().get(0), "field Graph.edges");
        assertV(graph.field("vertices").type().asParameterizedType().arguments().get(0), "field Graph.vertices");
        assertE(graph.method("getEdges").returnType().asParameterizedType().arguments().get(0), "method Graph.getEdges");
        assertV(graph.method("getVertices").returnType().asParameterizedType().arguments().get(0), "method Graph.getVertices");

        ClassInfo edge = index.getClassByName(Edge.class);
        assertClassInTypeFamily(edge, "class Edge");
        assertG(edge.field("graph").type(), "field Edge.graph");
        assertV(edge.field("source").type(), "field Edge.source");
        assertV(edge.field("target").type(), "field Edge.target");
        assertG(edge.method("getGraph").returnType(), "method Edge.getGraph");
        assertV(edge.method("getSource").returnType(), "method Edge.getSource");
        assertV(edge.method("getTarget").returnType(), "method Edge.getTarget");

        ClassInfo vertex = index.getClassByName(Vertex.class);
        assertClassInTypeFamily(vertex, "class Vertex");
        assertG(vertex.field("graph").type(), "field Vertex.graph");
        assertE(vertex.field("incoming").type().asParameterizedType().arguments().get(0), "field Vertex.incoming");
        assertE(vertex.field("outgoing").type().asParameterizedType().arguments().get(0), "field Vertex.outgoing");
        assertG(vertex.method("getGraph").returnType(), "method Vertex.getGraph");
        assertE(vertex.method("getIncoming").returnType().asParameterizedType().arguments().get(0),
                "method Vertex.getIncoming");
        assertE(vertex.method("getOutgoing").returnType().asParameterizedType().arguments().get(0),
                "method Vertex.getOutgoing");
    }

    private void assertClassInTypeFamily(ClassInfo clazz, String origin) {
        List<TypeVariable> typeParameters = clazz.typeParameters();
        assertEquals(3, typeParameters.size());
        assertG(typeParameters.get(0), origin);
        assertE(typeParameters.get(1), origin);
        assertV(typeParameters.get(2), origin);
    }

    private void assertG(Type g, String origin) {
        assertTrue(g.kind() == Type.Kind.TYPE_VARIABLE || g.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (g.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable gVar = g.asTypeVariable();
            assertEquals("G", gVar.identifier());
            assertEquals(DotName.createSimple(Graph.class.getName()), gVar.name());
            assertTrue(gVar.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":var:g", gVar.annotation(MyAnnotation.DOT_NAME).value().asString());
            assertEquals(1, gVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, gVar.bounds().get(0).kind());
            ParameterizedType gBound = gVar.bounds().get(0).asParameterizedType();
            assertEquals(3, gBound.arguments().size());
            assertG(gBound.arguments().get(0), "g");
            assertSame(g, gBound.arguments().get(0).asTypeVariableReference().follow());
            assertE(gBound.arguments().get(1), "g");
            assertV(gBound.arguments().get(2), "g");
        } else {
            TypeVariableReference gRef = g.asTypeVariableReference();
            assertEquals("G", gRef.identifier());
            assertEquals(DotName.createSimple(Graph.class.getName()), gRef.name());
            assertTrue(gRef.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":ref:g", gRef.annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    private void assertE(Type e, String origin) {
        assertTrue(e.kind() == Type.Kind.TYPE_VARIABLE || e.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (e.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable eVar = e.asTypeVariable();
            assertEquals("E", eVar.identifier());
            assertEquals(DotName.createSimple(Edge.class.getName()), eVar.name());
            assertTrue(eVar.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":var:e", eVar.annotation(MyAnnotation.DOT_NAME).value().asString());
            assertEquals(1, eVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, eVar.bounds().get(0).kind());
            ParameterizedType eBound = eVar.bounds().get(0).asParameterizedType();
            assertEquals(3, eBound.arguments().size());
            assertG(eBound.arguments().get(0), "e");
            assertE(eBound.arguments().get(1), "e");
            assertSame(e, eBound.arguments().get(1).asTypeVariableReference().follow());
            assertV(eBound.arguments().get(2), "e");
        } else {
            TypeVariableReference eRef = e.asTypeVariableReference();
            assertEquals("E", eRef.identifier());
            assertEquals(DotName.createSimple(Edge.class.getName()), eRef.name());
            assertTrue(eRef.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":ref:e", eRef.annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    private void assertV(Type v, String origin) {
        assertTrue(v.kind() == Type.Kind.TYPE_VARIABLE || v.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE);
        if (v.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable vVar = v.asTypeVariable();
            assertEquals("V", vVar.identifier());
            assertEquals(DotName.createSimple(Vertex.class.getName()), vVar.name());
            assertEquals(origin + ":var:v", vVar.annotation(MyAnnotation.DOT_NAME).value().asString());
            assertEquals(1, vVar.bounds().size());
            assertEquals(1, vVar.bounds().size());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, vVar.bounds().get(0).kind());
            ParameterizedType vBound = vVar.bounds().get(0).asParameterizedType();
            assertEquals(3, vBound.arguments().size());
            assertG(vBound.arguments().get(0), "v");
            assertE(vBound.arguments().get(1), "v");
            assertV(vBound.arguments().get(2), "v");
            assertSame(v, vBound.arguments().get(2).asTypeVariableReference().follow());
        } else {
            TypeVariableReference vRef = v.asTypeVariableReference();
            assertEquals("V", vRef.identifier());
            assertEquals(DotName.createSimple(Vertex.class.getName()), vRef.name());
            assertTrue(vRef.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals(origin + ":ref:v", vRef.annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }
}
