package org.alfresco.rest.framework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Generic methods used by ResourceInspector
 * @author Gethin James
 */
public class ResourceInspectorUtil
{
    private static Log logger = LogFactory.getLog(ResourceInspectorUtil.class);
    /**
     * Determine the expected type as the returned type of the method.
     * If the return type is a List it will return the generic element type instead of a List.
     * @param resource - resource with methods
     * @param methodName
     * @return Class - type of class it needs.
     */
    @SuppressWarnings("rawtypes")
    protected static Class determineType(Class resource, Method method)
    {
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        Class returnType = GenericTypeResolver.resolveReturnType(resolvedMethod, resource);
        if (List.class.isAssignableFrom(returnType))
        {
            return GenericCollectionTypeResolver.getCollectionReturnType(method);
        }
        return returnType;
    }

    /**
     * Finds methods for the given annotation
     * 
     * It first finds all public member methods of the class or interface represented by objClass, 
     * including those inherited from superclasses and superinterfaces.
     * 
     * It then loops through these methods searching for a single Annotation of annotationType,
     * traversing its super methods if no annotation can be found on the given method itself.
     * 
     * @param objClass - the class
     * @param annotationType - the annotation to find
     * @return - the List of Method or an empty List
     */
    @SuppressWarnings("rawtypes")
    protected static List<Method> findMethodsByAnnotation(Class objClass, Class<? extends Annotation> annotationType)
    {
    
        List<Method> annotatedMethods = new ArrayList<Method>();
        Method[] methods = objClass.getMethods();
        for (Method method : methods)
        {
            Annotation annot = AnnotationUtils.findAnnotation(method, annotationType);
            if (annot != null) {
                //Just to be sure, lets make sure its not a Bridged (Generic) Method
                Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
                annotatedMethods.add(resolvedMethod);
            }
        }
        
        return annotatedMethods;
        
    }

    /**
     * Invokes a method and returns the result
     * @param annotatedMethod
     * @param obj
     * @return result of method call
     */
    public static Object invokeMethod(Method annotatedMethod, Object obj)
    {
        if (annotatedMethod != null)
        {
            try
            { 
              return annotatedMethod.invoke(obj, null);
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
            }
        }
        return null;
    }

}
