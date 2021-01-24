package ru.serge2nd.stream.util;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ru.serge2nd.NoInstanceTest;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
class ArrayAccumulatorsTest implements NoInstanceTest<ArrayAccumulators> {
}