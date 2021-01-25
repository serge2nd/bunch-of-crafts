package ru.serge2nd.function;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A provider of an unary operator to apply to objects of the particular type.
 * @param <T> the type which this operator provider accepts
 */
public interface OperatorProvider<T> {

    /**
     * The result is the unary operator for the given type.
     * @param type the type
     * @return the unary operator for the given type or {@link Optional#empty()} if that type not supported
     */
    Optional<UnaryOperator<T>> forType(Type type);
}
