package ru.serge2nd.function;

import static java.lang.invoke.MethodHandles.lookup;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

public class Functions {
    private Functions() { throw errNotInstantiable(lookup()); }

    @FunctionalInterface
    public interface ToBoolFunction<T>  { boolean asBoolean(T x); }
    @FunctionalInterface
    public interface ToCharFunction<T>  { char asChar(T x); }
    @FunctionalInterface
    public interface ToByteFunction<T>  { byte asByte(T x); }
    @FunctionalInterface
    public interface ToShortFunction<T> { short asShort(T x); }
    @FunctionalInterface
    public interface ToFloatFunction<T> { float asFloat(T x); }
}
