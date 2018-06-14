package com.programyourhome.adventureroom.dsl.util;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtil {

    private ReflectionUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForNameNoCheckedException(String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class not found", e);
        }
    }

    public static <T> T callConstructorNoCheckedException(Class<?> clazz) {
        return callConstructorNoCheckedException(clazz, new Class<?>[0], new Object[0]);
    }

    public static <T, R> T callConstructorNoCheckedException(Class<?> clazz, Class<R> parameterType, R argument) {
        return callConstructorNoCheckedException(clazz, new Class<?>[] { parameterType }, new Object[] { argument });
    }

    @SuppressWarnings("unchecked")
    public static <T> T callConstructorNoCheckedException(Class<?> clazz, Class<?>[] parameterTypes, Object[] arguments) {
        try {
            return (T) clazz.getDeclaredConstructor(parameterTypes).newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalStateException("Exception during calling constructor", e);
        }
    }

}
