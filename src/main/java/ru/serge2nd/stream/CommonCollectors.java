package ru.serge2nd.stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.serge2nd.stream.util.Collecting;
import ru.serge2nd.stream.util.CollectingOptions;
import ru.serge2nd.stream.util.WithCharacteristics;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.stream.util.Collecting.*;
import ru.serge2nd.stream.util.CollectingOptions.OptionsResolver;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.CharacteristicSet.maskOf;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight collectors.
 * @see ru.serge2nd.stream
 * @see CollectingOptions
 */
@SuppressWarnings("unchecked")
public class CommonCollectors {
    private CommonCollectors() { throw errNotInstantiable(lookup().lookupClass()); }

    //region Factory methods

    public static <E> Collector<E, ?, String>  toStr(Supplier<StringJoiner> supplier, int opts) {
        return has(NON_NULL, opts) ? new NonNullToString<>(supplier) : new AllToString<>(supplier);
    }
    public static <E> Collector<E, ?, List<E>> toList(int opts) { return TO_LIST_BY_OPTS.resolve(opts); }
    public static <E> Collector<E, ?, Set<E>>  toSet(int opts)  { return TO_SET_BY_OPTS.resolve(opts); }
    @SuppressWarnings("ConstantConditions")
    public static <E, C extends Collection<E>> Collector<E, ?, C> to(Supplier<C> supplier, int opts, Characteristics... cs) {
        return TO_SUPPLIED_BY_OPTS.resolve(opts).apply(supplier, cs);
    }
    //endregion

    //region Hidden singletons

    static final Collector<?, ?, ?> TO_SET                        = new ToSet<>();
    static final Collector<?, ?, ?> TO_LIST                       = new ToList<>();
    static final Collector<?, ?, ?> NON_NULL_TO_SET               = new NonNullToSet<>();
    static final Collector<?, ?, ?> NON_NULL_TO_LIST              = new NonNullToList<>();
    static final Collector<?, ?, ?> TO_UNMODIFIABLE_SET           = new ToUnmodifiableSet<>();
    static final Collector<?, ?, ?> TO_UNMODIFIABLE_LIST          = new ToUnmodifiableList<>();
    static final Collector<?, ?, ?> NON_NULL_TO_UNMODIFIABLE_SET  = new NonNullToUnmodifiableSet<>();
    static final Collector<?, ?, ?> NON_NULL_TO_UNMODIFIABLE_LIST = new NonNullToUnmodifiableList<>();
    @SuppressWarnings("rawtypes")
    static final OptionsResolver<Collector> TO_LIST_BY_OPTS = new OptionsResolver<>(UNMODIFIABLE | NON_NULL,
            TO_LIST, TO_UNMODIFIABLE_LIST, NON_NULL_TO_LIST, NON_NULL_TO_UNMODIFIABLE_LIST);
    @SuppressWarnings("rawtypes")
    static final OptionsResolver<Collector> TO_SET_BY_OPTS = new OptionsResolver<>(UNMODIFIABLE | NON_NULL,
            TO_SET, TO_UNMODIFIABLE_SET, NON_NULL_TO_SET, NON_NULL_TO_UNMODIFIABLE_SET);

    static final class ToUnmodifiableSet<E> implements Collecting.ToSet<E, Set<E>, Set<E>>, Unordered<E, Set<E>, Set<E>> {
        @Override public Function<Set<E>, Set<E>> finisher() { return Unmodifiable::ofSet; }
    }
    static final class ToUnmodifiableList<E> implements ToCollection<E, List<E>, List<E>>, NoFeatures<E, List<E>, List<E>> {
        @Override public Function<List<E>, List<E>> finisher() { return Unmodifiable::ofList; }
    }
    static final class NonNullToUnmodifiableSet<E> implements Collecting.NonNullToSet<E, Set<E>, Set<E>>, Unordered<E, Set<E>, Set<E>> {
        @Override public Function<Set<E>, Set<E>> finisher() { return Unmodifiable::ofSet; }
    }
    static final class NonNullToUnmodifiableList<E> implements NonNullToCollection<E, List<E>, List<E>>, NoFeatures<E, List<E>, List<E>> {
        @Override public Function<List<E>, List<E>> finisher() { return Unmodifiable::ofList; }
    }
    static final class ToSet<E>         implements Collecting.ToSet<E, Set<E>, Set<E>>       , IdentityFinishUnordered<E, Set<E>> {}
    static final class ToList<E>        implements ToCollection<E, List<E>, List<E>>         , IdentityFinish<E, List<E>> {}
    static final class NonNullToSet<E>  implements Collecting.NonNullToSet<E, Set<E>, Set<E>>, IdentityFinishUnordered<E, Set<E>> {}
    static final class NonNullToList<E> implements NonNullToCollection<E, List<E>, List<E>>  , IdentityFinish<E, List<E>> {}
    //endregion

