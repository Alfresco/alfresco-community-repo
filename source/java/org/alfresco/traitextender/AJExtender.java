/*
 * #%L
 * Alfresco Repository
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

/**
 * Static utility used for aspectJ extension consistency , routing and for
 * maintaining thread-local extension-bypass context stack.<br>
 * AspectJ extension routing distinguishes between the following contexts in
 * which an extended method (i.e. a method with an {@link Extend} annotation)
 * can be called:
 * <ol>
 * <li>Extend context<br>
 * when an extended method is called and the extension overrides the method call
 * </li>
 * <li>Local proceed context<br>
 * when an extension method needs to execute the original method that it has
 * overridden.</li>
 * <li>Extension bypass context<br>
 * when a call to an extended method needs to be completed with the extensions
 * disabled for that call</li>
 * </ol>
 * <br>
 * The {@link AJExtender} can check {@link ExtensionPoint} definitions for
 * consistency by compiling extensible classes. The compilation process fails if
 * there are inconsistencies between {@link ExtensionPoint}s and {@link Extend}
 * annotated methods (egg. an annotated method can not be mapped to a method in
 * the indicated extension by signature matching).
 * 
 * @author Bogdan Horje
 */
public class AJExtender
{
    private static final Object[] SAFE_NULL_ARGS = new Object[0];

    private static Log logger = LogFactory.getLog(AJExtender.class);

    private static ConcurrentHashSet<ExtensionRoute> oneTimeLogSet = null;

    /**
     *  
     */
    private static final ThreadLocal<Stack<Boolean>> ajPointsLocalEnabled = new ThreadLocal<Stack<Boolean>>()
    {
        protected Stack<Boolean> initialValue()
        {
            Stack<Boolean> enablementStack = new Stack<Boolean>();
            enablementStack.push(true);
            return enablementStack;
        };
    };

    /**
     * @author Bogdan Horje
     */
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

    /**
     * Implementors are aspectJ extension ignoring closures. When executing
     * {@link ExtensionBypass}es using {@link AJExtender#run(ExtensionBypass)} or
     * {@link AJExtender#run(ExtensionBypass, Class[])} the {@link #run()}
     * method code will ignore extension overrides.
     *
     * @author Bogdan Horje
     */
    public static interface ExtensionBypass<R>
    {
        R run() throws Throwable;
    }

    /**
     * Thrown-exception or stored error resulted compiling inconsistencies found
     * during aspectJ extensible classes compilation.
     *
     * @see {@link AJExtender#compile(Class)}
     * @author Bogdan Horje
     */
    interface AJExtensibleCompilingError
    {
        String getShortMessage();
    }

    /**
     * Thrown or stored on compiling inconsistencies found during aspectJ
     * extensible classes compilation.
     *
     * @see {@link AJExtender#compile(Class)}
     * @author Bogdan Horje
     */
    static class AJExtensibleCompilingException extends Exception implements AJExtensibleCompilingError
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        AJExtensibleCompilingException()
        {
            super();
        }

        AJExtensibleCompilingException(String message, Throwable cause, boolean enableSuppression,
                    boolean writableStackTrace)
        {
            super(message,
                  cause,
                  enableSuppression,
                  writableStackTrace);
        }

        AJExtensibleCompilingException(String message, Throwable cause)
        {
            super(message,
                  cause);
        }

        AJExtensibleCompilingException(String message)
        {
            super(message);
        }

        AJExtensibleCompilingException(Throwable cause)
        {
            super(cause);
        }

