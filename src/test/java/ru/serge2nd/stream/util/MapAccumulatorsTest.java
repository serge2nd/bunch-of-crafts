package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.util.HashMap;

import static java.util.Collections.singletonMap;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.CommonMatch.illegalState;

@TestInstance(Lifecycle.PER_CLASS)
class MapAccumulatorsTest implements NoInstanceTest<MapAccumulators> {

    @Test void testPutUnique() {
        assertThat(()->MapAccumulators.putUnique(new HashMap<>(singletonMap(5, "five")), 5, "six"), illegalState());
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