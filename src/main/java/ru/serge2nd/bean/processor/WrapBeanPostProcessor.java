package ru.serge2nd.bean.processor;

import lombok.NonNull;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static org.springframework.core.ResolvableType.NONE;
import static ru.serge2nd.bean.definition.BeanDefinitions.tryResolveFactoryMethod;

/**
 * A {@link BeanPostProcessor} wrapping beans filtered through the given functions.
 */
public class WrapBeanPostProcessor implements BeanPostProcessor {
    public static final BeanFilter ALL_BEANS = (type, bean) -> true;
    public static final BeanNameFilter ALL_NAMES = name -> true;

    private final Wrapper wrapper;
    private final BeanFilter beanFilter;
    private final BeanNameFilter beanNameFilter;
    private final ConfigurableListableBeanFactory ctx;

    public WrapBeanPostProcessor(@NonNull Wrapper wrapper,
                                 @Nullable BeanFilter beanFilter,
                                 @Nullable BeanNameFilter beanNameFilter,
                                 @NonNull ConfigurableListableBeanFactory ctx) {
        this.wrapper = wrapper;
        this.beanFilter = beanFilter != null ? beanFilter : ALL_BEANS;
        this.beanNameFilter = beanNameFilter != null ? beanNameFilter : ALL_NAMES;
        this.ctx = ctx;
    }

    /** {@inheritDoc} */ @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        BeanDefinition bd = ctx.containsBeanDefinition(beanName) ? ctx.getBeanDefinition(beanName) : null;
        if (!(beanNameFilter.test(beanName) && beanFilter.test(bd, bean)))
            return bean;

        if (bd instanceof RootBeanDefinition)
            tryResolveFactoryMethod((RootBeanDefinition)bd, ctx::getType);
        ResolvableType resolvable = bd != null ? bd.getResolvableType() : NONE;

        return wrapper.apply(resolvable != NONE ? resolvable.getType() : null, bean);
    }

    @FunctionalInterface
    public interface Wrapper extends BiFunction<Type, Object, Object> {
    }
    @FunctionalInterface
    public interface BeanFilter extends BiPredicate<BeanDefinition, Object> {
    }
    @FunctionalInterface
    public interface BeanNameFilter extends Predicate<String> {
    }
}
