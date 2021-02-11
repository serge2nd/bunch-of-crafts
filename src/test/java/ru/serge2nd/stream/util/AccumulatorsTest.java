package ru.serge2nd.stream.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class AccumulatorsTest implements NoInstanceTest<Accumulators> {

    @Test
    void testCollectionNonNullAdd() {
        List<?> l = new ArrayList<>(); Accumulators.addingNonNull().accept(l, 0);
        assertThat(l, contains(0));
    }
    @Test
    void testCollectionNonNullAddNull() {
        List<?> l = new ArrayList<>(); Accumulators.addingNonNull().accept(l, null);
        assertThat(l, empty());
    }

    @Test @SuppressWarnings("ResultOfMethodCallIgnored")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> Accumulators.filtering(null, 0),
        () -> Accumulators.filteringToStr(null, 0),
        () -> Accumulators.mapping(null, 0),
        () -> Accumulators.mappingToStr(null, 0),
        () -> Accumulators.aFlatMapping(null, 0),
        () -> Accumulators.aFlatMappingToStr(null, 0));
    }
}