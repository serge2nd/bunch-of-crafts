package ru.serge2nd.bean.definition;

import org.springframework.beans.factory.config.BeanDefinition;
import ru.serge2nd.bean.BeanCfg;

import java.util.function.BiConsumer;

/**
 * A factory of {@link BeanDefinition} instances.
 */
public interface BeanDefinitionFactory {
    /**
     * Creates a {@link BeanDefinition} from the given {@link BeanCfg}.
     * @param beanCfg bean configuration to build bean definition from
     * @return created bean definition
     */
    BeanDefinition from(BeanCfg beanCfg);


}
