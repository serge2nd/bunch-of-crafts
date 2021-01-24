package ru.serge2nd.collection;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.test.util.ToRun;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.stream.MappingCollectors.mapToList;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.ObjectAssist.flatArray;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.equalTo;
import static ru.serge2nd.test.match.CommonMatch.fails;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;
import static ru.serge2nd.test.match.CommonMatch.sameAs;
import static ru.serge2nd.test.match.CommonMatch.sameClass;
import static ru.serge2nd.test.match.CommonMatch.unsupported;

@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("RedundantUnmodifiable")
public class HardPropertiesTest {
    public static final Map<Object, Object> UNMOD_MAP = unmodifiableMap(emptyMap());
    static final LocalDateTime DT = LocalDateTime.now();
    static final BiConsumer<?, ?> ASSERT_SAME = Assertions::assertSame;
    static final BiConsumer<?, ?> ASSERT_EQUALS = Assertions::assertEquals;

    @Test
    void testEmpty() { assertSame(emptyMap(), HardProperties.EMPTY.toMap());}

    //region properties() tests

    static List<Arguments> keyValSequenceProvider() { return collect(new Object[][][] {
        {},
        {{"k0", null}},
        {{"k0", "v0"}},
        {{"k0", "v0"}, {"dt", DT}},
        {{"k0", "v0"}, {"dt", DT}, {"nil", null}}}, mapToList(keyValSeq ->

        //        Key-value pairs       Expected properties as a map
        arguments(flatArray(keyValSeq), collect(keyValSeq, toMap(p -> p[0], p -> p[1], 0))), 0)); }

    @ParameterizedTest @MethodSource("keyValSequenceProvider")
    void testProperties(Object[] keyValSeq, Map<String, Object> expected) {
        assertEquals(expected, properties(keyValSeq));
    }
    @Test
    void testIncorrectArgs()  {
        assertForMany(illegalArgument(),
        () -> properties("k0"),
        () -> properties(null, "nil"),
        () -> properties(5, "five"));
    }
    //endregion

    //region from() tests

    @SuppressWarnings("unchecked,rawtypes")
    static List<Arguments> mapsProvider() { return asList(
        /*
        0 - assertion to evaluate on the result
        1 - expected result
        2 - maps to create properties from
        */
        //        0              1                         2
        arguments(ASSERT_SAME  , HardProperties.EMPTY    , new Map[] {}),
        arguments(ASSERT_SAME  , HardProperties.EMPTY    , new Map[] {null}),
        arguments(ASSERT_SAME  , HardProperties.EMPTY    , new Map[] {emptyMap(), emptyMap()}),
        arguments(ASSERT_SAME  , HardProperties.EMPTY    , new Map[] {singletonMap(null, "nil")}),
        arguments(ASSERT_SAME  , HardProperties.EMPTY    , new Map[] {singletonMap(DT, "now")}),
        arguments(ASSERT_EQUALS, singletonMap("dt", DT)  , new Map[] {new HashMap() {{put(5, "five"); put("dt", DT);}}}),
        arguments(ASSERT_EQUALS, singletonMap("k0", "v0"), new Map[] {new HashMap() {{put("k0", "v0"); put(null, "nil");}}}),
        arguments(ASSERT_EQUALS, new HashMap() {{put("k0", "v0"); put("dt", DT);}},
                                                           new Map[] {singletonMap("k0", "v0"), singletonMap("dt", DT)})); }
    @ParameterizedTest @MethodSource("mapsProvider")
    void testFrom(BiConsumer<Object, Object> assertion, Map<?, ?> expected, Map<?, ?>[] maps) {
        assertion.accept(expected, HardProperties.from(maps));
    }
    //endregion

    //region of() tests

    @Test @SuppressWarnings("unchecked,rawtypes")
    void testOf() {
        Map<?, ?> map = new HashMap() {{ put(5, "five"); }};

        HardProperties result = HardProperties.of(map);
        assertThat(
        result.toMap()               , sameClass(UNMOD_MAP),
        getField(result.toMap(), "m"), sameAs(map));
    }
    @Test
    void testOfEmptyMap()          { assertSame(HardProperties.EMPTY, HardProperties.of(emptyMap())); }
    @Test
    void testOfEmptySortedMap()    { assertSame(HardProperties.EMPTY, HardProperties.of(emptyMap())); }
    @Test
    void testOfEmptyNavigableMap() { assertSame(HardProperties.EMPTY, HardProperties.of(emptyMap())); }
    //endregion

    //region Delegated java.util.Map methods tests

