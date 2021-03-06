package ru.serge2nd.type;

import lombok.NonNull;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.String.join;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.nCopies;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;
import static ru.serge2nd.type.Classes.arrayClass;
import static ru.serge2nd.type.Classes.className;

/**
 * Discovering {@link Type} and extensions.
 */
public class Types {
    private Types() { throw errNotInstantiable(lookup()); }

    public static final Type[] NO_TYPES = new Type[0];

    public static <T> Class<T> rawClass(Type type) { return rawClass(type, null); }
    @SuppressWarnings("unchecked")
    public static <T> Class<T> rawClass(Type type, ClassLoader classLoader) {
        if (type instanceof Class)
            return (Class<T>)type;
        if (type instanceof ParameterizedType)
            return rawClass(((ParameterizedType)type).getRawType());

        int dims = 0;
        for (; type instanceof GenericArrayType; dims++) {
            type = ((GenericArrayType)type).getGenericComponentType();
        }
        if (dims > 0) return (Class<T>)arrayClass(rawClass(type), dims, classLoader);

        throw new IllegalArgumentException("unsupported type " + (type != null ? type.getTypeName() : null));
    }

    public static ParameterizedType makeParameterizedType(@NonNull Class<?> raw, @NonNull Type[] typeArgs, Type owner) {
        if (owner instanceof Class && (((Class<?>)owner).isArray() || ((Class<?>)owner).isPrimitive())
            || !(owner == null || owner instanceof Class || owner instanceof ParameterizedType))
            throw new IllegalArgumentException("incorrect owner type - " + owner.getClass().getName());

        return new ParameterizedType() {
            public Type   getRawType()             { return raw; }
            public Type[] getActualTypeArguments() { return typeArgs; }
            public Type   getOwnerType()           { return owner; }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof ParameterizedType)) return false;
                ParameterizedType other = (ParameterizedType)o;

                return Objects.equals(getRawType(), other.getRawType()) &&
                        Arrays.equals(getActualTypeArguments(), other.getActualTypeArguments()) &&
                        Objects.equals(getOwnerType(), other.getOwnerType());
            }
            @Override
            public int hashCode() {
                return Arrays.hashCode(typeArgs) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
            }
            @Override
            public String toString() {
                return className(raw, owner) + collect(typeArgs,
                        new StringJoiner(", ", "<", ">").setEmptyValue(""),
                        (sj, t) -> sj.add(t.getTypeName()));
            }
        };
    }

    public static GenericArrayType makeGenericArrayType(@NonNull Type component) {
        if (!(component instanceof Class
            || component instanceof ParameterizedType
            || component instanceof GenericArrayType
            || component instanceof TypeVariable))
            throw new IllegalArgumentException("incorrect kind of component - " + component.getClass().getName());

        return new GenericArrayType() {
            public Type getGenericComponentType() { return component; }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof GenericArrayType)) return false;

                GenericArrayType other = (GenericArrayType)o;
                return Objects.equals(getGenericComponentType(), other.getGenericComponentType());
            }
            @Override
            public int hashCode() { return Objects.hashCode(component); }
            @Override
            public String toString() {
                Type t = component; int dims = 1;
                for (; t instanceof GenericArrayType; dims++) {
                    t = ((GenericArrayType)t).getGenericComponentType();
                }
                return t.getTypeName() + join("", nCopies(dims, "[]"));
            }
        };
    }
}
