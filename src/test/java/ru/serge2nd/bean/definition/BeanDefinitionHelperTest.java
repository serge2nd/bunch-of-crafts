package ru.serge2nd.bean.definition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import ru.serge2nd.NoInstanceTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.data.util.ReflectionUtils.findRequiredMethod;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class BeanDefinitionHelperTest implements NoInstanceTest<BeanDefinitionHelper> {

    @Test void testAnnotatedBeanDefinitionOfAnnotated() {
        AnnotatedBeanDefinition abd = AnnotatedRootBeanDefinition.from(typed(null, null));
        assertSame(abd, BeanDefinitionHelper.annotatedBeanDefinition(abd, null));
    }
    @Test void testAnnotatedBeanDefinitionOfNull()    { assertNull(BeanDefinitionHelper.annotatedBeanDefinition(null, null)); }
    @Test void testAnnotatedBeanDefinitionOfNonRoot() { assertNull(BeanDefinitionHelper.annotatedBeanDefinition(new GenericBeanDefinition(), null)); }

    static List<Arguments> rootBeanDefinitionsProvider() { return asList(
        //        Title                          Bean definition                     Bean type getter  Expected resolved method
        arguments("static"                     , typed(FM.getName(), null)         , factoryClass()  , SFM),
        arguments("static, method not found"   , typed("nonExisting", null)        , factoryClass()  , null),
        arguments("static non-public allowed"  , typed(NSFM.getName(), null)       , factoryClass()  , NSFM),
        arguments("static non-public"          , typedLimited(NSFM.getName(), null), factoryClass()  , null),
        arguments("static, no bean class"      , untyped(FM.getName())             , factoryClass()  , null),
        arguments("factory"                    , typed(FM.getName(), "")           , factoryClass()  , FM),
        arguments("factory, non-public allowed", typed(NFM.getName(), "")          , factoryClass()  , NFM),
        arguments("factory, non-public"        , typedLimited(NFM.getName(), "")   , factoryClass()  , null),
        arguments("factory, method not found"  , typed("nonExisting", "")          , factoryClass()  , null),
        arguments("factory, no class"          , typed(FM.getName(), "")           , noFactoryClass(), null),
        arguments("factory, no type getter"    , typed(FM.getName(), "")           , null            , null)); }
    @ParameterizedTest(name = "{0}") @MethodSource("rootBeanDefinitionsProvider")
    void testTryResolveFactoryMethod(String title, RootBeanDefinition rbd, Function<String, Class<?>> typeGetter, Method expectedFactoryMethod) {
        assertSame(expectedFactoryMethod, BeanDefinitionHelper.tryResolveFactoryMethod(rbd, typeGetter).getResolvedFactoryMethod());
    }
    @Test
    void testAlreadyResolvedFactoryMethod() {
        RootBeanDefinition rbd = untyped(FM.getName());
        rbd.setResolvedFactoryMethod(SFM);
        assertSame(SFM, BeanDefinitionHelper.tryResolveFactoryMethod(rbd, factoryClass()).getResolvedFactoryMethod());
    }

    @Test
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> BeanDefinitionHelper.annotatedBeanDefinitions(null, factoryClass(), ""),
        () -> BeanDefinitionHelper.annotatedBeanDefinitions($->untyped(""), factoryClass(), (String[])null));
    }

    static RootBeanDefinition typed(String method, String factory) {
        return (RootBeanDefinition)rootBeanDefinition(BeanDefinitionHelperTest.class)
                .setFactoryMethodOnBean(method, factory)
                .getRawBeanDefinition();
    }
    static RootBeanDefinition typedLimited(String method, String factory) {
        RootBeanDefinition rbd = typed(method, factory);
        rbd.setNonPublicAccessAllowed(false);
        return rbd;
    }
    @SuppressWarnings("ConstantConditions")
    static RootBeanDefinition untyped(String method) {
        return (RootBeanDefinition)rootBeanDefinition((Class<?>)null)
                .setFactoryMethodOnBean(method, null)
                .getRawBeanDefinition();
    }

    static final Method SFM = findRequiredMethod(BeanDefinitionHelperTest.class, "testFactoryMethod");
    static final Method NSFM = findRequiredMethod(BeanDefinitionHelperTest.class, "nonPublicFactoryMethod");
    static final Method FM = findRequiredMethod(TestFactory.class, "testFactoryMethod");
    static final Method NFM = findRequiredMethod(TestFactory.class, "nonPublicFactoryMethod");
    static Function<String, Class<?>> factoryClass()   { return $ -> TestFactory.class; }
    static Function<String, Class<?>> noFactoryClass() { return $ -> null; }

    public static Object testFactoryMethod() { return null; }
    static Object nonPublicFactoryMethod() { return null; }

    static class TestFactory {
        public Object testNonFactoryMethod() { return null; }
        public Object testFactoryMethod() { return null; }
        Object nonPublicFactoryMethod() { return null; }
    }
}