package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.util.HashMap;

import static java.util.Collections.singletonMap;
import static ru.serge2nd.stream.util.MapAccumulators.putUnique;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;
import static ru.serge2nd.test.match.CoreMatch.illegalState;

@TestInstance(Lifecycle.PER_CLASS)
class MapAccumulatorsTest implements NoInstanceTest<MapAccumulators> {

    @Test void testPutUnique() {
        assertThat(()-> putUnique(new HashMap<>(singletonMap(5, "five")), 5, "six"), illegalState());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> MapAccumulators.keyAccumulator(null, 0),
        () -> MapAccumulators.valueAccumulator(null, 0),
        () -> MapAccumulators.valueFromKeyAccumulator(null, k -> k, 0),
        () -> MapAccumulators.valueFromKeyAccumulator(k -> k, null, 0),
        () -> MapAccumulators.keyFromValueAccumulator(null, v -> v, 0),
        () -> MapAccumulators.keyFromValueAccumulator(v -> v, null, 0));
    }
}