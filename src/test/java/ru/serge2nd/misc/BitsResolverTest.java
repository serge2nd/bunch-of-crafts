package ru.serge2nd.misc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.ArrayMatch.items;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class BitsResolverTest {

    @Test @SuppressWarnings("ConstantConditions")
    void testNewBitsResolver() { assertThat(
        getField(new BitsResolver<>(3, "x"), "vals")     , items("x"),
        getField(new BitsResolver<>(0, "x", "y"), "vals"), items("x"),
        ()->new BitsResolver<>(0, (Object[])null)        , illegalArgument());
    }

    @Test void testResolve() {
        int opt1 = 128, opt2 = Integer.MIN_VALUE >>> 10, opt3 = Integer.MIN_VALUE >>> 1;
        Integer[] vals = {5, 7, 9, 11, 13, 15};
        BitsResolver<Integer> r = new BitsResolver<>(opt1 | opt2 | opt3, vals);
        assertEach(() ->
        assertEquals(vals[0], r.resolve(0)), () ->
        assertEquals(vals[0], r.resolve(opt1 >> 1)), () ->
        assertEquals(vals[0], r.resolve((opt1 >> 1) | (opt1 << 1))), () ->
        assertEquals(vals[1], r.resolve(opt1)), () ->
        assertEquals(vals[1], r.resolve(opt1 | (opt1 >> 1))), () ->
        assertEquals(vals[1], r.resolve(opt1 | (opt1 >> 1) | (opt1 << 1))), () ->
        assertEquals(vals[2], r.resolve(opt2)), () ->
        assertEquals(vals[3], r.resolve(opt2 | opt1)), () ->
        assertEquals(vals[4], r.resolve(opt3)), () ->
        assertEquals(vals[5], r.resolve(opt3 | opt1)), () ->
        assertNull(r.resolve(opt3 | opt2)), () ->
        assertNull(r.resolve(opt3 | opt2 | opt1)));
    }
}