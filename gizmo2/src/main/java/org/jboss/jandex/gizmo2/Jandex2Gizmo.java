package org.jboss.jandex.gizmo2;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.WildcardType;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.TypeArgument;
import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.creator.MethodCreator;
import io.quarkus.gizmo2.creator.ModifiableCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.TypeParameterizedCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * Bridge methods from {@code org.jboss.jandex} types to the Gizmo 2 API.
 */
public class Jandex2Gizmo {
    private static final Map<DotName, ClassDesc> CLASS_DESC_CACHE = new ConcurrentHashMap<>();

    /**
     * {@return the {@link ClassDesc} corresponding to the given Jandex {@link DotName}}
     * See {@link Type#name()} for the description of the format this method recognizes.
     *
     * @param name the Jandex {@code DotName} (must not be {@code null})
     */
    public static ClassDesc classDescOf(DotName name) {
        // optimistic get to avoid computeIfAbsent for most calls
        ClassDesc classDesc = CLASS_DESC_CACHE.get(name);
        if (classDesc != null) {
            return classDesc;
        }

        return CLASS_DESC_CACHE.computeIfAbsent(name, nameParam -> {
            if (nameParam.prefix() == null) {
                String local = nameParam.local();
                return switch (local) {
                    case "void" -> ConstantDescs.CD_void;
                    case "boolean" -> ConstantDescs.CD_boolean;
                    case "byte" -> ConstantDescs.CD_byte;
                    case "short" -> ConstantDescs.CD_short;
                    case "int" -> ConstantDescs.CD_int;
                    case "long" -> ConstantDescs.CD_long;
                    case "float" -> ConstantDescs.CD_float;
                    case "double" -> ConstantDescs.CD_double;
                    case "char" -> ConstantDescs.CD_char;
                    default -> ofClassOrArray(local);
                };
            }
            return ofClassOrArray(nameParam.toString());
        });
    }

    private static ClassDesc ofClassOrArray(String name) {
        int dimensions = 0;
        while (name.charAt(dimensions) == '[') {
            dimensions++;
        }
        if (dimensions == 0) {
            // `name` must be a binary name of a class
            return ClassDesc.of(name);
        }

        ClassDesc elementType = name.charAt(dimensions) == 'L'
                // class type, need to skip `L` at the beginning and `;` at the end
                ? ClassDesc.of(name.substring(dimensions + 1, name.length() - 1))
                // primitive type, the rest of `name` is just the primitive descriptor
                : ClassDesc.ofDescriptor(name.substring(dimensions));
        return elementType.arrayType(dimensions);
    }

    /**
     * {@return the {@link ClassDesc} corresponding to the erasure of given Jandex {@link Type}}
     *
     * @param type the Jandex type (must not be {@code null})
     */
    public static ClassDesc classDescOf(Type type) {
        return switch (type.kind()) {
            case VOID -> ConstantDescs.CD_void;
            case PRIMITIVE -> switch (type.asPrimitiveType().primitive()) {
                case BOOLEAN -> ConstantDescs.CD_boolean;
                case BYTE -> ConstantDescs.CD_byte;
                case SHORT -> ConstantDescs.CD_short;
                case INT -> ConstantDescs.CD_int;
                case LONG -> ConstantDescs.CD_long;
                case FLOAT -> ConstantDescs.CD_float;
                case DOUBLE -> ConstantDescs.CD_double;
                case CHAR -> ConstantDescs.CD_char;
            };
            case ARRAY -> {
                ArrayType arrayType = type.asArrayType();
                ClassDesc element = classDescOf(arrayType.elementType());
                yield element.arrayType(arrayType.deepDimensions());
            }
            default -> classDescOf(type.name());
        };
    }

    /**
     * {@return the {@link ClassDesc} corresponding to the given Jandex {@link ClassInfo}}
     *
     * @param clazz the Jandex class (must not be {@code null})
     */
    public static ClassDesc classDescOf(ClassInfo clazz) {
        return classDescOf(clazz.name());
    }

