package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.stream.Stream;

import static java.lang.Integer.bitCount;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.serge2nd.stream.util.CollectingOptions.allOptions;
import static ru.serge2nd.test.Asserting.assertEach;

@TestInstance(Lifecycle.PER_CLASS)
class CollectingOptionsTest {

    @Test void testUniqueOptions() { assertEach(concat(stream(allOptions()).mapToObj(
        opt -> ()->assertEquals(1, bitCount(opt))), Stream.of(
        () -> assertEquals(allOptions().length, bitCount(stream(allOptions()).reduce(0, (a, i) -> a | i))))));
    }
}