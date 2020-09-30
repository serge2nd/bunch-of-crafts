package ru.serge2nd.bean.definition;

import lombok.Getter;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;

/**
 * A {@link RootBeanDefinition} extension to support bean annotation metadata by implementing {@link AnnotatedBeanDefinition}.<br>
 * An analog of the corresponding internal class in the {@code ConfigurationClassBeanDefinitionReader} from the {@code spring-context} module.<br>
 * May be useful for test/debug purposes and customizing bean definitions.
 * @see RootBeanDefinition
 * @see AnnotatedBeanDefinition
 */
public final class AnnotatedRootBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {
    private final @Getter AnnotationMetadata metadata;
    private final @Getter MethodMetadata factoryMethodMetadata;

    /**
     * Constructs a deep copy of the given annotated root bean definition.
     * @see RootBeanDefinition#RootBeanDefinition(RootBeanDefinition)
     */
    public AnnotatedRootBeanDefinition(AnnotatedRootBeanDefinition src) {
        super(src);
        this.metadata = src.getMetadata();
        this.factoryMethodMetadata = src.getFactoryMethodMetadata();
    }

    /**
     * Creates an annotated bean definition based on the deep copy of the given {@link RootBeanDefinition}
     * extracting annotation metadata from resolved factory method and bean class kept by that argument.<br>
     *
     * <b>Note:</b> if argument is an {@link AnnotatedRootBeanDefinition}
     * then returns {@link AnnotatedRootBeanDefinition#AnnotatedRootBeanDefinition(AnnotatedRootBeanDefinition)}.
     *
     * @param src root bean definition to create an annotated from
     * @return root bean definition containing factory method and bean class annotation metadata
     *         or <code>null</code> if neither factory method nor bean class are available
     */
    @SuppressWarnings("deprecation")
    public static AnnotatedRootBeanDefinition from(RootBeanDefinition src) {
        if (src instanceof AnnotatedRootBeanDefinition)
            return new AnnotatedRootBeanDefinition((AnnotatedRootBeanDefinition)src);

        Method factoryMethod = src.getResolvedFactoryMethod();
        MethodMetadata factoryMethodMetadata = factoryMethod != null ? new StandardMethodMetadata(factoryMethod) : null;

        Class<?> beanClass = src.getResolvableType().getRawClass();
        AnnotationMetadata annotationMetadata = beanClass != null ? AnnotationMetadata.introspect(beanClass) : null;

        if (annotationMetadata == null && factoryMethodMetadata == null)
            return null;

        return new AnnotatedRootBeanDefinition(src, annotationMetadata, factoryMethodMetadata);
    }

    /**
     * @see RootBeanDefinition#RootBeanDefinition(RootBeanDefinition)
     */
    AnnotatedRootBeanDefinition(RootBeanDefinition src, AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
        super(src);
        this.metadata = metadata;
        this.factoryMethodMetadata = factoryMethodMetadata;
    }

    /** {@inheritDoc} */ @Override
    public AnnotatedRootBeanDefinition cloneBeanDefinition() { return new AnnotatedRootBeanDefinition(this); }
}
