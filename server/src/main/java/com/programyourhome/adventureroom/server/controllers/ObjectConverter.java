package com.programyourhome.adventureroom.server.controllers;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;

// Without the raw types (using <?>), the whole code would be a lot less readable and it doesn't add any compile time safety.
@SuppressWarnings("rawtypes")
@Component
public class ObjectConverter {

    @Inject
    private ObjectMapper objectMapper;

    private final MrBeanModule mrBeanModule;

    private final Map<Class, Class> collectionTypeChooser;
    private final Map<Class, Class> mapTypeChooser;

    public ObjectConverter() {
        this.mrBeanModule = new MrBeanModule();
        this.collectionTypeChooser = new HashMap<>();
        this.mapTypeChooser = new HashMap<>();

        this.collectionTypeChooser.put(Collection.class, ArrayList.class);
        this.collectionTypeChooser.put(List.class, ArrayList.class);
        this.collectionTypeChooser.put(Set.class, HashSet.class);

        this.mapTypeChooser.put(Map.class, HashMap.class);
        this.mapTypeChooser.put(SortedMap.class, TreeMap.class);
    }

    // TODO: make more generic, see:
    // https://github.com/ewjmulder/program-your-home/blob/master/server/src/main/java/com/programyourhome/server/aop/ServiceCleanupReturnValueAspect.java
    @SuppressWarnings({ "unchecked", "deprecation" })
    public synchronized <O> O convert(final Object input, final Class<O> outputClass) {
        try {
            final Object output;
            if (input == null) {
                output = null;
            } else {
                if (outputClass.isInterface()) {
                    // If we encounter an interface type: apply cleaning to the object.
                    // We don't check if the type is our 'own' interface type, because this logic should be universally applicable.
                    // We take the stand here that an interface object returned from a service, should never be more than a POJO
                    // and you should not be able to call any 'functional logic' methods on it.
                    // First, create a 'clean' interface implementation class, using the MrBean Jackson module.
                    final Class interfaceImplementation = this.mrBeanModule.getMaterializer().resolveAbstractType(this.objectMapper.getDeserializationConfig(),
                            this.objectMapper.getTypeFactory().constructSimpleType(outputClass, new JavaType[0])).getRawClass();
                    // Create a new instance of that class (always has public no-args constructor)
                    output = interfaceImplementation.newInstance();
                    // System.out.println("Copying from: " + value.getClass() + " to " + cleanedValue.getClass());
                    // Copy the properties from the 'dirty' to the clean value, thereby also cleaning these properties.
                    this.copyProperties(input, output);
                } else {
                    // Not a specific type we're interested in, so just pass as-is.
                    // It could be that this type has fields of a type we are interested in, e.g. in case of a 'custom' or unmapped
                    // type of collection. This is (for now) not supported. That does make sense, since we expect 'clean' interface definitions.
                    output = input;
                }
            }
            return (O) output;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Exception during conversion", e);
        }
    }

    /**
     * Copy the property values of the given source bean into the given target bean.
     * <p>
     * Note: The source and target classes do not have to match or even be derived from each other, as long as the properties match. Any bean properties that
     * the source bean exposes but the target bean does not will silently be ignored.
     *
     * @param source the source bean
     * @param target the target bean
     * @param editable the class (or interface) to restrict property setting to
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    // Copied from BeanUtils Spring class to allow for customization: no extra params and calling clean on the properties before copying them.
    private void copyProperties(final Object source, final Object target)
            throws BeansException {
        final PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(target.getClass());

        for (final PropertyDescriptor targetPd : targetPds) {
            final Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null) {
                final PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    final Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null &&
                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            final Object value = readMethod.invoke(source);

                            // TODO: document this adaption, clean up every copied value.
                            // Please note: we have to inspect the target type for the 'follow up types', because the target is the 'clean' type.
                            final Object cleanedValue = this.convert(value, writeMethod.getParameterTypes()[0]);

                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, cleanedValue);
                        } catch (final Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }

}
