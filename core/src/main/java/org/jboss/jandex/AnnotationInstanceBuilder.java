package org.jboss.jandex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder for {@link AnnotationInstance}. Instances of the builder are not reusable.
 * <p>
 * Expected usage is: call {@code add()} as many times as required to add annotation members,
 * and then call {@code build()} to create an {@code AnnotationInstance} without target, or
 * {@code buildWithTarget()} to create an {@code AnnotationInstance} with target.
 * Attempt to {@code add()} a member with the same name multiple times leads to an exception.
 * <p>
 * This builder does <em>not</em> check whether the annotation type declares a member with given
 * name or whether that member declares a default value.
 */
public final class AnnotationInstanceBuilder {
    private static final String VALUE = "value";

    private final DotName annotationType;
    private final boolean runtimeVisible;
    private final List<AnnotationValue> values = new ArrayList<>();
    private final Set<String> alreadyAdded = new HashSet<>();

    AnnotationInstanceBuilder(DotName annotationType, boolean runtimeVisible) {
        this.annotationType = annotationType;
        this.runtimeVisible = runtimeVisible;
    }

    /**
     * Adds a pre-defined annotation member whose name and value are defined by given {@code value}.
     *
     * @param value the pre-defined annotation member, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(AnnotationValue value) {
        if (!alreadyAdded.add(value.name())) {
            throw new IllegalArgumentException("Annotation member '" + value.name() + "' already added");
        }

        values.add(value);
        return this;
    }

    /**
     * Adds all pre-defined annotation members whose names and values are defined by given {@code values}.
     *
     * @param values the pre-defined annotation members, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder addAll(List<AnnotationValue> values) {
        for (AnnotationValue value : values) {
            add(value);
        }
        return this;
    }

    // ---

    /**
     * Adds a boolean-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the boolean value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, boolean value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createBooleanValue(name, value));
        return this;
    }

    /**
     * Adds a boolean array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the boolean array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, boolean[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createBooleanValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a byte-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the byte value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, byte value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createByteValue(name, value));
        return this;
    }

    /**
     * Adds a byte array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the byte array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, byte[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createByteValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a short-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the short value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, short value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createShortValue(name, value));
        return this;
    }

    /**
     * Adds a short array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the short array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, short[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createShortValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds an int-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the int value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, int value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createIntegerValue(name, value));
        return this;
    }

    /**
     * Adds an int array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the int array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, int[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createIntegerValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a long-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the long value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, long value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createLongValue(name, value));
        return this;
    }

    /**
     * Adds a long array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the long array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, long[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createLongValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a float-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the float value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, float value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createFloatValue(name, value));
        return this;
    }

    /**
     * Adds a float array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the float array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, float[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createFloatValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a double-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the double value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, double value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createDoubleValue(name, value));
        return this;
    }

    /**
     * Adds a double array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the double array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, double[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createDoubleValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a char-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the char value
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, char value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createCharacterValue(name, value));
        return this;
    }

    /**
     * Adds a char array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the char array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, char[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createCharacterValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a String-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the String value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createStringValue(name, value));
        return this;
    }

    /**
     * Adds a String array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the String array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, String[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createStringValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds an enum-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the enum value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Enum<?> value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        DotName enumTypeName = DotName.createSimple(value.getDeclaringClass().getName());
        String enumValue = value.name();
        this.values.add(AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    /**
     * Adds an enum array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the enum array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Enum<?>[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            DotName enumTypeName = DotName.createSimple(values[i].getDeclaringClass().getName());
            String enumValue = values[i].name();
            array[i] = AnnotationValue.createEnumValue(name, enumTypeName, enumValue);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds an enum-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param enumType the enum type, must not be {@code null}
     * @param enumValue name of the enum constant, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Class<? extends Enum<?>> enumType, String enumValue) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        DotName enumTypeName = DotName.createSimple(enumType.getName());
        this.values.add(AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    /**
     * Adds an enum array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param enumType the enum type, must not be {@code null}
     * @param enumValues names of the enum constants, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Class<? extends Enum<?>> enumType, String[] enumValues) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[enumValues.length];
        DotName enumTypeName = DotName.createSimple(enumType.getName());
        for (int i = 0; i < enumValues.length; i++) {
            array[i] = AnnotationValue.createEnumValue(name, enumTypeName, enumValues[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds an enum-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param enumType the enum type, must not be {@code null}
     * @param enumValue name of the enum constant, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, ClassInfo enumType, String enumValue) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        DotName enumTypeName = enumType.name();
        this.values.add(AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    /**
     * Adds an enum array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param enumType the enum type, must not be {@code null}
     * @param enumValues names of the enum constants, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, ClassInfo enumType, String[] enumValues) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[enumValues.length];
        DotName enumTypeName = enumType.name();
        for (int i = 0; i < enumValues.length; i++) {
            array[i] = AnnotationValue.createEnumValue(name, enumTypeName, enumValues[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a class-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the class value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Class<?> value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        DotName className = DotName.createSimple(value.getName());
        Type clazz = Type.create(className, Type.Kind.CLASS);
        this.values.add(AnnotationValue.createClassValue(name, clazz));
        return this;
    }

    /**
     * Adds a class array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, Class<?>[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            DotName className = DotName.createSimple(values[i].getName());
            Type clazz = Type.create(className, Type.Kind.CLASS);
            array[i] = AnnotationValue.createClassValue(name, clazz);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds a class-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the class value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, ClassInfo value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        DotName className = value.name();
        Type jandexClass = Type.create(className, Type.Kind.CLASS);
        this.values.add(AnnotationValue.createClassValue(name, jandexClass));
        return this;
    }

    /**
     * Adds a class array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, ClassInfo[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            DotName className = values[i].name();
            Type jandexClass = Type.create(className, Type.Kind.CLASS);
            array[i] = AnnotationValue.createClassValue(name, jandexClass);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    private void validateType(Type type) {
        if (type instanceof VoidType) {
            return;
        } else if (type instanceof PrimitiveType) {
            return;
        } else if (type instanceof ClassType) {
            return;
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = type.asArrayType();
            Type elementType = arrayType.component();
            while (elementType.kind() == Type.Kind.ARRAY) {
                elementType = elementType.asArrayType().component();
            }
            if (elementType instanceof PrimitiveType) {
                return;
            } else if (elementType instanceof ClassType) {
                return;
            }
        }

        throw new IllegalArgumentException("Type can't be present in annotation: " + type);
    }

    /**
     * Adds a class-valued annotation member with given {@code name}.
     * The {@code value} parameter may only be:
     * <ul>
     * <li>{@link VoidType};</li>
     * <li>{@link PrimitiveType};</li>
     * <li>{@link ClassType};</li>
     * <li>{@link ArrayType} whose element type is either {@link PrimitiveType} or {@link ClassType}.</li>
     * </ul>
     * Any other value results in an exception.
     *
     * @param name the member name, must not be {@code null}
     * @param value the class value, must not be {@code null}
     * @return this builder
     * @throws IllegalArgumentException if given type is invalid, as described above
     */
    public AnnotationInstanceBuilder add(String name, Type value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        validateType(value);
        this.values.add(AnnotationValue.createClassValue(name, value));
        return this;
    }

