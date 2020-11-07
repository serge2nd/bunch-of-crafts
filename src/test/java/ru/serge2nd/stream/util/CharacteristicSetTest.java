package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.stream.util.CharacteristicSet.CharacteristicsSpliterator;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import static java.lang.Integer.bitCount;
import static java.lang.Integer.numberOfTrailingZeros;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static java.util.Collections.*;
import static java.util.Spliterator.*;
import static java.util.stream.Collector.Characteristics.values;
import static java.util.stream.Stream.concat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static ru.serge2nd.stream.util.CharacteristicSet.mask;
import static ru.serge2nd.stream.util.CharacteristicSet.maskOf;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.SequentMatch.emits;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.SequentMatch.hasNext;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.SequentMatch.noNext;
import static ru.serge2nd.test.matcher.CommonMatch.noSuchElement;
import static ru.serge2nd.test.matcher.CommonMatch.sameAs;
import static ru.serge2nd.test.matcher.CommonMatch.sameClass;

@TestInstance(Lifecycle.PER_CLASS)
class CharacteristicSetTest {
    static Characteristics ID     = Characteristics.IDENTITY_FINISH;
    static Characteristics UNORD  = Characteristics.UNORDERED;
    static Characteristics CONCUR = Characteristics.CONCURRENT;
    static int M_ID     = 1 << ID.ordinal();
    static int M_UNORD  = 1 << UNORD.ordinal();
    static int M_CONCUR = 1 << CONCUR.ordinal();
    @SuppressWarnings("rawtypes")
    static final Supplier<Stream<Entry<Set, Integer>>> NO_CH = () -> Stream.of(new SimpleEntry<>(emptySet(), 0));

    //region mask(), maskOf() tests

    static Stream<Arguments> singleCharacteristicProvider() { return concat(Stream.of(values())
            .map(c -> arguments(c, 1 << c.ordinal())), Stream.of(arguments(null, 0))); }

    @SuppressWarnings("unchecked,rawtypes")
    static Stream<Arguments> characteristicsProvider() {
        Stream<Entry<Set, Integer>> result = concat(NO_CH.get(), Stream.of(values())
                .map(c -> new SimpleEntry<>(singleton(c), 1 << c.ordinal())));

        for (int i = 0; i < values().length - 1; i++)
            result = result.flatMap(a -> concat(NO_CH.get(), Stream.of(values())
                .map(c -> new SimpleEntry<>(
                    new HashSet(a.getKey()) {{ add(c); }},
                    a.getValue() | (1 << c.ordinal())))));

        return result.distinct().map(a -> {
            Characteristics[] cs = (Characteristics[])a.getKey().toArray(new Characteristics[0]); sort(cs);
            return arguments(cs, new CharacteristicSet(cs), a.getValue());
        });
    }

    @ParameterizedTest @MethodSource("singleCharacteristicProvider")
    void testObjectMask(Characteristics c, int expected)          { assertEquals(expected, mask((Object)c)); }
    @ParameterizedTest @MethodSource("singleCharacteristicProvider")
    void testMask(Characteristics c, int expected)                { assertEquals(expected, mask(c)); }
    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testMaskOf(Characteristics[] cs, Object $, int expected) { assertEquals(expected, maskOf(cs)); }
    @Test
    void testNonCharacteristicMask() { assertEquals(0, mask(Characteristics.UNORDERED.name())); }
    @Test
    void testComplementedMask() { assertEquals(M_ID | M_UNORD, maskOf(M_UNORD, null, Characteristics.IDENTITY_FINISH)); }
    //endregion

    //region toArray(), toString(), stream(), forEach() tests

    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testToArray(Characteristics[] cs, CharacteristicSet set) {
        // WHEN
        Object[] buf = new Object[cs.length];
        Object[] result = set.toArray(buf);

        /* THEN */ assertThat(
        result                             , sameAs(buf),
        buf                                , equalTo(cs),
        set.toArray()                      , equalTo(cs),
        set.toArray(new Characteristics[0]), equalTo(cs));
    }
    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testToString(Characteristics[] cs, CharacteristicSet set) {
        assertEquals(Arrays.toString(cs), set.toString());
    }
    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testStream(Characteristics[] cs, CharacteristicSet set, int expected) {
        assertEquals(expected, set.stream().mapToInt(c -> 1 << c.ordinal()).reduce(0, (a, i) -> a | i));
    }
    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testParallelStream(Object $, CharacteristicSet set, int expected) {
        assertEquals(expected, set.parallelStream().mapToInt(c -> 1 << c.ordinal()).reduce(0, (a, i) -> a | i));
    }

