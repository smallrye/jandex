package org.jboss.jandex;

import java.util.Objects;

// see Type.parse() for the grammar
class TypeParser {
    private final String str;

    private int pos = 0;

    TypeParser(String str) {
        this.str = Objects.requireNonNull(str);
    }

    Type parse() {
        Type result;

        String token = nextToken();
        if (token.isEmpty()) {
            throw unexpected(token);
        } else if (token.equals("void")) {
            result = VoidType.VOID;
        } else if (isPrimitiveType(token) && peekToken().isEmpty()) {
            result = PrimitiveType.decode(token);
        } else {
            result = parseReferenceType(token);
        }

        expect("");
        return result;
    }

    private Type parseReferenceType(String token) {
        if (isPrimitiveType(token)) {
            PrimitiveType primitive = PrimitiveType.decode(token);
            return parseArrayType(primitive);
        } else if (isClassType(token)) {
            Type result = ClassType.create(token);
            if (peekToken().equals("<")) {
                expect("<");
                ParameterizedType.Builder builder = ParameterizedType.builder(result.name());
                builder.addArgument(parseTypeArgument());
                while (peekToken().equals(",")) {
                    expect(",");
                    builder.addArgument(parseTypeArgument());
                }
                expect(">");
                result = builder.build();
            }
            if (peekToken().equals("[")) {
                return parseArrayType(result);
            }
            return result;
        } else {
            throw unexpected(token);
        }
    }

    private Type parseArrayType(Type elementType) {
        expect("[");
        expect("]");
        int dimensions = 1;
        while (peekToken().equals("[")) {
            expect("[");
            expect("]");
            dimensions++;
        }
        return ArrayType.create(elementType, dimensions);
    }

    private Type parseTypeArgument() {
        String token = nextToken();
        if (token.equals("?")) {
            if (peekToken().equals("extends")) {
                expect("extends");
                Type bound = parseReferenceType(nextToken());
                return WildcardType.createUpperBound(bound);
            } else if (peekToken().equals("super")) {
                expect("super");
                Type bound = parseReferenceType(nextToken());
                return WildcardType.createLowerBound(bound);
            } else {
                return WildcardType.UNBOUNDED;
            }
        } else {
            return parseReferenceType(token);
        }
    }

    private boolean isPrimitiveType(String token) {
        return token.equals("boolean")
                || token.equals("byte")
                || token.equals("short")
                || token.equals("int")
                || token.equals("long")
                || token.equals("float")
                || token.equals("double")
                || token.equals("char");
    }

    private boolean isClassType(String token) {
        return !token.isEmpty() && Character.isJavaIdentifierStart(token.charAt(0));
    }

    // ---

    private void expect(String expected) {
        String token = nextToken();
        if (!expected.equals(token)) {
            throw unexpected(token);
        }
    }

    private IllegalArgumentException unexpected(String token) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Unexpected end of input: " + str);
        }
        return new IllegalArgumentException("Unexpected token '" + token + "' at position " + (pos - token.length())
                + ": " + str);
    }

    private String peekToken() {
        // skip whitespace
        while (pos < str.length() && Character.isWhitespace(str.charAt(pos))) {
            pos++;
        }

        // end of input
        if (pos == str.length()) {
            return "";
        }

        int pos = this.pos;

        // current char is a token on its own
        if (isSpecial(str.charAt(pos))) {
            return str.substring(pos, pos + 1);
        }

        // token is a keyword or fully qualified name
        int begin = pos;
        while (pos < str.length() && Character.isJavaIdentifierStart(str.charAt(pos))) {
            do {
                pos++;
            } while (pos < str.length() && Character.isJavaIdentifierPart(str.charAt(pos)));

            if (pos < str.length() && str.charAt(pos) == '.') {
                pos++;
            } else {
                return str.substring(begin, pos);
            }
        }

        if (pos == str.length()) {
            throw new IllegalArgumentException("Unexpected end of input: " + str);
        }
        throw new IllegalArgumentException("Unexpected character '" + str.charAt(pos) + "' at position " + pos + ": " + str);
    }

    private String nextToken() {
        String result = peekToken();
        pos += result.length();
        return result;
    }

    private boolean isSpecial(char c) {
        return c == ',' || c == '?' || c == '<' || c == '>' || c == '[' || c == ']';
    }
}
