package ru.serge2nd.bean.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import ru.serge2nd.collection.HardProperties;
import ru.serge2nd.type.TypeWrap;
import ru.serge2nd.bean.BeanCfg;
import ru.serge2nd.bean.definition.BeanDefinitionFactory;
import ru.serge2nd.bean.definition.BeanDefinitionFactoryImpl;
import ru.serge2nd.function.DelegatingOperatorProvider;
import ru.serge2nd.function.OperatorProvider;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static org.springframework.util.ReflectionUtils.getDeclaredMethods;
import static ru.serge2nd.bean.BeanCfg.builder;
import static ru.serge2nd.bean.BeanCfg.from;
import static ru.serge2nd.bean.definition.BeanDefinitionHelper.annotatedBeanDefinitions;
import static ru.serge2nd.test.matcher.AssertThat.assertThat;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;
import static ru.serge2nd.test.matcher.CommonMatch.sameClass;

@TestInstance(Lifecycle.PER_CLASS)
class WrapBeanPostProcessorTest {
    @Qualifier("immutableCollection")        @Autowired Collection<String> immutableCollection;
    @Qualifier("immutableList")              @Autowired List<String> immutableList;
    @Qualifier("immutableSet")               @Autowired Set<String> immutableSet;
    @Qualifier("immutableSortedSet")         @Autowired SortedSet<String> immutableSortedSet;
    @Qualifier("immutableNavigableSet")      @Autowired NavigableSet<String> immutableNavigableSet;
    @Qualifier("immutableMap")               @Autowired Map<String, Integer> immutableMap;
    @Qualifier("immutableSortedMap")         @Autowired SortedMap<String, Integer> immutableSortedMap;
    @Qualifier("immutableNavigableMap")      @Autowired NavigableMap<String, Integer> immutableNavigableMap;
    @Qualifier("immutableStringToStringMap") @Autowired Map<String, String> immutableStringToStringMap;
    @Qualifier("immutableProperties")        @Autowired Properties immutableProperties;
    @Autowired StringBuilder notImmutableBean;

    static final Collection<String> EXPECTED = singleton("z");
    static final Map<String, Integer> EXPECTEDM = singletonMap("z", "z".codePointAt(0));
    static final Map<String, String> EXPECTED_STRINGS = singletonMap("z", "z");

    final TestBeanFactory beanFactory = new TestBeanFactory("testCtx"); {
        beanFactory.addBeanPostProcessor(new WrapBeanPostProcessor(ImmutableFilter.INSTANCE, (type, bean) -> ofNullable(type)
                .flatMap(IMMUTABLES::forType)
                .map(w -> w.apply(bean))
                .orElse(bean), beanFactory));
        registerImmutableBeans(beanFactory);
        beanFactory.forceAnnotatedBeanDefinitions();
        beanFactory.autowireBean(this);
    }

    @Test void testImmutableCollection() { assertThat(
        immutableCollection               , sameClass(unmodifiableCollection(emptySet())),
        new HashSet<>(immutableCollection), equalTo(EXPECTED));
    }

    @Test void testImmutableList() { assertThat(
        immutableList               , sameClass(unmodifiableList(emptyList())),
        new HashSet<>(immutableList), equalTo(EXPECTED));
    }

    @Test void testImmutableSet() { assertThat(
        immutableSet               , sameClass(unmodifiableSet(emptySet())),
        new HashSet<>(immutableSet), equalTo(EXPECTED));
    }

    @Test void testImmutableSortedSet() { assertThat(
        immutableSortedSet               , sameClass(unmodifiableSortedSet(emptySortedSet())),
        new HashSet<>(immutableSortedSet), equalTo(EXPECTED));
    }

    @Test void testImmutableNavigableSet() { assertThat(
        immutableNavigableSet               , sameClass(unmodifiableNavigableSet(emptyNavigableSet())),
        new HashSet<>(immutableNavigableSet), equalTo(EXPECTED));
    }

    @Test void testImmutableMap() { assertThat(
        immutableMap               , sameClass(unmodifiableMap(emptyMap())),
        new HashMap<>(immutableMap), equalTo(EXPECTEDM));
    }

    @Test void testImmutableSortedMap() { assertThat(
        immutableSortedMap               , sameClass(unmodifiableSortedMap(emptySortedMap())),
        new HashMap<>(immutableSortedMap), equalTo(EXPECTEDM));
    }

    @Test void testImmutableNavigableMap() { assertThat(
        immutableNavigableMap               , sameClass(unmodifiableNavigableMap(emptyNavigableMap())),
        new HashMap<>(immutableNavigableMap), equalTo(EXPECTEDM));
    }