    @SuppressWarnings("unchecked,ConstantConditions,EqualsBetweenInconvertibleTypes")
    static List<Arguments> mapDelegatesProvider() { return asList(
        arguments("toString"     , f(Map::toString)),
        arguments("hashCode"     , f(Map::hashCode)),
        arguments("size"         , f(Map::size)),
        arguments("isEmpty"      , f(Map::isEmpty)),
        arguments("equals"       , f(m -> m.equals(K))),
        arguments("get"          , f(m -> m.get(K))),
        arguments("getOrDefault" , f(m -> m.getOrDefault(K, "z"))),
        arguments("containsKey"  , f(m -> m.containsKey(K))),
        arguments("containsValue", f(m -> m.containsValue(V))),
        arguments("keySet"       , f(Map::keySet)),
        arguments("values"       , f(Map::values)),
        arguments("entrySet"     , f(Map::entrySet)),
        arguments("forEach"      , a(m -> m.forEach(A)))); }
    @SuppressWarnings("unchecked,rawtypes")
    @ParameterizedTest(name = "{0}") @MethodSource("mapDelegatesProvider")
    void testMapDelegates(String method, Object op) { withCalled((map, props, called) -> {
        if (op instanceof Function) {
            Function<Map, ?> f = (Function<Map, ?>)op;
            assertEquals(f.apply(map), f.apply(props));
        } else {
            ((Consumer)op).accept(map);
            ((Consumer)op).accept(props);
        }
        assertCalledOnly(method, 2, called);
    });}
    //endregion

    //region getProperty(), contains, keys(), elements(), clone() tests

    @Test void testGetProperty() {
        assertEquals("v0", properties("k0", "v0", "dt", DT).getProperty("k0"));
    }
    @Test void testGetNonStringProperty() { withCalled((map, props, c) ->
        assertThat(()->props.getProperty(K), fails(ClassCastException.class), () ->
        assertCalledOnly("get", 1, c)));
    }
    @Test void testGetPropertyDefault() { withCalled((map, props, c) ->
        assertThat(props.getProperty(K, "xyz"), equalTo("xyz"), () ->
        assertCalledOnly("getOrDefault", 1, c)));
    }
    @Test void testContains() { withCalled((map, props, c) ->
        assertThat(props.contains(V), equalTo(true), () ->
        assertCalledOnly("containsValue", 1, c)));
    }
    @Test void testPropertyNames() { withCalled((map, props, c) ->
        assertThat(list(props.propertyNames()), contains(K), () ->
        assertCalledOnly("keySet", 1, c)));
    }
    @Test void testKeys() { withCalled((map, props, c) ->
        assertThat(list(props.keys()), contains(K), () ->
        assertCalledOnly("keySet", 1, c)));
    }
    @Test void testElements() { withCalled((map, props, c) ->
        assertThat(list(props.elements()), contains(V), () ->
        assertCalledOnly("values", 1, c)));
    }
    @Test void testClone() {
        assertThat(properties()::clone, fails(CloneNotSupportedException.class));
    }
    //endregion

    //region Unsupported operations tests

    @SuppressWarnings("ResultOfMethodCallIgnored,ConstantConditions")
    static List<Arguments> unsupportedMapDelegatesProvider() { return asList(
        arguments("put"             , c(p -> p.put(null, null))),
        arguments("putAll"          , c(p -> p.putAll(null))),
        arguments("remove"          , c(p -> p.remove(null))),
        arguments("remove2"         , c(p -> p.remove(null, null))),
        arguments("clear"           , c(HardProperties::clear)),
        arguments("replace"         , c(p -> p.replace(null, null))),
        arguments("replace2"        , c(p -> p.replace(null, null, null))),
        arguments("replaceAll"      , c(p -> p.replaceAll(null))),
        arguments("putIfAbsent"     , c(p -> p.putIfAbsent(null, null))),
        arguments("computeIfAbsent" , c(p -> p.computeIfAbsent(null, null))),
        arguments("computeIfPresent", c(p -> p.computeIfPresent(null, null))),
        arguments("compute"         , c(p -> p.compute(null, null))),
        arguments("merge"           , c(p -> p.merge(null, null, null)))); }
    @ParameterizedTest(name = "{0}") @MethodSource("unsupportedMapDelegatesProvider")
    void testUnsupportedMapDelegates(String method, Consumer<HardProperties> call) { withCalled((map, props, c) ->
        assertThat(()->call.accept(props), unsupported(), ()->assertNoCalls(c)));
    }

    @Test void testUnsupported() { withCalled((map, props, c) ->
        assertForMany(unsupported(), noCalls(c),
        () -> props.setProperty(null, null),
        () -> props.load((Reader)null),
        () -> props.load((InputStream)null),
        () -> props.loadFromXML(null),
        () -> props.save(null, null),
        () -> props.store((Writer)null, null),
        () -> props.store((OutputStream)null, null),
        () -> props.storeToXML(null, null),
        () -> props.storeToXML(null, null, (String)null),
        () -> props.storeToXML(null, null, (Charset)null),
        () -> props.list((PrintStream)null),
        () -> props.list((PrintWriter)null)));
    }
    //endregion

    //region Serialization tests

