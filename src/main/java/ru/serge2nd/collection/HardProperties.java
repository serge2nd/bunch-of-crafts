package ru.serge2nd.collection;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.stream.IntStream.range;

/**
 * An unmodifiable {@link Properties}. Can store any object or {@code null} as a value.<br>
 * Use the {@link HardProperties#toMap()} to get the underlying unmodifiable map.
 */
public final class HardProperties extends Properties implements Serializable {
    public static final HardProperties EMPTY = new HardProperties(emptyMap());

    private final Map<String, Object> map;

    // region Factory methods

    /**
     * Creates unmodifiable properties from the given key-val pair sequence.
     * @param keyValSeq <code>{key1, val1, key2, val2, ...}</code>
     * @return a {@link HardProperties} created from the given key-val pair sequence
     */
    public static HardProperties properties(@NonNull Object... keyValSeq) {
        if (keyValSeq.length == 0)
            return EMPTY;
        if (keyValSeq.length % 2 != 0)
            throw new IllegalArgumentException("no value for the last key in " + Arrays.toString(keyValSeq));
        if (keyValSeq.length == 2)
            return new HardProperties(propertyKey(keyValSeq[0]), keyValSeq[1]);

        return new HardProperties(range(0, keyValSeq.length/2).collect(
                HashMap::new, (m, i) -> m
                .put(propertyKey(keyValSeq[2*i]), keyValSeq[2*i + 1]),
                Map::putAll),
                false);
    }

    /**
     * Creates unmodifiable properties copying entries from the given maps.
     * Non-string keys are ignored as well as null maps.
     * @param sources property sources represented by the {@link Map} objects
     * @return a {@link HardProperties} created from the given sources
     */
    public static HardProperties from(@NonNull Map<?, ?>... sources) {
        if (allEmpty(sources)) return EMPTY;
        @SuppressWarnings("unchecked,rawtypes")
        Map<String, Object> src = stream((Map[])sources).reduce(
            new HashMap<>(), (result, source) -> {
                if (source != null) source.forEach((k, v) -> {if (isPropertyKey(k)) result.put(k, v);
            }); return result; });

        int size = src.size();
        if (size == 0)
            return EMPTY;
        if (size == 1)
            return new HardProperties(toSingletonMap(src));

        return new HardProperties(src, false);
    }

    /**
     * Creates unmodifiable properties backed by the given map (no copying).
     * @param src map to base on
     * @return a {@link HardProperties} backed by the given map
     */
    @SuppressWarnings("unchecked")
    public static HardProperties of(@NonNull Map<?, ?> src) {
        return emptyMap() == src || emptySortedMap() == src || emptyNavigableMap() == src
                ? EMPTY : new HardProperties((Map<String, Object>)src, false);
    }
    // endregion

    //region Constructors & related helpers

    private HardProperties(Map<String, Object> map)               { this.map = map; }
    private HardProperties(String key, Object val)                { this.map = singletonMap(key, val); }
    private HardProperties(Map<String, Object> src, boolean copy) { this.map = unmodifiableMap(copy ? new HashMap<>(src) : src); }

    private static Map<String, Object> toSingletonMap(Map<String, Object> src) {
        String key = src.keySet().iterator().next();
        return singletonMap(key, src.get(key));
    }
    private static boolean allEmpty(Map<?, ?>[] maps) {
        for (Map<?, ?> map : maps)
            if (map != null && !map.isEmpty()) return false;
        return true;
    }
    //endregion

    //region Single value

    @Override
    public int     size()                                        { return map.size(); }
    @Override
    public boolean isEmpty()                                     { return map.isEmpty(); }
    @Override
    public String  getProperty(String key)                       { return asString(key, map.get(key)); }
    @Override
    public String  getProperty(String key, String defaultValue)  { return asString(key, map.getOrDefault(key, defaultValue)); }
    @Override
    public Object  get(Object key)                               { return map.get(propertyKey(key)); }
    @Override
    public Object  getOrDefault(Object key, Object defaultValue) { return map.getOrDefault(propertyKey(key), defaultValue); }
    @Override
    public boolean contains(Object value)                        { return this.containsValue(value); }
    @Override
    public boolean containsValue(Object value)                   { return map.containsValue(value); }
    @Override
    public boolean containsKey(Object key)                       { return map.containsKey(key); }
    //endregion

    //region Iterables