    @Test void testImmutableStringToStringMap() { assertThat(
        immutableStringToStringMap               , sameClass(HardProperties.class),
        new HashMap<>(immutableStringToStringMap), equalTo(EXPECTED_STRINGS));
    }

    @Test void testImmutableProperties() { assertThat(
        immutableProperties               , sameClass(HardProperties.class),
        new HashMap<>(immutableProperties), equalTo(EXPECTED_STRINGS));
    }

    @Test void testNotImmutableBean() { assertThat(notImmutableBean, sameClass(StringBuilder.class)); }

    void registerImmutableBeans(TestBeanFactory bf) {
        for (Method m : getDeclaredMethods(bf.getClass()))
            if (m.isAnnotationPresent(Immutable.class))
                bf.registerBean(builder()
                        .name(m.getName())
                        .factoryMethod(m.getName())
                        .factoryBean(bf.name)
                        .build());
    }

    static class TestBeanFactory extends DefaultListableBeanFactory {
        final String name;
        TestBeanFactory(String name) {
            this.name = name;
            registerBean(new AutowiredAnnotationBeanPostProcessor());
            registerBean(from(this, name).build());
        }

        @Immutable Collection<String> immutableCollection() { return new AbstractCollection<String>() {
            public int              size()     { return EXPECTED.size(); }
            public boolean          isEmpty()  { return EXPECTED.isEmpty(); }
            public Iterator<String> iterator() { return EXPECTED.iterator(); }}; }
        @Immutable List<String>                  immutableList()              { return new ArrayList<>(EXPECTED); }
        @Immutable Set<String>                   immutableSet()               { return new HashSet<>(EXPECTED); }
        @Immutable SortedSet<String>             immutableSortedSet()         { return new TreeSet<>(EXPECTED); }
        @Immutable NavigableSet<String>          immutableNavigableSet()      { return new TreeSet<>(EXPECTED); }
        @Immutable Map<String, Integer>          immutableMap()               { return new HashMap<>(EXPECTEDM); }
        @Immutable SortedMap<String, Integer>    immutableSortedMap()         { return new TreeMap<>(EXPECTEDM); }
        @Immutable NavigableMap<String, Integer> immutableNavigableMap()      { return new TreeMap<>(EXPECTEDM); }
        @Immutable Map<String, String>           immutableStringToStringMap() { return new HashMap<>(EXPECTED_STRINGS); }
        @Immutable Properties                    immutableProperties()        { return new Properties(){{putAll(EXPECTED_STRINGS);}}; }
        @Immutable StringBuilder                 notImmutableBean()           { return new StringBuilder(); }

        void registerBean(BeanCfg beanCfg) {
            BeanDefinitionFactory.registerBean(beanCfg, BeanDefinitionFactoryImpl.INSTANCE, this::registerBeanDefinition);
        }
        @SuppressWarnings("ConstantConditions")
        void registerBean(BeanPostProcessor beanPostProcessor) {
            addBeanPostProcessor((BeanPostProcessor)initializeBean(beanPostProcessor, null));
        }
        void forceAnnotatedBeanDefinitions() {
            annotatedBeanDefinitions(this::getMergedBeanDefinition, this::getType,
                    getBeanDefinitionNames()).forEach(this::registerBeanDefinition);
        }
    }

    static final OperatorProvider<Object> IMMUTABLES = new DelegatingOperatorProvider<Object>() {{
        addDelegate(Properties.class,   $ -> Optional.of(HardProperties::of));
        addDelegate(NavigableMap.class, $ -> Optional.of(Collections::<Object, Object>unmodifiableNavigableMap));
        addDelegate(SortedMap.class,    $ -> Optional.of(Collections::<Object, Object>unmodifiableSortedMap));
        addDelegate(Map.class,          $ -> Optional.of(Collections::<Object, Object>unmodifiableMap));
        addPrimaryDelegate(
                TypeWrap.of(Map.class, String.class, String.class),
                $ -> Optional.of(HardProperties::of));
        addDelegate(NavigableSet.class, $ -> Optional.of(Collections::<Object>unmodifiableNavigableSet));
        addDelegate(SortedSet.class,    $ -> Optional.of(Collections::<Object>unmodifiableSortedSet));
        addDelegate(Set.class,          $ -> Optional.of(Collections::<Object>unmodifiableSet));
        addDelegate(List.class,         $ -> Optional.of(Collections::<Object>unmodifiableList));
        addDelegate(Collection.class,   $ -> Optional.of(Collections::<Object>unmodifiableCollection));
    }};
}
