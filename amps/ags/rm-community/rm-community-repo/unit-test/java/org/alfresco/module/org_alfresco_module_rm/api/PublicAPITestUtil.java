/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import org.alfresco.api.AlfrescoPublicApi;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 * A utility class to help testing the Alfresco public API.
 *
 * @author Tom Page
 * @since 2.5
 */
public class PublicAPITestUtil
{
    private static final String ALFRESCO_PACKAGE = "org.alfresco";

    /**
     * Check the consistency of the public API exposed from the given package. For each class in the package that is
     * annotated {@link AlfrescoPublicApi}, check that no exposed methods (or fields, constructors, etc.) use
     * non-public-API classes from Alfresco.
     *
     * @param basePackageName The package to check classes within.
     * @param knownBadReferences Any references that would cause this test to fail, but which we don't want to change.
     *            The keys should be public API classes within our code and the values should be the non-public-API
     *            class that is being referenced.
     */
    public static void testPublicAPIConsistency(String basePackageName, SetMultimap<Class<?>, Class<?>> knownBadReferences)
    {
        Reflections reflections = new Reflections(basePackageName, TypesAnnotated);
        Set<Class<?>> publicAPIClasses = reflections.getTypesAnnotatedWith(AlfrescoPublicApi.class, true);

        SetMultimap<Class<?>, Class<?>> referencedFrom = HashMultimap.create();
        Set<Class<?>> referencedClasses = new HashSet<>();
        for (Class<?> publicAPIClass : publicAPIClasses)
        {
            Set<Class<?>> referencedClassesFromClass = getReferencedClassesFromClass(publicAPIClass, new HashSet<>());
            referencedClassesFromClass.forEach(clazz -> referencedFrom.put(clazz, publicAPIClass));

            // Remove any references in knownBadReferences and error if an expected reference wasn't found.
            if (knownBadReferences.containsKey(publicAPIClass))
            {
                for (Class<?> clazz : knownBadReferences.get(publicAPIClass))
                {
                    assertTrue("Supplied knownBadReferences expects " + clazz + " to be referenced by " + publicAPIClass
                                + ", but no such error was found", referencedClassesFromClass.remove(clazz));
                }
            }

            referencedClasses.addAll(referencedClassesFromClass);
        }

        List<String> errorMessages = new ArrayList<>();
        for (Class<?> referencedClass : referencedClasses)
        {
            if (isInAlfresco(referencedClass) && !isPartOfPublicApi(referencedClass))
            {
                Set<String> referencerNames = referencedFrom.get(referencedClass).stream().map(c -> c.getName())
                            .collect(Collectors.toSet());
                errorMessages.add(referencedClass.getName() + " <- " + StringUtils.join(referencerNames, ", "));
            }
        }

        if (!errorMessages.isEmpty())
        {
            System.out.println("Errors found:");
            System.out.println(StringUtils.join(errorMessages, "\n"));
        }

        assertEquals("Found references to non-public API classes from public API classes.", Collections.emptyList(),
                    errorMessages);
    }

    /**
     * Check if the given class is a part of the Alfresco public API.
     *
     * @param clazz The class to check.
     * @return {@code true} if the given class is annotated with {@link AlfrescoPublicApi}.
     */
    private static boolean isPartOfPublicApi(Class<?> clazz)
    {
        if (clazz.getAnnotation(AlfrescoPublicApi.class) != null)
        {
            return true;
        }
        if (clazz.getEnclosingClass() != null)
        {
            return isPartOfPublicApi(clazz.getEnclosingClass());
        }
        return false;
    }

    /**
     * Get all the classes referenced by the given class, which might be used by an extension. We consider visible
     * methods, constructors, fields and inner classes, as well as superclasses and interfaces extended by the class.
     *
     * @param initialClass The class to analyse.
     * @param consideredClasses Classes that have already been considered, and which should not be considered again. If
     *            the given class has already been considered then an empty set will be returned. This set will be
     *            updated with the given class.
     * @return The set of classes that might be accessible by an extension of this class.
     */
    private static Set<Class<?>> getReferencedClassesFromClass(Class<?> initialClass, Set<Class<?>> consideredClasses)
    {
        Set<Class<?>> referencedClasses = new HashSet<>();

        if (consideredClasses.add(initialClass))
        {
            for (Method method : initialClass.getDeclaredMethods())
            {
                if (isVisibleToExtender(method.getModifiers()))
                {
                    referencedClasses.addAll(getClassesFromMethod(method));
                }
            }
            for (Constructor<?> constructor : initialClass.getDeclaredConstructors())
            {
                if (isVisibleToExtender(constructor.getModifiers()))
                {
                    referencedClasses.addAll(getClassesFromConstructor(constructor));
                }
            }
            for (Field field : initialClass.getDeclaredFields())
            {
                if (isVisibleToExtender(field.getModifiers()))
                {
                    referencedClasses.addAll(getClassesFromField(field));
                }
            }
            for (Class<?> clazz : initialClass.getDeclaredClasses())
            {
                if (isVisibleToExtender(clazz.getModifiers()))
                {
                    referencedClasses.addAll(getReferencedClassesFromClass(clazz, consideredClasses));
                }
            }
            if (initialClass.getSuperclass() != null)
            {
                referencedClasses
                            .addAll(getReferencedClassesFromClass(initialClass.getSuperclass(), consideredClasses));
            }
            for (Class<?> clazz : initialClass.getInterfaces())
            {
                referencedClasses.addAll(getReferencedClassesFromClass(clazz, consideredClasses));
            }
        }
        return referencedClasses;
    }