    /**
     * {@return the {@link FieldDesc} corresponding to the given Jandex {@link FieldInfo}}
     *
     * @param field the Jandex field (must not be {@code null})
     */
    public static FieldDesc fieldDescOf(FieldInfo field) {
        return FieldDesc.of(classDescOf(field.declaringClass()), field.name(), classDescOf(field.type()));
    }

    /**
     * {@return the {@link MethodDesc} corresponding to the given Jandex {@link MethodInfo}}
     *
     * @param method the Jandex method (must not be {@code null})
     * @throws IllegalArgumentException if the {@code method} is a static initializer or constructor
     */
    public static MethodDesc methodDescOf(MethodInfo method) {
        if (method.isConstructor()) {
            throw new IllegalArgumentException("Cannot create MethodDesc for constructor: " + method);
        }
        if (method.isStaticInitializer()) {
            throw new IllegalArgumentException("Cannot create MethodDesc for static initializer: " + method);
        }

        ClassDesc owner = classDescOf(method.declaringClass());
        ClassDesc returnType = classDescOf(method.returnType());
        ClassDesc[] paramTypes = new ClassDesc[method.parametersCount()];
        for (int i = 0; i < method.parametersCount(); i++) {
            paramTypes[i] = classDescOf(method.parameterType(i));
        }
        MethodTypeDesc methodTypeDesc = MethodTypeDesc.of(returnType, paramTypes);
        return method.declaringClass().isInterface()
                ? InterfaceMethodDesc.of(owner, method.name(), methodTypeDesc)
                : ClassMethodDesc.of(owner, method.name(), methodTypeDesc);
    }

    /**
     * {@return the {@link ConstructorDesc} corresponding to the given Jandex {@link MethodInfo}}
     *
     * @param ctor the Jandex constructor (must not be {@code null})
     * @throws IllegalArgumentException if the {@code ctor} is not a constructor
     */
    public static ConstructorDesc constructorDescOf(MethodInfo ctor) {
        if (ctor.isStaticInitializer()) {
            throw new IllegalArgumentException("Cannot create ConstructorDesc for static initializer: " + ctor);
        }
        if (!ctor.isConstructor()) {
            throw new IllegalArgumentException("Cannot create ConstructorDesc for regular method: " + ctor);
        }

        List<ClassDesc> paramTypes = new ArrayList<>(ctor.parametersCount());
        for (int i = 0; i < ctor.parametersCount(); i++) {
            paramTypes.add(classDescOf(ctor.parameterType(i)));
        }
        return ConstructorDesc.of(classDescOf(ctor.declaringClass()), paramTypes);
    }

    /**
     * {@return a {@link GenericType } corresponding to the given Jandex {@link DotName}}
     *
     * @param name the Jandex {@code DotName} (must not be {@code null})
     */
    public static GenericType genericTypeOf(DotName name) {
        return GenericType.of(classDescOf(name));
    }

    /**
     * {@return a {@link GenericType} corresponding to the given Jandex {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     */
    public static GenericType genericTypeOf(Type type) {
        return genericTypeOf(type, null);
    }

    /**
     * {@return a {@link GenericType} corresponding to the given Jandex {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     */
    public static GenericType genericTypeOf(Type type, IndexView index) {
        return switch (type.kind()) {
            case VOID, PRIMITIVE -> genericTypeOfPrimitive(type, index);
            default -> genericTypeOfReference(type, index);
        };
    }

    /**
     * {@return a {@link GenericType.OfPrimitive} corresponding to the given Jandex primitive {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a primitive type or {@code void}
     */
    public static GenericType.OfPrimitive genericTypeOfPrimitive(Type type) {
        return genericTypeOfPrimitive(type, null);
    }

