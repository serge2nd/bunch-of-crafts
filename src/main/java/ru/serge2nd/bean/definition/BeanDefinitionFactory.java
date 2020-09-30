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

    /**
     * Uses provided bean configuration and bean definition factory
     * to create bean definition and pass it to the given registration method.
     * @param beanCfg bean configuration to build bean definition from
     * @param factory bean definition factory
     * @param registry bean definition registration method
     */
    static void registerBean(BeanCfg beanCfg, BeanDefinitionFactory factory, BiConsumer<String, BeanDefinition> registry) {
        registry.accept(beanCfg.getName(), factory.from(beanCfg));
    }
}
