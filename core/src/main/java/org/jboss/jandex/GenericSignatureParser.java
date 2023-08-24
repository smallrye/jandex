/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple recursive decent generic signature parser.
 *
 * @author Jason T. Greene
 */
class GenericSignatureParser {
    // @formatter:off
    /*
     * Complete Grammar (VM Spec v8)
     *
     * JavaTypeSignature:
     *   ReferenceTypeSignature
     *   BaseType
     *
     * BaseType:
     *   B C D F I J S Z
     *
     * VoidDescriptor:
     *   V
     *
     * ReferenceTypeSignature:
     *   ClassTypeSignature
     *   TypeVariableSignature
     *   ArrayTypeSignature
     *
     * ClassTypeSignature:
     *   L [PackageSpecifier] SimpleClassTypeSignature {ClassTypeSignatureSuffix} ;
     *
     * PackageSpecifier:
     *   Identifier / {PackageSpecifier}
     *
     * SimpleClassTypeSignature:
     *   Identifier [TypeArguments]
     *
     * TypeArguments:
     *   < TypeArgument {TypeArgument} >
     *
     * TypeArgument:
     *   [WildcardIndicator] ReferenceTypeSignature
     *   *
     *
     * WildcardIndicator:
     *   +
     *   -
     *
     * ClassTypeSignatureSuffix:
     *   . SimpleClassTypeSignature
     *
     * TypeVariableSignature:
     *   T Identifier ;
     *
     * ArrayTypeSignature:
     *   [ JavaTypeSignature
     *
     * ClassSignature:
     *   [TypeParameters] SuperclassSignature {SuperinterfaceSignature}
     *
     * TypeParameters:
     *   < TypeParameter {TypeParameter} >
     *
     * TypeParameter:
     *   Identifier ClassBound {InterfaceBound}
     *
     * ClassBound:
     *   : [ReferenceTypeSignature]
     *
     * InterfaceBound:
     *   : ReferenceTypeSignature
     *
     * SuperclassSignature:
     *   ClassTypeSignature
     *
     * SuperinterfaceSignature:
     *   ClassTypeSignature
     *
     * MethodSignature:
     *   [TypeParameters] ( {JavaTypeSignature} ) Result {ThrowsSignature}
     *
     * Result:
     *   JavaTypeSignature
     *   VoidDescriptor
     *
     * ThrowsSignature:
     *   ^ ClassTypeSignature
     *   ^ TypeVariableSignature
     *
     * FieldSignature:
     *   ReferenceTypeSignature
     *
     */
    // @formatter:on
    private static final WildcardType UNBOUNDED_WILDCARD = new WildcardType(null, true);
    private String signature;
    private int pos;
    private NameTable names;
    private Map<String, TypeVariable> typeParameters;
    private Map<String, TypeVariable> elementTypeParameters = new HashMap<String, TypeVariable>();
    private Map<String, TypeVariable> classTypeParameters = new HashMap<String, TypeVariable>();
    private DotName currentClassName;

    // used to track enclosing type variables when determining if a type is recursive
    // and when patching type variable references; present here to avoid allocating
    // a new stack for each type that needs to be traversed
    private Deque<TypeVariable> typeVariableStack = new ArrayDeque<>();

    GenericSignatureParser(NameTable names) {
        names.intern(DotName.OBJECT_NAME, '/');
        this.names = names;
    }

    static class ClassSignature {
        private final Type[] parameters;
        private final Type superClass;
        private final Type[] interfaces;

        private ClassSignature(Type[] parameters, Type superClass, Type[] interfaces) {
            this.parameters = parameters;
            this.superClass = superClass;
            this.interfaces = interfaces;
        }

        Type[] parameters() {
            return parameters;
        }

        Type superClass() {
            return superClass;
        }

        Type[] interfaces() {
            return interfaces;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (parameters.length > 0) {
                builder.append('<');
                builder.append(parameters[0]);
                for (int i = 1; i < parameters.length; i++) {
                    builder.append(", ").append(parameters[i]);
                }
                builder.append('>');
            }

            if (superClass.name() != DotName.OBJECT_NAME) {
                builder.append(" extends ").append(superClass);
            }

            if (interfaces.length > 0) {
                builder.append(" implements ").append(interfaces[0]);

                for (int i = 1; i < interfaces.length; i++) {
                    builder.append(", ").append(interfaces[i]);
                }
            }

            return builder.toString();
        }
    }

    static class MethodSignature {
        private final Type[] typeParameters;
        private final Type[] methodParameters;
        private final Type returnType;
        private final Type[] throwables;

