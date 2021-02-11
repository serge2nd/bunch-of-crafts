package ru.serge2nd.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.NoInstanceTest;
import ru.serge2nd.collection.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.stream.util.Accumulators.adding;
import static ru.serge2nd.stream.util.Accumulators.addingNonNull;
import static ru.serge2nd.stream.util.Collecting.*;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL;
import static ru.serge2nd.stream.util.CollectingOptions.UNMODIFIABLE;
import static ru.serge2nd.stream.util.WithCharacteristics.*;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.equalTo;
import static ru.serge2nd.test.match.CoreMatch.sameAs;
import static ru.serge2nd.test.match.CoreMatch.sameClass;

@TestInstance(Lifecycle.PER_CLASS)
class CommonCollectorsTest implements NoInstanceTest<CommonCollectors> {
    static final Class<?> CONCUR_SET_CLS = ConcurrentSkipListSet.class;
    static <E> Supplier<Set<E>> concurSet() { return ConcurrentSkipListSet::new; }

    @SuppressWarnings("unchecked,rawtypes")
    static List<Arguments> collectorsProvider() { return asList(
        /*
        0 - collector to test
        1 - expected accumulating container class
        2 - expected accumulator
        3 - expected combiner
        4 - expected result superclass
        5 - expected characteristics
        */
        //      1                2                       3                   4                   5
        arguments(CommonCollectors.toList(0), // 0
                ArrayList.class, adding()       , toFirstCombiner() , ArrayList.class   , S_IDENTITY_FINISH),
        arguments(CommonCollectors.toList(UNMODIFIABLE),
                ArrayList.class, adding()       , toFirstCombiner() , Unmodifiable.class, emptySet()),
        arguments(CommonCollectors.toList(NON_NULL),
                ArrayList.class, addingNonNull(), toFirstCombiner() , ArrayList.class   , S_IDENTITY_FINISH),
        arguments(CommonCollectors.toList(UNMODIFIABLE | NON_NULL),
                ArrayList.class, addingNonNull(), toFirstCombiner() , Unmodifiable.class, emptySet()),
        arguments(CommonCollectors.toSet(0),
                HashSet.class  , adding()       , toLargerCombiner(), HashSet.class     , S_IDENTITY_FINISH_UNORDERED),
        arguments(CommonCollectors.toSet(UNMODIFIABLE),
                HashSet.class  , adding()       , toLargerCombiner(), Unmodifiable.class, S_UNORDERED),
        arguments(CommonCollectors.toSet(NON_NULL),
                HashSet.class  , addingNonNull(), toLargerCombiner(), HashSet.class     , S_IDENTITY_FINISH_UNORDERED),
        arguments(CommonCollectors.toSet(UNMODIFIABLE | NON_NULL),
                HashSet.class  , addingNonNull(), toLargerCombiner(), Unmodifiable.class, S_UNORDERED),
        arguments(CommonCollectors.to(TreeSet::new, 0, M_UNORDERED),
                TreeSet.class  , adding()       , toFirstCombiner() , TreeSet.class     , S_IDENTITY_FINISH_UNORDERED),
        arguments(CommonCollectors.to(concurSet(), UNMODIFIABLE | NON_NULL, M_UNORDERED | M_CONCURRENT),
                CONCUR_SET_CLS , addingNonNull(), toFirstCombiner() , Unmodifiable.class, new HashSet(){{add(UNORDERED);add(CONCURRENT);}})); }

    @ParameterizedTest @MethodSource("collectorsProvider")
    void testTo(Collector<?, Object, ?> collector, Class<?> aCls,
                BiConsumer<?, ?> accumulator, BinaryOperator<?> combiner,
                Class<?> fCls, Set<Collector.Characteristics> cs) {
        Object a = collector.supplier().get();
        Object f = collector.finisher().apply(a);
        assertThat(
        a, sameClass(aCls),
        f, instanceOf(fCls),
        collector.accumulator()    , sameAs(accumulator),
        collector.combiner()       , sameAs(combiner),
        collector.characteristics(), equalTo(cs));
    }

    @Test void testToStr() {
        assertEquals("{null;5}", collect(CommonCollectors.toStr(()->new StringJoiner(";", "{", "}"), 0), null, 5));
    }
    @Test void testNonNullToStr() {
        assertEquals("{5}", collect(CommonCollectors.toStr(()->new StringJoiner(";", "{", "}"), NON_NULL), null, 5));
    }
}