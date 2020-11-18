package ru.serge2nd.bean.definition;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import ru.serge2nd.type.Classes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL_VAL;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * Helpers for creating and transforming bean definitions.
 */
public class BeanDefinitionHelper {
    private BeanDefinitionHelper() { throw errNotInstantiable(lookup()); }

    /**
     * Given a bean definition by-name getter, bean class by-name getter and bean names,
     * creates annotated bean definition by-name map using {@link #annotatedBeanDefinition(BeanDefinition, Function)}
     * to construct map values. If the result of the construction is empty, it's dropped.
     * @param bdGetter bean definition by-name getter
     * @param typeGetter bean class by-name getter
     * @param beanNames bean names used to get bean definition and type
     * @return map containing annotated bean definitions by their names
     *         excluding ones with empty result of {@link #annotatedBeanDefinition(BeanDefinition, Function)}
     * @see #annotatedBeanDefinition(BeanDefinition, Function)
     */
    public static Map<String, AnnotatedBeanDefinition> annotatedBeanDefinitions(@NonNull Function<String, BeanDefinition> bdGetter,
                                                                                Function<String, Class<?>> typeGetter,
                                                                                @NonNull String... beanNames) {
        return collect(beanNames, toMap(
                name -> annotatedBeanDefinition(bdGetter.apply(name), typeGetter),
                NON_NULL_VAL));
    }

    /**
     * If the given bean definiton is an {@link AnnotatedBeanDefinition}, simply returns it.
     * If the given bean definiton is a {@link RootBeanDefinition} and the type and/or factory method metadata can be resolved,
     * creates an {@link AnnotatedBeanDefinition} based on it.
     * Tries to resolve factory method via {@link #tryResolveFactoryMethod(RootBeanDefinition, Function)} before creation.
     * @param bd bean definition
     * @param typeGetter bean class by-name getter
     * @return annotated bean definition or {@code null} if cannot resolve the type and/or factory method metadata
     * @see #tryResolveFactoryMethod(RootBeanDefinition, Function)
     */
    public static AnnotatedBeanDefinition annotatedBeanDefinition(BeanDefinition bd,
                                                                  Function<String, Class<?>> typeGetter) {
        if (bd instanceof AnnotatedBeanDefinition) return (AnnotatedBeanDefinition)bd;
        if (bd instanceof RootBeanDefinition) {
            RootBeanDefinition rbd = tryResolveFactoryMethod((RootBeanDefinition)bd, typeGetter);
            return AnnotatedRootBeanDefinition.from(rbd);
        }
        return null;
    }

    /**
     * Looks for a factory method using information provided by the given root bean definition.
     * If unique method is found, calls {@link RootBeanDefinition#setResolvedFactoryMethod(Method)}.
     * If the factory method has already resolved ({@link RootBeanDefinition#getResolvedFactoryMethod()} returns non-null), does nothing.
     * @param rbd root bean definition
     * @param typeGetter bean class by-name getter
     * @return the first argument
     * @see Classes#getUserClass(Class)
     */
    public static RootBeanDefinition tryResolveFactoryMethod(RootBeanDefinition rbd,
                                                             Function<String, Class<?>> typeGetter) {
        if (rbd.getResolvedFactoryMethod() != null) return rbd;
        String factoryName = rbd.getFactoryBeanName();
        boolean isStatic   = factoryName == null;
        boolean anyAllowed = rbd.isNonPublicAccessAllowed();

        Class<?> factoryClass = isStatic
                ? rbd.getResolvableType().getRawClass()
                : typeGetter != null ? typeGetter.apply(factoryName) : null;
        if (factoryClass == null) return rbd;

        rbd.setResolvedFactoryMethod(Classes.findUniqueMethod(
                Classes.getUserClass(factoryClass), method -> (
                anyAllowed || isPublic(method.getModifiers())) &&
                isStatic == isStatic(method.getModifiers()) &&
                rbd.isFactoryMethod(method)));
        return rbd;
    }
}