    @ParameterizedTest @MethodSource("characteristicsProvider")
    void testForEach(Object $, CharacteristicSet set, int expected) {
        /* GIVEN */ int[] mask = new int[1];
        /* WHEN  */ set.forEach(c -> mask[0] |= (1 << c.ordinal()));
        /* THEN  */ assertEquals(expected, mask[0]);
    }
    //endregion

    //region containsAll(), equals(), hashCode() tests

    @SuppressWarnings("RedundantCollectionOperation")
    @Test void testContainsAll()          { assertTrue(new CharacteristicSet(UNORD, CONCUR).containsAll(singleton(UNORD)), "must contain all from subset"); }
    @Test void testContainsNotAll()       { assertFalse(new CharacteristicSet(UNORD).containsAll(asList(UNORD, CONCUR)), "must not contain all with non-existing"); }
    @Test void testContainsAllFromCS()    { assertTrue(new CharacteristicSet(UNORD, CONCUR).containsAll(new CharacteristicSet(UNORD)), "must contain all from subset"); }
    @Test void testContainsNotAllFromCS() { assertFalse(new CharacteristicSet(UNORD).containsAll(new CharacteristicSet(UNORD, CONCUR)), "must not contain all with non-existing"); }

    @Test @SuppressWarnings("SimplifiableAssertion,EqualsWithItself,MismatchedQueryAndUpdateOfCollection")
    void testEqualToSelf()      { CharacteristicSet cs = new CharacteristicSet(); assertTrue(cs.equals(cs));}
    @Test @SuppressWarnings("SimplifiableAssertion,ConstantConditions")
    void testEqualToNull()      { assertFalse(new CharacteristicSet().equals(null));}
    @Test @SuppressWarnings("SimplifiableAssertion,EqualsBetweenInconvertibleTypes")
    void testEqualToOtherType() { assertFalse(new CharacteristicSet().equals(emptyList()));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testEqualToCS()        { assertTrue(new CharacteristicSet(ID).equals(new CharacteristicSet(ID)));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testNotEqualToCS()     { assertFalse(new CharacteristicSet(ID).equals(new CharacteristicSet(UNORD)));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testEqualToSet()       { assertTrue(new CharacteristicSet().equals(emptySet()));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testNotEqualToSet()    { assertFalse(new CharacteristicSet(ID).equals(singleton(UNORD)));}
    @Test @SuppressWarnings("SimplifiableAssertion")
    void testNotEqualSizes()    { assertFalse(new CharacteristicSet(ID).equals(emptySet()));}
    @Test
    void testHashCode()         { assertEquals(81, new CharacteristicSet(81).hashCode());}
    //endregion

    //region iterator() tests

    @SuppressWarnings("unchecked,rawtypes,RedundantOperationOnEmptyContainer,MismatchedQueryAndUpdateOfCollection")
    static List<Arguments> iteratorsProvider() {
        CharacteristicSet empty = new CharacteristicSet(),
                one = new CharacteristicSet(Characteristics.UNORDERED),
                two = new CharacteristicSet(Characteristics.IDENTITY_FINISH, Characteristics.CONCURRENT),
                three = new CharacteristicSet(Characteristics.IDENTITY_FINISH, Characteristics.CONCURRENT, Characteristics.UNORDERED);
        Iterator<Characteristics> emptyIt = empty.iterator(),
                oneIt = one.iterator(), oneNext = one.iterator(),
                twoIt = two.iterator(), twoNext = two.iterator(), twoNextNext = two.iterator(),
                threeIt = three.iterator(), threeNext = three.iterator(), threeNextNext = three.iterator();
        oneNext.next(); twoNext.next(); twoNextNext.next(); twoNextNext.next();
        threeNext.next(); threeNextNext.next(); threeNextNext.next();
        return asList(
        //        Iterator       Expected next                    Expected remaining
        arguments(emptyIt      , null                           , emptySet()),
        arguments(oneIt        , Characteristics.UNORDERED      , singleton(Characteristics.UNORDERED)),
        arguments(oneNext      , null                           , emptySet()),
        arguments(twoIt        , Characteristics.CONCURRENT     , new HashSet(){{add(Characteristics.IDENTITY_FINISH);add(Characteristics.CONCURRENT);}}),
        arguments(twoNext      , Characteristics.IDENTITY_FINISH, singleton(Characteristics.IDENTITY_FINISH)),
        arguments(twoNextNext  , null                           , emptySet()),
        arguments(threeIt      , Characteristics.CONCURRENT     , new HashSet(){{add(Characteristics.IDENTITY_FINISH);add(Characteristics.CONCURRENT);add(Characteristics.UNORDERED);}}),
        arguments(threeNext    , Characteristics.UNORDERED      , new HashSet(){{add(Characteristics.IDENTITY_FINISH);add(Characteristics.UNORDERED);}}),
        arguments(threeNextNext, Characteristics.IDENTITY_FINISH, singleton(Characteristics.IDENTITY_FINISH)));
    }
    @ParameterizedTest @MethodSource("iteratorsProvider")
    void testIteratorNext(Iterator<Characteristics> it, Characteristics next) {
        boolean available = next != null;
        assertThat(
        it      , available ? hasNext() : noNext(),
        it::next, available ? emits(next) : noSuchElement());
    }
    @ParameterizedTest @MethodSource("iteratorsProvider")
    void testIteratorForEach(Iterator<Characteristics> it, Object $, Set<Characteristics> remaining) {
        Set<Characteristics> result = new HashSet<Characteristics>() {{it.forEachRemaining(this::add);}};
        assertEquals(remaining, result);
    }
    //endregion

    //region spliterator() tests

    @Test void testSpliterator()   { assertEqualSp(sp(81), new CharacteristicSet(81).spliterator()); }
    @Test void testSpliteratorCs() { assertEquals(SIZED | SUBSIZED | IMMUTABLE | DISTINCT | NONNULL, sp(0).characteristics()); }
    @Test void testEstimateSize()  { assertEquals(bitCount(81), sp(81).estimateSize()); }
    @Test void testGetExactSize()  { assertEquals(bitCount(81), sp(81).getExactSizeIfKnown()); }

    static List<Arguments> spliteratorsProvider() { return asList(
        //        Before split                   After split         Split result
        arguments(sp(0)                        , sp(0)             , null),
        arguments(sp(M_ID)                     , sp(M_ID)          , null),
        arguments(sp(M_UNORD)                  , sp(M_UNORD)       , null),
        arguments(sp(M_CONCUR)                 , sp(M_CONCUR)      , null),
        arguments(sp(M_ID | M_UNORD)           , sp(M_ID)          , sp(M_UNORD)),
        arguments(sp(M_ID | M_CONCUR)          , sp(M_ID)          , sp(M_CONCUR)),
        arguments(sp(M_UNORD | M_CONCUR)       , sp(M_UNORD)       , sp(M_CONCUR)),
        arguments(sp(M_ID | M_UNORD | M_CONCUR), sp(M_ID | M_UNORD), sp(M_CONCUR))); }
    @ParameterizedTest @MethodSource("spliteratorsProvider")
    void testTrySplit(CharacteristicsSpliterator orig, CharacteristicsSpliterator expectedRest, CharacteristicsSpliterator expected) {
        Spliterator<Characteristics> result = orig.trySplit();
        assertEach(() ->
        assertEqualSp(expectedRest, orig), () ->
        assertEqualSp(expected, result));
    }
    @ParameterizedTest @MethodSource("spliteratorsProvider")
    void testSpliteratorForEach(CharacteristicsSpliterator sp) {
        /* GIVEN */ int expected = sp.mask; int[] mask = new int[1];
        /* WHEN  */ sp.forEachRemaining(c -> mask[0] |= (1 << c.ordinal()));
        /* THEN  */ assertEquals(expected, mask[0]);
    }

    @Test
    void testTryAdvance() {
        CharacteristicsSpliterator sp = sp(M_UNORD | M_ID);
        int i = numberOfTrailingZeros(sp.mask) + 1, expected = sp.mask >>> i << i;
        assertEach(() ->
        assertTrue(sp.tryAdvance(c ->
        assertEquals(values()[i - 1], c))), () ->
        assertEquals(expected, sp.mask));
    }
    @Test
    void testTryAdvanceEmpty() {
        assertFalse(sp(0).tryAdvance(c -> fail("unexpected call")));
    }
    //endregion

    @Test @SuppressWarnings("ConstantConditions,ResultOfMethodCallIgnored,RedundantOperationOnEmptyContainer")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> maskOf((Characteristics[])null),
        () -> new CharacteristicSet().containsAll(null),
        () -> new CharacteristicSet().toArray(null),
        () -> new CharacteristicSet().forEach(null),
        () -> sp(0).tryAdvance(null),
        () -> sp(0).forEachRemaining(null));
    }

    static CharacteristicsSpliterator sp(int mask) { return new CharacteristicsSpliterator(mask); }

    static void assertEqualSp(Spliterator<Characteristics> expected, Spliterator<Characteristics> actual) {
        if (expected == null)
            assertNull(actual);
        else if (actual == null)
            fail("unexpected null spliterator");
        else assertThat(
            actual                  , sameClass(expected),
            getField(actual, "mask"), equalTo(getField(expected, "mask")));
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    static Arguments arguments(Object... args) { return new Arguments() {
        public Object[] get()              { return args; }
        public int      hashCode()         { return args[0].hashCode(); }
        public boolean  equals(Object obj) { return Objects.equals(args[0], ((Arguments)obj).get()[0]); }
    }; }
}