package ru.serge2nd.collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.serge2nd.collection.Unmodifiable.UnmodifiableArrayListImpl;
import ru.serge2nd.collection.Unmodifiable.UnmodifiableCollectionImpl;
import ru.serge2nd.collection.Unmodifiable.UnmodifiableListImpl;
import ru.serge2nd.collection.Unmodifiable.UnmodifiableSetImpl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Comparator.naturalOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.support.ModifierSupport.isFinal;
import static org.springframework.data.util.ReflectionUtils.findRequiredMethod;
import static org.springframework.util.ReflectionUtils.findMethod;
import static ru.serge2nd.test.Asserting.assertEach;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.equalTo;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;
import static ru.serge2nd.test.match.CommonMatch.sameClass;

@TestInstance(Lifecycle.PER_CLASS)
class UnmodifiableTest {
    static final Object[]      A = {5, 7};
    static final List<Object>  L = asList(A);
    static final Set<?>        S = new HashSet<>(L);
    static final Collection<?> C = new AbstractCollection<Object>() {
        public int              size()     { return L.size(); }
        public boolean          isEmpty()  { return L.isEmpty(); }
        public Iterator<Object> iterator() { return L.iterator(); }};

    @Test void testOf() {
        List<?> u = Unmodifiable.of(A);
        assertThat(u, sameClass(UnmodifiableArrayListImpl.class), equalTo(L));
    }
    @Test void testOfEmpty() { assertSame(emptyList(), Unmodifiable.of()); }

    @Test void testOfList() {
        List<?> u = Unmodifiable.ofList(L);
        assertThat(u, sameClass(UnmodifiableListImpl.class), equalTo(L));
    }
    @Test void testOfEmptyList()               { assertSame(emptyList(), Unmodifiable.ofList(emptyList())); }
    @Test void testOfSingletonList()           { List<?> l = singletonList(5)   ; assertSame(l, Unmodifiable.ofList(l)); }
    @Test void testOfUnmodifiableList()        { List<?> u = unmodifiableList(L); assertSame(u, Unmodifiable.ofList(u)); }
    @Test void testOfAlreadyUnmodifiableList() { List<?> u = Unmodifiable.of(A) ; assertSame(u, Unmodifiable.ofList(u)); }

    @Test void testOfSet() {
        Set<?> u = Unmodifiable.ofSet(S);
        assertThat(u, sameClass(UnmodifiableSetImpl.class), equalTo(S));
    }
    @Test void testOfEmptySet()            { assertSame(emptySet(), Unmodifiable.ofSet(emptySet())); }
    @Test void testOfSingleton()           { Set<?> s = singleton(5)         ; assertSame(s, Unmodifiable.ofSet(s)); }
    @Test void testOfUnmodifiable()        { Set<?> u = unmodifiableSet(S)   ; assertSame(u, Unmodifiable.ofSet(u)); }
    @Test void testOfAlreadyUnmodifiable() { Set<?> u = Unmodifiable.ofSet(S); assertSame(u, Unmodifiable.ofSet(u)); }

    @Test void testOfCollection() {
        Collection<?> u = Unmodifiable.ofCollection(C);
        assertThat(
        u                 , sameClass(UnmodifiableCollectionImpl.class),
        new ArrayList<>(u), equalTo(L));
    }
    @Test void testOfListAsCollection()              { assertSame(UnmodifiableListImpl.class, Unmodifiable.ofCollection(L).getClass()); }
    @Test void testOfSetAsCollection()               { assertSame(UnmodifiableSetImpl.class, Unmodifiable.ofCollection(S).getClass()); }
    @Test void testOfUnmodifiableCollection()        { Collection<?> u = unmodifiableCollection(C); assertSame(u, Unmodifiable.ofCollection(u)); }
    @Test void testOfAlreadyUnmodifiableCollection() { Collection<?> u = Unmodifiable.ofCollection(C); assertSame(u, Unmodifiable.ofCollection(u)); }

    @SuppressWarnings("unchecked,rawtypes")
    static List<Arguments> mutatorsProvider() { return asList(
        arguments("add"       , Collection.class, new Class[]{Object.class}               , f(c -> c.add(null))),
        arguments("addAll"    , Collection.class, new Class[]{Collection.class}           , f(c -> c.addAll(emptySet()))),
        arguments("remove"    , Collection.class, new Class[]{Object.class}               , f(c -> c.remove(null))),
        arguments("removeIf"  , Collection.class, new Class[]{Predicate.class}            , f(c -> c.removeIf(e->false))),
        arguments("removeAll" , Collection.class, new Class[]{Collection.class}           , f(c -> c.removeAll(emptySet()))),
        arguments("retainAll" , Collection.class, new Class[]{Collection.class}           , f(c -> c.retainAll(emptySet()))),
        arguments("clear"     , Collection.class, new Class[0]                            , a(Collection::clear)),
        arguments("add"       , List.class      , new Class[]{int.class, Object.class}    , a((List l) -> l.add(0, null))),
        arguments("set"       , List.class      , new Class[]{int.class, Object.class}    , f((List l) -> l.set(0, null))),
        arguments("addAll"    , List.class      , new Class[]{int.class, Collection.class}, f((List l) -> l.addAll(0, emptySet()))),
        arguments("remove"    , List.class      , new Class[]{int.class}                  , f((List l) -> l.remove(0))),
        arguments("replaceAll", List.class      , new Class[]{UnaryOperator.class}        , a((List l) -> l.replaceAll(e -> e))),
        arguments("sort"      , List.class      , new Class[]{Comparator.class}           , a((List l) -> l.sort(naturalOrder())))); }
    @SuppressWarnings("unchecked,rawtypes")
    @ParameterizedTest(name = "[{index}]{0}") @MethodSource("mutatorsProvider")
    void testMutators(String name, Class<?> cls, Class<?>[] args, Object op) {
        List<?> l = new ArrayList<>(singleton(null)), u = Unmodifiable.ofList(l);
        assertEach(() ->
        assertNotNull(findMethod(cls, name, args), "method not found"), () ->
        assertTrue(isFinal(findRequiredMethod(Unmodifiable.class, name, args)), "must be final"), () ->
        assertThrows(UnsupportedOperationException.class, ()-> { if (op instanceof Function)
            ((Function)op).apply(u); else ((Consumer)op).accept(u); }), () ->
        assertDoesNotThrow(()-> { if (op instanceof Function)
            ((Function)op).apply(l); else ((Consumer)op).accept(l); }));
    }

    @Test @SuppressWarnings("ConstantConditions")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> Unmodifiable.of((Object[])null),
        () -> Unmodifiable.ofCollection(null),
        () -> Unmodifiable.ofList(null),
        () -> Unmodifiable.ofSet(null));
    }

    @SuppressWarnings("rawtypes")
    static Consumer<? extends Collection>        a(Consumer<? extends Collection> f)    { return f; }
    @SuppressWarnings("rawtypes")
    static <R> Function<? extends Collection, R> f(Function<? extends Collection, R> f) { return f; }
}