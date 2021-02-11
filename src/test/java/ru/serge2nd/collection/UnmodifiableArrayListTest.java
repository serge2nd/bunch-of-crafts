package ru.serge2nd.collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.PollMatch.emits;
import static ru.serge2nd.test.match.CoreMatch.equalTo;
import static ru.serge2nd.test.match.CoreMatch.noSuchElement;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class UnmodifiableArrayListTest {
    static Stream<Arguments> arraysProvider() { return Stream.of(
        new String[0],
        new String[] {"abc"},
        new String[] {"abc", "xyz"})
        .map(a -> arguments("size=" + a.length, a, new TestArrayList(a)));
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    void testSubList(String title, String[] a, TestArrayList l) { assertThat(
        l.subList(0, 0)               , sameAs(emptyList()),
        l.subList(0, a.length)        , equalTo(asList(a)),
        ()->l.subList(1, a.length)    , a.length > 1 ? emits(asList(copyOfRange(a, 1, a.length))) : anything(),
        ()->l.subList(-1, a.length)   , noSuchElement(),
        ()->l.subList(0, a.length + 1), noSuchElement());
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testHashAndGet(String title, String[] a, TestArrayList l) { assertThat(
        l.hashCode()           , equalTo(Arrays.hashCode(a)),
        ()->l.get(0)           , a.length > 0 ? emits(a[0]) : anything(),
        ()->l.get(a.length - 1), a.length > 0 ? emits(a[a.length - 1]) : anything(),
        ()->l.get(-1)          , noSuchElement(),
        ()->l.get(a.length)    , noSuchElement());
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    void testIndexOf(String title, String[] a, TestArrayList l) { assertThat(
        ()->l.indexOf(a[0])    , a.length > 0 ? emits(0) : anything(),
        ()->l.lastIndexOf(a[0]), a.length > 0 ? emits(0) : anything(),
        ()->l.indexOf(a[1])    , a.length > 1 ? emits(1) : anything(),
        ()->l.lastIndexOf(a[1]), a.length > 1 ? emits(1) : anything(), () ->
        assertForMany(equalTo(-1), l.indexOf("no"), l.lastIndexOf("no")));
    }

    @ParameterizedTest(name = "{0}") @MethodSource("arraysProvider")
    @SuppressWarnings("SimplifiableAssertion,EqualsWithItself,EqualsBetweenInconvertibleTypes")
    void testEquals(String title, String[] a, TestArrayList l) { assertEach(() ->
        assertTrue(l.equals(l)), () ->
        assertFalse(l.equals(new TreeSet<>(asList(a)))), () ->
        assertTrue(l.equals(new TestArrayList(copyOf(a, a.length)))), () -> {
        if (a.length > 0) assertFalse(l.equals(new TestArrayList(copyOfRange(a, 0, a.length - 1))));}, () -> {
        if (a.length > 0) assertFalse(l.equals(asList(copyOfRange(a, 0, a.length - 1))));}, () -> {
        if (a.length > 1) {String[] b = copyOf(a, a.length); b[1] = "no"; assertFalse(l.equals(asList(b)));}}, () ->
        assertTrue(l.equals(asList(a))));
    }

    @Test
    void testListIteratorOfEmpty() {
        ListIterator<String> it = new TestArrayList(new String[0]).listIterator();
        assertEach(() ->
        assertFalse(it.hasPrevious()), () ->
        assertEquals(-1, it.previousIndex()), () ->
        assertEquals(0, it.nextIndex()), () ->
        assertThrows(NoSuchElementException.class, it::previous));
    }
    @Test
    void testListIteratorOfOne() {
        TestArrayList l = new TestArrayList(new String[]{"abc"});
        ListIterator<String> it = l.listIterator(1);
        assertEach(() ->
        assertThrows(NoSuchElementException.class, ()->l.listIterator(-1)), () ->
        assertThrows(NoSuchElementException.class, ()->l.listIterator(2)), () ->
        assertTrue(it.hasPrevious()), () ->
        assertEquals(0, it.previousIndex()), () ->
        assertEquals(1, it.nextIndex()), () ->
        assertEquals("abc", it.previous()), () ->
        assertFalse(it.hasPrevious()));
    }
    @Test
    void testListIteratorOfTwo() {
        ListIterator<String> it = new TestArrayList(new String[]{"abc", "xyz"}).listIterator(2);
        assertEach(() ->
        assertTrue(it.hasPrevious()), () ->
        assertEquals(1, it.previousIndex()), () ->
        assertEquals(2, it.nextIndex()), () ->
        assertEquals("xyz", it.previous()), () ->
        assertTrue(it.hasPrevious()), () ->
        assertEquals(0, it.previousIndex()), () ->
        assertEquals(1, it.nextIndex()), () ->
        assertThrows(UnsupportedOperationException.class, ()->it.set("")), () ->
        assertThrows(UnsupportedOperationException.class, ()->it.add("")), () ->
        assertEquals("abc", it.previous()));
    }

    static class TestArrayList extends UnmodifiableArrayList<String> {
        TestArrayList(String[] array) { super(array); }
    }
}