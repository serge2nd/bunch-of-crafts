package ru.serge2nd.bean.processor;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import java.util.function.BiPredicate;

/**
 * A {@link BiPredicate} being satisfied if bean type or factory method has the {@link Immutable} annotation.
 */
@SuppressWarnings("ConstantConditions")
public enum ImmutableFilter implements BiPredicate<BeanDefinition, Object> {
    INSTANCE;
    public static final String IMMUTABLE_ANN = Immutable.class.getName();

    /** {@inheritDoc} */
    public boolean test(BeanDefinition bd, Object bean) {
        if (!(bd instanceof AnnotatedBeanDefinition))
            return false;

        AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) bd;
        AnnotationMetadata metadata = abd.getMetadata();
        MethodMetadata methodMetadata = abd.getFactoryMethodMetadata();

        return metadata != null && metadata.hasAnnotation(IMMUTABLE_ANN) ||
                methodMetadata != null && methodMetadata.isAnnotated(IMMUTABLE_ANN);
    }
}
