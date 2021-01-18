package ru.serge2nd.stream;

import lombok.NonNull;
import ru.serge2nd.stream.util.Collecting;
import ru.serge2nd.stream.util.CollectingOptions;
import ru.serge2nd.stream.util.WithCharacteristics;
import ru.serge2nd.collection.Unmodifiable;
import ru.serge2nd.stream.util.Collecting.*;
import ru.serge2nd.misc.BitsResolver;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.CollectingOptions.*;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * A factory of lightweight collectors.
 * @see ru.serge2nd.stream
 * @see CollectingOptions
 */
@SuppressWarnings("unchecked,ConstantConditions")
public class CommonCollectors {
    private CommonCollectors() { throw errNotInstantiable(lookup()); }

    //region Factory methods & skeleton impls

    public static <E> Collector<E, ?, String>  toStr(Supplier<StringJoiner> supplier, int opts) {
        return BitsResolver.has(NON_NULL, opts) ? new NonNullToString<>(supplier) : new AllToString<>(supplier);
    }
    public static <E> Collector<E, ?, List<E>> toList(int opts) { return TO_LIST_BY_OPTS.resolve(opts); }
    public static <E> Collector<E, ?, Set<E>>  toSet(int opts)  { return TO_SET_BY_OPTS.resolve(opts); }

    public static <E, C extends Collection<E>> Collector<E, ?, C> to(Supplier<C> supplier, int opts, int characteristics) {
        return TO_SUPPLIED_BY_OPTS.resolve(opts).newInstance(supplier, characteristics);
    }

    public static abstract class Supplied<E, A, R> extends WithCharacteristics<E, A, R> {
        @Override public final Supplier<A> supplier() { return supplier; }
        public Supplied(@NonNull Supplier<A> supplier, int characteristics) { super(characteristics); this.supplier = supplier; }
        final Supplier<A> supplier;
    }
    //endregion

    //region Hidden singletons

    @SuppressWarnings("rawtypes")
    static final BitsResolver<Collector> TO_LIST_BY_OPTS = new BitsResolver<>(UNMODIFIABLE | NON_NULL,
            new ToList(), new ToUnmodifiableList(), new NonNullToList(), new NonNullToUnmodifiableList());
    @SuppressWarnings("rawtypes")
    static final BitsResolver<Collector> TO_SET_BY_OPTS = new BitsResolver<>(UNMODIFIABLE | NON_NULL,
            new ToSet(), new ToUnmodifiableSet(), new NonNullToSet(), new NonNullToUnmodifiableSet());

    static final class ToSet<E>         implements Collecting.ToSet<E, Set<E>, Set<E>>       , IdentityFinishUnordered<E, Set<E>> {}
    static final class ToList<E>        implements ToCollection<E, List<E>, List<E>>         , IdentityFinish<E, List<E>> {}
    static final class NonNullToSet<E>  implements Collecting.NonNullToSet<E, Set<E>, Set<E>>, IdentityFinishUnordered<E, Set<E>> {}
    static final class NonNullToList<E> implements NonNullToCollection<E, List<E>, List<E>>  , IdentityFinish<E, List<E>> {}

    static final class ToUnmodifiableSet<E>
    implements Collecting.ToSet<E, Set<E>, Set<E>>, Unordered<E, Set<E>, Set<E>>         { public Function<Set<E>, Set<E>>   finisher() { return Unmodifiable::ofSet; }}
    static final class ToUnmodifiableList<E>
    implements ToCollection<E, List<E>, List<E>>, NoFeatures<E, List<E>, List<E>>        { public Function<List<E>, List<E>> finisher() { return Unmodifiable::ofList; }}
    static final class NonNullToUnmodifiableSet<E>
    implements Collecting.NonNullToSet<E, Set<E>, Set<E>>, Unordered<E, Set<E>, Set<E>>  { public Function<Set<E>, Set<E>>   finisher() { return Unmodifiable::ofSet; }}
    static final class NonNullToUnmodifiableList<E>
    implements NonNullToCollection<E, List<E>, List<E>>, NoFeatures<E, List<E>, List<E>> { public Function<List<E>, List<E>> finisher() { return Unmodifiable::ofList; }}
    //endregion

    //region Hidden implementations

    @SuppressWarnings("rawtypes")
    static final BitsResolver<SuppliedConstructor> TO_SUPPLIED_BY_OPTS = new BitsResolver<>(
            UNMODIFIABLE | NON_NULL,
            ToSuppliedCollection::new,
            ToSuppliedCollectionToUnmodifiable::new,
            NonNullToSuppliedCollection::new,
            NonNullToSuppliedCollectionToUnmodifiable::new);

    static final class ToSuppliedCollection<E, C extends Collection<E>> extends Supplied<E, C, C> implements ToCollection<E, C, C>, IdentityFinish<E, C> {
        ToSuppliedCollection(Supplier<C> supplier, int cs) { super(supplier, M_IDENTITY_FINISH | cs); }
    }
    static final class NonNullToSuppliedCollection<E, C extends Collection<E>> extends Supplied<E, C, C> implements NonNullToCollection<E, C, C>, IdentityFinish<E, C> {
        NonNullToSuppliedCollection(Supplier<C> supplier, int cs) { super(supplier, M_IDENTITY_FINISH | cs); }
    }

    static final class ToSuppliedCollectionToUnmodifiable<E, C extends Collection<E>> extends Supplied<E, C, C> implements ToCollection<E, C, C> {
        @Override public Function<C, C> finisher() { return c -> (C)Unmodifiable.ofCollection(c); }
        ToSuppliedCollectionToUnmodifiable(Supplier<C> supplier, int cs) { super(supplier, cs); }
    }
    static final class NonNullToSuppliedCollectionToUnmodifiable<E, C extends Collection<E>> extends Supplied<E, C, C> implements NonNullToCollection<E, C, C> {
        @Override public Function<C, C> finisher() { return c -> (C)Unmodifiable.ofCollection(c); }
        NonNullToSuppliedCollectionToUnmodifiable(Supplier<C> supplier, int cs) { super(supplier, cs); }
    }

    static final class AllToString<E> extends ToStringJoiner<E, String> implements NoFeatures<E, StringJoiner, String> {
        @Override public Function<StringJoiner, String> finisher() { return StringJoiner::toString; }
        AllToString(Supplier<StringJoiner> supplier) { super(supplier); }
    }
    static final class NonNullToString<E> extends NonNullToStringJoiner<E, String> implements NoFeatures<E, StringJoiner, String> {
        @Override public Function<StringJoiner, String> finisher() { return StringJoiner::toString; }
        NonNullToString(Supplier<StringJoiner> supplier) { super(supplier); }
    }

    @SuppressWarnings("rawtypes")
    interface SuppliedConstructor { Collector newInstance(Supplier supplier, int characteristics); }
    //endregion
}
