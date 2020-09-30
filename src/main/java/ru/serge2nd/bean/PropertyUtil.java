package ru.serge2nd.bean;

import lombok.NonNull;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.MapCollectors.toMap;
import static ru.serge2nd.stream.util.CollectingOptions.NON_NULL;
import static ru.serge2nd.stream.util.CollectingOptions.UNMODIFIABLE;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

/**
 * Utility methods working with properties, especially collecting them from some sources.
 */
public class PropertyUtil {
    private PropertyUtil() { throw errNotInstantiable(lookup().lookupClass()); }

    public static <V> PropertyOrigin<V> propertyOrigin(Function<String, V> propertyGetter, String... prefixes) {
        return propertyOrigin(propertyGetter, NAME, prefixes);
    }
    public static <V> PropertyOrigin<V> propertySuffixOrigin(Function<String, V> propertyGetter, String... prefixes) {
        return propertyOrigin(propertyGetter, SUFFIX, prefixes);
    }
    public static <V> PropertyOrigin<V> propertyOrigin(@NonNull Function<String, V> propertyGetter,
                                                       @NonNull NameTransformer transformer,
                                                       @NonNull String... prefixes) {
        return names -> collect(names, toMap(
                name -> transformer.apply(name, findPrefix(name, prefixes)),
                propertyGetter,
                NON_NULL | UNMODIFIABLE));
    }

    public static String findPrefix(String name, String... prefixes) {
        if (name == null || prefixes == null) return null;
        for (String prefix : prefixes)
            if (prefix != null && name.startsWith(prefix))
                return prefix;
        return null;
    }

    @FunctionalInterface
    public interface PropertyOrigin<V> extends Function<Iterable<String>, Map<String, V>> {
        Map<String, V>         get(Iterable<String> names);
        default Map<String, V> apply(Iterable<String> names) { return get(names); }
    }
    @FunctionalInterface
    public interface NameTransformer extends BinaryOperator<String> {
        String         applyTo(String name, String prefix);
        default String apply(String name, String prefix) { return applyTo(name, prefix); }
    }

    private static final NameTransformer NAME   = (name, prefix) -> prefix != null ? name : null;
    private static final NameTransformer SUFFIX = (name, prefix) -> prefix != null && name != null ? name.substring(prefix.length()) : null;
}
