package ru.serge2nd.bean.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static ru.serge2nd.bean.definition.BeanDefinitionHelperTest.SFM;
import static ru.serge2nd.test.match.AssertAllMatch.assertAllMatch;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.notNullValue;

@TestInstance(Lifecycle.PER_CLASS)
class AnnotatedRootBeanDefinitionTest {
    static final AnnotatedRootBeanDefinition DUMMY_ABD = new AnnotatedRootBeanDefinition(typed(), null, null);

    static List<Arguments> rootBeanDefinitionsProvider() { return asList(
        //        Title              Bean def   Factory method  Expected annotated root bean definition
        arguments("no metadata"    , untyped(), null          , aa(Assertions::assertNull)),
        arguments("factory"        , untyped(), SFM           , aa(abd -> assertThat(abd.getFactoryMethodMetadata(), notNullValue("method metadata")))),
        arguments("class"          , typed()  , null          , aa(abd -> assertThat(abd.getMetadata()             , notNullValue("metadata")))),

        arguments("factory & class", typed()  , SFM           , aa(abd -> assertAllMatch(notNullValue("metadata"), abd.getFactoryMethodMetadata(), abd.getMetadata()))),

        arguments("abd"            , DUMMY_ABD, null          , aa(abd -> assertThat(abd.getBeanClass()            , sameInstance(DUMMY_ABD.getBeanClass()),
                                                                                     abd.getFactoryMethodMetadata(), nullValue(),
                                                                                     abd.getMetadata()             , nullValue())))); }
    @ParameterizedTest(name = "{0}") @MethodSource("rootBeanDefinitionsProvider")
    void testFrom(String title, RootBeanDefinition rbd, Method factoryMethod, Consumer<AnnotatedRootBeanDefinition> abdAssertion) {
        rbd.setResolvedFactoryMethod(factoryMethod);
        abdAssertion.accept(AnnotatedRootBeanDefinition.from(rbd));
    }

    static RootBeanDefinition typed() {
        return (RootBeanDefinition)rootBeanDefinition(BeanDefinitionHelperTest.class).getRawBeanDefinition();
    }
    @SuppressWarnings("ConstantConditions")
    static RootBeanDefinition untyped() {
        return (RootBeanDefinition)rootBeanDefinition((Class<?>)null).getRawBeanDefinition();
    }

    static Consumer<AnnotatedRootBeanDefinition> aa(Consumer<AnnotatedRootBeanDefinition> aa) { return aa; }
}