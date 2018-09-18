package com.programyourhome.adventureroom.model.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import one.util.streamex.StreamEx;

@SuppressWarnings("unchecked")
public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static <T> Class<T> classForNameNoCheckedException(String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class not found", e);
        }
    }

    public static <T> T callConstructorNoCheckedException(Class<T> clazz) {
        return callConstructorNoCheckedException(clazz, new Class<?>[0], new Object[0]);
    }

    public static <T, R> T callConstructorNoCheckedException(Class<T> clazz, Class<R> parameterType, R argument) {
        return callConstructorNoCheckedException(clazz, new Class<?>[] { parameterType }, new Object[] { argument });
    }

    public static Object callConstructorNoCheckedExceptionUntypedParameter(Class<?> clazz, Class<?> externalClass, Object externalObject) {
        return callConstructorNoCheckedException(clazz, new Class<?>[] { externalClass }, new Object[] { externalObject });
    }

    public static <T> T callConstructorNoCheckedException(Class<T> clazz, Class<?>[] parameterTypes, Object[] arguments) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalStateException("Exception during calling constructor", e);
        }
    }

    public static <T> T callConstructorNoCheckedException(Class<T> clazz, Properties properties, BiFunction<String, Class<?>, Object> converter) {
        return callConstructorNoCheckedException(clazz, (Map<String, String>) (Map<?, ?>) properties, converter);
    }

    public static <T> T callConstructorNoCheckedException(Class<T> clazz, Map<String, String> inputParameters, BiFunction<String, Class<?>, Object> converter) {
        try {
            Constructor<?> matchedConstructor = StreamEx.of(clazz.getConstructors())
                    .filter(constructor -> isMatch(constructor, inputParameters.keySet()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("No matched constructor found for class [" + clazz + "] "
                            + "and properties [" + inputParameters + "]"));
            Object[] arguments = StreamEx.of(matchedConstructor.getParameters())
                    .mapToEntry(Parameter::getName)
                    .mapValues(inputParameters::get)
                    .mapKeyValue((parameter, parameterValue) -> converter.apply(parameterValue, parameter.getType()))
                    .toList().toArray();
            return (T) matchedConstructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            throw new IllegalStateException("Exception during calling constructor", e);
        }
    }

    private static boolean isMatch(Constructor<?> constructor, Set<String> propertyNames) {
        Arrays.asList(constructor.getParameters()).forEach(parameter -> {
            if (!parameter.isNamePresent()) {
                throw new IllegalStateException("Constructor parameter for class [" + constructor.getDeclaringClass() + "] does not have a name. "
                        + "Please make sure the module is compiled preserving parameter names.");
            }
        });
        Set<String> contructorParameterNames = StreamEx.of(constructor.getParameters())
                .map(Parameter::getName)
                .toSet();
        return contructorParameterNames.equals(propertyNames);
    }

    public static Class<?> getGenericParameter(Class<?> baseClass, Class<?> typedClass) {
        return getGenericParameter(baseClass, Arrays.asList(typedClass));
    }

    public static Class<?> getGenericParameter(Class<?> baseClass, Collection<Class<?>> typedClasses) {
        for (Class<?> typedClass : typedClasses) {
            if (typedClass.getTypeParameters().length != 1) {
                throw new IllegalStateException("Typed class [" + typedClass + "] should contain exactly one type parameter");
            }
        }
        Set<ParameterizedType> genericSuperTypes = new HashSet<>();
        populateGenericSuperTypes(baseClass, genericSuperTypes);

        return StreamEx.of(genericSuperTypes)
                .filter(type -> typedClasses.contains(type.getRawType()))
                .filter(type -> type.getActualTypeArguments()[0] instanceof Class)
                .map(type -> (Class<?>) type.getActualTypeArguments()[0])
                .findFirst().orElseThrow(() -> new IllegalStateException("Typed classes " + typedClasses + " not found in hierarchy of [" + baseClass + "] "
                        + "or type parameter is not instantiated"));
    }

    public static Class<?> getGenericParameterWithSuperClass(Class<?> baseClass, Class<?> superClassToSearch) {
        Set<ParameterizedType> genericSuperTypes = new HashSet<>();
        populateGenericSuperTypes(baseClass, genericSuperTypes);
        return StreamEx.of(genericSuperTypes)
                .flatMap(type -> StreamEx.of(type.getActualTypeArguments()))
                .filter(type -> type instanceof Class)
                .map(type -> (Class<?>) type)
                .filter(superClassToSearch::isAssignableFrom)
                .findFirst().orElseThrow(() -> new IllegalStateException("No actual type parameter found in hierarchy of [" + baseClass + "] "
                        + "with super class [" + superClassToSearch + "]"));
    }

    private static void populateGenericSuperTypes(Class<?> baseClass, Set<ParameterizedType> genericSuperTypes) {
        // Whether or not to continue upwards in the tree.
        Function<Type, Boolean> include = type -> type != null && type instanceof ParameterizedType;
        // Check the super class of the base class.
        Type genericSuperClass = baseClass.getGenericSuperclass();
        if (include.apply(genericSuperClass)) {
            genericSuperTypes.add((ParameterizedType) genericSuperClass);
        }
        if (baseClass.getSuperclass() != null) {
            populateGenericSuperTypes(baseClass.getSuperclass(), genericSuperTypes);
        }
        // Check all implemented interfaces of the base class.
        for (Type genericSuperInterface : baseClass.getGenericInterfaces()) {
            if (include.apply(genericSuperInterface)) {
                genericSuperTypes.add((ParameterizedType) genericSuperInterface);
            }
        }
        for (Class<?> superInterface : baseClass.getInterfaces()) {
            populateGenericSuperTypes(superInterface, genericSuperTypes);
        }
    }

}