    /**
     * {@return a {@link GenericType.OfPrimitive} corresponding to the given Jandex primitive {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a primitive type or {@code void}
     */
    public static GenericType.OfPrimitive genericTypeOfPrimitive(Type type, IndexView index) {
        GenericType.OfPrimitive result = switch (type.kind()) {
            case VOID -> GenericType.ofPrimitive(void.class);
            case PRIMITIVE -> switch (type.asPrimitiveType().primitive()) {
                case BOOLEAN -> GenericType.ofPrimitive(boolean.class);
                case BYTE -> GenericType.ofPrimitive(byte.class);
                case SHORT -> GenericType.ofPrimitive(short.class);
                case INT -> GenericType.ofPrimitive(int.class);
                case LONG -> GenericType.ofPrimitive(long.class);
                case FLOAT -> GenericType.ofPrimitive(float.class);
                case DOUBLE -> GenericType.ofPrimitive(double.class);
                case CHAR -> GenericType.ofPrimitive(char.class);
            };
            default -> throw new IllegalArgumentException("Not a primitive type: " + type);
        };
        return index == null ? result : result.withAnnotations(copyAnnotations(type, index));
    }

    /**
     * {@return a {@link GenericType.OfReference} corresponding to the given Jandex reference {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a reference type
     */
    public static GenericType.OfReference genericTypeOfReference(Type type) {
        return genericTypeOfReference(type, null);
    }

    /**
     * {@return a {@link GenericType.OfReference} corresponding to the given Jandex reference {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a reference type
     */
    public static GenericType.OfReference genericTypeOfReference(Type type, IndexView index) {
        return switch (type.kind()) {
            case VOID, PRIMITIVE -> throw new IllegalArgumentException("Not a reference type: " + type);
            case ARRAY -> genericTypeOfArray(type, index);
            default -> genericTypeOfThrows(type, index);
        };
    }

    /**
     * {@return a {@link GenericType.OfArray} corresponding to the given Jandex array {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not an array type
     */
    public static GenericType.OfArray genericTypeOfArray(Type type) {
        return genericTypeOfArray(type, null);
    }

    /**
     * {@return a {@link GenericType.OfArray} corresponding to the given Jandex array {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not an array type
     */
    public static GenericType.OfArray genericTypeOfArray(Type type, IndexView index) {
        if (type.kind() != Type.Kind.ARRAY) {
            throw new IllegalArgumentException("Not an array type: " + type);
        }

        GenericType constituent = genericTypeOf(type.asArrayType().constituent(), index);
        GenericType.OfArray result = constituent.arrayType();
        for (int i = 1; i < type.asArrayType().dimensions(); i++) {
            result = result.arrayType();
        }
        assert result != null;
        return index == null ? result : result.withAnnotations(copyAnnotations(type, index));
    }

    /**
     * {@return a {@link GenericType.OfThrows} corresponding to the given Jandex throwable {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a throwable type
     */
    public static GenericType.OfThrows genericTypeOfThrows(Type type) {
        return genericTypeOfThrows(type, null);
    }

    /**
     * {@return a {@link GenericType.OfThrows} corresponding to the given Jandex throwable {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a throwable type
     */
    public static GenericType.OfThrows genericTypeOfThrows(Type type, IndexView index) {
        return switch (type.kind()) {
            case VOID, PRIMITIVE, ARRAY -> throw new IllegalArgumentException("Not a throwable type: " + type);
            case CLASS, PARAMETERIZED_TYPE -> genericTypeOfClass(type, index);
            case TYPE_VARIABLE, TYPE_VARIABLE_REFERENCE, UNRESOLVED_TYPE_VARIABLE -> genericTypeOfTypeVariable(type, index);
            case WILDCARD_TYPE -> throw new IllegalArgumentException("Wildcard types may only be type arguments");
        };
    }

    /**
     * {@return a {@link GenericType.OfClass} corresponding to the given Jandex class or parameterized {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a class or parameterized type
     */
    public static GenericType.OfClass genericTypeOfClass(Type type) {
        return genericTypeOfClass(type, null);
    }

