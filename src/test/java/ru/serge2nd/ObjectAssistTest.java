package ru.serge2nd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class ObjectAssistTest implements NoInstanceTest<ObjectAssist> {
    @Test @SuppressWarnings("ObviousNullCheck")
    void testNotNull() { assertEquals(9, ObjectAssist.nullSafe(9, "")); }
    @Test @SuppressWarnings("ConstantConditions")
    void testNull()    { assertThat(()->ObjectAssist.nullSafe(null, ""), illegalArgument()); }

    //region flatArray() tests

    static List<Arguments> arraysProvider() { return asList(
        //        Input array                                          Expected flat array
        arguments(new float[0]                                       , new float[0]),
        arguments(new float[] {5, 7}                                 , new float[] {5, 7}),
        arguments(new float[][][] {{{5, 7}, {}, {9}}, {}, {{11, 13}}}, new float[] {5, 7, 9, 11, 13})); }
    @ParameterizedTest @MethodSource("arraysProvider")
    void testFlatArray(Object a, float[] expected) { assertArrayEquals(expected, ObjectAssist.flatArray(a)); }
    @Test void testIllegalFlatArray()              { assertThat(()->ObjectAssist.flatArray(5), illegalArgument()); }
    @Test void testIllegalNewFlatArray()           { assertThat(()->ObjectAssist.newFlatArray(5), illegalArgument()); }
    //endregion

    //region component() tests

    static List<Arguments> componentsProvider() { return asList(
        //        Input object      Expected component
        arguments(null            , null),
        arguments(5               , Integer.class),
        arguments(new int[0]      , int.class),
        arguments(new int[0][0][0], int.class)); }
    @ParameterizedTest @MethodSource("componentsProvider")
    void testComponent(Object obj, Class<?> expected) { assertSame(expected, ObjectAssist.component(obj)); }
    //endregion

    //region nDims() tests

    static List<Arguments> dimsProvider() { return asList(
        //        Input object      Expected dims
        arguments(null            , 0),
        arguments(5               , 0),
        arguments(new int[0]      , 1),
        arguments(new int[0][0][0], 3)); }
    @ParameterizedTest @MethodSource("dimsProvider")
    void testNDims(Object obj, int expected) { assertEquals(expected, ObjectAssist.nDims(obj)); }
    //endregion

    //region nElems() tests

    static List<Arguments> elemsProvider() { return asList(
        //        Input object                                       Expected n elems
        arguments(null                                             , -1),
        arguments(5                                                , -1),
        arguments(new int[0]                                       , 0),
        arguments(new int[][][] {{{5, 7}, {}, {9}}, {}, {{11, 13}}}, 5)); }
    @ParameterizedTest @MethodSource("elemsProvider")
    void testNElems(Object obj, int expected) { assertEquals(expected, ObjectAssist.nElems(obj)); }
    //endregion
}