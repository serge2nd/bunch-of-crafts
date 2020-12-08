package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.stream.util.CollectingOptions.OptionsResolver;

import java.util.stream.Stream;

import static java.lang.Integer.bitCount;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static ru.serge2nd.stream.util.CollectingOptions.collectingOptions;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.ArrayMatch.isArray;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class CollectingOptionsTest {

    @Test void testUniqueOptions() { assertEach(concat(stream(collectingOptions()).mapToObj(
        opt -> ()->assertEquals(1, bitCount(opt))), Stream.of(
        () -> assertEquals(collectingOptions().length, bitCount(stream(collectingOptions()).reduce(0, (a, i) -> a | i))))));
    }

    @SuppressWarnings("ConstantConditions")
    @Test void testNewOptionsResolver() { assertThat(
        getField(new OptionsResolver<>(3, "x"), "vals")     , isArray("x"),
        getField(new OptionsResolver<>(0, "x", "y"), "vals"), isArray("x"),
        ()->new OptionsResolver<>(0, (Object[])null)        , illegalArgument());
    }

    @Test void testResolve() {
        int opt1 = 128, opt2 = Integer.MIN_VALUE >>> 10, opt3 = Integer.MIN_VALUE >>> 1;
        Integer[] vals = {5, 7, 9, 11, 13, 15};
        OptionsResolver<Integer> r = new OptionsResolver<>(opt1 | opt2 | opt3, vals);
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