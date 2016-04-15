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
package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.mozilla.javascript.Scriptable;

/**
 * Object representing the behaviour information
 * 
 * @author Roy Wetherall
 */
public class Behaviour implements Scopeable, Serializable 
{
	/** Serial version UID **/
	private static final long serialVersionUID = 1936017361886646100L;
	
	/** Service registry **/
	private ServiceRegistry services;
	
	/** Script scope **/
	private Scriptable scope;
	
	/** The name of the policy that this behaviour is linked to **/
	private String name;
	
	/** The behaviour argument values **/
	private Object[] args;
	
	/** Cached js converted argument values **/
	private Serializable[] jsArgs;
	
	/**
	 * Constructor 
	 * 
	 * @param services	the service registry
	 * @param name		the name of the policy associated with this behaviour
	 * @param args		the argument values
	 */
	public Behaviour(ServiceRegistry services, String name, Object[] args)
	{
		this.services = services;
		this.name = name;
		this.args = args;
	}

	/**
	 * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
	 */
	public void setScope(Scriptable scope) 
	{
		this.scope = scope;
	}
	
	/**
	 * Get the policy name
	 * 
	 * @return	the name of the policy
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * The argument values
	 * 
	 * @return	array containing the argument values
	 */
	public Serializable[] getArgs()
	{
		if (this.jsArgs == null)
		{
			ValueConverter valueConverter = new ValueConverter();
			this.jsArgs = new Serializable[args.length];
			int index = 0;
			for (Object arg : this.args) 
			{
				this.jsArgs[index] = valueConverter.convertValueForScript(services, this.scope, null, (Serializable)arg);
				index ++;
			}
		}
		return this.jsArgs;
	}
}
