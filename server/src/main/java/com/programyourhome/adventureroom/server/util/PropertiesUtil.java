package com.programyourhome.adventureroom.server.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;

public class PropertiesUtil {

    private PropertiesUtil() {
    }

    // TODO: instead of all possible overloads, use some kind of config that can be initialized with files, streams, targets, etc.
    // Always return populated object, only create one when target is not set yet.
    public static <T> T loadPropertiesIntoFields(String input, Class<T> targetClass, ConversionService conversionService) {
        return loadPropertiesIntoFields(new ByteArrayInputStream(input.getBytes()), targetClass, conversionService);
    }

    public static <T> T loadPropertiesIntoFields(InputStream inputStream, Class<T> targetClass, ConversionService conversionService) {
        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            loadPropertiesIntoFields(inputStream, instance, conversionService);
            return instance;
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Exception during loading of properties into fields", e);
        }
    }

    public static void loadPropertiesIntoFields(InputStream inputStream, Object target, ConversionService conversionService) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);

            for (String propertyName : properties.stringPropertyNames()) {
                loadPropertyIntoObject(properties, propertyName, target, conversionService);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Exception during loading of properties into fields", e);
        }
    }

    public static void loadPropertyIntoObject(Properties properties, String propertyName, Object target, ConversionService conversionService) {
        try {
            Map<String, Field> targetFields = new HashMap<>();
            ReflectionUtils.doWithFields(target.getClass(), field -> targetFields.put(field.getName(), field));

            if (!targetFields.containsKey(propertyName)) {
                throw new IllegalStateException("No field '" + propertyName + "' found on type: '" + target.getClass() + "'");
            }
            Field targetField = targetFields.get(propertyName);
            targetField.setAccessible(true);
            Object value = conversionService.convert(properties.getProperty(propertyName), targetField.getType());
            targetField.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Exception during loading of properties into fields", e);
        }
    }

}
