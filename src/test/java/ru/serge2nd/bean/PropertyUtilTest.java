package ru.serge2nd.bean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import java.time.LocalDate;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.serge2nd.bean.PropertyUtil.NameTransformer;
import static ru.serge2nd.bean.PropertyUtil.findPrefix;
import static ru.serge2nd.bean.PropertyUtil.propertyOrigin;
import static ru.serge2nd.bean.PropertyUtil.propertySuffixOrigin;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.test.matcher.AssertForMany.assertForMany;
import static ru.serge2nd.test.matcher.AssertMatches.assertMatches;
import static ru.serge2nd.test.matcher.CommonMatch.illegalArgument;

@TestInstance(Lifecycle.PER_CLASS)
class PropertyUtilTest implements NoInstanceTest<PropertyUtil> {
    static final LocalDate NOW = LocalDate.now();
    static final Properties SRC = properties(
            "str.one", "abc",
            "val.number.int", 7,
            "date.tomorrow", NOW.plusDays(1),
            "str.two", "xyz",
            "val.boolean", true,
            "date.today", NOW
    );

    @Test
    void testPropertyOrigin() {
        assertEquals(properties(
            "val.number.int", 7,
            "val.boolean", true,
            "date.today", NOW
        ), propertyOrigin(SRC::get,
            "val.", "abc.", null, "date.today"
        ).get(SRC.stringPropertyNames()));
    }

    @Test
    void testPropertySuffixOrigin() {
        assertEquals(properties(
            "number.int", 7,
            "boolean", true,
            "", NOW
        ), propertySuffixOrigin(SRC::get,
            "val.", "abc.", "date.today"
        ).get(SRC.stringPropertyNames()));
    }

    @Test
    void testComplicatedPropertyOrigin() {
        assertEquals(properties(
            "VAL.NUMBER.INT", 7,
            "VAL.BOOLEAN", true,
            "DATE.TODAY", NOW,
            "STR.TWO", "xyz"
        ), propertyOrigin(SRC::get, (name, p) -> p != null || name.endsWith("o") ? name.toUpperCase() : null,
            "val.", "abc.", "date.today"
        ).get(SRC.stringPropertyNames()));
    }

    @Test @SuppressWarnings("ConstantConditions")
    void testFindPrefixNullArgs() {
        assertMatches(nullValue(), findPrefix(null, ""), findPrefix("", (String[])null));
    }

    @Test @SuppressWarnings("ResultOfMethodCallIgnored")
    void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> propertyOrigin(null, (s, p) -> s),
        () -> propertyOrigin(s -> s, (NameTransformer)null),
        () -> propertyOrigin(s -> s, (s, p) -> s, (String[])null));
    }
}