    /**
     * {@return a {@link GenericType.OfClass} corresponding to the given Jandex class or parameterized {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a class or parameterized type
     */
    public static GenericType.OfClass genericTypeOfClass(Type type, IndexView index) {
        GenericType.OfClass result = switch (type.kind()) {
            case CLASS -> GenericType.ofClass(classDescOf(type.name()));
            case PARAMETERIZED_TYPE -> {
                List<Type> typeArgs = type.asParameterizedType().arguments();
                List<TypeArgument> translatedTypeArgs = new ArrayList<>(typeArgs.size());
                for (Type typeArg : typeArgs) {
                    translatedTypeArgs.add(typeArgumentOf(typeArg, index));
                }
                Type owner = type.asParameterizedType().owner();
                if (owner != null) {
                    GenericType.OfClass translatedOwner = genericTypeOfClass(owner, index);
                    yield GenericType.ofInnerClass(translatedOwner, type.name().local()).withArguments(translatedTypeArgs);
                } else {
                    yield GenericType.ofClass(classDescOf(type.name()), translatedTypeArgs);
                }
            }
            default -> throw new IllegalArgumentException("Not a class/parameterized type: " + type);
        };
        return index == null ? result : result.withAnnotations(copyAnnotations(type, index));
    }

    /**
     * {@return a {@link GenericType.OfTypeVariable} corresponding to the given Jandex {@link Type} variable}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a type variable
     */
    public static GenericType.OfTypeVariable genericTypeOfTypeVariable(Type type) {
        return genericTypeOfTypeVariable(type, null);
    }

    /**
     * {@return a {@link GenericType.OfTypeVariable} corresponding to the given Jandex {@link Type} variable}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} is not a type variable
     */
    public static GenericType.OfTypeVariable genericTypeOfTypeVariable(Type type, IndexView index) {
        GenericType.OfTypeVariable result = switch (type.kind()) {
            case TYPE_VARIABLE ->
                GenericType.ofTypeVariable(type.asTypeVariable().identifier(), classDescOf(type.name()));
            case TYPE_VARIABLE_REFERENCE ->
                GenericType.ofTypeVariable(type.asTypeVariableReference().identifier(), classDescOf(type.name()));
            case UNRESOLVED_TYPE_VARIABLE ->
                GenericType.ofTypeVariable(type.asUnresolvedTypeVariable().identifier(), CD_Object);
            default -> throw new IllegalArgumentException("Not a type variable: " + type);
        };
        return index == null ? result : result.withAnnotations(copyAnnotations(type, index));
    }

    /**
     * {@return a {@link TypeArgument} corresponding to the given Jandex {@link Type}}
     * The result does <em>not</em> include type annotations.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @throws IllegalArgumentException if the given {@code type} cannot be a type argument
     */
    public static TypeArgument typeArgumentOf(Type type) {
        return typeArgumentOf(type, null);
    }

    /**
     * {@return a {@link TypeArgument} corresponding to the given Jandex {@link Type}}
     * The result includes type annotations if the {@code index} is not {@code null}.
     *
     * @param type the Jandex {@code Type} (must not be {@code null})
     * @param index the Jandex index that contains classes of type annotations (may be {@code null})
     * @throws IllegalArgumentException if the given {@code type} cannot be a type argument
     */
    public static TypeArgument typeArgumentOf(Type type, IndexView index) {
        return switch (type.kind()) {
            case VOID, PRIMITIVE -> throw new IllegalArgumentException("Primitive types cannot be type arguments");
            case CLASS, PARAMETERIZED_TYPE, ARRAY, TYPE_VARIABLE, TYPE_VARIABLE_REFERENCE, UNRESOLVED_TYPE_VARIABLE ->
                TypeArgument.ofExact(genericTypeOfReference(type, index));
            case WILDCARD_TYPE -> {
                TypeArgument.OfWildcard result;
                WildcardType wildcard = type.asWildcardType();
                if (wildcard.superBound() != null) {
                    result = TypeArgument.ofSuper(genericTypeOfReference(wildcard.superBound(), index));
                } else if (wildcard.extendsBound() != null && (!DotName.OBJECT_NAME.equals(wildcard.extendsBound().name())
                        || !wildcard.extendsBound().annotations().isEmpty())) {
                    // the `extends` bound is either not `Object`, or it has type annotations and must be explicit
                    result = TypeArgument.ofExtends(genericTypeOfReference(wildcard.extendsBound(), index));
                } else {
                    result = TypeArgument.ofUnbounded();
                }
                yield index == null ? result : result.withAnnotations(copyAnnotations(type, index));
            }
        };
    }

