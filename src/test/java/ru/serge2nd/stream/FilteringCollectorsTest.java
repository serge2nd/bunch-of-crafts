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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toCollection;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.stream.MapCollectorsTest.NOW;
import static ru.serge2nd.stream.MappingCollectorsTest.s;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;

@TestInstance(Lifecycle.PER_CLASS)
class FilteringCollectorsTest implements NoInstanceTest<FilteringCollectors> {
    static final BiFunction<Predicate<?>, Integer, Collector<?, ?, ?>> TO_LIST = FilteringCollectors::filterToList;
    static final BiFunction<Predicate<?>, Integer, Collector<?, ?, ?>> TO_SET  = FilteringCollectors::filterToSet;

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
    @ParameterizedTest @MethodSource("toCollectionArgsProvider")
    @SuppressWarnings("unchecked,rawtypes")
    void testToCollection(Supplier<Collection> supplier,
                          BiFunction<Predicate<?>, Integer, Collector<LocalDate, ?, Collection<?>>> collector,
                          int opts, Class<?> expectedCls) {
        Collection<LocalDate> src = supplier.get(); src.addAll(asList(null, NOW, NOW.withYear(0)));
        Collection<?> expected = (Collection)(has(NON_NULL, opts)
                ? src.stream().filter(Objects::nonNull).filter(NON_ZERO_YEAR).collect(toCollection(supplier))
                : src.stream().filter(NON_ZERO_YEAR).collect(toCollection(supplier)));

        Collection<?> result = collect(src, collector.apply(NON_ZERO_YEAR, opts));
        assertThat(result, instanceOf(expectedCls), equalTo(expected));
    }

    @Test void testToStr() {
        assertEquals(
                "{null;4}",
                collect(FilteringCollectors.filterToStr(EVEN_OR_NULL, ()->new StringJoiner(";", "{", "}"), 0),
                        null, -5, 4));
    }
    @Test void testNonNullToStr() {
        assertEquals(
                "{4}",
                collect(FilteringCollectors.filterToStr(EVEN_OR_NULL, ()->new StringJoiner(";", "{", "}"), NON_NULL),
                        null, -5, 4));
    }

    static Predicate<LocalDate> NON_ZERO_YEAR = d -> d == null || d.getYear() != 0;
    static Predicate<Integer> EVEN_OR_NULL = i -> i == null || i % 2 == 0;
}