package ru.serge2nd.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.ReflectionUtils.getDeclaredConstructor;
import static org.springframework.util.ClassUtils.resolveClassName;
import static ru.serge2nd.stream.util.Collecting.accumulate;
import static ru.serge2nd.type.Types.NO_TYPES;

@TestInstance(Lifecycle.PER_CLASS)
class TypeWrapTest {
    static List<Arguments> typesProvider() { return asList(
        //        Raw type     Type args                                  Owner            Expected wrapped type
        arguments(Map.class  , null                                     , null           , wrap(Map.class)),
        arguments(Map.class  , NO_TYPES                                 , null           , wrap(Map.class)),
        arguments(Map.class  , types(Byte.class, Long.class)            , null           , wrap(Map.class, Byte.class, Long.class, null)),
        arguments(Entry.class, types(Byte.class, Long.class)            , Map.class      , wrap(Entry.class, Byte.class, Long.class, Map.class)),
        arguments(Entry.class, types(wrap(Byte.class), wrap(Long.class)), wrap(Map.class), wrap(Entry.class, Byte.class, Long.class, Map.class))); }
    @ParameterizedTest @MethodSource("typesProvider")
    <T> void testOf(Class<T> raw, Type[] typeArgs, Type owner, TypeWrap<T> expected) {
        assertEquals(expected, TypeWrap.of(raw, typeArgs, owner));
    }

    static List<Arguments> arrayTypesProvider() { return asList(
        //        Raw type     Dims  Type args                                  Owner            Expected wrapped type
        arguments(Map.class  , 0   , null                                     , null           , wrap(Map.class)),
        arguments(Map.class  , 1   , null                                     , null           , wrap(Map.class, 1)),
        arguments(Map.class  , 2   , types(Byte.class, Long.class)            , null           , wrap(Map.class, 2, Byte.class, Long.class, null)),
        arguments(Entry.class, 3   , types(Byte.class, Long.class)            , Map.class      , wrap(Entry.class, 3, Byte.class, Long.class, Map.class)),
        arguments(Entry.class, 1   , types(wrap(Byte.class), wrap(Long.class)), wrap(Map.class), wrap(Entry.class, 1, Byte.class, Long.class, Map.class))); }
    @ParameterizedTest @MethodSource("arrayTypesProvider")
    <T> void testOfArray(Class<T> raw, int dims, Type[] typeArgs, Type owner, TypeWrap<T> expected) {
        assertEquals(expected, TypeWrap.of(raw, dims, typeArgs, owner));
    }

    @Test void testIsRaw()          { assertTrue(wrap(Map.class).isRaw()); }
    @Test void testNotRaw()         { assertFalse(wrap(Map.class, (Type)null).isRaw()); }
    @Test void testGetTypeName()    { assertEquals(Map[].class.getTypeName(), wrap(Map[].class).getTypeName()); }
    @Test void testGetRawType()     { assertSame(Map.class, wrap(Map.class, (Type)null).getRawType()); }
    @Test void testTrivialRawType() { assertSame(Map[].class, wrap(Map[].class).getRawType()); }

    @Test void testGetActualTypeArguments()  { assertArrayEquals(new Type[]{Byte.class}, wrap(Map.class, Byte.class, null).getActualTypeArguments()); }
    @Test void testRawTypeArguments()        { assertEquals(NO_TYPES, wrap(Map.class).getActualTypeArguments()); }
    @Test void testGetOwner()                { assertSame(Map.class, wrap(Entry.class, Byte.class, Map.class).getOwnerType()); }
    @Test void testRawOwner()                { assertNull(wrap(Entry.class).getOwnerType()); }
    @Test void testGetGenericComponentType() { assertEquals(
                                                    parameterizedTestType(Map.class, Byte.class, null),
                                                    wrap(Map.class, 1, Byte.class, null).getGenericComponentType()); }
    @Test void testRawGenericComponentType() { assertNull(wrap(Map[].class).getGenericComponentType()); }

    @Test @SuppressWarnings("SimplifiableAssertion,EqualsWithItself")
    void testEqualToSelf()      { TypeWrap<?> t = wrap(Map.class); assertTrue(t.equals(t));}
    @Test @SuppressWarnings("SimplifiableAssertion,ConstantConditions")
    void testEqualToNull()      { assertFalse(wrap(Map.class).equals(null));}
    @Test @SuppressWarnings("SimplifiableAssertion,EqualsBetweenInconvertibleTypes")
    void testEqualToOtherType() { assertFalse(wrap(Map.class).equals(Map.class));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testEqualToOther()     { assertFalse(wrap(Map.class).equals(wrap(AbstractMap.class)));}

    static <T> TypeWrap<T> wrap(Class<?> raw, Type... types) {
        return wrap(parameterizedTestType(raw, types));
    }
    static <T> TypeWrap<T> wrap(Class<?> raw, int dims, Type... types) {
        if (types.length < 2) {
            return wrap(resolveClassName(accumulate(nCopies(dims, "[]"),
                    new StringBuilder(raw.getName()), StringBuilder::append).toString(), null));
        }
        Type t = parameterizedTestType(raw, types);
        while (dims-- > 0) t = arrayTestType(t);
        return wrap(t);
    }
    @SuppressWarnings("unchecked")
    static <T> TypeWrap<T> wrap(Type t) {
        return ReflectionUtils.newInstance(getDeclaredConstructor(TypeWrap.class), t);
    }
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    static ParameterizedType parameterizedTestType(Class<?> raw, Type... types) { return new ParameterizedType() {
        public Type   getRawType()             { return raw; }
        public Type[] getActualTypeArguments() { return copyOfRange(types, 0, types.length - 1); }
        public Type   getOwnerType()           { return types[types.length - 1]; }
        public boolean equals(Object obj)      {
            ParameterizedType other = (ParameterizedType)obj;
            return !(other instanceof TypeWrap) &&
                    Objects.equals(getRawType(), other.getRawType()) &&
                    Arrays.equals(getActualTypeArguments(), other.getActualTypeArguments()) &&
                    Objects.equals(getOwnerType(), other.getOwnerType());
        }
    };}
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    static GenericArrayType arrayTestType(Type component) { return new GenericArrayType() {
        public Type getGenericComponentType() { return component; }
        public boolean equals(Object obj)     {
            GenericArrayType other = (GenericArrayType)obj;
            return !(other instanceof TypeWrap) && component.equals(other.getGenericComponentType());
        }
    };}
    static Type[] types(Type... types) { return types; }
}