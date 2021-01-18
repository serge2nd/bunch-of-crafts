package ru.serge2nd.type;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static lombok.AccessLevel.PRIVATE;
import static ru.serge2nd.ObjectAssist.nullSafe;
import static ru.serge2nd.stream.util.ArrayAccumulators.mapping;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.type.Types.NO_TYPES;
import static ru.serge2nd.type.Classes.arrayClass;
import static ru.serge2nd.type.Types.makeGenericArrayType;
import static ru.serge2nd.type.Types.makeParameterizedType;
import static ru.serge2nd.type.Types.rawClass;

/**
 * Generic {@link Type} wrapper.
 * @param <T> the type value assumed to be a literal of the wrapped type
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class TypeWrap<T> implements ParameterizedType, GenericArrayType {
    private final @NonNull @Getter Type type;

    //region Factory methods

    /**
     * Equivalent of {@link #of(Class, Type[], Type) of(raw, typeArgs, null)}.
     */
    public static <T> TypeWrap<T> of(Class<? super T> raw, Type... typeArgs) { return of(raw, typeArgs, null); }
    /**
     * Equivalent of {@link #of(Class, Type...)} for convenient usage via static import.
     */
    public static <T> TypeWrap<T> type(Class<? super T> raw, Type... typeArgs) { return of(raw, typeArgs); }

    /**
     * Equivalent of {@link #of(Class, int, Type[], Type, ClassLoader) of(raw, dims, typeArgs, null, null)}.
     */
    public static <T> TypeWrap<T> of(Class<? super T> raw, int dims, Type... typeArgs) { return of(raw, dims, typeArgs, null, null); }
    /**
     * Equivalent of {@link #of(Class, int, Type...)} for convenient usage via static import.
     */
    public static <T> TypeWrap<T> type(Class<? super T> raw, int dims, Type... typeArgs) { return of(raw, dims, typeArgs); }

    /**
     * Equivalent of {@link #of(Class, int, Type[], Type, ClassLoader) of(raw, dims, typeArgs, owner, null)}.
     */
    public static <T> TypeWrap<T> of(Class<? super T> raw, int dims, Type[] typeArgs, Type owner) { return of(raw, dims, typeArgs, owner, null); }
    /**
     * Equivalent of {@link #of(Class, int, Type[], Type)} for convenient usage via static import.
     */
    public static <T> TypeWrap<T> type(Class<? super T> raw, int dims, Type[] typeArgs, Type owner) { return of(raw, dims, typeArgs, owner); }

    /**
     * Wraps the given class with the type arguments as a parameterized type
     * or simply the class itself if type arguments array is null or empty.
     * @param raw the class
     * @param typeArgs the type arguments
     * @param owner the type owner (outer type)
     * @param <T> the type value assumed to be a literal of the wrapped type
     * @return the generic type wrapper
     * @see Types#makeParameterizedType(Class, Type[], Type)
     */
    public static <T> TypeWrap<T> of(Class<? super T> raw, Type[] typeArgs, Type owner) {
        if (noTypes(typeArgs)) return new TypeWrap<>(raw);
        return new TypeWrap<>(makeParameterizedType(raw, unwrap(typeArgs), unwrap(owner)));
    }
    /**
     * Equivalent of {@link #of(Class, Type[], Type)} for convenient usage via static import.
     */
    public static <T> TypeWrap<T> type(Class<? super T> raw, Type[] typeArgs, Type owner) { return of(raw, typeArgs, owner); }

    /**
     * Wraps the given class with the arguments and the number of dimensions
     * as generic array type or parameterized type or raw array class or the class itself.
     * @param raw the class
     * @param dims the number of array dimensions
     * @param typeArgs the type arguments
     * @param owner the type owner (outer type)
     * @param classLoader the class loader used to get the raw array class by name if necessary
     * @param <T> the type value assumed to be a literal of the wrapped type
     * @return the generic type wrapper
     * @see #of(Class, Type[], Type)
     * @see Types#makeParameterizedType(Class, Type[], Type)
     * @see Types#makeGenericArrayType(Type)
     */
    public static <T> TypeWrap<T> of(Class<? super T> raw, int dims, Type[] typeArgs, Type owner, ClassLoader classLoader) {
        if (dims < 1) return of(raw, typeArgs, owner);
        if (noTypes(typeArgs)) return new TypeWrap<>(arrayClass(raw, dims, classLoader));

        Type resultType = makeParameterizedType(raw, unwrap(typeArgs), unwrap(owner));
        while (dims-- > 0) resultType = makeGenericArrayType(resultType);

        return new TypeWrap<>(resultType);
    }
    /**
     * Equivalent of {@link #of(Class, int, Type[], Type, ClassLoader)} for convenient usage via static import.
     */
    public static <T> TypeWrap<T> type(Class<? super T> raw, int dims, Type[] typeArgs, Type owner, ClassLoader classLoader) {
        return of(raw, dims, typeArgs, owner, classLoader);
    }
    //endregion

    public boolean       isRaw()              { return type instanceof Class; }
    public static Type   unwrap(Type t)       { return t instanceof TypeWrap ? ((TypeWrap<?>)t).getType() : t; }
    public static Type[] unwrap(Type[] types) {
        if (noTypes(types)) return types;
        return collect(types, new Type[types.length], mapping(t -> unwrap(nullSafe(t, "null type"))));
    }

    //region Implementations

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;

        return type.equals(((TypeWrap<?>)obj).type);
    }
    @Override
    public int      hashCode()    { return type.hashCode(); }
    @Override
    public String   toString()    { return type.toString(); }
    @Override
    public String   getTypeName() { return type.getTypeName(); }
    @Override
    public Class<T> getRawType()  { return rawClass(type, null); }

    @Override
    public Type[] getActualTypeArguments() {
        return type instanceof ParameterizedType ? ((ParameterizedType)type).getActualTypeArguments() : NO_TYPES;
    }
    @Override
    public Type getOwnerType() {
        return type instanceof ParameterizedType ? ((ParameterizedType)type).getOwnerType() : null;
    }
    @Override
    public Type getGenericComponentType() {
        return type instanceof GenericArrayType ? ((GenericArrayType)type).getGenericComponentType() : null;
    }
    //endregion

    private static boolean noTypes(Type[] types) { return types == null || types.length == 0; }
}