        private MethodSignature(Type[] typeParameters, Type[] methodParameters, Type returnType, Type[] throwables) {
            this.typeParameters = typeParameters;
            this.methodParameters = methodParameters;
            this.returnType = returnType;
            this.throwables = throwables;
        }

        public Type[] typeParameters() {
            return typeParameters;
        }

        public Type returnType() {
            return returnType;
        }

        public Type[] methodParameters() {
            return methodParameters;
        }

        public Type[] throwables() {
            return throwables;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (typeParameters.length > 0) {
                builder.append("<");
                builder.append(typeParameters[0]);
                for (int i = 1; i < typeParameters.length; i++) {
                    builder.append(", ").append(typeParameters[i]);
                }
                builder.append("> ");
            }

            builder.append(returnType).append(" (");
            if (methodParameters.length > 0) {
                builder.append(methodParameters[0]);

                for (int i = 1; i < methodParameters.length; i++) {
                    builder.append(", ").append(methodParameters[i]);
                }
            }
            builder.append(')');

            if (throwables.length > 0) {
                builder.append(" throws ").append(throwables[0]);

                for (int i = 1; i < throwables.length; i++) {
                    builder.append(", ").append(throwables[i]);
                }
            }

            return builder.toString();
        }

    }

    void beforeNewClass(DotName className) {
        this.currentClassName = className;
        this.classTypeParameters.clear();
        this.elementTypeParameters.clear();
    }

    void beforeNewElement() {
        this.elementTypeParameters.clear();
    }

    ClassSignature parseClassSignature(String signature, DotName className) {
        beforeNewClass(className);
        this.signature = signature;
        this.typeParameters = this.classTypeParameters;
        this.pos = 0;
        Type[] parameters = parseTypeParameters();
        Type superClass = names.intern(parseClassTypeSignature());
        int end = signature.length();
        List<Type> interfaces = new ArrayList<Type>();
        while (pos < end) {
            interfaces.add(names.intern(parseClassTypeSignature()));
        }

        Type[] intfArray = names.intern(interfaces.toArray(new Type[interfaces.size()]));
        return new ClassSignature(parameters, superClass, intfArray);
    }

    private void expect(char c) {
        if (signature.charAt(pos++) != c) {
            throw new IllegalArgumentException("Expected character '" + c + "' at position " + (pos - 1));
        }
    }

    Type parseFieldSignature(String signature) {
        beforeNewElement();
        this.signature = signature;
        this.typeParameters = this.elementTypeParameters;
        this.pos = 0;

        // the grammar in the JVMS says:
        //
        // FieldSignature:
        //   ReferenceTypeSignature
        //
        // however, there are class files in the wild that contain
        // a primitive type signature as a field signature
        return parseJavaType();
    }

    MethodSignature parseMethodSignature(String signature) {
        beforeNewElement();
        this.signature = signature;
        this.typeParameters = this.elementTypeParameters;
        this.pos = 0;

        Type[] typeParameters = parseTypeParameters();

        expect('(');
        List<Type> parameters = new ArrayList<Type>();
        while (signature.charAt(pos) != ')') {
            Type type = parseJavaType();
            if (type == null) {
                throw new IllegalArgumentException("Corrupted argument, or unclosed brace at: " + pos);
            }
            parameters.add(type);
        }
        pos++;

        Type returnType = parseReturnType();
        List<Type> exceptions = new ArrayList<Type>();
        while (pos < signature.length()) {
            expect('^');
            exceptions.add(parseReferenceType());
        }

        Type[] exceptionsArray = names.intern(exceptions.toArray(new Type[exceptions.size()]));
        Type[] types = names.intern(parameters.toArray(new Type[parameters.size()]));
        return new MethodSignature(typeParameters, types, returnType, exceptionsArray);

    }

    private Type parseClassTypeSignature() {
        String signature = this.signature;
        DotName name = parseName();
        Type[] types = parseTypeArguments();
        Type type = null;

        if (types.length > 0) {
            type = new ParameterizedType(name, types, null);
        }

        // Suffix
        while (signature.charAt(pos) == '.') {
            int mark = ++pos;
            int suffixEnd = advanceNameEnd();
            name = names.wrap(name, signature.substring(mark, suffixEnd), true);
            types = parseTypeArguments();

            // A suffix is a parameterized type if it has typeParameters or it's owner is a parameterized type
            // The first parameterized type needs a standard class type for the owner
            if (type == null && types.length > 0) {
                type = names.intern(new ClassType(name.prefix()));
            }

            if (type != null) {
                type = names.intern(new ParameterizedType(name, types, type));
            }
        }
        this.pos++; // ;
        return type != null ? type : names.intern(new ClassType(name));
    }