    /**
     * Copies type parameters from given {@code clazz} to the given {@code creator}, preserving order.
     *
     * @param clazz the Jandex {@link ClassInfo} (must not be {@code null})
     * @param creator the Gizmo {@link TypeParameterizedCreator} (must not be {@code null})
     */
    public static void copyTypeParameters(ClassInfo clazz, TypeParameterizedCreator creator) {
        copyTypeParameters(clazz.typeParameters(), creator);
    }

    /**
     * Copies type parameters from given {@code method} to the given {@code creator}, preserving order.
     *
     * @param method the Jandex {@link MethodInfo} (must not be {@code null})
     * @param creator the Gizmo {@link TypeParameterizedCreator} (must not be {@code null})
     */
    public static void copyTypeParameters(MethodInfo method, TypeParameterizedCreator creator) {
        copyTypeParameters(method.typeParameters(), creator);
    }

    private static void copyTypeParameters(List<TypeVariable> typeParameters, TypeParameterizedCreator creator) {
        for (TypeVariable typeParameter : typeParameters) {
            addTypeParameter(typeParameter, creator);
        }
    }

    /**
     * Adds the given {@code typeParameter} to the given {@code creator}.
     *
     * @param typeParameter the Jandex {@link TypeVariable} (must not be {@code null})
     * @param creator the Gizmo {@link TypeParameterizedCreator} (must not be {@code null})
     */
    public static void addTypeParameter(TypeVariable typeParameter, TypeParameterizedCreator creator) {
        List<Type> bounds = typeParameter.bounds();
        if (bounds.isEmpty()) {
            creator.typeParameter(typeParameter.identifier());
        } else if (bounds.get(0) instanceof TypeVariable) {
            creator.typeParameter(typeParameter.identifier(), tpc -> {
                tpc.setFirstBound(genericTypeOfTypeVariable(bounds.get(0)));
            });
        } else {
            creator.typeParameter(typeParameter.identifier(), tpc -> {
                int firstInterfaceBound = 0;
                if (!typeParameter.hasImplicitObjectBound()) {
                    tpc.setFirstBound(genericTypeOfClass(bounds.get(0)));
                    firstInterfaceBound = 1;
                }
                if (typeParameter.bounds().size() > firstInterfaceBound) {
                    List<GenericType.OfClass> other = new ArrayList<>(bounds.size() - firstInterfaceBound);
                    for (int i = firstInterfaceBound; i < bounds.size(); i++) {
                        other.add(genericTypeOfClass(bounds.get(i)));
                    }
                    tpc.setOtherBounds(other);
                }
            });
        }
    }

    /**
     * {@return an {@link AnnotatableCreator} consumer that copies all annotations from given {@code AnnotationTarget}}
     *
     * @param annotationTarget the {@link AnnotationTarget} from which to copy the annotations (must not be {@code null})
     * @param index the Jandex index that contains annotation classes (must not be {@code null})
     */
    public static Consumer<AnnotatableCreator> copyAnnotations(AnnotationTarget annotationTarget, IndexView index) {
        return creator -> {
            for (AnnotationInstance annotation : annotationTarget.annotations()) {
                addAnnotation(creator, annotation, index);
            }
        };
    }

    /**
     * {@return an {@link AnnotatableCreator} consumer that copies all annotations from given {@code Type}}
     *
     * @param type the {@link Type} from which to copy the annotations (must not be {@code null})
     * @param index the Jandex index that contains annotation classes (must not be {@code null})
     */
    public static Consumer<AnnotatableCreator> copyAnnotations(Type type, IndexView index) {
        return creator -> {
            for (AnnotationInstance annotation : type.annotations()) {
                addAnnotation(creator, annotation, index);
            }
        };
    }

