package ru.serge2nd.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static ru.serge2nd.stream.ArrayCollectors.NO_INTS;
import static ru.serge2nd.stream.MapCollectorsTest.NOW;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.ArrayMatch.isArray;
import static ru.serge2nd.test.match.ArrayMatch.ints;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class ArrayCollectorsTest implements NoInstanceTest<ArrayCollectors> {

    @Test void testMapEmptyToInts() {
        assertSame(NO_INTS, collect(ArrayCollectors.mapToInts(LocalDate::getYear, 0)));
    }
    @Test void testMapToInts() {
        assertThat(collect(ArrayCollectors.mapToInts(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7)), ints(5, 7));
    }
    @Test void testMapToTruncInts() {
        assertThat(collect(ArrayCollectors.mapToInts(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)), ints(5, 7));
    }

    @Test void testMapToArray() {
        assertThat(collect(ArrayCollectors.mapToArray(Object::toString, ()->new String[2]), 5, 7), isArray("5", "7"));
    }
    @Test void testMapToTruncArray() {
        assertThat(collect(ArrayCollectors.mapToArray(Object::toString, ()->new String[2]), 5, 7, 9), isArray("5", "7"));
    }

    @Test @SuppressWarnings("ResultOfMethodCallIgnored")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> ArrayCollectors.mapToInts(null, 0),
        () -> ArrayCollectors.mapToArray(null, () -> new Object[0]),
        () -> ArrayCollectors.mapToArray(x -> x, null),
        () -> ArrayCollectors.mappingAccumulator(null),
        () -> ArrayCollectors.mappingToIntAccumulator(null));
    }
}