    @Test void testSerialize() {
        Map<String, Object> expected = new HashMap<String, Object>() {{
            put("dt", DT);
            put("abc", "xyz");
        }};
        assertEquals(expected, deserialize(serialize(HardProperties.from(expected))));
    }
    @Test void testSerializeSingleton() {
        assertEquals(singletonMap("abc", "def"), deserialize(serialize(properties("abc", "def"))));
    }
    @Test void testSerializeEmpty() {
        assertSame(HardProperties.EMPTY, deserialize(serialize(HardProperties.EMPTY)));
    }
    //endregion

    @Test @SuppressWarnings("ConstantConditions")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> properties((Object[])null),
        () -> HardProperties.from((Map<?, ?>[])null),
        () -> HardProperties.of(null));
    }

    @SuppressWarnings("unchecked,rawtypes,EqualsWhichDoesntCheckParameterClass")
    static void withCalled(TriConsumer<Map<Object, Object>, HardProperties, Map<String, Integer>> test) {
        Map<String, Integer> called = new HashMap<>();
        class Called {Called(String name) { called.merge(name, 1, Math::addExact); }}

        Map<Object, Object> map = new Map<Object, Object>() {
            public boolean equals(Object obj) { if (!K.equals(obj)) fail(); new Called("equals"); return true; }
            public int     hashCode()         { new Called("hashCode"); return V; }
            public String  toString()         { new Called("toString"); return K; }
            public int     size()             { new Called("size"); return V; }
            public boolean isEmpty()          { new Called("isEmpty"); return true; }

            public boolean containsKey(Object key)                       { if (!K.equals(key)) fail(); new Called("containsKey"); return true; }
            public boolean containsValue(Object value)                   { if (!V.equals(value)) fail(); new Called("containsValue"); return true; }
            public Object  get(Object key)                               { if (!K.equals(key)) fail(); new Called("get"); return V; }
            public Object  getOrDefault(Object key, Object defaultValue) { if (!K.equals(key)) fail(); new Called("getOrDefault"); return defaultValue; }

            public Set<Object>        keySet() { new Called("keySet"); return singleton(K); }
            public Collection<Object> values() { new Called("values"); return new ArrayList() {{add(V);}
                public boolean equals(Object o) { return super.equals(new ArrayList((Collection)o)); }
            }; }

            public Set<Entry<Object, Object>> entrySet() { new Called("entrySet"); return singleton(new SimpleEntry<>(K, V)); }
            public void forEach(BiConsumer<? super Object, ? super Object> action) { if (action != A) fail(); new Called("forEach"); }

            public Object  put(Object key, Object value)    { new Called("put"); return null; }
            public void    putAll(Map<?, ?> m)              { new Called("putAll"); }
            public Object  remove(Object key)               { new Called("remove"); return null; }
            public boolean remove(Object key, Object value) { new Called("remove2"); return false; }
            public void    clear()                          { new Called("clear"); }
            public Object  replace(Object key, Object value)                                  { new Called("replace"); return null; }
            public boolean replace(Object key, Object oldValue, Object newValue)              { new Called("replace2"); return false; }
            public void    replaceAll(BiFunction<? super Object, ? super Object, ?> function) { new Called("replaceAll"); }

            public Object putIfAbsent(Object key, Object value)                                                            { new Called("putIfAbsent"); return null; }
            public Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction)                         { new Called("computeIfAbsent"); return null; }
            public Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction)    { new Called("computeIfPresent"); return null; }
            public Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction)             { new Called("compute"); return null; }
            public Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) { new Called("merge"); return null; }
        };
        HardProperties props = HardProperties.of(map);

        test.accept(map, props, called);
    }
    static final String K = "abc";
    static final Integer V = 81;
    @SuppressWarnings("rawtypes") static final BiConsumer A = ($0, $1) -> {};

    static void assertCalledOnly(String method, int count, Map<String, Integer> called) {
        assertEquals(singletonMap(method, count), called);
    }
    static void assertNoCalls(Map<String, Integer> called) {
        assertThat(called, anEmptyMap());
    }

    static Matcher<ToRun> noCalls(Map<String, Integer> called) { return new BaseMatcher<ToRun>() {
        @Override
        public boolean matches(Object $)                                   { return called.isEmpty(); }
        @Override
        public void describeTo(Description description)                    { description.appendText("no calls"); }
        @Override
        public void describeMismatch(Object $, Description description) { description.appendText("was ").appendValue(called); }
    };}

    static Consumer<HardProperties> c(Consumer<HardProperties> c) { return c; }
    @SuppressWarnings("rawtypes")
    static Consumer<Map>            a(Consumer<Map> f)            { return f; }
    @SuppressWarnings("rawtypes")
    static <R> Function<Map, R>     f(Function<Map, R> f)         { return f; }

    interface TriConsumer<S, T, U> { void accept(S s, T t, U u); }
}