    /**
     * Adds the given {@code annotation} to the given {@code annotatableCreator}.
     *
     * @param annotatableCreator the {@link AnnotatableCreator} to which to add the annotation (must not be {@code null})
     * @param annotation the {@link AnnotationInstance} to add (must not be {@code null})
     * @param index the Jandex index that contains annotation classes (must not be {@code null})
     */
    public static void addAnnotation(AnnotatableCreator annotatableCreator, AnnotationInstance annotation, IndexView index) {
        RetentionPolicy retention = annotation.runtimeVisible() ? RetentionPolicy.RUNTIME : RetentionPolicy.CLASS;
        annotatableCreator.addAnnotation(classDescOf(annotation.name()), retention, creatorFor(annotation, index));
    }

    private static Consumer<AnnotationCreator<Annotation>> creatorFor(AnnotationInstance annotation, IndexView index) {
        return creator -> {
            for (AnnotationValue member : annotation.values()) {
                switch (member.kind()) {
                    case BOOLEAN -> creator.add(member.name(), member.asBoolean());
                    case BYTE -> creator.add(member.name(), member.asByte());
                    case SHORT -> creator.add(member.name(), member.asShort());
                    case INTEGER -> creator.add(member.name(), member.asInt());
                    case LONG -> creator.add(member.name(), member.asLong());
                    case FLOAT -> creator.add(member.name(), member.asFloat());
                    case DOUBLE -> creator.add(member.name(), member.asDouble());
                    case CHARACTER -> creator.add(member.name(), member.asChar());
                    case STRING -> creator.add(member.name(), member.asString());
                    case CLASS -> creator.add(member.name(), classDescOf(member.asClass()));
                    case ENUM -> creator.add(member.name(), classDescOf(member.asEnumType()), member.asEnum());
                    case NESTED -> creator.add(member.name(), classDescOf(member.asNested().name()),
                            creatorFor(member.asNested(), index));
                    case ARRAY -> {
                        switch (member.componentKind()) {
                            case BOOLEAN -> creator.addArray(member.name(), member.asBooleanArray());
                            case BYTE -> creator.addArray(member.name(), member.asByteArray());
                            case SHORT -> creator.addArray(member.name(), member.asShortArray());
                            case INTEGER -> creator.addArray(member.name(), member.asIntArray());
                            case LONG -> creator.addArray(member.name(), member.asLongArray());
                            case FLOAT -> creator.addArray(member.name(), member.asFloatArray());
                            case DOUBLE -> creator.addArray(member.name(), member.asDoubleArray());
                            case CHARACTER -> creator.addArray(member.name(), member.asCharArray());
                            case STRING -> creator.addArray(member.name(), member.asStringArray());
                            case CLASS -> {
                                Type[] in = member.asClassArray();
                                ClassDesc[] out = new ClassDesc[in.length];
                                for (int i = 0; i < in.length; i++) {
                                    out[i] = classDescOf(in[i]);
                                }
                                creator.addArray(member.name(), out);
                            }
                            case ENUM -> {
                                DotName[] array = member.asEnumTypeArray();
                                assert array.length > 0;
                                ClassDesc enumType = classDescOf(array[0]);
                                creator.addArray(member.name(), enumType, member.asEnumArray());
                            }
                            case NESTED -> {
                                AnnotationInstance[] array = member.asNestedArray();
                                assert array.length > 0;
                                ClassDesc nestedType = classDescOf(array[0].name());
                                List<Consumer<AnnotationCreator<Annotation>>> creators = new ArrayList<>(array.length);
                                for (AnnotationInstance nested : array) {
                                    creators.add(creatorFor(nested, index));
                                }
                                creator.addArray(member.name(), nestedType, creators);
                            }
                            case UNKNOWN -> {
                                // empty array -- the only place where we need the `index`
                                ClassInfo annotationClass = index.getClassByName(annotation.name());
                                if (annotationClass == null) {
                                    throw new IllegalArgumentException("Given index does not contain " + annotation.name());
                                }
                                MethodInfo memberMethod = annotationClass.method(member.name());
                                assert memberMethod.returnType().kind() == Type.Kind.ARRAY;
                                Type type = memberMethod.returnType().asArrayType().elementType();
                                if (PrimitiveType.BOOLEAN.equals(type)) {
                                    creator.addArray(member.name(), new boolean[0]);
                                } else if (PrimitiveType.BYTE.equals(type)) {
                                    creator.addArray(member.name(), new byte[0]);
                                } else if (PrimitiveType.SHORT.equals(type)) {
                                    creator.addArray(member.name(), new short[0]);
                                } else if (PrimitiveType.INT.equals(type)) {
                                    creator.addArray(member.name(), new int[0]);
                                } else if (PrimitiveType.LONG.equals(type)) {
                                    creator.addArray(member.name(), new long[0]);
                                } else if (PrimitiveType.FLOAT.equals(type)) {
                                    creator.addArray(member.name(), new float[0]);
                                } else if (PrimitiveType.DOUBLE.equals(type)) {
                                    creator.addArray(member.name(), new double[0]);
                                } else if (PrimitiveType.CHAR.equals(type)) {
                                    creator.addArray(member.name(), new char[0]);
                                } else if (DotName.STRING_NAME.equals(type.name())) {
                                    creator.addArray(member.name(), new String[0]);
                                } else if (DotName.CLASS_NAME.equals(type.name())) {
                                    creator.addArray(member.name(), new Class[0]);
                                } else {
                                    assert type.kind() == Type.Kind.CLASS;
                                    ClassInfo clazz = index.getClassByName(type.name());
                                    if (clazz.isEnum()) {
                                        creator.addArray(member.name(), classDescOf(clazz), new String[0]);
                                    } else if (clazz.isAnnotation()) {
                                        creator.addArray(member.name(), classDescOf(clazz), List.of());
                                    } else {
                                        throw new IllegalArgumentException("Unknown type of empty array: "
                                                + memberMethod + " at " + memberMethod.declaringClass());
                                    }
                                }
                            }
                            default -> throw impossibleSwitchCase(member.kind());
                        }
                    }
                    default -> throw impossibleSwitchCase(member.kind());
                }
            }
        };
    }

