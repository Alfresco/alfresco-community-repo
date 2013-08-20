/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.api.impl;

import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An interceptor that catches exceptions and handlers them in some way, possibly by re-throwing as a different exception.
 * 
 * @author steveglover
 *
 */
public class ExceptionInterceptor implements MethodInterceptor
{
	private List<ExceptionHandler> exceptionHandlers;

    public void setExceptionHandlers(List<ExceptionHandler> exceptionHandlers)
	{
		this.exceptionHandlers = exceptionHandlers;
	}

	public ExceptionInterceptor()
    {
        super();
    }

    public Object invoke(MethodInvocation mi) throws Throwable
    {
        try
        {
            return mi.proceed();
        }
        catch(Throwable t)
        {
        	for(ExceptionHandler handler : exceptionHandlers)
        	{
        		if(handler.handle(t))
        		{
        			break;
        		}
        	}
        	throw t;
        }
    }
}
