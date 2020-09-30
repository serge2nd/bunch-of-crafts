package ru.serge2nd.bean;

import lombok.*;

import java.util.Map;
import java.util.function.Supplier;

import static java.lang.Character.toLowerCase;

/**
 * Simple immutable configuration of (<i>typically Spring</i>) container-managed bean.
 * Easy to create via {@link BeanCfgBuilder}.
 */
@Value
@Builder
public class BeanCfg {
    @NonNull String name;
    Class<?> beanClass;

    //region Construction/destroying

    @EqualsAndHashCode.Exclude @ToString.Exclude
    Supplier<?> supplier;

    String factoryMethod;

    String factoryBean;

    String initMethod;

    String destroyMethod;
    //endregion

    //region Lifecycle config

    @Builder.Default
    boolean lazyInit = true;

    @Builder.Default
    String scope = "singleton";
    //endregion

    //region Autowiring config

    int autowireMode;

    @Singular
    Map<String, Object> properties;
    //endregion

    //region Builder factory methods

    /**
     * Given an instance, sets its class as bean class, the name based on the class, and the instance supplier.
     * @see #of(Class, Supplier)
     */
    public static BeanCfgBuilder of(@NonNull Object bean) {
        return of(bean.getClass(), () -> bean);
    }

    /**
     * Given an instance and name, sets the bean name and the instance supplier.
     * @see #of(Supplier, String)
     */
    public static BeanCfgBuilder of(@NonNull Object bean, String name) {
        return of(() -> bean, name);
    }

    /**
     * Sets the bean class, the name based on it and the instance supplier.
     * @see #of(Class)
     */
    public static BeanCfgBuilder of(Class<?> beanClass, @NonNull Supplier<?> supplier) {
        return of(beanClass).supplier(supplier);
    }

    /** Given an instance and name, sets the bean name, <b>class</b> and instance supplier. */
    public static BeanCfgBuilder from(@NonNull Object bean, String name) {
        return BeanCfg.builder().name(name).beanClass(bean.getClass()).supplier(() -> bean);
    }

    /** Sets the bean class and name. */
    public static BeanCfgBuilder of(@NonNull Class<?> beanClass, String name) {
        return BeanCfg.builder().name(name).beanClass(beanClass);
    }

    /** Sets the bean instance supplier and name. */
    public static BeanCfgBuilder of(@NonNull Supplier<?> supplier, String name) {
        return BeanCfg.builder().supplier(supplier).name(name);
    }

    /**
     * Sets the bean class and name based on it.
     * @see #beanNameByClass(Class)
     */
    public static BeanCfgBuilder of(Class<?> beanClass) {
        return BeanCfg.builder().name(beanNameByClass(beanClass)).beanClass(beanClass);
    }

    /**
     * Equivalent is {@code BeanCfg.beanNameBySimpleName(beanClass.getSimpleName())}.
     * @see BeanCfg#beanNameBySimpleName(String)
     */
    public static String beanNameByClass(@NonNull Class<?> beanClass) {
        return beanNameBySimpleName(beanClass.getSimpleName());
    }

    /**
     * The result is a string same to the given except the first character converted to the lower case.
     */
    public static String beanNameBySimpleName(String simpleName) {
        return toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
    //endregion
}
