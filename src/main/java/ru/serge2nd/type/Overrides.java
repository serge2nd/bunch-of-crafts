package ru.serge2nd.type;

import java.lang.annotation.*;

/**
 * TODO
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Overrides {
    /**
     * TODO
     */
    Class<?>[] value() default {};
}
