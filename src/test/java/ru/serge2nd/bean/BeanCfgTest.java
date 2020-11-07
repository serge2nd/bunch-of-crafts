package ru.serge2nd.bean;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.bean.BeanCfg.*;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.SequentMatch.givesSame;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;
import static ru.serge2nd.test.matcher.CommonMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class BeanCfgTest {
    static List<Arguments> argsProvider() {
        String              customName    = "xyz"; Map<?, ?> m = emptyMap(); Class<?> instanceClass = m.getClass();
        String              nameFromClass = instanceClass.getSimpleName().substring(0, 1).toLowerCase() + instanceClass.getSimpleName().substring(1);
        Supplier<Map<?, ?>> supplier      = ()->unmodifiableMap(emptyMap());
    return asList(
        //        Instance builder                      Expected name  Expected class  Expected supplier
        arguments(s(()->of(m))                        , nameFromClass, instanceClass, givesSame(m)),
        arguments(s(()->of(m, customName))            , customName   , null         , givesSame(m)),
        arguments(s(()->from(m, customName))          , customName   , instanceClass, givesSame(m)),
        arguments(s(()->of(instanceClass, supplier))  , nameFromClass, instanceClass, sameAs(supplier)),
        arguments(s(()->of(instanceClass, customName)), customName   , instanceClass, nullValue()),
        arguments(s(()->of(supplier, customName))     , customName   , null         , sameAs(supplier)),
        arguments(s(()->of(instanceClass))            , nameFromClass, instanceClass, nullValue()));
    }
    @ParameterizedTest @MethodSource("argsProvider")
    void testOf(Supplier<BeanCfg> supplier, String expectedName, Class<?> expectedClass, Matcher<Supplier<?>> supplierMatcher) {
        BeanCfg result = supplier.get();
        assertThat("",
        result.getName()     , equalTo(expectedName),
        result.getBeanClass(), equalTo(expectedClass),
        result.getSupplier() , supplierMatcher);
    }

    @Test @SuppressWarnings("ConstantConditions")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> of((Object)null),
        () -> of((Object)null, ""),
        () -> of(Map.class, (Supplier<?>)null),
        () -> of(null, () -> null),
        () -> from(null, ""),
        () -> of((Class<?>)null, ""),
        () -> of((Supplier<?>)null, ""),
        () -> of(null));
    }

    static Supplier<BeanCfg> s(Supplier<BeanCfg.BeanCfgBuilder> s) { return () -> s.get().build(); }
}