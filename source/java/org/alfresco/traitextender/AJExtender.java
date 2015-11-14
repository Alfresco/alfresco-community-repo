/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.traitextender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.hazelcast.util.ConcurrentHashSet;

public class AJExtender
{
    private static final Object[] SAFE_NULL_ARGS = new Object[0];

    private static Log logger = LogFactory.getLog(AJExtender.class);

    private static ConcurrentHashSet<ExtensionRoute> oneTimeLogSet = null;

    private static final ThreadLocal<Stack<Boolean>> ajPointsLocalEnabled = new ThreadLocal<Stack<Boolean>>()
    {
        protected Stack<Boolean> initialValue()
        {
            Stack<Boolean> enablementStack = new Stack<Boolean>();
            enablementStack.push(true);
            return enablementStack;
        };
    };

    static class ProceedingContext
    {
        final Extend extend;

        final ProceedingJoinPoint proceedingJoinPoint;

        ProceedingContext(Extend extend, ProceedingJoinPoint proceedingJoinPoint)
        {
            super();
            this.extend = extend;
            this.proceedingJoinPoint = proceedingJoinPoint;
        }

    }

    private static final ThreadLocal<Stack<ProceedingContext>> ajLocalProceedingJoinPoints = new ThreadLocal<Stack<ProceedingContext>>()
    {
        protected java.util.Stack<ProceedingContext> initialValue()
        {
            return new Stack<>();
        };
    };

    public static interface ExtensionBypass<R>
    {
        R run() throws Throwable;
    }

    public static class CompiledExtensible
    {
        private Class<? extends Extensible> extensible;

        private Map<Method, ExtensionRoute> routedMethods = new HashMap<>();

        private Map<Method, ExtensionRoute> notRoutedMethods = new HashMap<>();

        private List<AJExtensibleCompilingError> errors = new LinkedList<>();

        public CompiledExtensible(Class<? extends Extensible> extensible)
        {
            super();
            this.extensible = extensible;
        }

        public Class<? extends Extensible> getExtensible()
        {
            return this.extensible;
        }

        public void add(AJExtensibleCompilingError error)
        {
            this.errors.add(error);
        }

        public boolean hasErrors()
        {
            return !errors.isEmpty();
        }

        public String getErrorsString()
        {
            StringBuilder builder = new StringBuilder();

            for (AJExtensibleCompilingError error : errors)
            {
                builder.append(error.getShortMessage());
                builder.append("\n");
            }

            return builder.toString();
        }

        public List<AJExtensibleCompilingError> getErrors()
        {
            return this.errors;
        }

        public void add(ExtensionRoute route)
        {
            if (route.extensionMethod == null)
            {
                notRoutedMethods.remove(route.extendedMethod);
                routedMethods.put(route.extendedMethod,
                                  route);
            }
            else if (!routedMethods.containsKey(route.extendedMethod))
            {
                routedMethods.put(route.extendedMethod,
                                  route);
            }
        }

        public Collection<ExtensionRoute> getAllNotRouted()
        {
            return notRoutedMethods.values();
        }

        public int getExtendedMethodCount()
        {
            return routedMethods.size() + notRoutedMethods.size();
        }

        public String getInfo()
        {
            return extensible.getName() + "{ " + routedMethods.size() + " routed methods; " + notRoutedMethods.size()
                        + " not routed methods;" + errors.size() + " errors}";
        }
    }

    public static class ExtensionRoute
    {
        final Extend extendAnnotation;

        final Method extendedMethod;

        final Method extensionMethod;

        ExtensionRoute(Extend extendAnnotation, Method traitMethod)
        {
            this(extendAnnotation,
                 traitMethod,
                 null);
        }

        ExtensionRoute(Extend extendAnnotation, Method extendedMethod, Method extensionMethod)
        {
            super();
            ParameterCheck.mandatory("extendAnnotation",
                                     extendAnnotation);
            ParameterCheck.mandatory("traitMethod",
                                     extendedMethod);

            this.extendAnnotation = extendAnnotation;
            this.extendedMethod = extendedMethod;
            this.extensionMethod = extensionMethod;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof ExtensionRoute)
            {
                ExtensionRoute route = (ExtensionRoute) obj;
                return extendAnnotation.traitAPI().equals(route.extendAnnotation.traitAPI())
                            && extendAnnotation.extensionAPI().equals(route.extendAnnotation.extensionAPI())
                            && extendedMethod.equals(route.extendedMethod)
                            && ((extensionMethod == null && route.extensionMethod == null) || (extensionMethod != null && extensionMethod
                                        .equals(route.extensionMethod)));
            }
            else
            {
                return false;
            }
        }

