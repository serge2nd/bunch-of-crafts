package ru.serge2nd.function;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A provider of an unary operator to apply to objects of the particular type.
 * @param <T> type for which the operator provider is assumed
 */
public interface OperatorProvider<T> {

    /**
     * The result is the unary operator for the given type.
     * @param type type
     * @return the unary operator for the given type
     */
    Optional<UnaryOperator<T>> forType(Type type);
}
