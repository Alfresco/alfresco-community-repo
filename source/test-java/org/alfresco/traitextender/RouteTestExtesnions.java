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

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("org.alfresco.traitextender.RouteTestExtesnions,org.alfresco.traitextender.RouteExtensions")
public class RouteTestExtesnions
{
    @AfterThrowing(pointcut = "execution(@org.alfresco.traitextender.Extend * *(..) throws TestException) && (@annotation(extendAnnotation))", throwing = "ete")
    public void intercept(Extend extendAnnotation, ExtensionTargetException ete) throws TestException
    {
        Throwable exception = AJExtender.asCheckThrowable(ete.getCause(),
                                                          TestException.class);
        if (exception instanceof TestException)
        {
            throw (TestException) exception;
        }
        else
        {
            throw ete;
        }
    }
}
