package ru.serge2nd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.ArrayMatch.doubles;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("ConstantConditions")
class DoubleSumTest {
    static final double E = 2.2204460492503130E-16;
    static final double F = 1e101;

    @Test void testOf() {
        DoubleSum s1 = DoubleSum.of(2, E, -1);
        DoubleSum s2 = DoubleSum.of(1, F, 1, -F);
        assertThat(
        getField(s1, "sum"), doubles(1+2e-16, 0),
        getField(s2, "sum"), doubles(0      , 0));
    }
    @Test void testConstructor() {
        DoubleSum s1 = new DoubleSum(2).add(E).add(-1);
        DoubleSum s2 = new DoubleSum(1).add(F).add(1).add(-F);
        assertThat(
        getField(s1, "sum"), doubles(1+2e-16, 0),
        getField(s2, "sum"), doubles(0      , 0));
    }

    @Test void testOfCustomOp() {
        DoubleSum s = DoubleSum.of(DoubleAlgs::neumaierSum, 1, 1, F, 1, -F);
        assertThat(
        getField(s, "sum"), doubles(0, 2));
    }
    @Test void testConstructorCustomOp() {
        DoubleSum s = new DoubleSum(1, DoubleAlgs::neumaierSum, 1).add(F).add(1).add(-F);
        assertThat(
        getField(s, "sum"), doubles(0, 2));
    }

    @Test void testNullOp()            { assertThat(()->new DoubleSum(0, null, 0)                 , illegalArgument()); }
    @Test void testNegativeCmpnOrder() { assertThat(()->new DoubleSum(0, DoubleAlgs::kahanSum, -1), illegalArgument()); }

    @Test void testGetters() {
        DoubleSum s = DoubleSum.of();
        setField(s, "sum", new double[] {5, 7, 9});
        assertEach(() ->
        assertEquals(21, s.get()), () ->
        assertEquals(21, s.fine()), () ->
        assertEquals(5 , s.raw()));
    }
}