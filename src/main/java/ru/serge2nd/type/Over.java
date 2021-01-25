package ru.serge2nd.type;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks some method as expected to override the member of the class/interface referred by {@link #origin()}
 * when the type whose method is marked inherits from that origin <i>implicitly</i>.<br>
 * Intended to cover <i>early</i> implementations of some interface(s)
 * without providing explicit {@link Override}s at the current level of type hierarchy.
 * The corresponding checks of method signatures are highly desirable before runtime (e.g. on annotation processing).
 * If multiple origins are given, the first containing the suitable signature is used raising an error if no such signature.<br>
 * This annotation can be placed on the type itself to specify the default origin
 * for all {@link Over}-annotated methods of this type.<br>
 * See
 * {@link ru.serge2nd.collection.NotList},
 * {@link ru.serge2nd.collection.Unmodifiable},
 * {@link ru.serge2nd.collection.UnmodifiableCollection},
 * {@link ru.serge2nd.collection.UnmodifiableList}
 * for the usage example.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Over {
    /**
     * @see #origin()
     */
    @AliasFor("origin")
    Class<?>[] value() default {};

    /**
     * The origin of the annotated overriding.
     * See the {@link Over class-level description} for details.
     */
    Class<?>[] origin() default {};
}
