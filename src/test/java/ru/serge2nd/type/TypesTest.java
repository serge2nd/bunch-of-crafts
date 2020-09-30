package ru.serge2nd.type;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.NoInstanceTest;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.data.util.ReflectionUtils.getMethod;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.type.Types.NO_TYPES;
import static ru.serge2nd.collection.HardProperties.properties;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@TestInstance(Lifecycle.PER_CLASS)
public class TypesTest implements NoInstanceTest<Types> {
    public static final TestClassLoader TEST_CLS_LDR = new TestClassLoader();
    static final Map<String, Path> CLASS_PATHS = properties(
            E.class.getName(), Paths.get(toURI(E.class))
    ).toMap();

    //region rawClass() tests

    @Test
    void testTrivialRawClass()     { assertSame(Map[].class, Types.rawClass(Map[].class)); }
    @Test
    void testRawFromGeneric()      { assertSame(List.class, Types.rawClass(ANY_LIST)); }
    @Test
    void testRawFromGenericArray() { assertSame(E[].class, Types.rawClass(GENERIC_ARRAY)); }
    @Test
    void testRawFromGeneric2D()    { assertSame(E[][].class, Types.rawClass(GENERIC_2D_ARRAY)); }
    @Test
    void testRawFromGenericArrayWithCustomClassLoader() {
        Class<?> result = Types.rawClass(GENERIC_ARRAY, TEST_CLS_LDR);
        assertNotSame(E[].class, result);
        assertEquals(E.class.getName(), result.getComponentType().getName());
    }
    @Test
    void testRawFromGeneric2DWithCustomClassLoader() {
        Class<?> result = Types.rawClass(GENERIC_2D_ARRAY, TEST_CLS_LDR);
        assertNotSame(E[][].class, result);
        assertEquals(E.class.getName(), result.getComponentType().getComponentType().getName());
    }
    @Test
    void testRawFromUnsupported() {
        assertThrows(IllegalArgumentException.class, ()->Types.rawClass(TYPE_VAR));
    }
    //endregion

    //region makeParameterizedType() tests

    static List<Arguments> validTypesProvider() { return asList(
        //        Raw type      Type args                            Owner            Expected string representation
        arguments(String.class, NO_TYPES                           , null,            "java.lang.String"),
        arguments(Map.class   , new Type[] {Byte.class, Long.class}, null,            "java.util.Map<java.lang.Byte, java.lang.Long>"),
        arguments(E.class     , new Type[] {Long.class}            , TypesTest.class, E.class.getName() + "<java.lang.Long>")); }
    @ParameterizedTest
    @MethodSource("validTypesProvider")
    @SuppressWarnings("SimplifiableAssertion")
    void testMakeParameterizedType(Class<?> raw, Type[] typeArgs, Type owner, String strRepr) {
        ParameterizedType t = Types.makeParameterizedType(raw, typeArgs, owner);
        assertEach(() ->
        assertSame(raw     , t.getRawType()), () ->
        assertSame(typeArgs, t.getActualTypeArguments()), () ->
        assertSame(owner   , t.getOwnerType()), () ->
        assertTrue(t.equals(new ParameterizedType() {
            public Type   getRawType()             { return raw; }
            public Type[] getActualTypeArguments() { return typeArgs; }
            public Type   getOwnerType()           { return owner; }
        })), () ->
        assertEquals(raw.hashCode() ^ Arrays.hashCode(typeArgs) ^ Objects.hashCode(owner), t.hashCode()), () ->
        assertEquals(strRepr, t.toString()));
    }
    @Test
    void testInvalidParameterizedType() { assertEach(Stream.of(
        void.class, Map[].class, Types.makeGenericArrayType(Map.class))
        .map(owner -> ()-> assertThat(()->Types.makeParameterizedType(E.class, NO_TYPES, owner), illegalArgument())));
    }
    //endregion

    //region makeGenericArrayType()

    @Test
    @SuppressWarnings("SimplifiableAssertion")
    void testMakeGenericArrayType() {
        Type componentType = Types.makeParameterizedType(Map.class, new Type[] {Byte.class, Long.class}, null);
        GenericArrayType t = Types.makeGenericArrayType(componentType);
        assertEach(() ->
        assertSame(componentType, t.getGenericComponentType()), () ->
        assertTrue(t.equals((GenericArrayType)()->componentType)), () ->
        assertEquals(componentType.hashCode(), t.hashCode()), () ->
        assertEquals("java.util.Map<java.lang.Byte, java.lang.Long>[]", t.toString()));
    }
    @Test
    void testMultiDimGenericArrayToString() {
        Type componentType = Types.makeParameterizedType(Map.class, new Type[]{Byte.class, Long.class}, null);
        GenericArrayType t = Types.makeGenericArrayType(Types.makeGenericArrayType(Types.makeGenericArrayType(componentType)));
        assertEquals("java.util.Map<java.lang.Byte, java.lang.Long>[][][]", t.toString());
    }
    @Test
    void testInvalidGenericArrayType() {
        assertThat(()->Types.makeGenericArrayType(new WildcardType() {
            public Type[] getUpperBounds() { return NO_TYPES; }
            public Type[] getLowerBounds() { return NO_TYPES; }
        }), illegalArgument());
    }
    //endregion

    @Test @SuppressWarnings("ConstantConditions")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> Types.makeParameterizedType(null, NO_TYPES, null),
        () -> Types.makeParameterizedType(Map.class, null, null),
        () -> Types.makeGenericArrayType(null));
    }

    static final Type ANY_LIST         = getMethod(TypesTest.class, "anyList").get().getGenericReturnType();
    static final Type GENERIC_ARRAY    = getMethod(TypesTest.class, "genericArray").get().getGenericReturnType();
    static final Type GENERIC_2D_ARRAY = getMethod(TypesTest.class, "generic2DArray").get().getGenericReturnType();
    static final Type TYPE_VAR         = getMethod(E.class        , "f").get().getGenericReturnType();
    static List<?>    anyList()        { return null; }
    static E<?>[]     genericArray()   { return null; }
    static E<?>[][]   generic2DArray() { return null; }

    @SneakyThrows @SuppressWarnings("ConstantConditions")
    static URI toURI(Class<?> cls) {
        return currentThread().getContextClassLoader().getResource(toClassPath(cls)).toURI();
    }
    static String toClassPath(Class<?> cls) {
        return cls.getName().replace('.', '/') + ".class";
    }

    static class TestClassLoader extends ClassLoader {
        @SneakyThrows
        protected Class<?> loadClass(String name, boolean b) {
            Path path = CLASS_PATHS.get(name);
            if (path != null) {
                byte[] classBytes = readAllBytes(path);
                return defineClass(name, classBytes, 0, classBytes.length);
            }
            return super.loadClass(name, b);
        }
    }

    public static class E<R> { R f() { return null; } }
}