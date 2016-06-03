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

import java.lang.reflect.Method;

import org.alfresco.traitextender.AJExtender.ExtensionRoute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * An method override extension routing aspect.<br>
 * Overrides calls to methods marked by an {@link Extend} annotation with calls
 * to methods having the same signature in extensions registered for the
 * {@link ExtensionPoint} referred by the {@link Extend} method annotation.<br>
 * Overriding methods can call the overridden method using its correspondent
 * {@link Trait} representation (i.e. a method having the same signature).<br>
 * If no extension is defined the call proceeds with the original method.<br>
 * The aspect uses the {@link AJExtender} static utility to for extension
 * invocation and for maintaining thread-local extension-bypass contexts as not all
 * calls must be overridden and calls from within the extension must be aware of
 * this context (see {@link AJProxyTrait}).
 *
 * @author Bogdan Horje
 */
@Aspect
public class RouteExtensions
{
    private static Log logger = LogFactory.getLog(RouteExtensions.class);

    @Around("execution(@org.alfresco.traitextender.Extend * *(..)) && (@annotation(extendAnnotation))")
    public Object intercept(ProceedingJoinPoint pjp, Extend extendAnnotation) throws Throwable
    {
        boolean ajPointsEnabled = AJExtender.areAJPointsEnabled();
        try
        {
            AJExtender.enableAJPoints();
            if (ajPointsEnabled)
            {
                Object extensibleObject = pjp.getThis();
                if (!(extensibleObject instanceof Extensible))
                {
                    throw new InvalidExtension("Invalid extension point for non extensible class  : "
                                + extensibleObject.getClass());
                }
                Extensible extensible = (Extensible) extensibleObject;

                @SuppressWarnings({ "rawtypes", "unchecked" })
                ExtensionPoint point = new ExtensionPoint(extendAnnotation.extensionAPI(),
                                                          extendAnnotation.traitAPI());
                @SuppressWarnings("unchecked")
                Object extension = Extender.getInstance().getExtension(extensible,
                                                                       point);
                if (extension != null)
                {

                    return AJExtender.extendAroundAdvice(pjp,
                                                         extensible,
                                                         extendAnnotation,
                                                         extension);
                }
                else if (logger.isDebugEnabled())
                {
                    MethodSignature ms = (MethodSignature) pjp.getSignature();
                    Method traitMethod = ms.getMethod();

                    AJExtender.oneTimeLiveLog(logger,
                                              new ExtensionRoute(extendAnnotation,
                                                                 traitMethod));
                }
            }
            return pjp.proceed();
        }
        finally
        {
            AJExtender.revertAJPoints();
        }
    }

}
