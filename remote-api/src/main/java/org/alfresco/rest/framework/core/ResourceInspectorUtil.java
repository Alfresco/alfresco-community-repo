/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.framework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import org.alfresco.rest.framework.Operation;

/**
 * Generic methods used by ResourceInspector
 * 
 * @author Gethin James
 */
public class ResourceInspectorUtil
{
    private static Log logger = LogFactory.getLog(ResourceInspectorUtil.class);

    /**
     * Determine the expected type as the returned type of the method. If the return type is a List it will return the generic element type instead of a List.
     * 
     * @param resource
     *            - resource with methods
     * @param method
     *            Method
     * @return Class - type of class it needs.
     */
    @SuppressWarnings("rawtypes")
    protected static Class determineType(Class resource, Method method)
    {
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);

        /* The api is consistent that the object passed in must match the object passed out however, operations are different, if the param is supplied it doesn't have to match the return type. So we need special logic for operations */
        Annotation annot = AnnotationUtils.findAnnotation(resolvedMethod, Operation.class);
        if (annot != null)
        {
            return determineOperationType(resource, method);
        }
        else
        {
            Class returnType = GenericTypeResolver.resolveReturnType(resolvedMethod, resource);
            if (List.class.isAssignableFrom(returnType))
            {
                return ResolvableType.forMethodReturnType(method).asCollection().getGeneric(0).resolve();
            }
            return returnType;
        }
    }

    protected static Class determineOperationType(Class resource, Method method)
    {
        // Its an operation annotated method and its a bit special
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes != null)
        {
            switch (paramTypes.length)
            {
            case 4:
                // EntityResource operation by id, same logic as RelationshipEntityResource operation by id
            case 5:
                int position = paramTypes.length - 3;
                if (Void.class.equals(paramTypes[position]))
                {
                    return null;
                }
                else
                {
                    return paramTypes[position];
                }
            }
        }

        throw new IllegalArgumentException("Your method signature should have 4 parameters (uniqueId, typePassedin, Parameters, WithResponse)," +
                " use Void if you are not interested in the second argument. " + resource.getName() + " " + method.getName());
    }

    /**
     * Finds methods for the given annotation
     * 
     * It first finds all public member methods of the class or interface represented by objClass, including those inherited from superclasses and superinterfaces.
     * 
     * It then loops through these methods searching for a single Annotation of annotationType, traversing its super methods if no annotation can be found on the given method itself.
     * 
     * @param objClass
     *            - the class
     * @param annotationType
     *            - the annotation to find
     * @return - the List of Method or an empty List
     */
    @SuppressWarnings("rawtypes")
    public static List<Method> findMethodsByAnnotation(Class objClass, Class<? extends Annotation> annotationType)
    {

        List<Method> annotatedMethods = new ArrayList<Method>();
        Method[] methods = objClass.getMethods();
        for (Method method : methods)
        {
            Annotation annot = AnnotationUtils.findAnnotation(method, annotationType);
            if (annot != null)
            {
                // Just to be sure, lets make sure its not a Bridged (Generic) Method
                Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
                annotatedMethods.add(resolvedMethod);
            }
        }

        return annotatedMethods;

    }

    /**
     * Invokes a no arg method and returns the result
     * 
     * @param annotatedMethod
     *            Method
     * @param obj
     *            Object
     * @return result of method call
     */
    public static Object invokeMethod(Method annotatedMethod, Object obj)
    {
        try
        {
            return invokeMethod(annotatedMethod, obj, null);
        }
        catch (Throwable error)
        {
            logger.error("Invocation failure", error);
            return null;
        }
    }

    /**
     * Invokes a method and returns the result
     * 
     * @param annotatedMethod
     *            Method
     * @param obj
     *            Object
     * @return result of method call
     */
    public static Object invokeMethod(Method annotatedMethod, Object obj, Object... args) throws Throwable
    {
        if (annotatedMethod != null)
        {
            try
            {
                return annotatedMethod.invoke(obj, args);
            }
            catch (IllegalArgumentException error)
            {
                logger.warn("Invocation error", error);
            }
            catch (IllegalAccessException error)
            {
                logger.warn("IllegalAccessException", error);
            }
            catch (InvocationTargetException error)
            {
                logger.warn("InvocationTargetException", error);
                throw error.getCause();
            }
        }
        return null;
    }

}
