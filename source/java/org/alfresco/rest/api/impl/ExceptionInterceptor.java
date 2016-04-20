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