    /**
     * Check if the supplied {@link Executable#getModifiers() modifiers} indicate that an extension can access the
     * element. Here we assume that an extension can see public and protected items, but not package protected (or
     * private).
     *
     * @param modifiers The java language modifiers.
     * @return {@code true} if the item is visible to an extension.
     */
    private static boolean isVisibleToExtender(int modifiers)
    {
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }

    /**
     * Get all classes involved in the signature of the given method.
     *
     * @param method The method to analyse.
     * @return The set of classes.
     */
    private static Set<Class<?>> getClassesFromMethod(Method method)
    {
        Set<Type> types = getTypesFromMethod(method);
        return getClassesFromTypes(types);
    }

    /**
     * Get all classes involved in the signature of the given constructor.
     *
     * @param constructor The constructor to analyse.
     * @return The set of classes.
     */
    private static Set<Class<?>> getClassesFromConstructor(Constructor<?> constructor)
    {
        Set<Type> types = getTypesFromConstructor(constructor);
        return getClassesFromTypes(types);
    }

    /**
     * Get all classes involved in the type of the supplied field. For example {@code Pair<Set<String>, Integer> foo}
     * involves four classes.
     *
     * @param field The field to look at.
     * @return The set of classes.
     */
    private static Set<Class<?>> getClassesFromField(Field field)
    {
        Set<Type> types = Sets.newHashSet(field.getGenericType());
        return getClassesFromTypes(types);
    }

    /**
     * Get all types references by the supplied method signature (i.e. the parameters, return type and exceptions).
     *
     * @param method The method to analyse.
     * @return The set of types.
     */
    private static Set<Type> getTypesFromMethod(Method method)
    {
        Set<Type> methodTypes = new HashSet<>();
        methodTypes.addAll(Sets.newHashSet(method.getGenericParameterTypes()));
        methodTypes.add(method.getGenericReturnType());
        methodTypes.addAll(Sets.newHashSet(method.getGenericExceptionTypes()));
        return methodTypes;
    }

    /**
     * Get all types referenced by the supplied constructor (i.e. the parameters and exceptions).
     *
     * @param constructor The constructor to analyse.
     * @return The set of types.
     */
    private static Set<Type> getTypesFromConstructor(Constructor<?> constructor)
    {
        Set<Type> methodTypes = new HashSet<>();
        methodTypes.addAll(Sets.newHashSet(constructor.getGenericParameterTypes()));
        methodTypes.addAll(Sets.newHashSet(constructor.getGenericExceptionTypes()));
        return methodTypes;
    }

    /**
     * Find all classes that are within the supplied types. For example a {@code Pair<Set<String>, Integer>} contains
     * references to four classes.
     *
     * @param methodTypes The set of types to examine.
     * @return The set of classes used to form the given types.
     */
    private static Set<Class<?>> getClassesFromTypes(Set<Type> methodTypes)
    {
        Set<Class<?>> methodClasses = new HashSet<>();
        for (Type type : methodTypes)
        {
            methodClasses.addAll(getClassesFromType(type, new HashSet<>()));
        }
        return methodClasses;
    }

    /**
     * Find all classes that are within the supplied type. For example a {@code Pair<Set<String>, Integer>} contains
     * references to four classes.
     *
     * @param type The type to examine.
     * @param processedTypes The set of types which have already been processed. If {@code type} is within this set then
     *            the method returns an empty set, to prevent analysis of the same type multiple times, and to guard
     *            against circular references. The underlying set is updated with the given type.
     * @return The set of classes used to form the given type.
     */
    private static Set<Class<?>> getClassesFromType(Type type, Set<Type> processedTypes)
    {
        Set<Class<?>> returnClasses = new HashSet<>();

        if (processedTypes.add(type))
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                returnClasses.add((Class<?>) parameterizedType.getRawType());

                for (Type t : parameterizedType.getActualTypeArguments())
                {
                    returnClasses.addAll(getClassesFromType(t, processedTypes));
                }
            }
            else if (type instanceof Class)
            {
                Class<?> clazz = (Class<?>) type;
                if (clazz.isArray())
                {
                    returnClasses.add(clazz.getComponentType());
                }
                returnClasses.add(clazz);
            }
            else if (type instanceof WildcardType)
            {
                // No-op - Caller can choose what type to use.
            }
            else if (type instanceof TypeVariable<?>)
            {
                TypeVariable<?> typeVariable = (TypeVariable<?>) type;
                for (Type bound : typeVariable.getBounds())
                {
                    returnClasses.addAll(getClassesFromType(bound, processedTypes));
                }
            }
            else if (type instanceof GenericArrayType)
            {
                GenericArrayType genericArrayType = (GenericArrayType) type;
                returnClasses.addAll(getClassesFromType(genericArrayType.getGenericComponentType(), processedTypes));
            }
            else
            {
                throw new IllegalStateException("This test was not written to work with type " + type);
            }
        }
        return returnClasses;
    }

    /**
     * Check if a class is within org.alfresco, and so whether it could potentially be part of the public API.
     *
     * @param type The class to check.
     * @return {@code true} if this is an Alfresco class.
     */
    private static boolean isInAlfresco(Class<?> type)
    {
        if (type.getPackage() == null)
        {
            return false;
        }
        return type.getPackage().getName().startsWith(ALFRESCO_PACKAGE);
    }
}