    private Type[] parseTypeArguments() {
        return parseTypeList(true);
    }

    private Type[] parseTypeParameters() {
        return parseTypeList(false);
    }

    private Type[] parseTypeList(boolean argument) {
        String signature = this.signature;
        if (signature.charAt(pos) != '<') {
            return Type.EMPTY_ARRAY;
        }
        pos++;

        ArrayList<Type> types = new ArrayList<Type>();
        for (;;) {
            Type t = argument ? parseTypeArgument() : parseTypeParameter();
            if (t == null) {
                break;
            }
            types.add(t);
        }
        if (!argument) {
            resolveTypeList(types);
        }
        return names.intern(types.toArray(new Type[types.size()]));
    }

    private Type parseTypeArgument() {
        char c = signature.charAt(pos++);
        switch (c) {
            case '>':
                return null;
            case '*':
                return UNBOUNDED_WILDCARD;
            case '-': {
                return parseWildCard(false);
            }
            case '+': {
                return parseWildCard(true);
            }
            default:
                pos--;
                return parseReferenceType();
        }
    }

    private Type parseWildCard(boolean isExtends) {
        Type bound = parseReferenceType();
        return new WildcardType(bound, isExtends);
    }

    private Type parseTypeParameter() {
        int start = pos;
        String signature = this.signature;

        if (signature.charAt(start) == '>') {
            pos++;
            return null;
        }

        int bound = advancePast(':');
        String name = names.intern(signature.substring(start, bound));

        // when parsing a recursive type parameter, we need to remember it early,
        // because bounds may refer to it
        typeParameters.put(name, null);

        ArrayList<Type> bounds = new ArrayList<Type>();

        // Class bound has an optional reference type
        if (signature.charAt(pos) != ':') {
            bounds.add(parseReferenceType());
        }

        boolean implicitObjectBound = false;
        // Interface bounds are none to many, with a required reference type
        while (signature.charAt(pos) == ':') {
            pos++;

            if (bounds.size() == 0) {
                implicitObjectBound = true;
            }
            bounds.add(parseReferenceType());
        }

        TypeVariable type = new TypeVariable(name, bounds.toArray(new Type[bounds.size()]), null, implicitObjectBound);
        typeParameters.put(name, type);
        return type;
    }

    private Type parseReturnType() {
        if (signature.charAt(pos) == 'V') {
            pos++;
            return VoidType.VOID;
        }

        return parseJavaType();
    }

    private Type parseReferenceType() {
        int mark = pos;
        char c = signature.charAt(mark);
        Type type;
        switch (c) {
            case 'T':
                type = parseTypeVariable();
                break;
            case 'L':
                type = parseClassTypeSignature();
                break;
            case '[':
                type = parseArrayType();
                break;
            default:
                return null;
        }

        return names.intern(type);
    }

    private Type parseArrayType() {
        int mark = this.pos;
        int last = advanceNot('[');
        return new ArrayType(parseJavaType(), last - mark);
    }

    private Type parseTypeVariable() {
        String name = names.intern(signature.substring(pos + 1, advancePast(';')));
        Type type = resolveType(name);
        return type == null ? new UnresolvedTypeVariable(name) : type;
    }

    private void resolveTypeList(ArrayList<Type> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            typeVariableStack.clear(); // should not be needed, just for extra safety
            boolean isRecursive = isRecursive(list.get(i));
            Type type = resolveType(list.get(i), isRecursive);
            if (type != null) {
                list.set(i, type);
                typeParameters.put(type.asTypeVariable().identifier(), type.asTypeVariable());
            }
        }

