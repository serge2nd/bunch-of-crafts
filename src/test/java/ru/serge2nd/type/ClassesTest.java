package ru.serge2nd.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;
import ru.serge2nd.ObjectAssist;

import java.util.Map;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.data.util.ReflectionUtils.findRequiredMethod;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.type.TypesTest.TEST_CLS_LDR;

@TestInstance(Lifecycle.PER_CLASS)
class ClassesTest implements NoInstanceTest<Classes> {

    //region arrayClass() tests

    @Test void testTrivialArrayClass()   { assertSame(Map[].class, Classes.arrayClass(Map[].class, 0)); }
    @Test void testArrayClass()          { assertSame(Map[][].class, Classes.arrayClass(Map.class, 2)); }
    @Test void testPrimitiveArrayClass() { assertSame(int[][].class, Classes.arrayClass(int[].class, 1)); }
    @Test void testArrayClassWithCustomClassLoader() {
        Class<?> result = Classes.arrayClass(TypesTest.E.class, 3, TEST_CLS_LDR);
        assertNotSame(TypesTest.E[][][].class, result);
        assertEquals(TypesTest.E.class.getName(), result.getComponentType().getComponentType().getComponentType().getName());
    }
    @Test void testNullArrayClassName() {
        assertThat(()-> Classes.arrayClassName(null, 0), illegalArgument());
    }
    //endregion

    //region descriptor() tests

    @SuppressWarnings("ConstantConditions")
    @Test void testNullClass() { assertThat(()->Classes.descriptor(null), illegalArgument()); }
    @Test void testVoidClass() { assertEquals("V", Classes.descriptor(void.class)); }
    @Test void testDescriptor() { assertEach(Stream.of(
        boolean[].class, char[].class,
        byte[].class   , short[].class,
        int[].class    , long[].class,
        float[].class  , double[].class,
        Map[].class).map(arrayClass -> () ->
        assertEquals(arrayClass.getName().substring(1), Classes.descriptor(arrayClass.getComponentType())))
    ); }
    @Test void testDescriptorFromArray() {
        assertThat(()-> Classes.descriptor(Map[].class), illegalArgument());
    }
    //endregion

    @Test void testTrivialUserClass() { assertSame(A.class, Classes.getUserClass(A.class));}
    @Test void testUserClass()        { assertSame(A.class, Classes.getUserClass(Fake$$Sub.class));}

    @Test void testFindUniqueMethod()     { assertSame(findRequiredMethod(Object.class, "notify"), Classes.findUniqueMethod(Object.class, m -> m.getName().equals("notify")));}
    @Test void testNotUniqueMethod()      { assertNull(Classes.findUniqueMethod(Object.class, m -> m.getName().contains("notify"))); }
    @Test void testUniqueMethodNotFound() { assertNull(Classes.findUniqueMethod(Object.class, $ -> false)); }

    @Test void testNotInstantiable() {
        UnsupportedOperationException e = ObjectAssist.errNotInstantiable(lookup());
        assertEquals("non-instantiable: " + ClassesTest.class, e.getMessage());
    }

    static class A {}
    static class Fake$$Proxy extends A {}
    static class Fake$$Sub extends Fake$$Proxy {}
}