        @Override
        public String toString()
        {
            String extensionString = "NOT ROUTED";

            if (extensionMethod != null)
            {
                Class<?> exDeclClass = extendedMethod.getDeclaringClass();
                extensionString = extensionMethod.toGenericString() + "#" + exDeclClass;
            }

            return extendAnnotation.toString() + "\t\n[" + extendedMethod.toGenericString() + " -> " + extensionString
                        + "]";
        }

        @Override
        public int hashCode()
        {
            return extendAnnotation.hashCode();
        }
    }

    public static boolean areAJPointsEnabled()
    {
        return ajPointsLocalEnabled.get().peek();
    }

    static void enableAJPoints()
    {
        ajPointsLocalEnabled.get().push(true);
    }

    static void revertAJPoints()
    {
        ajPointsLocalEnabled.get().pop();
    }

    public static <R> R throwableRun(AJExtender.ExtensionBypass<R> section) throws Throwable
    {
        try
        {
            AJExtender.ajPointsLocalEnabled.get().push(false);
            return section.run();

        }
        finally
        {
            AJExtender.ajPointsLocalEnabled.get().pop();
        }
    }

    public static <R> R run(AJExtender.ExtensionBypass<R> section, Class<?>[] exTypes) throws Throwable
    {
        try
        {
            return throwableRun(section);
        }
        catch (Error | RuntimeException error)
        {
            throw error;
        }
        catch (Throwable error)
        {
            throw asCheckThrowable(error,
                                   exTypes);
        }
    }

    public static Throwable asCheckThrowable(Throwable error, Class<?>... checkedThrowableTypes)
    {
        Class<? extends Throwable> errorClass = error.getClass();
        for (int i = 0; i < checkedThrowableTypes.length; i++)
        {
            if (errorClass.equals(checkedThrowableTypes[i]))
            {
                return error;
            }
        }
        return new UndeclaredThrowableException(error);
    }

    public static <R> R run(AJExtender.ExtensionBypass<R> section)
    {
        try
        {
            return throwableRun(section);
        }
        catch (Error | RuntimeException error)
        {
            throw error;
        }
        catch (Throwable error)
        {
            throw new UndeclaredThrowableException(error);
        }
    }

    public static void oneTimeLiveLog(Log logger, ExtensionRoute route)
    {
        synchronized (AJExtender.class)
        {
            if (oneTimeLogSet == null)
            {
                oneTimeLogSet = new ConcurrentHashSet<>();
            }
        }

        synchronized (oneTimeLogSet)
        {
            if (oneTimeLogSet.contains(route))
            {
                return;
            }
            else
            {
                logger.debug(route.toString());
                oneTimeLogSet.add(route);
            }
        }
    }

    public static CompiledExtensible compile(Class<? extends Extensible> extensible)
                throws AJExtensibleCompilingException
    {
        logger.info("Compiling extensible " + extensible);

        CompiledExtensible compiledExtensible = new CompiledExtensible(extensible);

        List<Method> methods = new ArrayList<>();
        Class<?> extendDeclaring = extensible;
        while (extendDeclaring != null)
        {
            Method[] declaredMethods = extendDeclaring.getDeclaredMethods();
            methods.addAll(Arrays.asList(declaredMethods));
            extendDeclaring = extendDeclaring.getSuperclass();
        }
        Set<Extend> extendDeclarations = new HashSet<>();
        Set<Method> routedExtensionMethods = new HashSet<>();
        for (Method method : methods)
        {

            Extend extend = method.getAnnotation(Extend.class);
            if (extend != null)
            {
                try
                {
                    extendDeclarations.add(extend);
                    Class<?> extensionAPI = extend.extensionAPI();
                    Method extensionMethod = extensionAPI.getMethod(method.getName(),
                                                                    method.getParameterTypes());
                    compiledExtensible.add(new ExtensionRoute(extend,
                                                              method,
                                                              extensionMethod));
                    routedExtensionMethods.add(extensionMethod);
                }
                catch (NoSuchMethodException error)
                {
                    AJExtensibleCompilingException ajCompilingError = new AJExtensibleCompilingException("No route for "
                                                                                                                     + method.toGenericString()
                                                                                                                     + " @"
                                                                                                                     + extend,
                                                                                                         error);
                    compiledExtensible.add(ajCompilingError);
                }
                catch (SecurityException error)
                {
                    AJExtensibleCompilingException ajCompilingError = new AJExtensibleCompilingException("Access denined to route for "
                                                                                                                     + method.toGenericString()
                                                                                                                     + " @"
                                                                                                                     + extend,
                                                                                                         error);
                    compiledExtensible.add(ajCompilingError);
                }
            }

        }

        final Set<Method> allObjectMethods = new HashSet<>(Arrays.asList(Object.class.getMethods()));

        for (Extend extend : extendDeclarations)
        {
            Class<?> extension = extend.extensionAPI();

            Set<Method> allExtensionMethods = new HashSet<>(Arrays.asList(extension.getMethods()));
            allExtensionMethods.removeAll(allObjectMethods);
            allExtensionMethods.removeAll(routedExtensionMethods);
            if (!allExtensionMethods.isEmpty())
            {
                for (Method method : allExtensionMethods)
                {
                    compiledExtensible.add(new AJDanglingExtensionError(method,
                                                                        extend));
                }
            }
        }

        logger.info(compiledExtensible.getInfo());

        return compiledExtensible;
    }

    public static Object extendAroundAdvice(JoinPoint thisJoinPoint, Extensible extensible, Extend extendAnnotation,
                Object extension)
    {

        MethodSignature ms = (MethodSignature) thisJoinPoint.getSignature();
        Method method = ms.getMethod();
        try
        {
            ajLocalProceedingJoinPoints.get().push(new ProceedingContext(extendAnnotation,
                                                                         (ProceedingJoinPoint) thisJoinPoint));

            Method extensionMethod = extension.getClass().getMethod(method.getName(),
                                                                    method.getParameterTypes());
            if (logger.isDebugEnabled())
            {
                oneTimeLiveLog(AJExtender.logger,
                               new ExtensionRoute(extendAnnotation,
                                                  method,
                                                  extensionMethod));
            }

            return extensionMethod.invoke(extension,
                                          thisJoinPoint.getArgs());
        }
        catch (IllegalAccessException error)
        {
            throw new InvalidExtension("Ivalid extension : " + error.getMessage(),
                                       error);
        }
        catch (IllegalArgumentException error)
        {
            throw new InvalidExtension("Ivalid extension : " + error.getMessage(),
                                       error);
        }
        catch (InvocationTargetException error)
        {
            Throwable targetException = error.getTargetException();
            if (targetException instanceof RuntimeException)
            {
                throw (RuntimeException) targetException;
            }
            else
            {
                throw new ExtensionTargetException(targetException);
            }
        }
        catch (NoSuchMethodException error)
        {
            throw new InvalidExtension("Ivalid extension : " + error.getMessage(),
                                       error);
        }
        catch (SecurityException error)
        {
            throw new InvalidExtension("Ivalid extension : " + error.getMessage(),
                                       error);
        }
        finally
        {
            ajLocalProceedingJoinPoints.get().pop();
        }

    }

    public static boolean isLocalProceeder(Method method)
    {
        if (!ajLocalProceedingJoinPoints.get().isEmpty())
        {
            ProceedingContext proceedingCotext = ajLocalProceedingJoinPoints.get().peek();
            MethodSignature ms = (MethodSignature) proceedingCotext.proceedingJoinPoint.getSignature();
            Method jpMethod = ms.getMethod();
            return jpMethod.getName().endsWith(method.getName()) && Arrays.equals(jpMethod.getParameterTypes(),
                                                                                  method.getParameterTypes());
        }
        else
        {
            return false;
        }
    }

    public static Object localProceed(Object[] args) throws Throwable
    {
        ProceedingContext proceedingCotext = ajLocalProceedingJoinPoints.get().peek();
        Object[] safeArgs = args == null ? SAFE_NULL_ARGS : args;
        return proceedingCotext.proceedingJoinPoint.proceed(safeArgs);
    }
}
