package ru.serge2nd;

import lombok.SneakyThrows;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.System.arraycopy;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.singleton;
import static org.springframework.util.ObjectUtils.isArray;
import static org.springframework.util.StringUtils.hasText;

public class ObjectAssist {
    private ObjectAssist() { throw errNotInstantiable(lookup()); }

    /** The system property containing the type which instances are returned by {@link #nullRefError(String)}. */
    public static final String               P_NULL_REF_ERROR_TYPE       = "nullRefErrorType";
    /** The default type which instances are returned by {@link #nullRefError(String)}. */
    public static Class<? extends Throwable> DEFAULT_NULL_REF_ERROR_TYPE = IllegalArgumentException.class;

    private static final MethodHandle NULL_REF_ERROR_CONSTRUCTOR = nullRefErrorConstructor();
    @SneakyThrows
    private static MethodHandle nullRefErrorConstructor() {
        String errorTypeName = System.getProperty(P_NULL_REF_ERROR_TYPE);
        Class<?> errorType = hasText(errorTypeName)
                ? Class.forName(errorTypeName)
                : IllegalArgumentException.class;
        if (!Throwable.class.isAssignableFrom(errorType)) throw new IllegalArgumentException("not throwable: " + errorType.getName());
        return lookup().findConstructor(errorType, methodType(void.class, String.class));
    }

    /**
     * Equivalent of {@link #nullSafe(Object, String, Function) nullSafe(x, msg, ObjectAssist::nullRefError)}.
     */
    @NonNull
    public static <X> X nullSafe(@Nullable X x, String msg) {
        return nullSafe(x, msg, ObjectAssist::nullRefError);
    }
    /**
     * If the first arg is null, throws a {@link Throwable} via {@link #throwSneaky(Throwable) throwSneaky()}
     * using the provided function as a source of throwable object with the passed message.
     */
    @NonNull
    public static <X> X nullSafe(@Nullable X x, String msg, Function<String, ? extends Throwable> err) {
        if (x == null) { throwSneaky(err.apply(msg)); return ignored(); }
        return x;
    }
    /**
     * If the first arg is null, throws a {@link Throwable} via {@link #throwSneaky(Throwable) throwSneaky()}
     * using the provided supplier as a source of throwable object.
     */
    @NonNull
    public static <X> X nullSafe(@Nullable X x, Supplier<? extends Throwable> err) {
        if (x == null) { throwSneaky(err.get()); return ignored(); }
        return x;
    }
    /**
     * Instantiates a {@link Throwable} with the given message
     * and the type specified in the {@link #P_NULL_REF_ERROR_TYPE} system property.
     */
    @SneakyThrows
    public static Throwable nullRefError(String msg) { return (Throwable)NULL_REF_ERROR_CONSTRUCTOR.invoke(msg); }

    /**
     * Throws a checked exception as if it is unchecked eliminating need in try-catch.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwSneaky(Throwable t) throws T { throw (T)t; }

    /**
     * Flattens the specified array into one-dimensional array with the same component type.
     * @param a original array
     * @param <S> type assumed to be an array
     * @param <T> destination type assumed to be one-dimensional array
     * @return flattened array
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T flatArray(S a) { return (T)fillFlatArray(a, newFlatArray(a)); }

    /**
     * Constructs one-dimensional array with length equal to the count of the elements (not sub-arrays) of the specified array.
     * No contents are copied.
     * @param a array from which the count of the elements is determined
     * @param <S> type assumed to be an array
     * @param <T> destination type assumed to be one-dimensional array
     * @return new "clean" array (no contents are copied) with length same as of the flattened given array
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T newFlatArray(S a) {
        if (!isArray(a)) throw new IllegalArgumentException("expected an array, got " + (a != null ? a.getClass().getName() : "null"));
        return (T)newInstance(component(a), countElems(a));
    }

    /**
     * Returns the component type of the given multi-dimensional array if exists.
     * @param obj any object or {@code null}
     * @return the component type if the argument is an array, the argument type if it's not an array or {@code null} if the argument is null.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static Class<?> component(Object obj) {
        if (obj == null) return null;
        if (!isArray(obj)) return obj.getClass();

        Class<?> cls = obj.getClass();
        while ((cls = cls.getComponentType()).isArray());

        return cls;
    }

    /**
     * Returns the number of dimensions of the given array.
     * @param obj any object or {@code null}
     * @return the number of dimensions or 0 if the argument is not an array
     */
    public static int nDims(Object obj) {
        if (!isArray(obj)) return 0;

        int dims = 1;
        Class<?> cls = obj.getClass();
        while ((cls = cls.getComponentType()).isArray())
            dims++;

        return dims;
    }

    /**
     * Counts the number of the elements (not sub-arrays) of the given array.
     * @param obj any object or {@code null}
     * @return the number of the elements or -1 if the argument is not an array
     */
    public static int nElems(Object obj) {
        if (!isArray(obj)) return -1;
        return countElems(obj);
    }

    @SuppressWarnings("unchecked,rawtypes,SuspiciousSystemArraycopy")
    private static Object fillFlatArray(Object a, Object flat) {
        assert isArray(a)    : "required an array as the first arg";
        assert isArray(flat) : "required an array as the second arg";
        if (!a.getClass().getComponentType().isArray()) {
            arraycopy(a, 0, flat, 0, getLength(a));
            return flat;
        }

        int i = 0;
        for (Deque stack = new LinkedList(singleton(a)); !stack.isEmpty(); a = stack.pop()) {
            int len = getLength(a);
            if (!a.getClass().getComponentType().isArray()) {
                arraycopy(a, 0, flat, i, len);
                i += len;
            } else {
                Object[] aa = (Object[])a;
                for (int j = 0; j < len; j++)
                    stack.push(aa[len - j - 1]);
            }
        }

        return flat;
    }

    @SuppressWarnings("unchecked,rawtypes")
    private static int countElems(Object a) {
        assert isArray(a) : "required an array as the first arg";
        if (!a.getClass().getComponentType().isArray())
            return getLength(a);

        int elems = 0;
        for (Deque stack = new LinkedList(singleton(a)); !stack.isEmpty(); a = stack.pop()) {
            if (!a.getClass().getComponentType().isArray())
                elems += getLength(a);
            else for (Object e : (Object[])a)
                stack.push(e);
        }

        return elems;
    }

    public static UnsupportedOperationException errNotInstantiable(Lookup $) {
        return errNotInstantiable($.lookupClass());
    }
    public static UnsupportedOperationException errNotInstantiable(Class<?> cls) {
        return new UnsupportedOperationException("non-instantiable: " + cls);
    }

    /**
     * Helps static analyzers to work properly in some cases.
     */
    @SuppressWarnings("unchecked")
    private static <V> V ignored() { return (V)IGNORED; }
    private static final Object IGNORED = new Object();
}
