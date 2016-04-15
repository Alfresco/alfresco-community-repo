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
package org.alfresco.repo.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Base behaviour implementation
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public abstract class BaseBehaviour implements Behaviour 
{
	/** The notification frequency */
	protected NotificationFrequency frequency = NotificationFrequency.EVERY_EVENT;
	
	/** Disabled stack **/
	private StackThreadLocal disabled = new StackThreadLocal();
	
	/** Proxies **/
	protected Map<Class, Object> proxies = new HashMap<Class, Object>();

	/**
	 * Default constructor
	 */
	public BaseBehaviour()
	{
		// Default constructor
	}
	
	/**
	 * Constructor
	 * 
	 * @param frequency		the notification frequency
	 */
	public BaseBehaviour(NotificationFrequency frequency)
	{
		ParameterCheck.mandatory("Frequency", frequency);
		this.frequency = frequency;
	}
	
	public void setNotificationFrequency(NotificationFrequency frequency)
	{
		this.frequency = frequency;
	}

	/**
	 * Disable this behaviour for the curent thread
	 */
	public void disable() 
	{
	    Stack<Integer> stack = disabled.get();
	    stack.push(hashCode());
	}

	/**
	 * Enable this behaviour for the current thread
	 */
	public void enable() 
	{
	    Stack<Integer> stack = disabled.get();
	    if (stack.peek().equals(hashCode()) == false)
	    {
	        throw new PolicyException("Cannot enable " + this.toString() + " at this time - mismatched with disable calls");
	    }
	    stack.pop();
	}

	/**
	 * Indicates whether the this behaviour is current enabled or not
	 * 
	 * @return	true if the behaviour is enabled, false otherwise
	 */
	public boolean isEnabled() 
	{
	    Stack<Integer> stack = disabled.get();
	    return stack.search(hashCode()) == -1;
	}

	/**
	 * Get the notification frequency
	 * 
	 * @return	the notification frequency
	 */
	public NotificationFrequency getNotificationFrequency() 
	{
	    return frequency;
	}
	
	/**
     * Stack specific Thread Local
     * 
     * @author David Caruana
     */
    class StackThreadLocal extends ThreadLocal<Stack<Integer>>
    {
        @Override
        protected Stack<Integer> initialValue()
        {
            return new Stack<Integer>();
        }
    }
}
