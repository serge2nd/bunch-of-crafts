package ru.serge2nd.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.NoInstanceTest;
import ru.serge2nd.misc.BitsResolver;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.collection.HardPropertiesTest.UNMOD_MAP;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.equalTo;
import static ru.serge2nd.test.match.CoreMatch.illegalState;
import static ru.serge2nd.test.match.CoreMatch.sameClass;

@TestInstance(Lifecycle.PER_CLASS)
class MapCollectorsTest implements NoInstanceTest<MapCollectors> {
    static final LocalDate NOW = LocalDate.now();
    static final Class<?> UNMOD_MAP_CLS = UNMOD_MAP.getClass();

    //region toMapKey(), toMap() tests

    static List<Arguments> toMapArgsProvider() { return asList(
        /*
        0 - source list
        1 - key mapping function
        2 - value mapping function
        3 - collecting options
        4 - expected result class
        */
        //        0                                 1                 2               3             4
        arguments(listHolder(NOW)                 , GET_YEAR        , TO_STR        , 0           , HashMap.class),
        arguments(listHolder(NOW)                 , GET_YEAR        , TO_STR        , UNMODIFIABLE, UNMOD_MAP_CLS),
        arguments(listHolder(NOW, NOW.withYear(0)), GET_YEAR_OR_NULL, TO_STR        , NON_NULL    , HashMap.class),
        arguments(listHolder(NOW, NOW.withYear(1)), GET_YEAR        , TO_STR_OR_NULL, NON_NULL_VAL, HashMap.class),
        arguments(listHolder(NOW, NOW.withYear(0), NOW.withYear(1))
                                                  , GET_YEAR_OR_NULL, TO_STR_OR_NULL, NON_NULL | NON_NULL_VAL, HashMap.class)); }
    @ParameterizedTest @MethodSource("toMapArgsProvider")
    void testToMapKey(List<LocalDate>[] src, Function<LocalDate, Integer> key, Object $, int opts, Class<?> expectedCls) {
        if (BitsResolver.has(NON_NULL_VAL, opts)) src[0] = src[0].stream().filter(d -> d.getYear() != 1).collect(toList());
        Map<Integer, LocalDate> result = collect(src[0], MapCollectors.toMapKey(key, opts));
        assertThat(result, sameClass(expectedCls), equalTo(singletonMap(key.apply(src[0].get(0)), src[0].get(0))));
    }
    @ParameterizedTest @MethodSource("toMapArgsProvider")
    void testToMapVal(List<LocalDate>[] src, Object $, Function<LocalDate, String> val, int opts, Class<?> expectedCls) {
        if (BitsResolver.has(NON_NULL, opts)) src[0] = src[0].stream().filter(d -> d.getYear() != 0).collect(toList());
        Map<LocalDate, String> result = collect(src[0], MapCollectors.toMap(val, opts));
        assertThat(result, sameClass(expectedCls), equalTo(singletonMap(src[0].get(0), val.apply(src[0].get(0)))));
    }
    @ParameterizedTest @MethodSource("toMapArgsProvider")
    void testToMap(List<LocalDate>[] src, Function<LocalDate, Integer> key, Function<LocalDate, String> val, int opts, Class<?> expectedCls) {
        Map<Integer, String> result = collect(src[0], MapCollectors.toMap(key, val, opts));
        assertThat(result, sameClass(expectedCls), equalTo(singletonMap(key.apply(src[0].get(0)), val.apply(src[0].get(0)))));
    }
    //endregion

    //region toValueFromKeyMap(), toKeyFromValueMap() tests

    static List<Arguments> toXFromYMapArgsProvider() { return asList(
        /*
        0 - source list
        1 - key mapping function
        2 - value mapping function
        3 - value-from-key mapping function
        4 - key-from-value mapping function
        5 - collecting options
        6 - expected result class
        */
        //        0                                 1                 2               3    4    5             6
        arguments(listHolder(NOW)                 , GET_YEAR        , TO_STR        , I2S, S2I, 0           , HashMap.class),
        arguments(listHolder(NOW)                 , GET_YEAR        , TO_STR        , I2S, S2I, UNMODIFIABLE, UNMOD_MAP_CLS),
        arguments(listHolder(NOW, NOW.withYear(0)), GET_YEAR_OR_NULL, TO_STR        , I2S, S2I, NON_NULL    , HashMap.class),
        arguments(listHolder(NOW, NOW.withYear(1)), GET_YEAR        , TO_STR_OR_NULL, I2S, S2I, NON_NULL_VAL, HashMap.class)); }

    @ParameterizedTest @MethodSource("toXFromYMapArgsProvider")
    void testToValueFromKeyMap(List<LocalDate>[] src, Function<LocalDate, Integer> key, Object $, Function<Integer, String> val, Object $0, int opts, Class<?> expectedCls) {
        Map<Integer, String> result = collect(src[0], MapCollectors.toValueFromKeyMap(key, val, opts));
        assertThat(result, sameClass(expectedCls), equalTo(singletonMap(key.apply(src[0].get(0)), val.apply(key.apply(src[0].get(0))))));
    }
    @ParameterizedTest @MethodSource("toXFromYMapArgsProvider")
    void testToKeyFromValueMap(List<LocalDate>[] src, Object $, Function<LocalDate, String> val, Object $0, Function<String, Integer> key, int opts, Class<?> expectedCls) {
        Map<Integer, String> result = collect(src[0], MapCollectors.toKeyFromValueMap(key, val, opts));
        assertThat(result, sameClass(expectedCls), equalTo(singletonMap(key.apply(val.apply(src[0].get(0))), val.apply(src[0].get(0)))));
    }
    //endregion


    @Test void testMergeUnique() { assertEquals(properties("k0", "v0", "k1", "v1"), MapCollectors.mergeUnique(new HashMap<>(singletonMap("k1", "v1")), singletonMap("k0", "v0"))); }
    @Test void testMergeNotUnique() { assertThat(()->MapCollectors.mergeUnique(new HashMap<>(singletonMap("k0", "v0")), singletonMap("k0", "v1")), illegalState()); }

    static List<?>[] listHolder(Object... elements) { return new List[] {asList(elements)}; }

    static final Function<LocalDate, Integer> GET_YEAR         = d -> d.getYear() != 1 ? d.getYear() : MIN_VALUE;
    static final Function<LocalDate, String> TO_STR            = d -> d.getYear() != 0 ? d.toString() : "";
    static final Function<LocalDate, Integer> GET_YEAR_OR_NULL = d -> d.getYear() != 0 ? d.getYear() : null;
    static final Function<LocalDate, String> TO_STR_OR_NULL    = d -> d.getYear() != 1 ? d.toString() : null;

    static final Function<String, Integer> S2I = s -> "".equals(s) ? null : Objects.hashCode(s);
    static final Function<Integer, String> I2S = i -> MIN_VALUE == i ? null : String.valueOf(i);
}