    /** Returns the underlying unmodifiable map. */ @SuppressWarnings("unchecked")
    public <V> Map<String, V>             toMap()                          { return (Map<String, V>)map; }
    @Override
    public void forEach(BiConsumer<? super Object, ? super Object> action) { map.forEach(action); }
    @Override
    public Enumeration<?>                 propertyNames()                  { return this.keys(); }
    @Override
    public Set<String>                    stringPropertyNames()            { return map.keySet(); }
    @Override
    public Collection<Object>             values()                         { return map.values(); }
    @Override
    public Enumeration<Object>            keys()                           { return enumeration(this.keySet()); }
    @Override
    public Enumeration<Object>            elements()                       { return enumeration(this.values()); }
    @Override @SuppressWarnings("unchecked,rawtypes")
    public Set<Object>                    keySet()                         { return (Set)map.keySet(); }
    @Override @SuppressWarnings("unchecked,rawtypes")
    public Set<Map.Entry<Object, Object>> entrySet()                       { return (Set)map.entrySet(); }
    //endregion

    //region java.lang.Object methods

    @Override
    public String  toString()       { return map.toString(); }
    @Override
    public int     hashCode()       { return map.hashCode(); }
    @Override @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) { return this == o || map.equals(o); }
    @Override @SneakyThrows
    public Object  clone()          { throw new CloneNotSupportedException("use from() to create a shallow copy"); }
    //endregion

    //region Private helpers & serialization

    private static boolean isPropertyKey(Object key) { return key instanceof String; }
    private static String propertyKey(Object key) {
        if (!isPropertyKey(key))
            throw new IllegalArgumentException("expected non-null String as property key");
        return (String)key;
    }
    private static String asString(String key, Object val) {
        if (!(val == null || val instanceof String))
            throw new ClassCastException(key + ": expected a string, got " + val.getClass().getName());
        return (String)val;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(map);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Field fMap = HardProperties.class.getDeclaredField("map");
        fMap.setAccessible(true);
        fMap.set(this, in.readObject());
        fMap.setAccessible(false);
    }
    private Object readResolve() {
        if (Collections.<String, Object>emptyMap() == map)
            return EMPTY;
        return this;
    }
    //endregion

    //region Unsupported operations

    @Override
    public Object  setProperty(String key, String value)                              { throw errNotModifiable(); }
    @Override
    public Object  put(Object key, Object value)                                      { throw errNotModifiable(); }
    @Override
    public Object  putIfAbsent(Object key, Object value)                              { throw errNotModifiable(); }
    @Override
    public Object  replace(Object key, Object value)                                  { throw errNotModifiable(); }
    @Override
    public boolean replace(Object key, Object oldValue, Object newValue)              { throw errNotModifiable(); }
    @Override
    public void    replaceAll(BiFunction<? super Object, ? super Object, ?> function) { throw errNotModifiable(); }

    @Override
    public Object  compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction)             { throw errNotModifiable(); }
    @Override
    public Object  computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction)                         { throw errNotModifiable(); }
    @Override
    public Object  computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction)    { throw errNotModifiable(); }
    @Override
    public Object  merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) { throw errNotModifiable(); }
    @Override
    public Object  remove(Object key)               { throw errNotModifiable(); }
    @Override
    public boolean remove(Object key, Object value) { throw errNotModifiable(); }
    @Override
    public void    putAll(Map<?, ?> t)              { throw errNotModifiable(); }
    @Override
    public void    clear()                          { throw errNotModifiable(); }

    @Override
    public void load(Reader reader)                                          { throw errNotModifiable(); }
    @Override
    public void load(InputStream inStream)                                   { throw errNotModifiable(); }
    @Override
    public void loadFromXML(InputStream in)                                  { throw errNotModifiable(); }
    @Override @SuppressWarnings("deprecation")
    public void save(OutputStream out, String comments)                      { throw errNotListable(); }
    @Override
    public void store(Writer writer, String comments)                        { throw errNotListable(); }
    @Override
    public void store(OutputStream out, String comments)                     { throw errNotListable(); }
    @Override
    public void storeToXML(OutputStream os, String comment)                  { throw errNotListable(); }
    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) { throw errNotListable(); }
    // @Override // since 10
    public void storeToXML(OutputStream os, String comment, Charset charset) { throw errNotListable(); }
    @Override
    public void list(PrintStream out)                                        { throw errNotListable(); }
    @Override
    public void list(PrintWriter out)                                        { throw errNotListable(); }

    private static UnsupportedOperationException errNotModifiable() { return new UnsupportedOperationException("these properties not modifiable"); }
    private static UnsupportedOperationException errNotListable()   { return new UnsupportedOperationException("these properties not listable"); }
    //endregion
}