        // interspersing resolution (above) with patching would lead to type variable references
        // pointing to stale type variables, hence patching must be an extra editing pass
        for (int i = 0; i < list.size(); i++) {
            typeVariableStack.clear(); // should not be needed, just for extra safety
            patchTypeVariableReferences(list.get(i));
        }
    }

    private TypeVariable findOnTypeVariableStack(String typeVariableIdentifier) {
        for (TypeVariable typeVariable : typeVariableStack) {
            if (typeVariable.identifier().equals(typeVariableIdentifier)) {
                return typeVariable;
            }
        }
        return null;
    }

    private boolean isRecursive(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE) {
            return findOnTypeVariableStack(type.asTypeVariableReference().identifier()) != null;
        } else if (type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
            String unresolvedIdentifier = type.asUnresolvedTypeVariable().identifier();
            if (findOnTypeVariableStack(unresolvedIdentifier) != null) {
                return true;
            }
            if (typeParameters.containsKey(unresolvedIdentifier)) {
                return isRecursive(typeParameters.get(unresolvedIdentifier));
            }
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            if (findOnTypeVariableStack(type.asTypeVariable().identifier()) != null) {
                return true;
            }
            typeVariableStack.push(type.asTypeVariable());
            Type[] bounds = type.asTypeVariable().boundArray();
            for (int i = 0; i < bounds.length; i++) {
                if (isRecursive(bounds[i])) {
                    return true;
                }
            }
            typeVariableStack.pop();
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            if (type.asParameterizedType().owner() != null && isRecursive(type.asParameterizedType().owner())) {
                return true;
            }
            Type[] typeArguments = type.asParameterizedType().argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                if (isRecursive(typeArguments[i])) {
                    return true;
                }
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            return isRecursive(type.asWildcardType().bound());
        } else if (type.kind() == Type.Kind.ARRAY) {
            return isRecursive(type.asArrayType().component());
        }
        return false;
    }

    private Type resolveType(Type type, boolean isRecursive) {
        if (type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
            String identifier = type.asUnresolvedTypeVariable().identifier();
            if (isRecursive && typeParameters.containsKey(identifier)) {
                return new TypeVariableReference(identifier, currentClassName);
            } else if (elementTypeParameters.containsKey(identifier)) {
                return elementTypeParameters.get(identifier);
            } else if (classTypeParameters.containsKey(identifier)) {
                return classTypeParameters.get(identifier);
            }
            // otherwise the unresolved type variable can't really be resolved,
            // which basically means it's "fully resolved", so we return `null` below
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable variable = type.asTypeVariable();
            Type[] bounds = variable.boundArray();
            for (int i = 0; i < bounds.length; i++) {
                Type newBound = resolveType(bounds[i], isRecursive);
                if (newBound != null && newBound != bounds[i]) {
                    variable = variable.copyType(i, newBound);
                }
            }
            if (variable != type) {
                return variable;
            }
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType parameterized = type.asParameterizedType();
            if (parameterized.owner() != null) {
                Type newOwner = resolveType(parameterized.owner(), isRecursive);
                if (newOwner != null && newOwner != parameterized.owner()) {
                    parameterized = parameterized.copyType(newOwner);
                }
            }
            Type[] typeArguments = parameterized.argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                Type newTypeArgument = resolveType(typeArguments[i], isRecursive);
                if (newTypeArgument != null && newTypeArgument != typeArguments[i]) {
                    parameterized = parameterized.copyType(i, newTypeArgument);
                }
            }
            if (parameterized != type) {
                return parameterized;
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            WildcardType wildcard = type.asWildcardType();
            Type newBound = resolveType(wildcard.bound(), isRecursive);
            if (newBound != null && newBound != wildcard.bound()) {
                return wildcard.copyType(newBound);
            }
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            Type newComponent = resolveType(array.component(), isRecursive);
            if (newComponent != null && newComponent != array.component()) {
                return array.copyType(newComponent, array.dimensions());
            }
        }

        return null; // indicates that `type` is already fully resolved
    }

    private void patchTypeVariableReferences(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE_REFERENCE) {
            String identifier = type.asTypeVariableReference().identifier();

            TypeVariable typeVariable = findOnTypeVariableStack(identifier);
            if (typeVariable != null) {
                type.asTypeVariableReference().setTarget(typeVariable);
                return;
            }

            TypeVariable typeParameter = typeParameters.get(identifier);
            if (typeParameter != null) {
                type.asTypeVariableReference().setTarget(typeParameter);
            }
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            typeVariableStack.push(type.asTypeVariable());
            Type[] bounds = type.asTypeVariable().boundArray();
            for (int i = 0; i < bounds.length; i++) {
                patchTypeVariableReferences(bounds[i]);
            }
            typeVariableStack.pop();
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            if (type.asParameterizedType().owner() != null) {
                patchTypeVariableReferences(type.asParameterizedType().owner());
            }
            Type[] typeArguments = type.asParameterizedType().argumentsArray();
            for (int i = 0; i < typeArguments.length; i++) {
                patchTypeVariableReferences(typeArguments[i]);
            }
        } else if (type.kind() == Type.Kind.WILDCARD_TYPE) {
            patchTypeVariableReferences(type.asWildcardType().bound());
        } else if (type.kind() == Type.Kind.ARRAY) {
            patchTypeVariableReferences(type.asArrayType().component());
        }
    }

    private TypeVariable resolveType(String identifier) {
        if (elementTypeParameters.containsKey(identifier)) {
            // may be `null` if the type parameter is recursive
            // and we're in the middle of parsing it, but that's expected
            return elementTypeParameters.get(identifier);
        }
        return classTypeParameters.get(identifier);
    }

    private Type parseJavaType() {
        Type type = PrimitiveType.decode(signature.charAt(pos));
        if (type != null) {
            pos++;
            return type;
        }
        return parseReferenceType();
    }

    private int advancePast(char c) {
        int pos = signature.indexOf(c, this.pos);
        if (pos == -1) {
            throw new IllegalStateException("Corruption");
        }

        this.pos = pos + 1;
        return pos;
    }

    private int advanceNot(char c) {
        while (signature.charAt(pos) == c) {
            pos++;
        }

        return pos;
    }

    private DotName parseName() {
        int start = pos;
        int end = advanceNameEnd();

        if (signature.charAt(start++) != 'L') {
            throw new IllegalArgumentException("Invalid signature, invalid class designator");
        }

        return names.convertToName(signature.substring(start, end), '/');
    }

    private int advanceNameEnd() {
        int end = pos;
        String signature = this.signature;

        for (; end < signature.length(); end++) {
            char c = signature.charAt(end);
            if (c == '.' || c == '<' || c == ';') {
                return pos = end;
            }
        }

        throw new IllegalStateException("Corrupted name");
    }

    // @formatter:off
    public static void main(String[] args) throws IOException {
        GenericSignatureParser parser = new GenericSignatureParser(new NameTable());
        MethodSignature sig1 = parser.parseMethodSignature("<U:Ljava/lang/Foo;>(Ljava/lang/Class<TU;>;TU;)Ljava/lang/Class<+TU;>;");
//        MethodSignature sig1 = parser.parseMethodSignature("<U:Ljava/lang/Foo;>(Ljava/lang/Class<TU;>;TU;)Ljava/lang/Class<+TU;>;");
//        MethodSignature sig2 = parser.parseMethodSignature("<K:Ljava/lang/Object;V:Ljava/lang/Object;>(Ljava/util/Map<TK;TV;>;Ljava/lang/Class<TK;>;Ljava/lang/Class<TV;>;)Ljava/util/Map<TK;TV;>;");
//        MethodSignature sig3 = parser.parseMethodSignature("<T:Ljava/lang/Object;>(Ljava/util/Collection<-TT;>;[TT;)Z");
//       MethodSignature sig4 = parser.parseMethodSignature("(Ljava/util/Collection<*>;Ljava/util/Collection<*>;)Z");
//      MethodSignature sig7 = parser.parseMethodSignature("()Lcom/sun/xml/internal/bind/v2/model/impl/ElementInfoImpl<Ljava/lang/reflect/Type;Ljava/lang/Class;Ljava/lang/reflect/Field;Ljava/lang/reflect/Method;>.PropertyImpl;");
//        ClassSignature sig5 = parser.parseClassSignature("<C:Lio/undertow/server/protocol/framed/AbstractFramedChannel<TC;TR;TS;>;R:Lio/undertow/server/protocol/framed/AbstractFramedStreamSourceChannel<TC;TR;TS;>;S:Lio/undertow/server/protocol/framed/AbstractFramedStreamSinkChannel<TC;TR;TS;>;>Ljava/lang/Object;Lorg/xnio/channels/ConnectedChannel;");
//        ClassSignature sig6 = parser.parseClassSignature("Lcom/apple/laf/AquaUtils$RecyclableSingleton<Ljavax/swing/text/LayeredHighlighter$LayerPainter;>;");
//        System.out.println(sig1);
//        System.out.println(sig2);
//        System.out.println(sig3);
//        System.out.println(sig4);
//        System.out.println(sig5);
//        System.out.println(sig6);
//       System.out.println(sig7);

//        BufferedReader reader = new BufferedReader(new FileReader("/Users/jason/sigmethods.txt"));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            try {
//                System.out.println(parser.parseMethodSignature(line));
//            } catch (Exception e) {
//                System.err.println(line);
//                e.printStackTrace(System.err);
//                System.exit(-1);
//            }
//        }

    }
    // @formatter:on

}
