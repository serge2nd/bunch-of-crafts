package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.hasSize;
import static ru.serge2nd.stream.util.Collecting.*;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.CommonMatch.unsupported;
import static ru.serge2nd.type.Types.NO_TYPES;

@TestInstance(Lifecycle.PER_CLASS)
class CollectingTest implements NoInstanceTest<Collecting> {

    @Test
    void testToLargerCombinerLeft() {
        List<?> larger = new ArrayList<>(asList(0, 0));
        List<?> less = new ArrayList<>(singleton(0));
        toLargerCombiner().apply(larger, less);
        assertThat(
        larger, hasSize(3),
        less  , hasSize(1));
    }
    @Test
    void testToLargerCombinerRight() {
        List<?> larger = new ArrayList<>(asList(0, 0));
        List<?> less = new ArrayList<>(singleton(0));
        toLargerCombiner().apply(less, larger);
        assertThat(
        larger, hasSize(3),
        less  , hasSize(1));
    }
    @Test
    void testNoCombiner() {
        assertThat(()->noCombiner().apply(null, null), unsupported());
    }

    @Test @SuppressWarnings("ConstantConditions")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> collect((Iterable<?>)null, testCollector()),
        () -> collect(emptySet(), null),
        () -> collect(emptySet(), nullSupplier()),
        () -> collect(emptySet(), nullSupplierGet()),
        () -> collect(emptySet(), nullAccumulator()),
        () -> collect(emptySet(), nullFinisher()),
        () -> collect(emptySet(), nullCharacteristics()),
        () -> collect((List<?>)null, testCollector()),
        () -> collect(emptyList(), null),
        () -> collect(emptyList(), nullSupplier()),
        () -> collect(emptyList(), nullSupplierGet()),
        () -> collect(emptyList(), nullAccumulator()),
        () -> collect(emptyList(), nullFinisher()),
        () -> collect(emptyList(), nullCharacteristics()),
        () -> gather(null, testCollector()),
        () -> gather(emptySet(), null),
        () -> gather(emptySet(), nullSupplier()),
        () -> gather(emptySet(), nullSupplierGet()),
        () -> gather(emptySet(), nullAccumulator()),
        () -> gather(emptySet(), nullFinisher()),
        () -> gather(emptySet(), nullCharacteristics()),
        () -> collect((Object[])null, testCollector()),
        () -> collect(NO_TYPES, null),
        () -> collect(NO_TYPES, nullSupplier()),
        () -> collect(NO_TYPES, nullSupplierGet()),
        () -> collect(NO_TYPES, nullAccumulator()),
        () -> collect(NO_TYPES, nullFinisher()),
        () -> collect(NO_TYPES, nullCharacteristics()),
        () -> collect((Object[])null, "", (a, e) -> {}),
        () -> collect(NO_TYPES, null, (a, e) -> {}),
        () -> collect(NO_TYPES, "", null),
        () -> collect((List<?>)null, "", (a, e) -> {}),
        () -> collect(emptyList(), null, (a, e) -> {}),
        () -> collect(emptyList(), "", null),
        () -> collect((Iterable<?>)null, "", (a, e) -> {}),
        () -> collect(emptySet(), null, (a, e) -> {}),
        () -> collect(emptySet(), "", null),
        () -> gather(null, "", (a, e) -> {}),
        () -> gather(emptySet(), null, (a, e) -> {}),
        () -> gather(emptySet(), "", null));
    }

    static <E> TestCollector<E> nullSupplier() {
        return new TestCollector<>(null, (a, e)->{}, (a, b)->a, a->a, emptySet());
    }
    static <E> TestCollector<E> nullSupplierGet() {
        return new TestCollector<>(()->null, (a, e)->{}, (a, b)->a, a->a, emptySet());
    }
    static <E> TestCollector<E> nullAccumulator() {
        return new TestCollector<>(()->"", null, (a, b)->a, a->a, emptySet());
    }
    static <E> TestCollector<E> nullFinisher() {
        return new TestCollector<>(()->"", (a, e)->{}, (a, b)->a, null, emptySet());
    }
    static <E> TestCollector<E> nullCharacteristics() {
        return new TestCollector<>(()->"", (a, e)->{}, (a, b)->a, a->a, null);
    }
    static <E> TestCollector<E> testCollector() {
        return new TestCollector<>(()->"", (a, e)->{}, (a, b)->a, a->a, emptySet());
    }

    static class TestCollector<E> implements Collector<E, Object, Object> {
        Supplier<Object> supplier;
        BiConsumer<Object, E> accumulator;
        BinaryOperator<Object> combiner;
        Function<Object, Object> finisher;
        Set<Characteristics> characteristics;

        TestCollector(Supplier<Object> supplier,
                      BiConsumer<Object, E> accumulator,
                      BinaryOperator<Object> combiner,
                      Function<Object, Object> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        public Supplier<Object>         supplier()    { return supplier; }
        public BiConsumer<Object, E>    accumulator() { return accumulator; }
        public BinaryOperator<Object>   combiner()    { return combiner; }
        public Function<Object, Object> finisher()    { return finisher; }
        public Set<Characteristics> characteristics() { return characteristics; }
    }
}