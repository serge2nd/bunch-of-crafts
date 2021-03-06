package ru.serge2nd;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.platform.commons.util.ReflectionUtils.newInstance;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.unsupported;

@SuppressWarnings("unused")
public interface NoInstanceTest<T> {

    @Test default void notInstantiable() {
        for (Type t : this.getClass().getGenericInterfaces()) if (t instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)t;

            if (NoInstanceTest.class == p.getRawType()) {
                Class<?> clazz = extractClass(p.getActualTypeArguments()[0]);
                if (clazz == null) break;

                assertThat(()->newInstance(clazz), unsupported()); return;
            }
        }
        throw new IllegalStateException("implement " + NoInstanceTest.class.getSimpleName() + " with concrete type arg");
    }

    static Class<?> extractClass(Type t) {
        if (t instanceof Class)             return (Class<?>)t;
        if (t instanceof ParameterizedType) return (Class<?>)((ParameterizedType)t).getRawType();
        return null;
    }
}
