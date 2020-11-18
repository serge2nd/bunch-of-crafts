package ru.serge2nd.stream.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import ru.serge2nd.collection.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfLeadingZeros;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.lang.reflect.Array.newInstance;
import static ru.serge2nd.stream.util.Collecting.collect;

/**
 * An immutable set that effectively (by integer mask) stores
 * and determines the {@link Characteristics} values presence.
 * @see WithCharacteristics
 * @see Characteristics
 */
public final class CharacteristicSet extends Unmodifiable<Characteristics> implements Set<Characteristics> {
    private static final Characteristics[] V = Characteristics.values();
    private final int mask;

    // region Constructors & related helpers

    public CharacteristicSet(Characteristics... cs)           { this.mask = maskOf(0, cs); }
    public CharacteristicSet(int mask, Characteristics... cs) { this.mask = maskOf(mask, cs); }

    public static int mask(Object o)                { return o instanceof Characteristics ? mask((Characteristics)o) : 0; }
    public static int mask(Characteristics c)       { return c != null ? 1 << c.ordinal() : 0; }
    public static int maskOf(Characteristics... cs) { return maskOf(0, cs); }
    public static int maskOf(int mask, @NonNull Characteristics... characteristics) {
        for (Characteristics c : characteristics)
            mask |= mask(c);
        return mask;
    }
    // endregion

    // region Value presence

    @Override
    public boolean contains(Object o) { return (mask & mask(o)) != 0; }
    @Override
    public boolean containsAll(@NonNull Collection<?> coll) {
        if (coll instanceof CharacteristicSet) {
            int otherMask = ((CharacteristicSet)coll).mask;
            return (mask & otherMask) == otherMask;
        }
        for (Object e : coll) if (!contains(e)) return false;
        return true;
    }
    // endregion

    // region Size & conversions

    @Override
    public int size()                      { return bitCount(mask); }
    @Override
    public boolean isEmpty()               { return mask == 0; }
    @Override
    public Object[] toArray()              { return toArray(new Characteristics[size()]); }
    @Override @SuppressWarnings("unchecked")
    public <T> T[] toArray(@NonNull T[] a) {
        int size = size();
        if (a.length < size) a = (T[])newInstance(a.getClass().getComponentType(), size);

        T[] v = (T[])V;
        for (int mask = this.mask, i, j = 0; mask != 0; mask = mask >>> i << i, j++) {
            i = numberOfTrailingZeros(mask);
            a[j] = v[i++];
        }

        return a;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Set)) return false;
        if (obj instanceof CharacteristicSet) return this.mask == ((CharacteristicSet)obj).mask;

        Set<?> other = (Set<?>)obj;
        if (size() != other.size()) return false;
        for (Object e : other) if (!contains(e)) return false;
        return true;
    }
    @Override
    public int hashCode() { return mask; }
    @Override
    public String toString() {
        return isEmpty() ? "[]" : collect(this, new StringJoiner(", ", "[", "]"),
                (sj, c) -> sj.add(String.valueOf(c)))
                .toString();
    }
    // endregion

    // region Iterables

    @Override
    public Stream<Characteristics>      stream()                           { return StreamSupport.stream(spliterator(), false); }
    @Override
    public Stream<Characteristics>      parallelStream()                   { return StreamSupport.stream(spliterator(), true); }
    @Override
    public Spliterator<Characteristics> spliterator()                      { return new CharacteristicsSpliterator(mask); }
    @Override
    public void forEach(@NonNull Consumer<? super Characteristics> action) { forEachCharacteristic(mask, action); }
    @Override
    public Iterator<Characteristics>    iterator()                         { return new Iterator<Characteristics>() {
        int mask = CharacteristicSet.this.mask;
        @Override
        public boolean hasNext() { return mask != 0; }
        @Override
        public Characteristics next() {
            int mask = this.mask;
            if (mask == 0) throw new NoSuchElementException();

            int i = numberOfTrailingZeros(mask);
            Characteristics c = V[i++];
            this.mask = mask >>> i << i;
            return c;
        }
        @Override
        public void forEachRemaining(Consumer<? super Characteristics> action) {
            mask = forEachCharacteristic(mask, action);
        }
        @Override /* For debug purposes */
        public String toString() { return String.format("%s<%s>(%d)", Iterator.class.getSimpleName(), Characteristics.class.getSimpleName(), mask); }
    }; }
    // endregion

    @AllArgsConstructor
    static final class CharacteristicsSpliterator implements Spliterator<Characteristics> {
        int mask;

        @Override
        public boolean tryAdvance(@NonNull Consumer<? super Characteristics> action) {
            int mask = this.mask;
            if (mask != 0) {
                int i = numberOfTrailingZeros(mask);
                action.accept(V[i++]); this.mask = mask >>> i << i;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<Characteristics> trySplit() {
            int mask = this.mask, size = bitCount(mask);
            if (size < 2) return null;

            int mid = size >> 1;
            for (; size > mid; size--) {
                int i = numberOfLeadingZeros(mask) + 1;
                mask = mask << i >>> i;
            }

            this.mask ^= mask;
            return new CharacteristicsSpliterator(mask);
        }

        @Override
        public void forEachRemaining(@NonNull Consumer<? super Characteristics> action) {
            mask = forEachCharacteristic(mask, action);
        }

        @Override
        public long estimateSize()        { return bitCount(mask); }
        @Override
        public long getExactSizeIfKnown() { return estimateSize(); }
        @Override
        public int characteristics()      { return SIZED | SUBSIZED | IMMUTABLE | DISTINCT | NONNULL; }

        @Override /* For debug purposes */
        public String toString() { return String.format("%s(%d)", getClass().getSimpleName(), mask); }
    }

    private static int forEachCharacteristic(int mask, Consumer<? super Characteristics> action) {
        for (int i; mask != 0; mask = mask >>> i << i) {
            i = numberOfTrailingZeros(mask);
            action.accept(V[i++]);
        }
        return mask;
    }
}