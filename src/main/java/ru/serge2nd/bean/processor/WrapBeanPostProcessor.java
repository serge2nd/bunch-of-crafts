package ru.serge2nd.bean.processor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static org.springframework.core.ResolvableType.NONE;
import static ru.serge2nd.bean.definition.BeanDefinitionHelper.tryResolveFactoryMethod;

/**
 * A {@link BeanPostProcessor} filtering and wrapping beans via the given functions.
 */
@RequiredArgsConstructor
public class WrapBeanPostProcessor implements BeanPostProcessor {
    public static final BiPredicate<BeanDefinition, Object> ALL_BEANS = (type, bean) -> true;
    public static final Predicate<String> ALL_NAMES = name -> true;

    private final @NonNull BiPredicate<BeanDefinition, Object> beanFilter;
    private final @NonNull Predicate<String> beanNameFilter;
    private final @NonNull BiFunction<Type, Object, Object> wrapper;
    private final @NonNull ConfigurableListableBeanFactory beanFactory;

    /**
     * Constructs this bean processor accepting any beans and wrapping them by the given function.
     * @param wrapper the function to wrap a bean instance also considering its type from the container.
     * @param beanFactory the bean factory
     */
    public WrapBeanPostProcessor(BiFunction<Type, Object, Object> wrapper, ConfigurableListableBeanFactory beanFactory) {
        this(ALL_BEANS, wrapper, beanFactory);
    }

    /**
     * Constructs this bean processor accepting beans filtered through the given filter and wrapping them by the given function.
     * @param wrapper the function to wrap a bean instance also considering its type from the container.
     * @param beanFactory the bean factory
     */
    public WrapBeanPostProcessor(BiPredicate<BeanDefinition, Object> beanFilter, BiFunction<Type, Object, Object> wrapper, ConfigurableListableBeanFactory beanFactory) {
        this(beanFilter, ALL_NAMES, wrapper, beanFactory);
    }

    /** {@inheritDoc} */ @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        BeanDefinition bd = beanFactory.containsBeanDefinition(beanName) ? beanFactory.getBeanDefinition(beanName) : null;
        if (!(beanNameFilter.test(beanName) && beanFilter.test(bd, bean)))
            return bean;

        if (bd instanceof RootBeanDefinition)
            tryResolveFactoryMethod((RootBeanDefinition)bd, beanFactory::getType);
        ResolvableType resolvable = bd != null ? bd.getResolvableType() : NONE;

        return wrapper.apply(resolvable != NONE ? resolvable.getType() : null, bean);
    }
}
