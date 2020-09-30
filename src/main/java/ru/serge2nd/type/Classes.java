package ru.serge2nd.type;

import lombok.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.lookup;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ReflectionUtils.getUniqueDeclaredMethods;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;

public class Classes {
    private Classes() { throw errNotInstantiable(lookup().lookupClass()); }

    public static Class<?> arrayClass(Class<?> component, int dims) {
        return arrayClass(component, dims, null);
    }

    public static Class<?> arrayClass(Class<?> component, int dims, ClassLoader classLoader) {
        return resolveClassName(arrayClassName(component, dims), classLoader);
    }

    public static String arrayClassName(@NonNull Class<?> component, int dims) {
        if (dims < 1) return component.getName();

        while (component.isArray()) {
            component = component.getComponentType();
            dims++;
        }

        String descriptor = descriptor(component);
        StringBuilder result = new StringBuilder(dims + descriptor.length());
        for(int i = 0; i < dims; i++) result.append(Types.ARRAY_MARKER);

        return result.append(descriptor).toString();
    }

    public static String descriptor(@NonNull Class<?> cls) {
        if (cls.isPrimitive()) {
            if (boolean.class == cls) {
                return "Z";
            } else if (char.class == cls) {
                return "C";
            } else if (byte.class == cls) {
                return "B";
            } else if (short.class == cls) {
                return "S";
            } else if (int.class == cls) {
                return "I";
            } else if (long.class == cls) {
                return "J";
            } else if (double.class == cls) {
                return "D";
            } else if (float.class == cls) {
                return "F";
            } else if (void.class == cls) {
                return "V";
            }
        } else if (!cls.isArray()) {
            return "L" + cls.getName() + ";";
        }
        throw new IllegalArgumentException("cannot determine descriptor of " + cls.getName());
    }

    /**
     * Calls {@link ClassUtils#getUserClass(Class)} with the previous result (initially the given class)
     * till the original class (not a framework-generated proxy) is returned.<br>
     * This method is preferred to the mentioned above because
     * the latter does not consider the case of multiple subclasses chain.
     * @param cls the class
     * @return the same class if it's not a framework-generated proxy
     *         or the original class otherwise
     * @see ClassUtils#getUserClass(Class)
     */
    public static Class<?> getUserClass(Class<?> cls) {
        Class<?> result;
        while ((result = ClassUtils.getUserClass(cls)) != cls)
            cls = result;
        return result;
    }

    /**
     * @see org.springframework.util.ReflectionUtils#getUniqueDeclaredMethods(Class, MethodFilter)
     */
    public static Method findUniqueMethod(Class<?> cls, MethodFilter methodFilter) {
        Method[] resolved = getUniqueDeclaredMethods(cls, methodFilter);
        if (resolved.length != 1) return null; // not unique
        return resolved[0];
    }
}
