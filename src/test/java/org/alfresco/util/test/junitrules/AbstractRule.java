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
package org.alfresco.util.test.junitrules;

import org.alfresco.util.ParameterCheck;
import org.junit.rules.ExternalResource;
import org.springframework.context.ApplicationContext;

/**
 * Abstract junit rule, which provides access to the Spring application context.
 * 
 * An explicit ApplicationContext or an ApplicationContextInit rule can be passed at construction time. 
 * getApplicationContext will either return the instance passed in, or retrieve one from the rule.
 *
 * @author Alex Miller
 */
public abstract class AbstractRule extends ExternalResource 
{

	protected final ApplicationContext appContext;
	protected final ApplicationContextInit appContextRule;

	/**
	 * @param appContext for use by sub classes.
	 */
	protected AbstractRule(ApplicationContext appContext)
	{
	    ParameterCheck.mandatory("appContext", appContext);
	    
	    this.appContext = appContext;
	    this.appContextRule = null;
	}

	/**
	 * @param appContextRule {@link ApplicationContextInit} rule used to provide ApplicationContext to sub classes. 
	 */
	protected AbstractRule(ApplicationContextInit appContextRule)
	{
	    ParameterCheck.mandatory("appContextRule", appContextRule);
	    
	    this.appContext = null;
	    this.appContextRule = appContextRule;
	}

	/**
	 * This method provides the spring application context to subclasses. It either provides the explicit ApplicationContext, passed in 
	 * at construction time, or retrieves it from the {@link ApplicationContextInit} rule, passed in the alternative constructor.  
	 * 
	 * @return the spring application context
	 * @throws NullPointerException if the application context has not been initialised when requested.
	 */
	protected ApplicationContext getApplicationContext() {
	    ApplicationContext result = null;
	    
	    // The app context is either provided explicitly:
	    if (appContext != null)
	    {
	        result = appContext;
	    }
	    // or is implicitly accessed via another rule:
	    else 
	    {
	        ApplicationContext contextFromRule = appContextRule.getApplicationContext();
	        if (contextFromRule != null)
	        {
	            result = contextFromRule;
	        }
	        else
	        {
	            throw new NullPointerException("Cannot retrieve application context from provided rule.");
	        }
	    }
	    
	    return result;
	}

}