    //region Hidden implementations

    @SuppressWarnings("rawtypes")
    static final OptionsResolver<BiFunction<Supplier, Characteristics[], Collector>> TO_SUPPLIED_BY_OPTS = new OptionsResolver<>(UNMODIFIABLE | NON_NULL,
            ToSuppliedCollection::new, ToSuppliedCollectionToUnmodifiable::new, NonNullToSuppliedCollection::new, NonNullToSuppliedCollectionToUnmodifiable::new);

    static final class NonNullToSuppliedCollectionToUnmodifiable<E, C extends Collection<E>> extends Supplied<E, C, C> implements NonNullToCollection<E, C, C> {
        @Override public Function<C, C> finisher()                                             { return c -> (C)Unmodifiable.ofCollection(c); }
        NonNullToSuppliedCollectionToUnmodifiable(Supplier<C> supplier, Characteristics... cs) { super(supplier, maskOf(cs)); }
    }
    static final class ToSuppliedCollectionToUnmodifiable<E, C extends Collection<E>> extends Supplied<E, C, C> implements ToCollection<E, C, C> {
        @Override public Function<C, C> finisher()                                      { return c -> (C)Unmodifiable.ofCollection(c); }
        ToSuppliedCollectionToUnmodifiable(Supplier<C> supplier, Characteristics... cs) { super(supplier, maskOf(cs)); }
    }
    static final class NonNullToSuppliedCollection<E, C extends Collection<E>> extends Supplied<E, C, C> implements NonNullToCollection<E, C, C>, IdentityFinish<E, C> {
        NonNullToSuppliedCollection(Supplier<C> supplier, Characteristics... cs) { super(supplier, maskOf(M_IDENTITY_FINISH, cs)); }
    }
    static final class ToSuppliedCollection<E, C extends Collection<E>> extends Supplied<E, C, C> implements ToCollection<E, C, C>, IdentityFinish<E, C> {
        ToSuppliedCollection(Supplier<C> supplier, Characteristics... cs) { super(supplier, maskOf(M_IDENTITY_FINISH, cs)); }
    }
    static final class AllToString<E> extends ToString<E> {
        AllToString(Supplier<StringJoiner> supplier) { super(supplier); }
    }
    static final class NonNullToString<E> extends ToString<E> {
        @Override public BiConsumer<StringJoiner, E> accumulator() { return (sj, e) -> {if (e != null) sj.add(String.valueOf(e));}; }
        NonNullToString(Supplier<StringJoiner> supplier) { super(supplier); }
    }
    //endregion

    //region Public skeleton implementations

    public static abstract class Supplied<E, A, R> extends WithCharacteristics<E, A, R> {
        @Override public final Supplier<A> supplier() { return supplier; }
        final Supplier<A> supplier;
        Supplied(@NonNull Supplier<A> supplier, int characteristicMask) { super(characteristicMask); this.supplier = supplier; }
    }
    @RequiredArgsConstructor
    public static abstract class ToStringJoiner<E, R> implements Collecting.ToStringJoiner<E, R> {
        @Override public final Supplier<StringJoiner> supplier() { return supplier; }
        final @NonNull Supplier<StringJoiner> supplier;
    }
    public static abstract class ToString<E> extends ToStringJoiner<E, String> implements NoFeatures<E, StringJoiner, String> {
        @Override public final Function<StringJoiner, String> finisher() { return StringJoiner::toString; }
        ToString(Supplier<StringJoiner> supplier)                        { super(supplier); }
    }
    //endregion
}
