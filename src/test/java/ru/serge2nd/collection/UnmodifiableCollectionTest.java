package ru.serge2nd.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static ru.serge2nd.test.match.AssertThat.assertThat;

@TestInstance(Lifecycle.PER_CLASS)
class UnmodifiableCollectionTest {
    static final String STR = "[]";
    static final Object[] A1 = new Object[0], A2 = new Object[0];
    static final Stream<Integer> S = Stream.empty();
    static final Stream<Integer> PS = Stream.empty();
    static final Spliterator<Integer> SP = Spliterators.emptySpliterator();

    final Collection<Integer> coll = new ArrayList<Integer>() {
        public Object[] toArray()                 { return A1; }
        public <T> T[]  toArray(T[] a)            { return a; }
        public String   toString()                { return STR; }
        public Spliterator<Integer> spliterator() { return SP; }
        public Stream<Integer>      stream()      { return S; }
        public Stream<Integer>   parallelStream() { return PS; }
    };
    @BeforeEach void setUp() { coll.clear(); }

    final UnmodifiableCollection<Integer> u = new UnmodifiableCollection<Integer>(coll) {};

    @Test void testZeroSize()        { assertThat(u, hasSize(0)); }
    @Test void testSize()            { coll.addAll(asList(0, 0)); assertThat(u, hasSize(2)); }
    @Test void testIsEmpty()         { assertTrue(u.isEmpty(), "expected empty");}
    @Test void testIsNotEmpty()      { coll.add(0); assertFalse(u.isEmpty(), "expected not empty");}
    @Test void testToArray()         { assertSame(A1, u.toArray()); }
    @Test void testToGivenArray()    { assertSame(A2, u.toArray(A2)); }
    @Test void testToSuppliedArray() { assertSame(A2, u.toArray(i->A2)); }
    @Test void testToString()        { assertSame(STR, u.toString()); }

    @Test void testContains()       { coll.addAll(asList(1, 0)); assertTrue(u.contains(1), "expected containing"); }
    @Test void testNotContains()    { coll.addAll(asList(1, 0)); assertFalse(u.contains(-1), "expected not containing"); }
    @Test void testContainsAll()    { coll.addAll(asList(1, 0)); assertTrue(u.containsAll(asList(1, 0)), "expected containing"); }
    @Test void testNotContainsAll() { coll.addAll(asList(1, 0)); assertFalse(u.containsAll(asList(1, -1)), "expected not containing"); }
    @Test void testForEach()        { coll.addAll(asList(1, 0)); StringBuilder s = new StringBuilder(); u.forEach(s::append); assertEquals("10", s.toString()); }
    @Test void testSpliterator()    { assertSame(SP, u.spliterator()); }
    @Test void testStream()         { assertSame(S, u.stream()); }
    @Test void testParallelStream() { assertSame(PS, u.parallelStream()); }
}