    /**
     * {@return an enum constant corresponding to the given Jandex {@link FieldInfo}}
     *
     * @param enumConstant the Jandex enum constant (must not be {@code null})
     * @throws IllegalArgumentException if the {@code enumConstant} is not an actual enum constant
     */
    public static Const constOfEnum(FieldInfo enumConstant) {
        if (!enumConstant.isEnumConstant()) {
            throw new IllegalArgumentException("Not an enum constant: " + enumConstant);
        }
        return Const.of(Enum.EnumDesc.of(classDescOf(enumConstant.declaringClass()), enumConstant.name()));
    }

    /**
     * Copies all modifiers (including the access level) from the given Jandex {@code clazz}
     * to the given {@link ClassCreator creator}.
     *
     * @param clazz the Jandex class (must not be {@code null})
     * @param creator the Gizmo class creator (must not be {@code null})
     */
    public static void copyModifiers(ClassInfo clazz, ClassCreator creator) {
        copyModifiers(clazz.flags(), creator);
    }

    /**
     * Copies all modifiers (including the access level) from the given Jandex {@code method}
     * to the given {@link MethodCreator creator}.
     *
     * @param method the Jandex method (must not be {@code null})
     * @param creator the Gizmo method creator (must not be {@code null})
     */
    public static void copyModifiers(MethodInfo method, MethodCreator creator) {
        copyModifiers(method.flags(), creator);
    }

    /**
     * Copies all modifiers (including the access level) from the given Jandex {@code field}
     * to the given {@link FieldCreator creator}.
     *
     * @param field the Jandex field (must not be {@code null})
     * @param creator the Gizmo field creator (must not be {@code null})
     */
    public static void copyModifiers(FieldInfo field, FieldCreator creator) {
        copyModifiers(field.flags(), creator);
    }

    private static void copyModifiers(int flags, ModifiableCreator creator) {
        for (ModifierFlag flag : ModifierFlag.values()) {
            if (creator.supports(flag) && (flags & flag.mask()) == flag.mask()) {
                creator.addFlag(flag);
            }
        }
        creator.setAccess(AccessLevel.of(flags));
    }
}
