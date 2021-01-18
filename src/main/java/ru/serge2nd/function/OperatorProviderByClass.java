package ru.serge2nd.function;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static ru.serge2nd.type.Types.rawClass;

/** {@inheritDoc} */
public interface OperatorProviderByClass<T> extends OperatorProvider<T> {

    /**
     * The result is the unary operator for the given class.
     * @param clazz the class
     * @return the unary operator for the given class or {@link Optional#empty()} if that class not supported
     */
    Optional<UnaryOperator<T>> forType(Class<T> clazz);

    /**
     * {@inheritDoc}<br>
     * This default implementation calls {@link #forType(Class)}
     * with the raw class extracted from the given type.
     */
    @Override
    @SuppressWarnings("unchecked")
    default Optional<UnaryOperator<T>> forType(Type type) {
        Class<?> raw = rawClass(type);
        return this.forType((Class<T>)raw);
    }
}
