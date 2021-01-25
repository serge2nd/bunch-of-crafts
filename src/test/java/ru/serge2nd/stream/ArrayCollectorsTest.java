package ru.serge2nd.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertSame;
import static ru.serge2nd.stream.ArrayCollectors.*;
import static ru.serge2nd.stream.MapCollectorsTest.NOW;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.ArrayMatch.*;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class ArrayCollectorsTest implements NoInstanceTest<ArrayCollectors> {

    @Test void testMapToArray() { assertThat(collect(
        mapToArray(Object::toString, ()->new String[2]), 5, 7),
        items("5", "7"));
    }
    @Test void testMapToTruncArray() { assertThat(collect(
        mapToArray(Object::toString, ()->new String[2]), 5, 7, 9),
        items("5", "7"));
    }

    @Test void testMapToNoBools() {
        assertSame(NO_BOOLS, collect(mapToBools(Objects::nonNull, 0), ""));
    }
    @Test void testMapToBools() { assertThat(collect(
        mapToBools(Objects::nonNull, 2), NOW, null),
        flags(true, false));
    }
    @Test void testMapToTruncBools() { assertThat(collect(
        mapToBools(Objects::nonNull, 2), null, "", NOW),
        flags(false, true));
    }

    @Test void testMapToNoChars() {
        assertSame(NO_CHARS, collect(mapToChars(d -> d.toString().charAt(3), 0), NOW));
    }
    @Test void testMapToChars() { assertThat(collect(
        mapToChars(d -> d.toString().charAt(3), 2), NOW.withYear(5), NOW.withYear(7)),
        chars('5', '7'));
    }
    @Test void testMapToTruncChars() { assertThat(collect(
        mapToChars(d -> d.toString().charAt(3), 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
        chars('5', '7'));
    }

    @Test void testMapToNoBytes() {
        assertSame(NO_BYTES, collect(mapToBytes(d -> (byte)d.getYear(), 0), NOW));
    }
    @Test void testMapToBytes() { assertThat(collect(
        mapToBytes(d -> (byte)d.getYear(), 2), NOW.withYear(5), NOW.withYear(7)),
        bytes((byte)5, (byte)7));
    }
    @Test void testMapToTruncBytes() { assertThat(collect(
        mapToBytes(d -> (byte)d.getYear(), 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
        bytes((byte)5, (byte)7));
    }

    @Test void testMapToNoShorts() {
        assertSame(NO_SHORTS, collect(mapToShorts(d -> (short)d.getYear(), 0), NOW));
    }
    @Test void testMapToShorts() { assertThat(collect(
        mapToShorts(d -> (short)d.getYear(), 2), NOW.withYear(5), NOW.withYear(7)),
        shorts((short)5, (short)7));
    }
    @Test void testMapToTruncShorts() { assertThat(collect(
        mapToShorts(d -> (short)d.getYear(), 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
        shorts((short)5, (short)7));
    }

    @Test void testMapToNoInts() {
        assertSame(NO_INTS, collect(mapToInts(LocalDate::getYear, 0), NOW));
    }
    @Test void testMapToInts() { assertThat(collect(
        mapToInts(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7)),
        ints(5, 7));
    }
    @Test void testMapToTruncInts() { assertThat(collect(
        mapToInts(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
        ints(5, 7));
    }

    @Test void testMapToNoLongs() {
        assertSame(NO_LONGS, collect(mapToLongs(LocalDate::getYear, 0), NOW));
    }
    @Test void testMapToLongs() { assertThat(collect(
        mapToLongs(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7)),
        longs(5, 7));
    }
    @Test void testMapToTruncLongs() { assertThat(collect(
        mapToLongs(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
        longs(5, 7));
    }

    @Test void testMapToNoFloats() {
        assertSame(NO_FLOATS, collect(mapToFloats(LocalDate::getYear, 0)));
    }
    @Test void testMapToFloats() { assertThat(collect(
            mapToFloats(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7)),
            floats(5, 7));
    }
    @Test void testMapToTruncFloats() { assertThat(collect(
            mapToFloats(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
            floats(5, 7));
    }

    @Test void testMapToNoDoubles() {
        assertSame(NO_DOUBLES, collect(mapToDoubles(LocalDate::getYear, 0)));
    }
    @Test void testMapToDoubles() { assertThat(collect(
            mapToDoubles(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7)),
            doubles(5, 7));
    }
    @Test void testMapToTruncDoubles() { assertThat(collect(
            mapToDoubles(LocalDate::getYear, 2), NOW.withYear(5), NOW.withYear(7), NOW.withYear(9)),
            doubles(5, 7));
    }

    @Test
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> mapToBools(null, 0),
        () -> mapToChars(null, 0),
        () -> mapToBytes(null, 0),
        () -> mapToShorts(null, 0),
        () -> mapToInts(null, 0),
        () -> mapToLongs(null, 0),
        () -> mapToFloats(null, 0),
        () -> mapToDoubles(null, 0),
        () -> mapToArray(null, () -> new Object[0]),
        () -> mapToArray(x -> x, null));
    }
}