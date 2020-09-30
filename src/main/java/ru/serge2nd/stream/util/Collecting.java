package ru.serge2nd.stream.util;

import lombok.NonNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static ru.serge2nd.ObjectAssist.nullSafe;
import static ru.serge2nd.stream.util.Accumulators.collectionAdd;
import static ru.serge2nd.stream.util.Accumulators.collectionNonNullAdd;
import static ru.serge2nd.stream.util.WithCharacteristics.*;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * Collecting helpers.
 * @see Collector
 */
@SuppressWarnings("unchecked")
public class Collecting {
    private Collecting() { throw errNotInstantiable(lookup().lookupClass()); }

    //region Do collecting

    /**
     * Sequentially collects the elements with the given collector without using {@link java.util.stream.Stream}.
     * @param elements iterable elements to collect
     * @param collector collector to use
     * @return collected result
     * @see #accumulate(Iterable, Object, BiConsumer)
     */
    public static <E, A, R> R collect(Iterable<? extends E> elements, @NonNull Collector<E, A, R> collector) {
        A a = accumulate(elements, a(collector), collector.accumulator());
        return cs(collector).contains(IDENTITY_FINISH) ? (R)a : finisher(collector).apply(a);
    }

    /**
     * Analogous to {@link #collect(Iterable, Collector)} with random-access optimization
     * if the specified list implements the {@link RandomAccess}.
     * @see #accumulate(List, Object, BiConsumer)
     */
    public static <E, A, R> R collect(List<? extends E> list, @NonNull Collector<E, A, R> collector) {
        A a = accumulate(list, a(collector), collector.accumulator());
        return cs(collector).contains(IDENTITY_FINISH) ? (R)a : finisher(collector).apply(a);
    }

    /**
     * Sequentially collects the elements with the given collector without using {@link java.util.stream.Stream}.
     * @param elements array of elements to collect
     * @param collector collector to use
     * @return collected result
     * @see #accumulate(Object[], Object, BiConsumer)
     */
    public static <E, A, R> R collect(E[] elements, @NonNull Collector<E, A, R> collector) {
        A a = accumulate(elements, a(collector), collector.accumulator());
        return cs(collector).contains(IDENTITY_FINISH) ? (R)a : finisher(collector).apply(a);
    }

    /**
     * The result is {@link #collect(Object[], Collector) collect(elements, collector)}.
     */
    @SafeVarargs
    public static <E, A, R> R collect(Collector<E, A, R> collector, E... elements) {
        return collect(elements, collector);
    }

    /**
     * Accumulates the elements with the given accumulator.
     * @param elements array of elements to accumulate
     * @param a accumulating target
     * @param accumulator accumulating function
     * @return {@code a} (the second argument) after accumulating
     */
    public static <E, A> A accumulate(@NonNull E[] elements, @NonNull A a, @NonNull BiConsumer<A, E> accumulator) {
        for (E e : elements) accumulator.accept(a, e); return a;
    }

    /**
     * Accumulates the elements with the given accumulator.
     * @param elements iterable elements to accumulate
     * @param a accumulating target
     * @param accumulator accumulating function
     * @return {@code a} (the second argument) after accumulating
     */
    public static <E, A> A accumulate(@NonNull Iterable<? extends E> elements, @NonNull A a, @NonNull BiConsumer<A, E> accumulator) {
        for (E e : elements) accumulator.accept(a, e); return a;
    }

    /**
     * Analogous to {@link #collect(Iterable, Collector)} with random-access optimization
     * if the specified list implements the {@link RandomAccess}.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static <E, A> A accumulate(@NonNull List<? extends E> l, @NonNull A a, @NonNull BiConsumer<A, E> accumulator) {
        if (l instanceof RandomAccess) {
            for (int i = 0, n = l.size(); i < n; i++) accumulator.accept(a, l.get(i));
        } else {
            for (E e : l) accumulator.accept(a, e);
        }
        return a;
    }

    private static <A> A                 a(Collector<?, A, ?> collector)        { return nullSafe(collector.supplier(), "no supplier").get(); }
    private static <A, R> Function<A, R> finisher(Collector<?, A, R> collector) { return nullSafe(collector.finisher(), "no finisher"); }
    private static Set<Characteristics>  cs(Collector<?, ?, ?> collector)       { return nullSafe(collector.characteristics(), "no characteristics"); }
    //endregion

    //region Characteristics mixins

    public interface NoFeatures<E, A, R> extends Collector<E, A, R> {
        @Override default Set<Characteristics> characteristics() { return emptySet(); }
    }
    public interface Unordered<E, A, R> extends Collector<E, A, R> {
        @Override default Set<Characteristics> characteristics() { return S_UNORDERED; }
    }
    public interface IdentityFinish<E, R> extends Collector<E, R, R> {
        @Override default Function<R, R>       finisher()        { return identity(); }
        @Override default Set<Characteristics> characteristics() { return S_IDENTITY_FINISH; }
    }
    public interface IdentityFinishUnordered<E, R> extends Collector<E, R, R> {
        @Override default Function<R, R>       finisher()        { return identity(); }
        @Override default Set<Characteristics> characteristics() { return S_IDENTITY_FINISH_UNORDERED; }
    }
    //endregion

    //region Collection-specific mixins

    @SuppressWarnings("rawtypes")
    public interface ToSet<E, S extends Set, R> extends ToCollection<E, S, R> {
        @Override default Supplier<S>       supplier() { return () -> (S)new HashSet<>(); }
        @Override default BinaryOperator<S> combiner() { return toLargerCombiner(); }
    }
    @SuppressWarnings("rawtypes")
    public interface NonNullToSet<E, S extends Set, R> extends ToSet<E, S, R> {
        @Override default BiConsumer<S, E>  accumulator() { return collectionNonNullAdd(); }
    }
    @SuppressWarnings("rawtypes")
    public interface ToCollection<E, C extends Collection, R> extends Collector<E, C, R> {
        @Override default Supplier<C>       supplier()    { return () -> (C)new ArrayList<>(); }
        @Override default BiConsumer<C, E>  accumulator() { return collectionAdd(); }
        @Override default BinaryOperator<C> combiner()    { return toFirstCombiner(); }
    }
    @SuppressWarnings("rawtypes")
    public interface NonNullToCollection<E, C extends Collection, R> extends ToCollection<E, C, R> {
        @Override default BiConsumer<C, E>  accumulator() { return collectionNonNullAdd(); }
    }
    public interface ToStringJoiner<E, R> extends Collector<E, StringJoiner, R> {
        @Override default BiConsumer<StringJoiner, E> accumulator() { return (sj, e) -> sj.add(String.valueOf(e)); }
        @Override default BinaryOperator<StringJoiner> combiner()   { return StringJoiner::merge; }
    }
    //endregion

    //region Combiners

    @SuppressWarnings("rawtypes")
    public static <C extends Collection> BinaryOperator<C> toFirstCombiner()  { return (c1, c2) -> {c1.addAll(c2); return c1;}; }
    @SuppressWarnings("rawtypes")
    public static <C extends Collection> BinaryOperator<C> toLargerCombiner() { return (left, right) -> {
        if (left.size() < right.size()) {
            right.addAll(left); return right;
        } else {
            left.addAll(right); return left;
        }
    }; }
    public static <A> BinaryOperator<A> noCombiner() { return (a1, a2) -> {throw new UnsupportedOperationException("combining not supported");}; }
    //endregion
}