        @Override
        public String getShortMessage()
        {
            return getMessage();
        }

    }

    /**
     * Signals the existence of extension methods that are not matched with
     * same-signature methods through {@link Extend} annotations within
     * {@link Extensible}.
     *
     * @author Bogdan Horje
     */
    static class AJDanglingExtensionError implements AJExtensibleCompilingError
    {
        private Method danglingMethod;

        private Extend extendDeclaration;

        AJDanglingExtensionError(Method danglingMethod, Extend extendDeclaration)
        {
            super();
            this.danglingMethod = danglingMethod;
            this.extendDeclaration = extendDeclaration;
        }

        @Override
        public String getShortMessage()
        {
            return "Dangling extension method " + danglingMethod + " " + extendDeclaration;
        }
    }

    /**
     * An {@link Extensible} sub class compilation result containing all
     * {@link Extend} mapped routes, not routed or dangling methods within the
     * give extensible class and/or possible compilation errors.
     *
     * @author Bogdan Horje
     */
    static class CompiledExtensible
    {
        private Class<? extends Extensible> extensible;

        private Map<Method, ExtensionRoute> routedMethods = new HashMap<>();

        private Map<Method, ExtensionRoute> notRoutedMethods = new HashMap<>();

        private List<AJExtensibleCompilingError> errors = new LinkedList<>();

        CompiledExtensible(Class<? extends Extensible> extensible)
        {
            super();
            this.extensible = extensible;
        }

        Class<? extends Extensible> getExtensible()
        {
            return this.extensible;
        }

        void add(AJExtensibleCompilingError error)
        {
            this.errors.add(error);
        }

        boolean hasErrors()
        {
            return !errors.isEmpty();
        }

        String getErrorsString()
        {
            StringBuilder builder = new StringBuilder();

            for (AJExtensibleCompilingError error : errors)
            {
                builder.append(error.getShortMessage());
                builder.append("\n");
            }

            return builder.toString();
        }

        List<AJExtensibleCompilingError> getErrors()
        {
            return this.errors;
        }

        void add(ExtensionRoute route)
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

        Collection<ExtensionRoute> getAllNotRouted()
        {
            return notRoutedMethods.values();
        }

        int getExtendedMethodCount()
        {
            return routedMethods.size() + notRoutedMethods.size();
        }

        String getInfo()
        {
            return extensible.getName() + "{ " + routedMethods.size() + " routed methods; " + notRoutedMethods.size()
                        + " not routed methods;" + errors.size() + " errors}";
        }
    }

    /**
     * Encapsulates extended-method to extension-method mapping information.
     *
     * @author Bogdan Horje
     */
    static class ExtensionRoute
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

    /**
     * @return <code>true</code> if aspectJ routed extensions are enabled on the
     *         call stack of the current thread
     */
    static boolean areAJPointsEnabled()
    {
        return ajPointsLocalEnabled.get().peek();
    }

    /**
     * Pushes a new enabled state of the aspectJ routed extensions on the
     * current thread execution stack.
     */
    static void enableAJPoints()
    {
        ajPointsLocalEnabled.get().push(true);
    }

    /**
     * Pops the current aspectJ routed extensions enablement state from the
     * current thread execution stack.
     */
    static void revertAJPoints()
    {
        ajPointsLocalEnabled.get().pop();
    }

    /**
     * Exception throwing extension-bypass closure runner method. <br>
     * Sets up adequate call contexts to avoid exception calling and than
     * delegates to the given closure.
     * 
     * @param closure
     * @return
     * @throws Throwable
     */
    static <R> R throwableRun(AJExtender.ExtensionBypass<R> closure) throws Throwable
    {
        try
        {
            AJExtender.ajPointsLocalEnabled.get().push(false);
            return closure.run();

        }
        finally
        {
            AJExtender.ajPointsLocalEnabled.get().pop();
        }
    }

    /**
     * Extension-bypass closure runner method. <br>
     * Sets up adequate call contexts to avoid exception calling and than
     * delegates to the given closure.<br>
     * Only the given exTypes exceptions will be passed on to the calling
     * context, all others will be wrapped as
     * {@link UndeclaredThrowableException}s.
     * 
     * @param closure
     * @param exTypes
     * @return
     * @throws Throwable
     */
    public static <R> R run(AJExtender.ExtensionBypass<R> closure, Class<?>[] exTypes) throws Throwable
    {
        try
        {
            return throwableRun(closure);
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

    static Throwable asCheckThrowable(Throwable error, Class<?>... checkedThrowableTypes)
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

    /**
     * Extension-bypass closure runner method. <br>
     * Sets up adequate call contexts to avoid exception calling and than
     * delegates to the given closure.
     * 
     * @param closure
     * @return
     */
    public static <R> R run(AJExtender.ExtensionBypass<R> closure)
    {
        try
        {
            return throwableRun(closure);
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

    /**
     * Logs each method routing path once per session.
     * 
     * @param logger
     * @param route
     */
    static void oneTimeLiveLog(Log logger, ExtensionRoute route)
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

    /**
     * @param extensible
     * @return a compilation result containing all {@link Extend} mapped routes,
     *         not routed or dangling methods within the give extensible class.
     * @throws AJExtensibleCompilingException
     */
    static CompiledExtensible compile(Class<? extends Extensible> extensible) throws AJExtensibleCompilingException
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

    /**
     * Around advice helper that matches the advised method with its
     * corresponding extension method, sets up aspectJ call contexts (egg. the
     * local-proceed context) and delegates to the extension method.
     * 
     * @param thisJoinPoint
     * @param extensible
     * @param extendAnnotation
     * @param extension
     * @return the result of the extended method
     */
    static Object extendAroundAdvice(JoinPoint thisJoinPoint, Extensible extensible, Extend extendAnnotation,
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

    /**
     * @param method
     * @return <code>true</code> if the given method has the same signature as
     *         the currently aspectJ extension-overridden method
     */
    static boolean isLocalProceeder(Method method)
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

    /**
     * Calls the currently overridden method in local-proceed context - proceeds
     * with the aspectJ join point saved on the current call stack.<br>
     * 
     * @param args
     * @throws Throwable
     */
    static Object localProceed(Object[] args) throws Throwable
    {
        ProceedingContext proceedingCotext = ajLocalProceedingJoinPoints.get().peek();
        Object[] safeArgs = args == null ? SAFE_NULL_ARGS : args;
        return proceedingCotext.proceedingJoinPoint.proceed(safeArgs);
    }
}
