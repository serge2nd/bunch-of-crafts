package ru.serge2nd.function;

import lombok.NonNull;
import org.springframework.util.TypeUtils;
import ru.serge2nd.type.TypeWrap;

import java.lang.reflect.Type;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * An {@link OperatorProvider} which redirects the {@link OperatorProvider#forType(Type)} call
 * to the first delegate that matches by the specified delegate matcher and returns non-empty operator.
 * @see OperatorProvider
 */
public class DelegatingOperatorProvider<T> implements OperatorProvider<T> {
    public static final DelegateMatcher<?> DEFAULT_MATCHER = (key, delegate, type) -> TypeUtils.isAssignable(key.getType(), type);

    //region The state & constructors

    /** The delegates map */
    private final Map<TypeWrap<? extends T>, OperatorProvider<? extends T>> delegates = new HashMap<>();

    /** The ordered supported types (delegate keys) */
    private final Deque<TypeWrap<? extends T>> orderedTypes = new LinkedList<>();

    /** To choose right delegate by type */
    private final DelegateMatcher<T> delegateMatcher;

    @SuppressWarnings("unchecked")
    public DelegatingOperatorProvider()                                            { this.delegateMatcher = (DelegateMatcher<T>)DEFAULT_MATCHER; }
    public DelegatingOperatorProvider(@NonNull DelegateMatcher<T> delegateMatcher) { this.delegateMatcher = delegateMatcher; }
    //endregion

    /**
     * Gets the operator from the first delegate that matches by the specified delegate matcher and returns non-empty operator.
     * If no such delegate exists, {@link Optional#empty()} is returned.
     * @param type the type
     * @return the operator from the appropriate delegate or {@link Optional#empty()}
     * @see OperatorProvider#forType(Type)
     */
    @Override
    public Optional<UnaryOperator<T>> forType(@NonNull Type type) {
        Optional<UnaryOperator<T>> result;

        for (TypeWrap<? extends T> key : orderedTypes) {
            OperatorProvider<T> delegate = getDelegate(key);

            if (matches(key, delegate, type) && (result =
                delegate.forType(type)).isPresent())
                return result;
        }

        return Optional.empty();
    }

    //region Add delegates

    public <U extends T> void addDelegate(Class<U> clazz, OperatorProviderByClass<U> delegate)        { this.addDelegate(TypeWrap.of(clazz), delegate); }
    public <U extends T> void addPrimaryDelegate(Class<U> clazz, OperatorProviderByClass<U> delegate) { this.addPrimaryDelegate(TypeWrap.of(clazz), delegate); }
    public <U extends T> void addDelegate(TypeWrap<U> type, OperatorProvider<U> delegate)             { doAddDelegate(type, delegate, Deque::addLast); }
    public <U extends T> void addPrimaryDelegate(TypeWrap<U> type, OperatorProvider<U> delegate)      { doAddDelegate(type, delegate, Deque::addFirst); }

    private void doAddDelegate(@NonNull TypeWrap<? extends T> type, @NonNull OperatorProvider<? extends T> delegate,
                               BiConsumer<Deque<TypeWrap<? extends T>>, TypeWrap<? extends T>> addOp) {
        addOp.accept(orderedTypes, type);
        delegates.put(type, delegate);
    }
    //endregion

    //region Private helpers

    private boolean matches(TypeWrap<? extends T> key, OperatorProvider<T> delegate, Type type) {
        return delegateMatcher.matches(key, delegate, type);
    }
    @SuppressWarnings("unchecked")
    private OperatorProvider<T> getDelegate(TypeWrap<? extends T> key) { return (OperatorProvider<T>)delegates.get(key); }
    //endregion

    @FunctionalInterface
    public interface DelegateMatcher<T> {
        boolean matches(TypeWrap<? extends T> key, OperatorProvider<? extends T> delegate, Type type);
    }
}
