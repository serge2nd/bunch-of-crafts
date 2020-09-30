package ru.serge2nd.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.NoInstanceTest;
import ru.serge2nd.collection.Unmodifiable;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.stream.MapCollectorsTest.*;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;

@TestInstance(Lifecycle.PER_CLASS)
class MappingCollectorsTest implements NoInstanceTest<MappingCollectors> {
    static final BiFunction<Function<?, ?>, Integer, Collector<?, ?, ?>> TO_LIST = MappingCollectors::mapToList;
    static final BiFunction<Function<?, ?>, Integer, Collector<?, ?, ?>> TO_SET  = MappingCollectors::mapToSet;
    static final BiFunction<Function<?, Object[]>, Integer, Collector<?, ?, ?>> FLAT_TO_LIST = MappingCollectors::aFlatMapToList;
    static final BiFunction<Function<?, Object[]>, Integer, Collector<?, ?, ?>> FLAT_TO_SET  = MappingCollectors::aFlatMapToSet;

    //region mapToList(), mapToSet() tests

    static List<Arguments> toCollectionArgsProvider() { return asList(
        /*
        0 - collection supplier
        1 - collector to use
        2 - collecting options
        3 - expected result superclass
        */
        //        0                  1        2                        3
        arguments(s(ArrayList::new), TO_LIST, 0                      , ArrayList.class),
        arguments(s(ArrayList::new), TO_LIST, UNMODIFIABLE           , Unmodifiable.class),
        arguments(s(ArrayList::new), TO_LIST, UNMODIFIABLE | NON_NULL, Unmodifiable.class),
        arguments(s(HashSet::new)  , TO_SET , 0                      , HashSet.class),
        arguments(s(HashSet::new)  , TO_SET , UNMODIFIABLE           , Unmodifiable.class),
        arguments(s(HashSet::new)  , TO_SET , UNMODIFIABLE | NON_NULL, Unmodifiable.class)); }
    @ParameterizedTest
    @MethodSource("toCollectionArgsProvider")
    @SuppressWarnings("unchecked,rawtypes")
    void testToCollection(Supplier<Collection> supplier,
                          BiFunction<Function<?, ?>, Integer, Collector<LocalDate, ?, Collection<?>>> collector,
                          int opts, Class<?> expectedCls) {
        Collection<LocalDate> src = supplier.get(); src.addAll(asList(NOW, NOW.withYear(0)));
        Collection<?> expected = (Collection)(has(NON_NULL, opts)
                ? src.stream().map(GET_YEAR_OR_NULL).filter(Objects::nonNull).collect(toCollection(supplier))
                : src.stream().map(GET_YEAR_OR_NULL).collect(toCollection(supplier)));

        Collection<?> result = collect(src, collector.apply(GET_YEAR_OR_NULL, opts));
        assertThat(result, instanceOf(expectedCls), equalTo(expected));
    }
    //endregion

    //region aFlatMapToList(), aFlatMapToSet() tests

    static List<Arguments> flatToCollectionArgsProvider() { return asList(
        /*
        0 - collection supplier
        1 - collector to use
        2 - collecting options
        3 - expected result superclass
        */
        //        0                  1             2                        3
        arguments(s(ArrayList::new), FLAT_TO_LIST, 0                      , ArrayList.class),
        arguments(s(ArrayList::new), FLAT_TO_LIST, UNMODIFIABLE           , Unmodifiable.class),
        arguments(s(ArrayList::new), FLAT_TO_LIST, UNMODIFIABLE | NON_NULL, Unmodifiable.class),
        arguments(s(HashSet::new)  , FLAT_TO_SET , 0                      , HashSet.class),
        arguments(s(HashSet::new)  , FLAT_TO_SET , UNMODIFIABLE           , Unmodifiable.class),
        arguments(s(HashSet::new)  , FLAT_TO_SET , UNMODIFIABLE | NON_NULL, Unmodifiable.class)); }
    @ParameterizedTest
    @MethodSource("flatToCollectionArgsProvider")
    @SuppressWarnings("unchecked,rawtypes")
    void testFlatToCollection(Supplier<Collection> supplier,
                              BiFunction<Function<?, ?>, Integer, Collector<Integer, ?, Collection<?>>> collector,
                              int opts, Class<?> expectedCls) {
        Collection<Integer> src = supplier.get(); src.addAll(asList(0, 1, -5));
        Collection<?> expected = (Collection)(has(NON_NULL, opts)
                ? src.stream().map(ABS_ARRAY_OR_NULL).filter(a -> a != null && a[0] != null).flatMap(Stream::of).collect(toCollection(supplier))
                : src.stream().map(ABS_ARRAY_OR_NULL).filter(Objects::nonNull).flatMap(Stream::of).collect(toCollection(supplier)));

        Collection<?> result = collect(src, collector.apply(ABS_ARRAY_OR_NULL, opts));
        assertThat(result, instanceOf(expectedCls), equalTo(expected));
    }
    //endregion

    @Test void testToStr() {
        assertEquals(
                "{null;5}",
                collect(MappingCollectors.mapToStr(ABS_OR_NULL, ()->new StringJoiner(";", "{", "}"), 0),
                        0, -5));
    }
    @Test void testNonNullToStr() {
        assertEquals(
                "{5}",
                collect(MappingCollectors.mapToStr(ABS_OR_NULL, ()->new StringJoiner(";", "{", "}"), NON_NULL),
                        0, -5));
    }
    @Test void testFlatToStr() {
        assertEquals(
                "{null;5}",
                collect(MappingCollectors.aFlatMapToStr(ABS_ARRAY_OR_NULL, ()->new StringJoiner(";", "{", "}"), 0),
                        0, 1, -5));
    }
    @Test void testFlatNonNullToStr() {
        assertEquals(
                "{5}",
                collect(MappingCollectors.aFlatMapToStr(ABS_ARRAY_OR_NULL, ()->new StringJoiner(";", "{", "}"), NON_NULL),
                        0, 1, -5));
    }

    static Supplier<Collection<?>> s(Supplier<Collection<?>> s) { return s; }

    static UnaryOperator<Integer>       ABS_OR_NULL       = i -> i != 0 ? abs(i) : null;
    static Function<Integer, Integer[]> ABS_ARRAY_OR_NULL = i -> i == 0 ? null : new Integer[] {i == 1 ? null : abs(i)};
}