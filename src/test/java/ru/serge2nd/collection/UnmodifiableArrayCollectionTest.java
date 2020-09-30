package ru.serge2nd.collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.collection.UnmodifiableArrayListTest.TestArrayList;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.CommonMatch.hasNext;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.CommonMatch.nextIs;
import static ru.serge2nd.test.matcher.CommonMatch.noNext;
import static ru.serge2nd.test.matcher.CommonMatch.noSuchElement;
import static ru.serge2nd.test.matcher.CommonMatch.sameAs;
import static ru.serge2nd.test.matcher.CommonMatch.unsupported;

@TestInstance(Lifecycle.PER_CLASS)
class UnmodifiableArrayCollectionTest {
    static Stream<Arguments> arraysProvider() { return UnmodifiableArrayListTest.arraysProvider(); }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    void testSizeAndConversions(String title, String[] a, UnmodifiableArrayCollection<String> c) {
        Object[] buf = new Object[a.length];
        assertThat(
        c                       , hasSize(a.length),
        c.toArray()             , equalTo(a),
        c.toArray(new Object[0]), equalTo(a),
        c.toArray(buf)          , allOf(sameAs(buf), equalTo(a)),
        c.toArray(Object[]::new), equalTo(a),
        c.toString()            , equalTo(Arrays.toString(a)), () ->
        assertEquals(a.length == 0, c.isEmpty(), "wrong isEmpty()"));
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    void testContains(String title, String[] a, UnmodifiableArrayCollection<String> c) { assertEach(() -> {
        if (a.length > 0) assertTrue(c.contains(a[0]), "must contain the first");}, () -> {
        if (a.length > 1) assertTrue(c.contains(a[1]), "must contain the second");}, () ->
        assertFalse(c.contains("no"), "must not contain non-existing"), () ->
        assertTrue(c.containsAll(asList(a)), "must contain all from source"), () ->
        assertFalse(c.containsAll(new ArrayList<String>(asList(a)){{add("no");}}), "must not contain all with non-existing"));
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    @SuppressWarnings("SimplifyStreamApiCallChains")
    void testForEach(String title, String[] a, UnmodifiableArrayCollection<String> c) {
        StringBuilder expected = new StringBuilder(), actual = new StringBuilder();
        stream(a).forEach(expected::append);
        assertEach(() -> {c.forEach(actual::append);
        assertEquals(expected.toString(), actual.toString());}, () -> {c.stream().forEach(actual.delete(0, actual.length())::append);
        assertEquals(expected.toString(), actual.toString());}, () -> {c.parallelStream().iterator().forEachRemaining(actual.delete(0, actual.length())::append);
        assertEquals(expected.toString(), actual.toString());}, () -> {c.spliterator().forEachRemaining(actual.delete(0, actual.length())::append);
        assertEquals(expected.toString(), actual.toString());}, () -> {c.iterator().forEachRemaining(actual.delete(0, actual.length())::append);
        assertEquals(expected.toString(), actual.toString());});
    }

    @Test
    void testIteratorOfEmpty() {
        Iterator<String> it = new TestArrayList(new String[0]).iterator();
        assertThat(
        it        , noNext(),
        it::next  , noSuchElement(),
        it::remove, unsupported());
    }
    @Test
    void testIteratorOfOne() {
        Iterator<String> it = new TestArrayList(new String[]{"abc"}).iterator();
        assertThat(
        it        , hasNext(),
        it        , nextIs("abc"),
        it::remove, unsupported(),
        it        , noNext(),
        it::next  , noSuchElement(),
        it::remove, unsupported());
    }
    @Test
    void testIteratorOfTwo() {
        Iterator<String> it = new TestArrayList(new String[]{"abc", "xyz"}).iterator();
        assertThat(
        it        , hasNext(),
        it        , nextIs("abc"),
        it::remove, unsupported(),
        it        , hasNext(),
        it        , nextIs("xyz"),
        it        , noNext(),
        it::next  , noSuchElement(),
        it::remove, unsupported());
    }

    @Test @SuppressWarnings("ConstantConditions,ResultOfMethodCallIgnored")
    void testNullArgs() {
        TestArrayList c = new TestArrayList(new String[0]);
        assertForMany(illegalArgument(),
        () -> c.toArray((Object[])null),
        () -> c.toArray((IntFunction<Object[]>)null),
        () -> c.containsAll(null),
        () -> c.forEach(null));
    }
}