    /**
     * Adds a class array-valued annotation member with given {@code name}.
     * The {@code values} parameter may only include:
     * <ul>
     * <li>{@link VoidType};</li>
     * <li>{@link PrimitiveType};</li>
     * <li>{@link ClassType};</li>
     * <li>{@link ArrayType} whose element type is either {@link PrimitiveType} or {@link ClassType}.</li>
     * </ul>
     *
     * @param name the member name, must not be {@code null}
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     * @throws IllegalArgumentException if any given type is invalid, as described above
     */
    public AnnotationInstanceBuilder add(String name, Type[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        AnnotationValue[] array = new AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            validateType(values[i]);
            array[i] = AnnotationValue.createClassValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    /**
     * Adds an annotation-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param value the annotation value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, AnnotationInstance value) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        this.values.add(AnnotationValue.createNestedAnnotationValue(name, value));
        return this;
    }

    /**
     * Adds an annotation array-valued annotation member with given {@code name}.
     *
     * @param name the member name, must not be {@code null}
     * @param values the annotation array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder add(String name, AnnotationInstance[] values) {
        if (name == null) {
            throw new IllegalArgumentException("Annotation member name must be set");
        }
        if (!alreadyAdded.add(name)) {
            throw new IllegalArgumentException("Annotation member '" + name + "' already added");
        }

        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = AnnotationValue.createNestedAnnotationValue(name, values[i]);
        }
        this.values.add(AnnotationValue.createArrayValue(name, array));
        return this;
    }

    // ---

    /**
     * Adds a boolean-valued annotation member called {@code value}.
     *
     * @param value the boolean value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(boolean value) {
        return add(VALUE, value);
    }

    /**
     * Adds a boolean array-valued annotation member called {@code value}.
     *
     * @param values the boolean array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(boolean[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a byte-valued annotation member called {@code value}.
     *
     * @param value the byte value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(byte value) {
        return add(VALUE, value);
    }

    /**
     * Adds a byte array-valued annotation member called {@code value}.
     *
     * @param values the byte array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(byte[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a short-valued annotation member called {@code value}.
     *
     * @param value the short value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(short value) {
        return add(VALUE, value);
    }

    /**
     * Adds a short array-valued annotation member called {@code value}.
     *
     * @param values the short array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(short[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds an int-valued annotation member called {@code value}.
     *
     * @param value the int value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(int value) {
        return add(VALUE, value);
    }

    /**
     * Adds an int array-valued annotation member called {@code value}.
     *
     * @param values the int array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(int[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a long-valued annotation member called {@code value}.
     *
     * @param value the long value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(long value) {
        return add(VALUE, value);
    }

    /**
     * Adds a long array-valued annotation member called {@code value}.
     *
     * @param values the long array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(long[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a float-valued annotation member called {@code value}.
     *
     * @param value the float value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(float value) {
        return add(VALUE, value);
    }

    /**
     * Adds a float array-valued annotation member called {@code value}.
     *
     * @param values the float array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(float[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a double-valued annotation member called {@code value}.
     *
     * @param value the double value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(double value) {
        return add(VALUE, value);
    }

    /**
     * Adds a double array-valued annotation member called {@code value}.
     *
     * @param values the double array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(double[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a char-valued annotation member called {@code value}.
     *
     * @param value the char value
     * @return this builder
     */
    public AnnotationInstanceBuilder value(char value) {
        return add(VALUE, value);
    }

    /**
     * Adds a char array-valued annotation member called {@code value}.
     *
     * @param values the char array, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(char[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a String-valued annotation member called {@code value}.
     *
     * @param value the String value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(String value) {
        return add(VALUE, value);
    }

    /**
     * Adds a String array-valued annotation member called {@code value}.
     *
     * @param values the String array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(String[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds an enum-valued annotation member called {@code value}.
     *
     * @param value the enum value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Enum<?> value) {
        return add(VALUE, value);
    }

    /**
     * Adds an enum array-valued annotation member called {@code value}.
     *
     * @param values the enum array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Enum<?>[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds an enum-valued annotation member called {@code value}.
     *
     * @param enumType the enum type, must not be {@code null}
     * @param enumValue name of the enum constant, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Class<? extends Enum<?>> enumType, String enumValue) {
        return add(VALUE, enumType, enumValue);
    }

    /**
     * Adds an enum array-valued annotation member called {@code value}.
     *
     * @param enumType the enum type, must not be {@code null}
     * @param enumValues names of the enum constants, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Class<? extends Enum<?>> enumType, String[] enumValues) {
        return add(VALUE, enumType, enumValues);
    }

    /**
     * Adds an enum-valued annotation member called {@code value}.
     *
     * @param enumType the enum type, must not be {@code null}
     * @param enumValue name of the enum constant, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(ClassInfo enumType, String enumValue) {
        return add(VALUE, enumType, enumValue);
    }

    /**
     * Adds an enum array-valued annotation member called {@code value}.
     *
     * @param enumType the enum type, must not be {@code null}
     * @param enumValues names of the enum constants, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(ClassInfo enumType, String[] enumValues) {
        return add(VALUE, enumType, enumValues);
    }

    /**
     * Adds a class-valued annotation member called {@code value}.
     *
     * @param value the class value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Class<?> value) {
        return add(VALUE, value);
    }

    /**
     * Adds a class array-valued annotation member called {@code value}.
     *
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(Class<?>[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a class-valued annotation member called {@code value}.
     *
     * @param value the class value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(ClassInfo value) {
        return add(VALUE, value);
    }

    /**
     * Adds a class array-valued annotation member called {@code value}.
     *
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(ClassInfo[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds a class-valued annotation member called {@code value}.
     * The {@code value} parameter may only be:
     * <ul>
     * <li>{@link VoidType};</li>
     * <li>{@link PrimitiveType};</li>
     * <li>{@link ClassType};</li>
     * <li>{@link ArrayType} whose element type is either {@link PrimitiveType} or {@link ClassType}.</li>
     * </ul>
     *
     * @param value the class value, must not be {@code null}
     * @return this builder
     * @throws IllegalArgumentException if given type is invalid, as described above
     */
    public AnnotationInstanceBuilder value(Type value) {
        return add(VALUE, value);
    }

    /**
     * Adds a class array-valued annotation member called {@code value}.
     * The {@code values} parameter may only contain:
     * <ul>
     * <li>{@link VoidType};</li>
     * <li>{@link PrimitiveType};</li>
     * <li>{@link ClassType};</li>
     * <li>{@link ArrayType} whose element type is either {@link PrimitiveType} or {@link ClassType}.</li>
     * </ul>
     *
     * @param values the class array, must not be {@code null} or contain {@code null}
     * @return this builder
     * @throws IllegalArgumentException if any given type is invalid, as described above
     */
    public AnnotationInstanceBuilder value(Type[] values) {
        return add(VALUE, values);
    }

    /**
     * Adds an annotation-valued annotation member called {@code value}.
     *
     * @param value the annotation value, must not be {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(AnnotationInstance value) {
        return add(VALUE, value);
    }

    /**
     * Adds an annotation array-valued annotation member called {@code value}.
     *
     * @param values the annotation array, must not be {@code null} or contain {@code null}
     * @return this builder
     */
    public AnnotationInstanceBuilder value(AnnotationInstance[] values) {
        return add(VALUE, values);
    }

    // ---

    /**
     * Returns an {@link AnnotationInstance} that includes all annotation members defined by
     * previous method calls on this builder. The returned {@code AnnotationInstance} has no target
     * defined. After {@code build()} is called, this builder instance should be discarded.
     *
     * @return the built {@link AnnotationInstance}, never {@code null}
     */
    public AnnotationInstance build() {
        return AnnotationInstance.create(annotationType, runtimeVisible, null, values);
    }

    /**
     * Returns an {@link AnnotationInstance} that includes all annotation members defined by
     * previous method calls on this builder. The returned {@code AnnotationInstance} has given
     * {@code target}. After {@code buildWithTarget()} is called, this builder instance should be discarded.
     *
     * @return the built {@link AnnotationInstance}, never {@code null}
     */
    public AnnotationInstance buildWithTarget(AnnotationTarget target) {
        return AnnotationInstance.create(annotationType, runtimeVisible, target, values);
    }
}
