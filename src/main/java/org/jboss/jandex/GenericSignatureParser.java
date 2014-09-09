package org.jboss.jandex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jason T. Greene
 *
 */
class GenericSignatureParser {
    /*
     * Complete Grammar  (VM Spec v8)
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
    private static WildcardType UNBOUNDED_WILDCARD = new WildcardType(null, true);
    private String signature;
    private int pos;
    private NameTable names;
    private Map<String, TypeVariable> typeParameters = new HashMap<String, TypeVariable>();

    GenericSignatureParser() {
        names = new NameTable();
        names.intern(DotName.OBJECT_NAME, '/');
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

    ClassSignature parseClassSignature(String signature) {
        this.signature = signature;
        this.pos = 0;
        Type[] parameters = parseTypeParameters();
        Type superClass = parseClassTypeSignature();
        int end = signature.length();
        List<Type> interfaces = new ArrayList<Type>();
        while (pos < end) {
            interfaces.add(parseClassTypeSignature());
        }

        return new ClassSignature(parameters, superClass, interfaces.toArray(new Type[interfaces.size()]));

    }

    private void expect(char c) {
         if (signature.charAt(pos++) != c) {
             throw new IllegalArgumentException("Expected character '" + c + "' at position " + (pos - 1));
         }
     }


    MethodSignature parseMethodSignature(String signature) {
        this.signature = signature;
        this.typeParameters.clear();

        this.pos = 0;
        Type[] typeParameters = parseTypeParameters();

        expect('(');
        List<Type> parameters = new ArrayList<Type>();
        while (signature.charAt(pos) != ')') {
            parameters.add(parseJavaType());
        }
        pos++;

        Type returnType = parseReturnType();
        List<Type> throwables = new ArrayList<Type>();
        while (pos < signature.length()) {
            expect('^');
            throwables.add(parseReferenceType());
        }

        return new MethodSignature(typeParameters, parameters.toArray(new Type[parameters.size()]), returnType,
                throwables.toArray(new Type[throwables.size()]));

    }

    private Type parseClassTypeSignature() {
        int end = scanReferenceTypeEnd();
        String signature = this.signature;
        NameTable.Slice slice = names.createSlice(signature, pos, end);
        Type type = names.getType(slice);

        if (type != null) {
            this.pos = end;
            return type;
        }

        DotName name = parseName();
        Type[] types = parseTypeArguments();

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
                type = new ClassType(name.prefix());
            }

            if (type != null) {
                type = new ParameterizedType(name, types, type);
            }
        }
        this.pos++; // ;
        type = type != null ? type : new ClassType(name);
        names.storeType(slice, type);
        return type;
    }

    private int scanReferenceTypeEnd() {
        String signature = this.signature;
        int i = pos;
        int open = 0;

        while (i < signature.length()) {
            switch (signature.charAt(i++)) {
                case '<':
                    open++;
                    break;
                case '>':
                    open--;
                    break;
                case ';':
                    if (open == 0) {
                        return i;
                    }
            }
        }

        throw new IllegalArgumentException("Invalid signature, class type is missing terminator");
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

        int end = scanListEnd();
        NameTable.Slice slice = names.createSlice(signature, pos, end);
        Type[] typeList = names.getTypeList(slice);
        if (typeList != null) {
            this.pos = end;
            return typeList;
        }

        List<Type> types = new ArrayList<Type>();
        for (;;) {
            Type t = argument ? parseTypeArgument() : parseTypeParameter();
            if (t == null) {
                break;
            }
            types.add(t);
        }
        return names.storeTypeList(slice, types.toArray(new Type[types.size()]));
    }

    private int scanListEnd() {
        String signature = this.signature;
        int i = pos;
        int open = 0;

        while (i < signature.length()) {
            switch (signature.charAt(i++)) {
                case '<':
                    open++;
                    break;
                case '>':
                    if (--open < 0) {
                        return i;
                    }

                    break;
            }
        }

        throw new IllegalArgumentException("Invalid signature, class type is missing terminator");
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
        int end = scanReferenceTypeEnd();
        NameTable.Slice slice = names.createSlice(signature, pos, end);
        Type type = names.getType(slice);
        if (type != null) {
            pos = end;
            return type;
        }

        Type bound = parseReferenceType();
        return names.storeType(slice,  new WildcardType(bound, isExtends));
    }

    private Type parseTypeParameter() {
        int start = pos;
        String signature = this.signature;

        if (signature.charAt(start) == '>') {
            pos++;
            return null;
        }

        int end = scanTypeParameterEnd();
        NameTable.Slice slice = names.createSlice(signature, start, end);
        TypeVariable type = (TypeVariable) names.getType(slice);
        if (type != null) {
            return type;
        }

        int bound = advancePast(':');
        String name = names.intern(signature.substring(start, bound));

        ArrayList<Type> bounds = new ArrayList<Type>();

        // Class bound has an optional reference type
        if (signature.charAt(pos) != ':') {
            bounds.add(parseReferenceType());
        }

        // Interface bounds are none to many, with a required reference type
        while (signature.charAt(pos) == ':') {
            pos++;
            bounds.add(parseReferenceType());
        }

        type = new TypeVariable(name, bounds.toArray(new Type[bounds.size()]));
        names.storeType(slice, type);
        typeParameters.put(type.identifier(), type);
        return type;
    }

    private int scanTypeParameterEnd() {
        String signature = this.signature;
        int i = pos;
        int open = 0;

        while (i < signature.length() - 1) {
            char c = signature.charAt(i++);
            switch (c) {
                case ':': {
                    char peek = signature.charAt(i);
                    if (peek != 'T' && peek != 'L' && peek != '[') {
                        return i;
                    }
                    break;
                }
                case '<':
                    open++;
                    break;
                case '>':
                    open--;
                    break;
                case ';':
                    if (open == 0) {
                        char peek = signature.charAt(i);
                        if (peek != ':') {
                            return i;
                        }
                    }
                    break;
            }
        }

        throw new IllegalArgumentException("Invalid signature, class type is missing terminator");
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
        switch (c) {
            case 'T':
                return parseTypeVariable();
            case 'L':
                return parseClassTypeSignature();
            case '[':
                return parseArrayType();
            default:
                return null;
        }
    }

    private Type parseArrayType() {
        int mark = this.pos;
        int end = scanReferenceTypeEnd();
        NameTable.Slice slice = names.createSlice(signature, mark, end);

        Type type = names.getType(slice);
        if (type != null) {
            pos = end;
            return type;
        }

        int last = advanceNot('[');
        type = new ArrayType(parseJavaType(), last - mark);
        names.storeType(slice, type);
        return type;
    }

    private Type parseTypeVariable() {
        String name = names.intern(signature.substring(pos + 1, advancePast(';')));
        Type type = typeParameters.get(name);
        return type == null ? new TypeVariable(name) : type;
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
            if (c == '.' || c == '<' || c ==';') {
                return pos = end;
            }
        }

        throw new IllegalStateException("Corrupted name");
    }

    public static void main(String[] args) throws IOException {
        GenericSignatureParser parser = new GenericSignatureParser();
        MethodSignature sig1 = parser.parseMethodSignature("<U:Ljava/lang/Foo;>(Ljava/lang/Class<TU;>;TU;)Ljava/lang/Class<+TU;>;");
        MethodSignature sig2 = parser.parseMethodSignature("<K:Ljava/lang/Object;V:Ljava/lang/Object;>(Ljava/util/Map<TK;TV;>;Ljava/lang/Class<TK;>;Ljava/lang/Class<TV;>;)Ljava/util/Map<TK;TV;>;");
        MethodSignature sig3 = parser.parseMethodSignature("<T:Ljava/lang/Object;>(Ljava/util/Collection<-TT;>;[TT;)Z");
       MethodSignature sig4 = parser.parseMethodSignature("(Ljava/util/Collection<*>;Ljava/util/Collection<*>;)Z");
      MethodSignature sig7 = parser.parseMethodSignature("()Lcom/sun/xml/internal/bind/v2/model/impl/ElementInfoImpl<Ljava/lang/reflect/Type;Ljava/lang/Class;Ljava/lang/reflect/Field;Ljava/lang/reflect/Method;>.PropertyImpl;");
        ClassSignature sig5 = parser.parseClassSignature("<C:Lio/undertow/server/protocol/framed/AbstractFramedChannel<TC;TR;TS;>;R:Lio/undertow/server/protocol/framed/AbstractFramedStreamSourceChannel<TC;TR;TS;>;S:Lio/undertow/server/protocol/framed/AbstractFramedStreamSinkChannel<TC;TR;TS;>;>Ljava/lang/Object;Lorg/xnio/channels/ConnectedChannel;");
        ClassSignature sig6 = parser.parseClassSignature("Lcom/apple/laf/AquaUtils$RecyclableSingleton<Ljavax/swing/text/LayeredHighlighter$LayerPainter;>;");
        System.out.println(sig1);
        System.out.println(sig2);
        System.out.println(sig3);
        System.out.println(sig4);
        System.out.println(sig5);
        System.out.println(sig6);
       System.out.println(sig7);

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


}
