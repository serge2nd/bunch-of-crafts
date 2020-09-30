package ru.serge2nd.bean.definition;

import lombok.NonNull;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import ru.serge2nd.bean.BeanCfg;

/** {@inheritDoc} */
public enum BeanDefinitionFactoryImpl implements BeanDefinitionFactory {
    INSTANCE;

    /** {@inheritDoc} */
    public BeanDefinition from(@NonNull BeanCfg beanCfg) {
        AbstractBeanDefinition bd = BeanDefinitionBuilder
                .genericBeanDefinition(beanCfg.getBeanClass())
                .setFactoryMethodOnBean(beanCfg.getFactoryMethod(), beanCfg.getFactoryBean())
                .setInitMethodName(beanCfg.getInitMethod())
                .setDestroyMethodName(beanCfg.getDestroyMethod())
                .setLazyInit(beanCfg.isLazyInit())
                .setScope(beanCfg.getScope())
                .setAutowireMode(beanCfg.getAutowireMode())
                .getRawBeanDefinition();
        bd.setInstanceSupplier(beanCfg.getSupplier());
        bd.setPropertyValues(new MutablePropertyValues(beanCfg.getProperties()));
        return bd;
    }
}
